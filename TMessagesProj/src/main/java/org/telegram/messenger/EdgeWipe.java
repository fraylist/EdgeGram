package org.telegram.messenger;

import android.content.Context;
import android.content.Intent;

import java.io.File;

/** Emergency wipe: logs every account out (sessions die on the server) and deletes local data. */
public class EdgeWipe {

    public static void wipeAll(final Context context) {
        if (context == null) {
            return;
        }
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            try {
                if (UserConfig.getInstance(a).isClientActivated()) {
                    MessagesController.getInstance(a).performLogout(1);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
        try {
            context.getSharedPreferences("edgegram", Context.MODE_PRIVATE).edit().clear().commit();
            context.getSharedPreferences("userconfing", Context.MODE_PRIVATE).edit().clear().commit();
            SharedConfig.passcodeHash = "";
            SharedConfig.badPasscodeTries = 0;
            SharedConfig.saveConfig();
        } catch (Throwable e) {
            FileLog.e(e);
        }
        deleteTree(context.getExternalFilesDir(null));
        deleteTree(context.getFilesDir());
        deleteTree(context.getCacheDir());

        AndroidUtilities.runOnUIThread(() -> {
            try {
                final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });
    }

    private static void deleteTree(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            final File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteTree(child);
                }
            }
            file.delete();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }
}
