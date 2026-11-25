package org.telegram.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.LaunchActivity;

/**
 * Keeps the process and the MTProto connection alive, so messages arrive even when the app was
 * swiped away. Telegram relies on FCM for that, and FCM does not issue tokens to builds signed
 * with anything but the official certificate, so a foreground service is the only way.
 */
public class EdgeKeepAliveService extends Service {

    private static final String CHANNEL_ID = "edge_keep_alive";
    private static final int NOTIFICATION_ID = 1877;

    public static void start(Context context) {
        if (context == null) {
            return;
        }
        try {
            Intent intent = new Intent(context, EdgeKeepAliveService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public static void stop(Context context) {
        if (context == null) {
            return;
        }
        try {
            context.stopService(new Intent(context, EdgeKeepAliveService.class));
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            final Notification notification = buildNotification();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        applyPushConnection();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // the user swiped the app away: bring the connection back without a visible delay
        try {
            // keep the MTProto connection alive as well: the process survives, the socket should too
            ApplicationLoader.startPushService();
        } catch (Throwable e) {
            FileLog.e(e);
        }
        try {
            Intent restart = new Intent(this, EdgeKeepAliveService.class);
            int flags = PendingIntent.FLAG_ONE_SHOT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getService(this, 42, restart, flags);
            android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.set(android.app.AlarmManager.RTC, System.currentTimeMillis() + 1500, pendingIntent);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    /**
     * Telegram assumes FCM will wake the app and therefore closes the socket in background.
     * Without a working FCM token nothing would wake us, so keep the push connection alive.
     */
    private void applyPushConnection() {
        try {
            MessagesController.getGlobalNotificationsSettings()
                    .edit().putBoolean("pushConnection", true).apply();
        } catch (Throwable e) {
            FileLog.e(e);
        }
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            try {
                if (UserConfig.getInstance(a).isClientActivated()) {
                    ConnectionsManager.getInstance(a).setPushConnectionEnabled(true);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
        try {
            ApplicationLoader.startPushService();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    EdgeConfig.tr("Background connection", "Соединение в фоне"),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(EdgeConfig.tr("Keeps EdgeGram connected so messages arrive on time",
                    "Держит EdgeGram на связи, чтобы сообщения приходили вовремя"));
            channel.setShowBadge(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private Notification buildNotification() {
        Intent open = new Intent(this, LaunchActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, open, pendingFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("EdgeGram")
                .setContentText(EdgeConfig.tr("Connected in the background", "На связи в фоне"))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);
        return builder.build();
    }
}