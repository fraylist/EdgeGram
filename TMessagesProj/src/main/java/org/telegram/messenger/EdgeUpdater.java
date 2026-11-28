package org.telegram.messenger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Over-the-air updates: a small JSON on the server, then a normal APK install. */
public class EdgeUpdater {

    public static final String DEFAULT_URL = "https://raw.githubusercontent.com/fraylist/EdgeGram/main/latest.json";

    public static class Info {
        public int versionCode;
        public String versionName;
        public String url;
        public String changelog;
        public boolean force;
        public int minVersion;

        public boolean isForced(int currentVersionCode) {
            return force || (minVersion > 0 && currentVersionCode < minVersion);
        }
    }

    public interface Callback {
        void onResult(Info info, boolean upToDate);
    }

    public static String updateUrl() {
        final String custom = EdgeConfig.getString("updateUrl", "");
        return custom == null || custom.isEmpty() ? DEFAULT_URL : custom;
    }

    public static int currentVersionCode(Context context) {
        try {
            return (int) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
        } catch (Throwable e) {
            return 0;
        }
    }

    public static void check(Context context, Callback callback) {
        if (context == null) {
            return;
        }
        final Context appContext = context.getApplicationContext();
        new Thread(() -> {
            Info info = null;
            try {
                info = fetch(updateUrl());
            } catch (Throwable e) {
                FileLog.e(e);
            }
            final Info result = info;
            final int current = currentVersionCode(appContext);
            AndroidUtilities.runOnUIThread(() -> {
                if (callback != null) {
                    callback.onResult(result, result == null || result.versionCode <= current);
                }
            });
        }, "edge-updater").start();
    }

    /** arm64 phones take the small build, everything else falls back to the universal one. */
    private static String pickUrl(String single, String arm64, String universal) {
        boolean isArm64 = false;
        try {
            for (String abi : Build.SUPPORTED_ABIS) {
                if ("arm64-v8a".equals(abi)) {
                    isArm64 = true;
                    break;
                }
            }
        } catch (Throwable ignore) {
        }
        if (isArm64 && arm64 != null && !arm64.isEmpty()) {
            return arm64;
        }
        if (!isArm64 && universal != null && !universal.isEmpty()) {
            return universal;
        }
        return single;
    }

    private static Info fetch(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "EdgeGram");
        try {
            final int code = connection.getResponseCode();
            if (code != 200) {
                return null;
            }
            final StringBuilder builder = new StringBuilder();
            try (InputStream is = connection.getInputStream()) {
                final byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) > 0) {
                    builder.append(new String(buffer, 0, read, "UTF-8"));
                }
            }
            final JSONObject json = new JSONObject(builder.toString());
            final Info info = new Info();
            info.versionCode = json.optInt("versionCode", 0);
            info.versionName = json.optString("versionName", "");
            final String single = json.optString("url", "");
            final String arm64 = json.optString("arm64", "");
            final String universal = json.optString("universal", "");
            info.url = pickUrl(single, arm64, universal);
            info.changelog = json.optString("changelog", "");
            info.force = json.optBoolean("force", false);
            info.minVersion = json.optInt("minVersion", 0);
            return info.versionCode > 0 && !info.url.isEmpty() ? info : null;
        } finally {
            connection.disconnect();
        }
    }

    public static void showDialog(android.app.Activity activity, Info info) {
        if (activity == null || info == null) {
            return;
        }
        final boolean forced = info.isForced(currentVersionCode(activity));
        final org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(activity);
        builder.setTitle(EdgeConfig.tr("Update ", "Обновление ") + info.versionName);
        builder.setMessage(info.changelog);
        builder.setPositiveButton(EdgeConfig.tr("Update", "Обновить"), (d, w) -> downloadAndInstall(activity, info));
        if (!forced) {
            builder.setNegativeButton(EdgeConfig.tr("Later", "Позже"), null);
        }
        builder.show();
    }

    public static void downloadAndInstall(Context context, Info info) {
        if (context == null || info == null || info.url.isEmpty()) {
            return;
        }
        final Context appContext = context.getApplicationContext();
        final File dir = new File(appContext.getFilesDir(), "cache/updates");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File target = new File(dir, "EdgeGram-update.apk");
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(info.url).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("User-Agent", "EdgeGram");
                try (InputStream is = connection.getInputStream(); FileOutputStream os = new FileOutputStream(target)) {
                    final byte[] buffer = new byte[64 * 1024];
                    int read;
                    while ((read = is.read(buffer)) > 0) {
                        os.write(buffer, 0, read);
                    }
                }
                AndroidUtilities.runOnUIThread(() -> install(appContext, target));
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }, "edge-update-download").start();
    }

    private static void install(Context context, File apk) {
        try {
            if (Build.VERSION.SDK_INT >= 26 && !context.getPackageManager().canRequestPackageInstalls()) {
                final Intent settings = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                settings.setData(Uri.parse("package:" + context.getPackageName()));
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settings);
                return;
            }
            final Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apk);
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }
}
