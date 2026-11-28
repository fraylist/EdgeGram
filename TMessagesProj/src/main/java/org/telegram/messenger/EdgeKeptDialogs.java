package org.telegram.messenger;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Keeps dialogs that the server removed (channel deleted, kicked) so their cached history stays readable. */
public class EdgeKeptDialogs {

    private static final HashMap<Integer, HashSet<Long>> cache = new HashMap<>();

    /** Never touches the database on the caller thread: the list is preloaded in the background. */
    private static HashSet<Long> set(int account) {
        synchronized (cache) {
            HashSet<Long> ids = cache.get(account);
            if (ids == null) {
                ids = new HashSet<>();
                cache.put(account, ids);
                preload(account);
            }
            return ids;
        }
    }

    public static void preload(final int account) {
        Utilities.storageQueue.postRunnable(() -> {
            final HashSet<Long> loaded = new HashSet<>();
            for (EdgeStore.KeptDialog kept : EdgeStore.getInstance(account).getKeptDialogs()) {
                loaded.add(kept.did);
            }
            synchronized (cache) {
                HashSet<Long> ids = cache.get(account);
                if (ids == null) {
                    ids = new HashSet<>();
                    cache.put(account, ids);
                }
                ids.addAll(loaded);
            }
        });
    }

    public static boolean isKept(int account, long did) {
        return set(account).contains(did);
    }

    public static void keep(int account, TLRPC.Dialog dialog, String name) {
        if (dialog == null || dialog.id == 0 || !EdgeConfig.keepDialogs()) {
            return;
        }
        // no cached messages means restoring is pointless
        if (dialog.top_message <= 0) {
            return;
        }
        EdgeStore.KeptDialog kept = new EdgeStore.KeptDialog();
        kept.did = dialog.id;
        kept.date = (int) (System.currentTimeMillis() / 1000);
        kept.name = name;
        kept.archived = dialog.folder_id != 0;
        kept.folder = dialog.folder_id;
        kept.readOnly = true;
        kept.top = dialog.top_message;
        set(account).add(dialog.id);
        Utilities.storageQueue.postRunnable(() -> EdgeStore.getInstance(account).keepDialog(kept));
        FileLog.d("edge keep dialog " + dialog.id);
    }

    public static void unkeep(int account, long did) {
        EdgeStore.getInstance(account).unkeepDialog(did);
        set(account).remove(did);
    }

    public static ArrayList<EdgeStore.KeptDialog> list(int account) {
        return EdgeStore.getInstance(account).getKeptDialogs();
    }
}
