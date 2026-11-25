package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;

import java.util.Locale;

/** EdgeGram feature switches. */
public class EdgeConfig {
    private static SharedPreferences prefs;

    private static SharedPreferences p() {
        if (prefs == null) {
            prefs = ApplicationLoader.applicationContext.getSharedPreferences("edgegram", Context.MODE_PRIVATE);
        }
        return prefs;
    }

    public static boolean get(String key, boolean def) {
        try {
            return p().getBoolean(key, def);
        } catch (Throwable e) {
            return def;
        }
    }

    public static void set(String key, boolean value) {
        try {
            p().edit().putBoolean(key, value).apply();
        } catch (Throwable ignore) {
        }
    }

    public static boolean toggle(String key, boolean def) {
        boolean v = !get(key, def);
        set(key, v);
        return v;
    }

    public static final String KEEP_DELETED = "keepDeleted";
    public static final String KEEP_EDITS = "keepEdits";
    public static final String GHOST_READ = "ghostRead";
    public static final String GHOST_TYPING = "ghostTyping";
    public static final String GHOST_OFFLINE = "ghostOffline";
    public static final String ALLOW_RESTRICTED = "allowRestricted";
    public static final String NO_ADS = "noAds";
    public static final String HD_PHOTOS = "hdPhotos";

    public static boolean keepDeleted() {
        return get(KEEP_DELETED, true);
    }

    public static boolean keepEdits() {
        return get(KEEP_EDITS, true);
    }

    public static boolean ghostRead() {
        return get(GHOST_READ, false);
    }

    public static boolean ghostTyping() {
        return get(GHOST_TYPING, false);
    }

    public static boolean ghostOffline() {
        return get(GHOST_OFFLINE, false);
    }

    public static boolean allowRestricted() {
        return get(ALLOW_RESTRICTED, true);
    }

    public static boolean noAds() {
        return get(NO_ADS, true);
    }

    public static boolean hdPhotos() {
        return get(HD_PHOTOS, false);
    }

    public static final String TIME_SECONDS = "timeSeconds";
    public static final String NO_ROUNDING = "noRounding";
    public static final String TABLET_MODE = "tabletMode";
    public static final String STICKER_NO_TIME = "stickerNoTime";
    public static final String HIDE_KEYBOARD_SCROLL = "hideKeyboardScroll";
    public static final String GLASS_STROKE = "glassStroke";
    public static final String GOOEY_AVATAR = "gooeyAvatar";
    public static final String HIDE_BOTTOM_TABS = "hideBottomTabs";
    public static final String MINI_SENDER_AVATAR = "miniSenderAvatar";
    public static final String MEDIA_PREVIEW = "mediaPreview";
    public static final String STICKER_SIZE = "stickerSize";

    public static boolean timeWithSeconds() { return get(TIME_SECONDS, false); }
    public static boolean noRounding() { return get(NO_ROUNDING, false); }
    public static boolean stickerNoTime() { return get(STICKER_NO_TIME, false); }
    public static boolean hideKeyboardOnScroll() { return get(HIDE_KEYBOARD_SCROLL, false); }
    public static boolean glassStroke() { return get(GLASS_STROKE, true); }
    public static boolean gooeyAvatar() { return get(GOOEY_AVATAR, true); }
    public static boolean hideBottomTabs() { return get(HIDE_BOTTOM_TABS, false); }
    public static boolean miniSenderAvatar() { return get(MINI_SENDER_AVATAR, false); }
    public static boolean mediaPreview() { return get(MEDIA_PREVIEW, true); }

    /** 0 = auto, 1 = tablet, 2 = phone. */
    public static int tabletMode() {
        try {
            return p().getInt(TABLET_MODE, 0);
        } catch (Throwable e) {
            return 0;
        }
    }

    public static void setTabletMode(int v) {
        try {
            p().edit().putInt(TABLET_MODE, v).apply();
        } catch (Throwable ignore) {
        }
    }

    public static int stickerSize() {
        try {
            return Math.max(7, Math.min(24, p().getInt(STICKER_SIZE, 14)));
        } catch (Throwable e) {
            return 14;
        }
    }

    public static void setStickerSize(int v) {
        try {
            p().edit().putInt(STICKER_SIZE, Math.max(7, Math.min(24, v))).apply();
        } catch (Throwable ignore) {
        }
    }

