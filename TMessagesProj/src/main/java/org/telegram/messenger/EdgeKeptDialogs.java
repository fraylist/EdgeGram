package org.telegram.messenger;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Dialogs the server removed (channel deleted, kicked) while their cached history stays readable.
 * Metadata lives in memory so neither the dialog list nor the passcode screen ever waits for SQLite.
 */
public class EdgeKeptDialogs {

    private static final HashMap<Integer, LinkedHashMap<Long, EdgeStore.KeptDialog>> cache = new HashMap<>();

    private static LinkedHashMap<Long, EdgeStore.KeptDialog> map(int account) {
        LinkedHashMap<Long, EdgeStore.KeptDialog> map = cache.get(account);
        if (map == null) {
            map = new LinkedHashMap<>();
            cache.put(account, map);
            preload(account);
        }
        return map;
    }

    /** Loads the list once in the background, right after the accounts are initialised. */
    public static void preload(final int account) {
        Utilities.globalQueue.postRunnable(() -> {
            final ArrayList<EdgeStore.KeptDialog> loaded = EdgeStore.getInstance(account).getKeptDialogs();
            synchronized (cache) {
                LinkedHashMap<Long, EdgeStore.KeptDialog> map = cache.get(account);
                if (map == null) {
                    map = new LinkedHashMap<>();
                    cache.put(account, map);
                }
                for (int i = 0; i < loaded.size(); i++) {
                    map.put(loaded.get(i).did, loaded.get(i));
                }
            }
            if (!loaded.isEmpty() && EdgeConfig.keepDialogs()) {
                // the list may have been rebuilt before the load finished
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(account)
                        .postNotificationName(NotificationCenter.dialogsNeedReload));
            }
        });
    }

    public static boolean isKept(int account, long did) {
        synchronized (cache) {
            return map(account).containsKey(did);
        }
    }

    public static void keep(int account, TLRPC.Dialog dialog, String name) {
        if (dialog == null || dialog.id == 0 || !EdgeConfig.keepDialogs() || dialog.top_message <= 0) {
            return;
        }
        final EdgeStore.KeptDialog kept = new EdgeStore.KeptDialog();
        kept.did = dialog.id;
        kept.date = (int) (System.currentTimeMillis() / 1000);
        kept.name = name;
        kept.archived = dialog.folder_id != 0;
        kept.folder = dialog.folder_id;
        kept.readOnly = true;
        kept.top = dialog.top_message;
        synchronized (cache) {
            map(account).put(kept.did, kept);
        }
        Utilities.globalQueue.postRunnable(() -> EdgeStore.getInstance(account).keepDialog(kept));
        FileLog.d("edge keep dialog " + dialog.id);
    }

    public static void unkeep(int account, long did) {
        synchronized (cache) {
            map(account).remove(did);
        }
        Utilities.globalQueue.postRunnable(() -> EdgeStore.getInstance(account).unkeepDialog(did));
    }

    public static ArrayList<EdgeStore.KeptDialog> list(int account) {
        synchronized (cache) {
            return new ArrayList<>(map(account).values());
        }
    }
}
