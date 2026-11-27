package org.telegram.messenger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.Components.ForegroundDetector;

/** EdgeGram: periodic catch-up sync, so missed messages still raise notifications in Doze. */
public class EdgeSyncReceiver extends BroadcastReceiver {

    public static final String ACTION_EDGE_SYNC = "org.telegram.messenger.EDGE_SYNC";

    private static final int SYNC_REQUEST_CODE = 1878;
    private static final long SYNC_INTERVAL_MS = 10 * 60 * 1000L;
    private static final long SYNC_WAKE_LOCK_MS = 25 * 1000L;
    private static final String SYNC_WAKE_LOCK_TAG = "EdgeGram:sync";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        if (appContext == null) {
            appContext = context;
        }
        try {
            if (!EdgeConfig.keepAlive()) {
                cancel(appContext);
                return;
            }
            schedule(appContext);
            ApplicationLoader.postInitApplication();
            EdgeKeepAliveService.start(appContext);
            if (isAppVisible()) {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("edge sync: skipped, app is visible");
                }
                return;
            }
            syncNow(appContext);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private static boolean isAppVisible() {
        if (!ApplicationLoader.mainInterfacePaused) {
            return true;
        }
        ForegroundDetector detector = ForegroundDetector.getInstance();
        return detector != null && detector.isForeground();
    }

    private static void syncNow(Context context) {
        PowerManager.WakeLock wakeLock = null;
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SYNC_WAKE_LOCK_TAG);
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire(SYNC_WAKE_LOCK_MS);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }

        int accounts = 0;
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            try {
                if (!UserConfig.getInstance(a).isClientActivated()) {
                    continue;
                }
                ConnectionsManager.onInternalPushReceived(a);
                ConnectionsManager.getInstance(a).resumeNetworkMaybe();
                accounts++;
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }

        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            try {
                if (!UserConfig.getInstance(a).isClientActivated()) {
                    continue;
                }
                MessagesStorage storage = AccountInstance.getInstance(a).getMessagesStorage();
                if (storage == null || storage.getLastPtsValue() == 0) {
                    continue;
                }
                MessagesController.getInstance(a).getDifference();
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }

        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("edge sync: difference requested for " + accounts + " account(s)");
        }
        if (accounts == 0 && wakeLock != null) {
            try {
                wakeLock.release();
            } catch (Throwable ignore) {
            }
        }
    }

    public static void schedule(Context context) {
        if (context == null) {
            return;
        }
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                return;
            }
            PendingIntent pendingIntent = pendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);
            if (pendingIntent == null) {
                return;
            }
            long triggerAt = System.currentTimeMillis() + SYNC_INTERVAL_MS;
            if (canUseExact(alarmManager)) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                    return;
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public static void cancel(Context context) {
        if (context == null) {
            return;
        }
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                return;
            }
            PendingIntent pendingIntent = pendingIntent(context, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent == null) {
                return;
            }
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private static boolean canUseExact(AlarmManager alarmManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return false;
        }
        return true;
    }

    private static PendingIntent pendingIntent(Context context, int flags) {
        try {
            Intent intent = new Intent(context, EdgeSyncReceiver.class);
            intent.setAction(ACTION_EDGE_SYNC);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            return PendingIntent.getBroadcast(context, SYNC_REQUEST_CODE, intent, flags);
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }
}