    public static final String GHOST_STORIES = "ghostStories";
    public static final String GHOST_ON_ACTION = "ghostOnAction";
    public static final String SEND_SILENT = "sendSilent";
    public static final String KEEP_BOTS = "keepBots";
    public static final String SAVE_READ_DATE = "saveReadDate";
    public static final String SAVE_LAST_SEEN = "saveLastSeen";
    public static final String DIM_DELETED = "dimDeleted";
    public static final String LOCAL_PREMIUM = "localPremium";
    public static final String NO_COLORED_REPLIES = "noColoredReplies";
    public static final String KEEP_ALIVE = "keepAlive";
    public static final String SPEED_BOOST = "speedBoost";
    public static final String SHOW_USER_ID = "showUserId";
    public static final String SECRET_FREE = "secretFree";

    public static boolean ghostStories() { return get(GHOST_STORIES, false); }
    public static boolean ghostOnAction() { return get(GHOST_ON_ACTION, true); }
    public static boolean sendSilent() { return get(SEND_SILENT, false); }
    public static boolean keepBots() { return get(KEEP_BOTS, true); }
    public static boolean dimDeleted() { return get(DIM_DELETED, true); }
    public static boolean localPremium() { return get(LOCAL_PREMIUM, false); }
    public static boolean noColoredReplies() { return get(NO_COLORED_REPLIES, false); }

    public static String getStringPref(String key, String def) { return getString(key, def); }

    /** Marks the dialog as "seen" on the server only when the user acts in it (ghost mode "read on action"). */
    private static volatile long allowReadUntil = 0;

    /** Last known exact online time per user; used when the real status is hidden. */
    public static void rememberLastSeen(long userId, int time) {
        try {
            if (time > 0) {
                p().edit().putInt("seen_" + userId, time).apply();
            }
        } catch (Throwable ignore) {
        }
    }

    public static int getLastSeen(long userId) {
        try {
            return p().getInt("seen_" + userId, 0);
        } catch (Throwable e) {
            return 0;
        }
    }

    /** Called when the user writes or reacts: lets the next read receipt through for a short moment. */
    public static void allowReadNow() {
        allowReadUntil = System.currentTimeMillis() + 2500;
    }
    public static final String KEEP_TTL = "keepTtl";
    public static final String HIDE_PHONE = "hidePhone";
    public static final String HIDE_STORIES = "hideStories";
    public static final String HIDE_PREMIUM = "hidePremium";

    public static int speedBoost() {
        try {
            return p().getInt(SPEED_BOOST, 0);
        } catch (Throwable e) {
            return 0;
        }
    }

    public static void cycleSpeedBoost() {
        int v = (speedBoost() + 1) % 3;
        try {
            p().edit().putInt(SPEED_BOOST, v).apply();
        } catch (Throwable ignore) {
        }
    }

    public static String speedBoostName() {
        return speedBoost() == 2 ? tr("Maximum", "Максимальная") : speedBoost() == 1 ? tr("Fast", "Быстрая") : tr("Normal", "Обычная");
    }

    public static boolean keepAlive() { return get(KEEP_ALIVE, true); }

    public static boolean showUserId() { return get(SHOW_USER_ID, true); }
    public static boolean secretFree() { return get(SECRET_FREE, true); }
    public static boolean keepTtl() { return get(KEEP_TTL, true); }
    public static boolean hidePhone() { return get(HIDE_PHONE, false); }
    public static boolean hideStories() { return get(HIDE_STORIES, false); }
    public static boolean hidePremium() { return get(HIDE_PREMIUM, false); }

    public static String maskPhone(String phone) {
        return hidePhone() ? "+\u2022\u2022\u2022 \u2022\u2022\u2022 \u2022\u2022 \u2022\u2022" : phone;
    }

    public static String getString(String key, String def) {
        try {
            return p().getString(key, def);
        } catch (Throwable e) {
            return def;
        }
    }

    public static void putString(String key, String value) {
        try {
            p().edit().putString(key, value).apply();
        } catch (Throwable ignore) {
        }
    }

    public static String getSttKey() { return getString("sttKey", ""); }
    public static String getSttUrl() { return getString("sttUrl", "https://api.groq.com/openai/v1/audio/transcriptions"); }
    public static String getSttModel() { return getString("sttModel", "whisper-large-v3-turbo"); }

    public static final String MARK_TEXT = "markText";
    public static final String MARK_COLOR = "markColor";
    public static final int COLOR_RED = 0xFFE0524D;
    public static final int COLOR_BLUE = 0xFF3D8BFD;
    public static final int COLOR_PINK = 0xFFFF5FA2;

    public static String[] markPresets() {
        return new String[]{
                "\uD83D\uDDD1 " + tr("deleted", "удалено"),
                "\uD83D\uDDD1",
                "\u274C " + tr("deleted", "удалено"),
                "\uD83D\uDEAB",
                "\uD83D\uDC7B",
                "\uD83D\uDC80",
                "\uD83D\uDCA8",
                tr("deleted", "удалено"),
        };
    }

