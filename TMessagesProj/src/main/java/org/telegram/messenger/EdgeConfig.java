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
    public static final String SNOWFLAKES = "snowflakes";
    public static final String WAKE_LOCK = "wakeLock";
    public static final String IOS_PANEL = "iosPanel";
    public static final String HIDDEN_PREFIX = "hidden_acc_";
    public static final String DECOY_ACCOUNT = "decoyAccount";
    public static final String DURESS_HASH = "duressHash";
    public static final String DURESS_SALT = "duressSalt";
    private static final java.util.HashSet<String> unlockedChats = new java.util.HashSet<>();
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

    public static boolean snowflakes() { return get(SNOWFLAKES, false); }

    public static boolean wakeLock() { return get(WAKE_LOCK, false); }

    public static boolean iosPanel() { return get(IOS_PANEL, false); }

    public static boolean isChatLocked(int account, long dialogId) {
        return get("locked_" + account + "_" + dialogId, false);
    }

    public static void setChatLocked(int account, long dialogId, boolean locked) {
        set("locked_" + account + "_" + dialogId, locked);
    }

    public static boolean isChatUnlockedNow(int account, long dialogId) {
        return unlockedChats.contains(account + "_" + dialogId);
    }

    public static void markChatUnlocked(int account, long dialogId) {
        unlockedChats.add(account + "_" + dialogId);
    }

    public static void lockChatAgain(int account, long dialogId) {
        unlockedChats.remove(account + "_" + dialogId);
    }

    public static void forgetUnlockedChats() {
        unlockedChats.clear();
    }

    /** True while the app was opened with the hidden mode code. Not persisted on purpose. */
    public static boolean duressSession;

    /** True when this account must stay silent: hidden, or not the decoy while in hidden mode. */
    public static boolean isAccountSilent(int account) {
        if (duressSession) {
            return account != getDecoyAccount();
        }
        return isAccountHidden(account);
    }

    public static boolean isAccountVisibleInSwitcher(int account) {
        if (duressSession) {
            return account == getDecoyAccount();
        }
        return !isAccountHidden(account);
    }

    /** Hidden accounts are not listed in the switcher and never raise notifications. */
    public static boolean isAccountHidden(int account) {
        return get(HIDDEN_PREFIX + account, false);
    }

    public static void setAccountHidden(int account, boolean hidden) {
        set(HIDDEN_PREFIX + account, hidden);
    }

    public static int getDecoyAccount() {
        try {
            return p().getInt(DECOY_ACCOUNT, -1);
        } catch (Throwable e) {
            return -1;
        }
    }

    public static void setDecoyAccount(int account) {
        try {
            p().edit().putInt(DECOY_ACCOUNT, account).apply();
        } catch (Throwable ignore) {
        }
    }

    public static boolean hasDuressCode() {
        try {
            String hash = p().getString(DURESS_HASH, "");
            return hash != null && !hash.isEmpty();
        } catch (Throwable e) {
            return false;
        }
    }

    public static void setDuressCode(String code) {
        try {
            if (code == null || code.isEmpty()) {
                p().edit().remove(DURESS_HASH).remove(DURESS_SALT).apply();
                return;
            }
            String salt = Utilities.bytesToHex(Utilities.computeSHA256(("edgegram" + System.currentTimeMillis()).getBytes(), 0, 0));
            String hash = Utilities.bytesToHex(Utilities.computeSHA256((salt + code).getBytes(), 0, 0));
            p().edit().putString(DURESS_SALT, salt).putString(DURESS_HASH, hash).apply();
        } catch (Throwable ignore) {
        }
    }

    public static boolean checkDuressCode(String code) {
        try {
            String hash = p().getString(DURESS_HASH, "");
            String salt = p().getString(DURESS_SALT, "");
            if (hash == null || hash.isEmpty()) {
                return false;
            }
            String test = Utilities.bytesToHex(Utilities.computeSHA256((salt + code).getBytes(), 0, 0));
            return hash.equals(test);
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean showUserId() { return get(SHOW_USER_ID, true); }
    public static boolean secretFree() { return get(SECRET_FREE, true); }
    public static boolean keepTtl() { return get(KEEP_TTL, true); }
    public static boolean hidePhone() { return get(HIDE_PHONE, false); }
    public static boolean hideStories() { return get(HIDE_STORIES, false); }
    public static boolean hidePremium() { return get(HIDE_PREMIUM, false); }

    public static String maskPhone(String phone) {
        return hidePhone() ? "+\u2022\u2022\u2022 \u2022\u2022\u2022 \u2022\u2022 \u2022\u2022" : phone;
    }

    public static void setString(String key, String value) {
        try {
            p().edit().putString(key, value == null ? "" : value).apply();
        } catch (Throwable ignore) {
        }
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

    private static boolean isSecretKey(String key) {
        return "apiId".equals(key) || "apiHash".equals(key) || "sttKey".equals(key);
    }

    /** Whole settings set as a pretty JSON document. Secrets are never exported. */
    public static String exportSettings() {
        try {
            org.json.JSONObject root = new org.json.JSONObject();
            root.put("app", "EdgeGram");
            root.put("version", 1);
            root.put("date", System.currentTimeMillis() / 1000);
            org.json.JSONObject settings = new org.json.JSONObject();
            for (java.util.Map.Entry<String, ?> e : p().getAll().entrySet()) {
                final String key = e.getKey();
                if (isSecretKey(key) || key.startsWith("seen_")) {
                    continue;
                }
                final Object value = e.getValue();
                if (value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof String) {
                    settings.put(key, value);
                }
            }
            root.put("settings", settings);
            return root.toString(2);
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    /** Applies a previously exported document. Returns the number of applied keys, or -1 on error. */
    public static int importSettings(String json) {
        try {
            org.json.JSONObject root = new org.json.JSONObject(json);
            org.json.JSONObject settings = root.optJSONObject("settings");
            if (settings == null) {
                return -1;
            }
            SharedPreferences.Editor editor = p().edit();
            int applied = 0;
            java.util.Iterator<String> keys = settings.keys();
            while (keys.hasNext()) {
                final String key = keys.next();
                if (isSecretKey(key) || key.startsWith("seen_")) {
                    continue;
                }
                final Object value = settings.get(key);
                if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                } else if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else {
                    continue;
                }
                applied++;
            }
            editor.commit();
            return applied;
        } catch (Throwable e) {
            FileLog.e(e);
            return -1;
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
