package org.telegram.messenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Local history of messages deleted by the other side and of message edits. One database per account. */
public class EdgeStore {

    public static class Edit {
        public int date;
        public String oldText;
        public String newText;
    }

    private static final EdgeStore[] instances = new EdgeStore[UserConfig.MAX_ACCOUNT_COUNT];

    public static EdgeStore getInstance(int account) {
        synchronized (instances) {
            if (instances[account] == null) {
                instances[account] = new EdgeStore(account);
            }
            return instances[account];
        }
    }

    private static class Helper extends SQLiteOpenHelper {
        Helper(Context context, int account) {
            super(context, "edgegram_" + account + ".db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS deleted(uid INTEGER, mid INTEGER, date INTEGER, PRIMARY KEY(uid, mid))");
            db.execSQL("CREATE TABLE IF NOT EXISTS edits(uid INTEGER, mid INTEGER, date INTEGER, old_text TEXT, new_text TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS edits_idx ON edits(uid, mid)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private final int account;
    private Helper helper;
    private boolean loaded;
    private final HashMap<Long, HashSet<Integer>> deleted = new HashMap<>();
    private final HashSet<String> edited = new HashSet<>();

    private EdgeStore(int account) {
        this.account = account;
    }

    private synchronized SQLiteDatabase db() {
        if (helper == null) {
            helper = new Helper(ApplicationLoader.applicationContext, account);
        }
        return helper.getWritableDatabase();
    }

    private synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            SQLiteDatabase d = db();
            Cursor c = d.rawQuery("SELECT uid, mid FROM deleted", null);
            while (c.moveToNext()) {
                addDeletedToCache(c.getLong(0), c.getInt(1));
            }
            c.close();
            c = d.rawQuery("SELECT DISTINCT uid, mid FROM edits", null);
            while (c.moveToNext()) {
                edited.add(c.getLong(0) + ":" + c.getInt(1));
            }
            c.close();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private void addDeletedToCache(long uid, int mid) {
        HashSet<Integer> set = deleted.get(uid);
        if (set == null) {
            set = new HashSet<>();
            deleted.put(uid, set);
        }
        set.add(mid);
    }

    public synchronized boolean isDeleted(long dialogId, int mid) {
        ensureLoaded();
        HashSet<Integer> set = deleted.get(dialogId);
        return set != null && set.contains(mid);
    }

    public synchronized boolean hasEdits(long dialogId, int mid) {
        ensureLoaded();
        return edited.contains(dialogId + ":" + mid);
    }

    /** Must be called on the messages storage queue. key = dialog id, or 0 when the dialog is not known. */
    public void onServerDeleted(long key, ArrayList<Integer> mids) {
        try {
            ArrayList<long[]> pairs;
            if (key != 0) {
                pairs = new ArrayList<>();
                for (int i = 0; i < mids.size(); i++) {
                    pairs.add(new long[]{key, mids.get(i)});
                }
            } else {
                pairs = MessagesStorage.getInstance(account).edgeResolveDialogs(mids);
            }
            if (pairs.isEmpty()) {
                return;
            }
            ensureLoaded();
            final int now = (int) (System.currentTimeMillis() / 1000);
            final HashMap<Long, ArrayList<Integer>> byDialog = new HashMap<>();
            synchronized (this) {
                SQLiteDatabase d = db();
                d.beginTransaction();
                try {
                    for (long[] pair : pairs) {
                        ContentValues cv = new ContentValues();
                        cv.put("uid", pair[0]);
                        cv.put("mid", pair[1]);
                        cv.put("date", now);
                        d.insertWithOnConflict("deleted", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                        addDeletedToCache(pair[0], (int) pair[1]);
                        ArrayList<Integer> list = byDialog.get(pair[0]);
                        if (list == null) {
                            list = new ArrayList<>();
                            byDialog.put(pair[0], list);
                        }
                        list.add((int) pair[1]);
                    }
                    d.setTransactionSuccessful();
                } finally {
                    d.endTransaction();
                }
            }
            AndroidUtilities.runOnUIThread(() -> {
                for (HashMap.Entry<Long, ArrayList<Integer>> e : byDialog.entrySet()) {
                    NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.edgeMessagesDeleted, e.getKey(), e.getValue());
                }
            });
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    /** Remembers the previous text of an edited message. Safe to call from any thread, before the new version is stored. */
    public void recordEdit(TLRPC.Message message) {
        if (message == null || message.message == null) {
            return;
        }
        final long dialogId = message.dialog_id;
        final int mid = message.id;
        final String newText = message.message;
        final int editDate = message.edit_date != 0 ? message.edit_date : (int) (System.currentTimeMillis() / 1000);
        MessagesStorage.getInstance(account).getStorageQueue().postRunnable(() -> {
            try {
                TLRPC.Message old = MessagesStorage.getInstance(account).edgeGetMessage(dialogId, mid);
                if (old == null || old.message == null || old.message.equals(newText)) {
                    return;
                }
                ensureLoaded();
                synchronized (this) {
                    ContentValues cv = new ContentValues();
                    cv.put("uid", dialogId);
                    cv.put("mid", mid);
                    cv.put("date", editDate);
                    cv.put("old_text", old.message);
                    cv.put("new_text", newText);
                    db().insert("edits", null, cv);
                    edited.add(dialogId + ":" + mid);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
        });
    }

    /** {deleted count, edits count} */
    public synchronized int[] counts() {
        int[] r = new int[2];
        try {
            Cursor c = db().rawQuery("SELECT COUNT(*) FROM deleted", null);
            if (c.moveToFirst()) {
                r[0] = c.getInt(0);
            }
            c.close();
            c = db().rawQuery("SELECT COUNT(*) FROM edits", null);
            if (c.moveToFirst()) {
                r[1] = c.getInt(0);
            }
            c.close();
        } catch (Throwable e) {
            FileLog.e(e);
        }
        return r;
    }

    public synchronized void clearAll() {
        try {
            db().execSQL("DELETE FROM deleted");
            db().execSQL("DELETE FROM edits");
            deleted.clear();
            edited.clear();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public synchronized ArrayList<Edit> getEdits(long dialogId, int mid) {
        ArrayList<Edit> result = new ArrayList<>();
        try {
            Cursor c = db().rawQuery("SELECT date, old_text, new_text FROM edits WHERE uid = ? AND mid = ? ORDER BY date ASC, rowid ASC", new String[]{String.valueOf(dialogId), String.valueOf(mid)});
            while (c.moveToNext()) {
                Edit e = new Edit();
                e.date = c.getInt(0);
                e.oldText = c.getString(1);
                e.newText = c.getString(2);
                result.add(e);
            }
            c.close();
        } catch (Throwable e) {
            FileLog.e(e);
        }
        return result;
    }
}