    public static String getMarkText() {
        String s = "";
        try {
            s = p().getString(MARK_TEXT, "");
        } catch (Throwable ignore) {
        }
        return s == null || s.trim().isEmpty() ? markPresets()[0] : s;
    }

    public static void setMarkText(String text) {
        try {
            p().edit().putString(MARK_TEXT, text == null ? "" : text.trim()).apply();
        } catch (Throwable ignore) {
        }
    }

    public static int getMarkColor() {
        try {
            int c = p().getInt(MARK_COLOR, 0);
            return c == 0 ? COLOR_RED : c;
        } catch (Throwable e) {
            return COLOR_RED;
        }
    }

    public static void setMarkColor(int color) {
        try {
            p().edit().putInt(MARK_COLOR, color).apply();
        } catch (Throwable ignore) {
        }
    }

    public static void applyApiKeys() {
        // The build-time key must win: Telegram only accepts push registration for apps it knows
        // (APP_PUSH_APIKEY_MISSING otherwise), so a custom key silently kills notifications.
        try {
            if (BuildVars.APP_ID == 4) {
                return;
            }
            int id = p().getInt("apiId", 0);
            String hash = p().getString("apiHash", "");
            if (id > 0 && hash != null && hash.length() == 32) {
                BuildVars.APP_ID = id;
                BuildVars.APP_HASH = hash;
            }
        } catch (Throwable ignore) {
        }
    }

    public static boolean hasCustomApiKeys() {
        try {
            return p().getInt("apiId", 0) > 0;
        } catch (Throwable e) {
            return false;
        }
    }

    public static void saveApiKeys(int id, String hash) {
        p().edit().putInt("apiId", id).putString("apiHash", hash).commit();
    }

    public static void resetApiKeys() {
        p().edit().remove("apiId").remove("apiHash").commit();
    }

    public static void restartApp(Context context) {
        try {
            android.content.Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            android.app.PendingIntent pi = android.app.PendingIntent.getActivity(context, 7771, intent, android.app.PendingIntent.FLAG_CANCEL_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
            android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(android.app.AlarmManager.RTC, System.currentTimeMillis() + 400, pi);
        } catch (Throwable e) {
            FileLog.e(e);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /** Ghost mode: returns true when the request must not be sent. */
    public static boolean shouldDropRequest(TLObject object) {
        try {
            if (object instanceof TLRPC.TL_messages_readHistory || object instanceof TLRPC.TL_channels_readHistory) {
                if (ghostRead() && ghostOnAction() && System.currentTimeMillis() < allowReadUntil) {
                    return false;
                }
                return ghostRead();
            }
            if (ghostStories() && (object instanceof org.telegram.tgnet.tl.TL_stories.TL_stories_readStories || object instanceof org.telegram.tgnet.tl.TL_stories.TL_stories_incrementStoryViews)) {
                return true;
            }
            if (sendSilent()) {
                if (object instanceof TLRPC.TL_messages_sendMessage) {
                    ((TLRPC.TL_messages_sendMessage) object).silent = true;
                } else if (object instanceof TLRPC.TL_messages_sendMedia) {
                    ((TLRPC.TL_messages_sendMedia) object).silent = true;
                } else if (object instanceof TLRPC.TL_messages_sendMultiMedia) {
                    ((TLRPC.TL_messages_sendMultiMedia) object).silent = true;
                } else if (object instanceof TLRPC.TL_messages_forwardMessages) {
                    ((TLRPC.TL_messages_forwardMessages) object).silent = true;
                } else if (object instanceof TLRPC.TL_messages_sendInlineBotResult) {
                    ((TLRPC.TL_messages_sendInlineBotResult) object).silent = true;
                }
            }
            if (object instanceof TLRPC.TL_messages_setTyping) {
                return ghostTyping();
            }
            if (object instanceof TL_account.updateStatus && ghostOffline()) {
                ((TL_account.updateStatus) object).offline = true;
            }
        } catch (Throwable ignore) {
        }
        return false;
    }

    /** Returns the Russian text for Russian UI language and the English one otherwise. */
    public static String tr(String en, String ru) {
        try {
            Locale l = LocaleController.getInstance().getCurrentLocale();
            if (l == null) {
                l = Locale.getDefault();
            }
            return "ru".equals(l.getLanguage()) ? ru : en;
        } catch (Throwable e) {
            return en;
        }
    }
}
