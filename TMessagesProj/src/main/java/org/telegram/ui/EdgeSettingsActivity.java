package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.EdgeConfig;
import org.telegram.messenger.EdgeUpdater;
import org.telegram.messenger.EdgeKeepAliveService;
import org.telegram.messenger.EdgeStore;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.EdgePreviewView;
import org.telegram.ui.Components.EdgeStickerPreviewView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

/** EdgeGram settings: a main page with sections and one sub-page for each section. */
public class EdgeSettingsActivity extends UniversalFragment {
    public static final int PAGE_MAIN = 0;
    public static final int PAGE_GHOST = 1;
    public static final int PAGE_SPY = 2;
    public static final int PAGE_CUSTOM = 3;
    public static final int PAGE_SECRET = 4;
    public static final int PAGE_LOOK = 5;
    public static final int PAGE_TOOLS = 6;
    public static final int PAGE_NOTIFY = 9;
    public static final int PAGE_HIDDEN = 10;
    public static final int PAGE_LOCKED_CHATS = 11;

    private static final int ID_OPEN_BASE = 100;

    private static final int ID_KEEP_DELETED = 1;
    private static final int ID_KEEP_EDITS = 2;
    private static final int ID_GHOST_READ = 3;
    private static final int ID_GHOST_TYPING = 4;
    private static final int ID_GHOST_OFFLINE = 5;
    private static final int ID_RESTRICTED = 6;
    private static final int ID_NO_ADS = 7;
    private static final int ID_HD = 8;
    private static final int ID_MARK_TEXT = 9;
    private static final int ID_API_KEYS = 11;
    private static final int ID_SECRET = 12;
    private static final int ID_TTL = 13;
    private static final int ID_STT = 14;
    private static final int ID_HIDE_PHONE = 15;
    private static final int ID_HIDE_STORIES = 16;
    private static final int ID_HIDE_PREMIUM = 17;
    private static final int ID_GHOST_STORIES = 18;
    private static final int ID_GHOST_ON_ACTION = 19;
    private static final int ID_SEND_SILENT = 20;
    private static final int ID_LAST_SEEN = 21;
    private static final int ID_DIM = 22;
    private static final int ID_LOCAL_PREMIUM = 23;
    private static final int ID_CLEAR = 24;
    private static final int ID_APPEARANCE = 25;
    private static final int ID_STATS = 26;
    private static final int ID_TIME_SECONDS = 27;
    private static final int ID_NO_ROUNDING = 28;
    private static final int ID_TABLET = 29;
    private static final int ID_STICKER_TIME = 30;
    private static final int ID_KB_SCROLL = 31;
    private static final int ID_GLASS = 32;
    private static final int ID_GOOEY = 33;
    private static final int ID_BOTTOM_TABS = 34;
    private static final int ID_MINI_AVATAR = 35;
    private static final int ID_MEDIA_PREVIEW = 36;
    private static final int ID_STICKER_SIZE = 37;
    private static final int ID_SHOW_USER_ID = 38;
    private static final int ID_NOTIF_PERM = 40;
    private static final int ID_BATTERY = 41;
    private static final int ID_APP_SETTINGS = 42;
    private static final int ID_NOTIF_CHANNELS = 43;
    private static final int ID_SPEED_BOOST = 44;
    private static final int ID_HIDDEN_SEARCH = 45;
    private static final int ID_KEEP_ALIVE = 46;
    private static final int ID_SNOW = 49;
    private static final int ID_IOS_PANEL = 50;
    private static final int ID_DURESS = 60;
    private static final int ID_DECOY = 61;
    private static final int ID_HIDDEN_BASE = 200;
    private static final int ID_LOCK_BASE = 300;
    private static final int ID_CHECK_UPDATES = 70;
    private static final int ID_UPDATE_URL = 71;
    private static final int ID_LOCK_SEARCH = 72;
    private static final int ID_WAKE_LOCK = 73;
    private static final int ID_PIN_SCRAMBLE = 74;
    private static final int ID_BACKUP_EXPORT = 47;
    private static final int ID_BACKUP_IMPORT = 48;
    private static final int REQ_BACKUP_EXPORT = 4201;
    private static final int REQ_BACKUP_IMPORT = 4202;

    private final int page;
    private android.widget.EditText lockSearch;
    private String lockQuery = "";
    private PaletteView palette;
    private EdgePreviewView chatPreview;
    private EdgePreviewView listPreview;
    private EdgeStickerPreviewView stickerPreview;

    public EdgeSettingsActivity() {
        this(PAGE_MAIN);
    }

    public EdgeSettingsActivity(int page) {
        this.page = page;
    }

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        palette = new PaletteView(context, this::refreshPreviewsOnly,
                () -> EdgeMarks.showColorPicker(this, this::refreshPreviewsOnly, this::refreshViews));
        chatPreview = new EdgePreviewView(context, EdgePreviewView.MODE_CHAT);
        listPreview = new EdgePreviewView(context, EdgePreviewView.MODE_LIST);
        stickerPreview = new EdgeStickerPreviewView(context);
        fragmentView = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        return fragmentView;
    }

    @Override
    protected RecyclerListView getListViewForSimpleGlass() {
        return listView;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Override
    protected CharSequence getTitle() {
        switch (page) {
            case PAGE_GHOST:
                return t("Ghost mode", "Режим призрака");
            case PAGE_SPY:
                return t("Spy mode", "Режим шпиона");
            case PAGE_CUSTOM:
                return t("Customization", "Кастомизация");
            case PAGE_SECRET:
                return t("Secret chats and timers", "Секретные чаты и таймеры");
            case PAGE_LOOK:
                return t("Look and privacy", "Вид и приватность");
            case PAGE_TOOLS:
                return t("Tools", "Инструменты");
            case PAGE_NOTIFY:
                return t("Notifications and background", "Уведомления и фон");
            case PAGE_HIDDEN:
                return t("Hidden mode", "Скрытый режим");
            case PAGE_LOCKED_CHATS:
                return t("Chats with fingerprint", "Чаты с отпечатком");
            default:
                return "EdgeGram";
        }
    }

    private static String t(String en, String ru) {
        return EdgeConfig.tr(en, ru);
    }

    private static UItem section(int page, IconBackgroundColors c, int icon, String title, String sub) {
        return SettingsActivity.SettingCell.Factory.of(ID_OPEN_BASE + page, c.top, c.bottom, icon, title, sub);
    }

    private static UItem check(int id, String title, String sub, boolean value) {
        return UItem.asButtonCheck(id, title, sub).setChecked(value);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        switch (page) {
            case PAGE_GHOST:
                fillGhost(items);
                break;
            case PAGE_SPY:
                fillSpy(items);
                break;
            case PAGE_CUSTOM:
                fillCustom(items);
                break;
            case PAGE_SECRET:
                fillSecret(items);
                break;
            case PAGE_LOOK:
                fillLook(items);
                break;
            case PAGE_TOOLS:
                fillTools(items);
                break;
            case PAGE_NOTIFY:
                fillNotify(items);
                break;
            case PAGE_HIDDEN:
                fillHidden(items);
                break;
            case PAGE_LOCKED_CHATS:
                fillLockedChats(items);
                break;
            default:
                fillMain(items);
        }
    }

    private void fillMain(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Privacy", "Приватность")));
        items.add(section(PAGE_GHOST, IconBackgroundColors.PURPLE, R.drawable.settings_privacy, t("Ghost mode", "Режим призрака"),
                t("Don't read, don't type, stay offline", "Не читать, не печатать, быть не в сети")));
        items.add(section(PAGE_SECRET, IconBackgroundColors.GREEN, R.drawable.settings_devices, t("Secret chats and timers", "Секретные чаты и таймеры"),
                t("No limits, timed media stay", "Без ограничений, таймер-медиа остаются")));
        items.add(section(PAGE_HIDDEN, IconBackgroundColors.GRAY, R.drawable.settings_privacy, t("Hidden mode", "Скрытый режим"),
                t("Decoy passcode, invisible accounts", "Второй код и невидимые аккаунты")));
        items.add(section(PAGE_LOCKED_CHATS, IconBackgroundColors.GRAY, R.drawable.permission_locked, t("Chats with fingerprint", "Чаты с отпечатком"),
                t("Ask for biometrics when opening a chat", "Спрашивать отпечаток при открытии чата")));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("History", "История")));
        items.add(section(PAGE_SPY, IconBackgroundColors.RED, R.drawable.settings_data, t("Spy mode", "Режим шпиона"),
                t("Deleted messages, edits, last seen", "Удалённые, правки, последний онлайн")));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("Appearance", "Оформление")));
        items.add(section(PAGE_CUSTOM, IconBackgroundColors.ORANGE, R.drawable.settings_chat, t("Customization", "Кастомизация"),
                t("Deleted mark, colors, ads", "Метка удалённых, цвета, реклама")));
        items.add(section(PAGE_LOOK, IconBackgroundColors.BLUE, R.drawable.settings_features, t("Look and privacy", "Вид и приватность"),
                t("Phone number, stories, photo quality", "Номер, истории, качество фото")));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("More", "Ещё")));
        items.add(section(PAGE_TOOLS, IconBackgroundColors.CYAN, R.drawable.settings_policy, t("Tools", "Инструменты"),
                t("Voice transcription, API keys", "Расшифровка голосовых, API-ключи")));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_OPEN_BASE + 8, IconBackgroundColors.BLUE_ALT.top, IconBackgroundColors.BLUE_ALT.bottom, R.drawable.settings_faq,
                t("FAQ and help", "FAQ и помощь"), t("Every feature explained in plain words", "Про каждую функцию простыми словами")));
        items.add(UItem.asShadow(t("EdgeGram is built on Telegram for Android. Your data stays on the device.", "EdgeGram построен на Telegram для Android. Ваши данные остаются на устройстве.")));
    }

    private void fillGhost(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Ghost mode", "Режим призрака")));
        items.add(check(ID_GHOST_READ, t("Don't mark as read", "Не отмечать прочитанным"),
                t("Others do not see that you read their messages", "Собеседники не видят, что вы прочитали сообщение"), EdgeConfig.ghostRead()));
        items.add(check(ID_GHOST_ON_ACTION, t("Read on action", "Читать при действиях"),
                t("When you send a message, the chat is marked as read automatically", "При отправке сообщения чат автоматически отмечается прочитанным"), EdgeConfig.ghostOnAction()));
        items.add(UItem.asShadow(null));
        items.add(check(ID_GHOST_TYPING, t("Hide typing", "Скрывать набор текста"),
                t("The \"typing\" status is not sent", "Статус «печатает» не отправляется"), EdgeConfig.ghostTyping()));
        items.add(check(ID_GHOST_OFFLINE, t("Always offline", "Всегда не в сети"),
                t("Your online status is never sent", "Статус «в сети» не отправляется"), EdgeConfig.ghostOffline()));
        items.add(check(ID_GHOST_STORIES, t("Silent stories", "Тихие истории"),
                t("Stories are watched without a view mark", "Истории просматриваются без отметки о просмотре"), EdgeConfig.ghostStories()));
        items.add(UItem.asShadow(null));
        items.add(check(ID_SEND_SILENT, t("Send without sound", "Отправлять без звука"),
                t("All your messages are sent silently by default", "Все ваши сообщения по умолчанию уходят без звука"), EdgeConfig.sendSilent()));
        items.add(UItem.asShadow(t("Without \"Read on action\" you stay unseen even after replying.", "Без «Читать при действиях» вы остаётесь непрочитавшим даже после ответа.")));
    }

    private void fillSpy(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Spy mode", "Режим шпиона")));
        items.add(check(ID_KEEP_DELETED, t("Keep deleted messages", "Сохранять удалённые сообщения"),
                t("They stay in the chat with a mark", "Остаются в чате с пометкой"), EdgeConfig.keepDeleted()));
        items.add(check(ID_KEEP_EDITS, t("Keep edit history", "Сохранять историю правок"),
                t("Long-press an edited message \u2192 \"Edit history\"", "Долгое нажатие на изменённое сообщение \u2192 «История правок»"), EdgeConfig.keepEdits()));
        items.add(UItem.asShadow(null));
        items.add(check(ID_LAST_SEEN, t("Keep last seen", "Сохранять последний онлайн"),
                t("Remembers the last exact time you saw a person online and shows it as approximate when their status is hidden", "Запоминает последнее точное время, когда вы видели человека в сети, и показывает его как примерное, если статус скрыт"), EdgeConfig.get(EdgeConfig.SAVE_LAST_SEEN, true)));
        items.add(UItem.asShadow(null));
        items.add(UItem.asButton(ID_HIDDEN_SEARCH, t("What was hidden from me", "Скрытое от меня"), t("Search", "Поиск")));
        items.add(UItem.asButton(ID_STATS, t("Storage", "Хранилище"), statsText()));
        items.add(UItem.asButton(ID_CLEAR, t("Clear saved history", "Очистить сохранённую историю")));
        items.add(UItem.asShadow(t("Only messages already loaded on this device can be kept. Clearing removes the \"deleted\" marks and edit history.", "Сохраняются только сообщения, уже загруженные на это устройство. Очистка убирает метки «удалено» и историю правок.")));
    }

    private String statsText() {
        try {
            int[] st = EdgeStore.getInstance(currentAccount).counts();
            return t(st[0] + " deleted, " + st[1] + " edits", st[0] + " удалено, " + st[1] + " правок");
        } catch (Throwable e) {
            return "";
        }
    }

    /** Repaints the live previews and rebuilds the list, so changes show immediately. */
    private void refreshViews() {
        if (chatPreview != null) {
            chatPreview.invalidate();
        }
        if (listPreview != null) {
            listPreview.invalidate();
        }
        if (palette != null) {
            palette.invalidate();
        }
        if (stickerPreview != null) {
            stickerPreview.invalidate();
        }
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(false);
        }
    }

    /** Repaints only the preview widgets, without rebuilding the whole list. */
    private void refreshPreviewsOnly() {
        if (chatPreview != null) {
            chatPreview.invalidate();
        }
        if (listPreview != null) {
            listPreview.invalidate();
        }
        if (palette != null) {
            palette.invalidate();
        }
    }

    private void fillCustom(ArrayList<UItem> items) {
        if (chatPreview == null) {
            chatPreview = new EdgePreviewView(getContext(), EdgePreviewView.MODE_CHAT);
        }
        chatPreview.invalidate();
        items.add(UItem.asHeader(t("Preview", "Превью")));
        items.add(UItem.asCustomShadow(chatPreview, 186));
        items.add(UItem.asHeader(t("Deleted messages", "Удалённые сообщения")));
        items.add(check(ID_DIM, t("Semi-transparent deleted", "Полупрозрачные удалёнки"),
                t("Deleted messages are dimmed", "Удалённые сообщения затемняются"), EdgeConfig.dimDeleted()));
        items.add(UItem.asButton(ID_MARK_TEXT, t("Mark", "Метка удалёнок"), EdgeConfig.getMarkText()));
        if (palette == null) {
            palette = new PaletteView(getContext(), this::refreshPreviewsOnly,
                    () -> EdgeMarks.showColorPicker(this, this::refreshPreviewsOnly, this::refreshViews));
        }
        items.add(UItem.asCustom(palette, 70));
        items.add(UItem.asShadow(t("Pick a color for the mark. You can set any text or an emoji, e.g. a trash can.", "Выберите цвет метки. Текст можно задать любой, в том числе смайлик, например мусорное ведро.")));

        items.add(UItem.asHeader(t("Interface", "Интерфейс")));
        items.add(check(ID_TIME_SECONDS, t("Time with seconds", "Время с секундами"),
                t("Message time is shown as 12:34:56", "Время сообщений показывается как 12:34:56"), EdgeConfig.timeWithSeconds()));
        items.add(check(ID_NO_ROUNDING, t("No number rounding", "Без сокращения чисел"),
                t("4.8K becomes 4777", "4.8K превращается в 4777"), EdgeConfig.noRounding()));
        items.add(cycle(ID_TABLET, t("Tablet mode", "Планшетный режим"), tabletLabel()));
        items.add(check(ID_STICKER_TIME, t("Hide time on stickers", "Скрывать время на стикерах"),
                t("No clock in the corner of stickers", "Без часов в углу стикеров"), EdgeConfig.stickerNoTime()));
        items.add(check(ID_SHOW_USER_ID, t("Show user ID in profile", "Показывать ID пользователя в профиле"),
                t("Adds an item with the numeric ID to the profile menu, tap it to copy", "Добавляет в меню профиля пункт с числовым ID, нажатие копирует его"), EdgeConfig.showUserId()));
        items.add(check(ID_MINI_AVATAR, t("Mini sender avatars", "Мини-аватарки отправителей"),
                t("Small avatar next to the sender name in the chat list", "Маленький аватар рядом с именем отправителя в списке чатов"), EdgeConfig.miniSenderAvatar()));
        items.add(check(ID_MEDIA_PREVIEW, t("Media preview in chat list", "Превью медиа в списке чатов"),
                t("Show thumbnails of the last photo or video", "Показывать миниатюры последнего фото или видео"), EdgeConfig.mediaPreview()));
        if (stickerPreview == null) {
            stickerPreview = new EdgeStickerPreviewView(getContext());
        }
        stickerPreview.invalidate();
        items.add(UItem.asCustomShadow(stickerPreview, 210));
        items.add(UItem.asButton(ID_STICKER_SIZE, t("Sticker size", "Размер стикеров"), EdgeConfig.stickerSize() + " dp"));
        items.add(UItem.asButton(ID_SPEED_BOOST, t("Download speed", "Скорость загрузки"), EdgeConfig.speedBoostName()));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("Layout", "Раскладка")));
        items.add(check(ID_BOTTOM_TABS, t("Hide bottom tabs", "Скрыть нижние вкладки"),
                t("The tab bar at the bottom of the chat list disappears", "Нижняя панель вкладок в списке чатов пропадает"), EdgeConfig.hideBottomTabs()));
        items.add(check(ID_KB_SCROLL, t("Hide keyboard on scroll", "Скрывать клавиатуру при прокрутке"),
                t("The keyboard closes when you scroll the chat", "Клавиатура закрывается при прокрутке чата"), EdgeConfig.hideKeyboardOnScroll()));
        items.add(check(ID_GOOEY, t("\"Sticky\" avatar animation", "«Липкая» анимация аватара"),
                t("Profile avatar melts into the header when you scroll", "Аватар профиля «растворяется» в шапке при прокрутке"), EdgeConfig.gooeyAvatar()));
        items.add(check(ID_GLASS, t("Glass outlines", "Контуры стеклянных элементов"),
                t("Thin light outline around glass panels", "Тонкая светлая обводка у стеклянных панелей"), EdgeConfig.glassStroke()));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("Useful", "Полезное")));
        items.add(check(ID_NO_ADS, t("Hide ads", "Отключить рекламу"),
                t("Sponsored messages are not shown", "Спонсорские сообщения не показываются"), EdgeConfig.noAds()));
        items.add(check(ID_RESTRICTED, t("Allow saving and copying", "Разрешить сохранение и копирование"),
                t("Works in chats with restricted content", "Работает и в чатах с запретом копирования"), EdgeConfig.allowRestricted()));
        items.add(check(ID_LOCAL_PREMIUM, t("Local Premium", "Локальный Premium"),
                t("Unlocks Premium interface locally. Server features still need a real subscription", "Открывает Premium-интерфейс локально. Серверные функции требуют настоящей подписки"), EdgeConfig.localPremium()));
        items.add(UItem.asShadow(null));
    }

    private void openNotificationSettings() {
        if (getParentActivity() == null) {
            return;
        }
        try {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getParentActivity().getPackageName());
            getParentActivity().startActivity(intent);
        } catch (Throwable e) {
            openAppDetails();
        }
    }

    private void openAppDetails() {
        if (getParentActivity() == null) {
            return;
        }
        try {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getParentActivity().getPackageName()));
            getParentActivity().startActivity(intent);
        } catch (Throwable ignore) {
        }
    }

    private void requestIgnoreBattery() {
        if (getParentActivity() == null) {
            return;
        }
        try {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(android.net.Uri.parse("package:" + getParentActivity().getPackageName()));
            getParentActivity().startActivity(intent);
        } catch (Throwable e) {
            openAppDetails();
        }
    }

    private void openSection(int index) {
        if (index == 8) {
            presentFragment(new EdgeFaqActivity());
        } else {
            presentFragment(new EdgeSettingsActivity(index));
        }
    }

    private static UItem cycle(int id, String title, String value) {
        return UItem.asButton(id, title, value);
    }

    private String tabletLabel() {
        switch (EdgeConfig.tabletMode()) {
            case 1:
                return t("On", "Включён");
            case 2:
                return t("Off", "Выключен");
            default:
                return t("Auto", "Автоматически");
        }
    }

    private void fillSecret(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Secret chats", "Секретные чаты")));
        items.add(check(ID_SECRET, t("No limits", "Без ограничений"),
                t("Screenshots, forwarding and saving are allowed; screenshot notices are not sent", "Скриншоты, пересылка и сохранение разрешены, уведомления о скриншотах не отправляются"), EdgeConfig.secretFree()));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader(t("Timed media", "Медиа с таймером")));
        items.add(check(ID_TTL, t("Keep timed photos and videos", "Не удалять одноразовые фото и видео"),
                t("They stay on your device. A \"Mark as read\" button appears in the viewer", "Остаются у вас. В просмотрщике появляется кнопка «Прочитано»"), EdgeConfig.keepTtl()));
        items.add(UItem.asShadow(t("Nothing is marked as read until you press the button, so the sender does not see that you opened it.", "Пока вы не нажмёте кнопку, отправитель не узнает, что вы открыли медиа.")));
    }

    private void fillLook(ArrayList<UItem> items) {
        if (listPreview == null) {
            listPreview = new EdgePreviewView(getContext(), EdgePreviewView.MODE_LIST);
        }
        listPreview.invalidate();
        items.add(UItem.asHeader(t("Chat list preview", "Превью списка чатов")));
        items.add(UItem.asCustomShadow(listPreview, 80));
        items.add(UItem.asShadow(t("This is how your chat list looks with the current settings.", "Так выглядит ваш список чатов с текущими настройками.")));
        items.add(UItem.asHeader(t("Privacy", "Приватность")));
        items.add(check(ID_PIN_SCRAMBLE, t("Shuffle PIN keypad", "Перемешивать цифры PIN"),
                t("Digits change places every time the passcode screen opens", "Цифры меняются местами при каждом открытии экрана кода"),
                org.telegram.messenger.EdgeConfig.pinScramble()));
        items.add(check(ID_HIDE_PHONE, t("Hide my phone number", "Скрывать мой номер телефона"),
                t("Masks the number in settings and profile", "Скрывает номер в настройках и профиле"), EdgeConfig.hidePhone()));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader(t("Interface", "Интерфейс")));
        items.add(check(ID_HIDE_STORIES, t("Hide stories", "Скрывать истории"),
                t("Removes the stories row. Takes effect after restart", "Убирает ленту историй. Вступит в силу после перезапуска"), EdgeConfig.hideStories()));
        items.add(check(ID_HIDE_PREMIUM, t("Hide Premium offers", "Скрывать предложения Premium"),
                t("Removes Premium buttons and promos. Takes effect after restart", "Убирает кнопки и рекламу Premium. Вступит в силу после перезапуска"), EdgeConfig.hidePremium()));
        items.add(check(ID_IOS_PANEL, t("iOS-style input panel", "Панель ввода в стиле iOS"),
                t("Moves the attach button inside the field on the left. Takes effect when you reopen a chat",
                        "Переносит скрепку внутрь поля слева. Применяется при следующем открытии чата"), EdgeConfig.iosPanel()));
        items.add(check(ID_SNOW, t("Snowflakes", "Снежинки"),
                t("Snow falls over the chat list and chats", "Снег падает в списке чатов и в переписках"), EdgeConfig.snowflakes()));
        items.add(UItem.asButton(ID_APPEARANCE, t("Telegram themes and corners", "Темы и скругления Telegram")));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader(t("Media", "Медиа")));
        items.add(check(ID_HD, t("Larger photos", "Фото в большем размере"),
                t("Photos are sent in up to 2560 px. Takes effect after restart", "Фото отправляются до 2560 px. Вступит в силу после перезапуска"), EdgeConfig.hdPhotos()));
        items.add(UItem.asShadow(null));
    }

    private void fillNotify(ArrayList<UItem> items) {
        final android.content.Context ctx = getContext();
        final boolean notifGranted = ctx != null && androidx.core.content.ContextCompat.checkSelfPermission(ctx,
                android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean batteryOk = true;
        try {
            android.os.PowerManager pm = (android.os.PowerManager) ctx.getSystemService(android.content.Context.POWER_SERVICE);
            batteryOk = pm != null && pm.isIgnoringBatteryOptimizations(ctx.getPackageName());
        } catch (Throwable ignore) {
        }

        items.add(UItem.asHeader(t("State", "Состояние")));
        items.add(UItem.asButton(ID_NOTIF_PERM, t("Notifications", "Уведомления"),
                notifGranted ? t("Granted", "Разрешено") : t("Denied — tap to fix", "Запрещено — нажмите, чтобы исправить")));
        items.add(UItem.asButton(ID_BATTERY, t("Background work", "Работа в фоне"),
                batteryOk ? t("Allowed", "Разрешена") : t("Restricted — tap to allow", "Ограничена — нажмите, чтобы разрешить")));
        items.add(UItem.asHeader(t("Your phone", "Ваш телефон") + ": " + android.os.Build.MANUFACTURER));
        items.add(UItem.asShadow(brandHint()));
        items.add(check(ID_WAKE_LOCK, t("Active connection (wake lock)", "Активное соединение (wake lock)"),
                t("Keeps the CPU awake so notifications arrive even in Doze. Uses noticeably more battery.",
                        "Не даёт процессору засыпать, чтобы уведомления шли и в Doze. Заметно расходует батарею."),
                org.telegram.messenger.EdgeConfig.wakeLock()));
        items.add(check(ID_KEEP_ALIVE, t("Stay connected in background", "Держать соединение в фоне"),
                t("A quiet permanent notification; messages arrive even after the app is swiped away. Uses more battery.",
                        "Тихое постоянное уведомление; сообщения приходят даже после закрытия приложения. Расходует батарею."), EdgeConfig.keepAlive()));
        items.add(UItem.asButton(ID_APP_SETTINGS, t("Open app settings", "Открыть настройки приложения")));
        items.add(UItem.asShadow(t("If notifications do not arrive while the app is closed, the phone is killing it. Open the app settings and allow: Autostart (Автозапуск), Run in background (Работа в фоне), and remove battery restrictions.",
                "Если уведомления не приходят при закрытом приложении, телефон его убивает. Откройте настройки приложения и разрешите: «Автозапуск», «Работа в фоне», и снимите ограничения батареи.")));

        items.add(UItem.asHeader(t("Channels", "Каналы")));
        items.add(UItem.asButton(ID_NOTIF_CHANNELS, t("Notification channels", "Каналы уведомлений")));
        items.add(UItem.asShadow(t("Turn on the channels you need: messages, groups, channels, calls.",
                "Включите нужные каналы: сообщения, группы, каналы, звонки.")));
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        if (resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        final android.net.Uri uri = data.getData();
        if (requestCode == REQ_BACKUP_EXPORT) {
            try {
                final String json = EdgeConfig.exportSettings();
                if (json == null) {
                    return;
                }
                java.io.OutputStream os = getContext().getContentResolver().openOutputStream(uri);
                if (os == null) {
                    return;
                }
                os.write(json.getBytes("UTF-8"));
                os.close();
                android.widget.Toast.makeText(getContext(), t("Settings exported", "Настройки экспортированы"), android.widget.Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                org.telegram.messenger.FileLog.e(e);
                android.widget.Toast.makeText(getContext(), t("Could not export", "Не удалось экспортировать"), android.widget.Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQ_BACKUP_IMPORT) {
            try {
                java.io.InputStream is = getContext().getContentResolver().openInputStream(uri);
                if (is == null) {
                    return;
                }
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) > 0) {
                    bos.write(buffer, 0, read);
                }
                is.close();
                final int applied = EdgeConfig.importSettings(new String(bos.toByteArray(), "UTF-8"));
                if (applied >= 0) {
                    refreshViews();
                    android.widget.Toast.makeText(getContext(), t("Imported", "Импортировано") + ": " + applied, android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    android.widget.Toast.makeText(getContext(), t("Wrong file", "Неверный файл"), android.widget.Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable e) {
                org.telegram.messenger.FileLog.e(e);
            }
        }
    }

    private void fillLockedChats(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Chats", "Чаты")));
        items.add(UItem.asButton(ID_LOCK_SEARCH, t("Search chats", "Поиск по чатам"),
                lockQuery.isEmpty() ? t("All chats", "Все чаты") : lockQuery));
        items.add(UItem.asShadow(t("Turn the switch on and the chat will ask for a fingerprint when opened. The lock resets when the app is locked.",
                "Включите переключатель — чат будет спрашивать отпечаток при открытии. Блокировка сбрасывается при блокировке приложения.")));
        final ArrayList<org.telegram.tgnet.TLRPC.Dialog> dialogs =
                org.telegram.messenger.MessagesController.getInstance(currentAccount).getAllDialogs();
        int shown = 0;
        for (int i = 0; i < dialogs.size() && shown < 100; i++) {
            final org.telegram.tgnet.TLRPC.Dialog dialog = dialogs.get(i);
            if (dialog == null || dialog.id == 0
                    || org.telegram.messenger.DialogObject.isEncryptedDialog(dialog.id)
                    || dialog.id > 1_000_000_000_000L) {
                continue;
            }
            final String name = chatNameForLock(dialog.id);
            if (!lockQuery.isEmpty() && !name.toLowerCase().contains(lockQuery.toLowerCase())) {
                continue;
            }
            items.add(check(ID_LOCK_BASE + i, name, null,
                    org.telegram.messenger.EdgeConfig.isChatLocked(currentAccount, dialog.id)));
            shown++;
        }
        if (shown == 0) {
            items.add(UItem.asShadow(t("No chats yet.", "Чатов пока нет.")));
        }
        items.add(UItem.asShadow(null));
    }

    private String chatNameForLock(long dialogId) {
        try {
            final org.telegram.messenger.MessagesController controller = org.telegram.messenger.MessagesController.getInstance(currentAccount);
            if (dialogId > 0) {
                final org.telegram.tgnet.TLRPC.User user = controller.getUser(dialogId);
                if (user != null) {
                    final String name = org.telegram.messenger.ContactsController.formatName(user.first_name, user.last_name);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            } else {
                final org.telegram.tgnet.TLRPC.Chat chat = controller.getChat(-dialogId);
                if (chat != null && chat.title != null && !chat.title.isEmpty()) {
                    return chat.title;
                }
            }
        } catch (Throwable ignore) {
        }
        return String.valueOf(dialogId);
    }

    private void fillHidden(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Accounts", "Аккаунты")));
        boolean any = false;
        for (int a = 0; a < org.telegram.messenger.UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final org.telegram.messenger.UserConfig config = org.telegram.messenger.UserConfig.getInstance(a);
            if (!config.isClientActivated()) {
                continue;
            }
            any = true;
            final org.telegram.tgnet.TLRPC.User user = config.getCurrentUser();
            final String name = user != null
                    ? org.telegram.messenger.ContactsController.formatName(user.first_name, user.last_name)
                    : t("Account", "Аккаунт") + " " + (a + 1);
            items.add(check(ID_HIDDEN_BASE + a, name,
                    t("Hidden from the switcher, no notifications", "Скрыт из переключателя, без уведомлений"),
                    org.telegram.messenger.EdgeConfig.isAccountHidden(a)));
        }
        if (!any) {
            items.add(UItem.asShadow(t("Add a second account first, then hide it here.", "Сначала добавьте второй аккаунт, потом скройте его здесь.")));
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("Hidden passcode", "Код скрытого режима")));
        items.add(UItem.asButton(ID_DURESS, t("Hidden mode code", "Код скрытого режима"),
                org.telegram.messenger.EdgeConfig.hasDuressCode() ? t("Set", "Задан") : t("Not set", "Не задан")));
        items.add(UItem.asButton(ID_DECOY, t("Decoy account", "Подставной аккаунт"), decoyAccountName()));
        items.add(UItem.asShadow(t("With this code the app opens only the decoy account. The regular passcode keeps hidden accounts invisible and silent.",
                "С этим кодом приложение откроет только подставной аккаунт. Обычный код оставляет скрытые аккаунты невидимыми и беззвучными.")));
    }

    private String decoyAccountName() {
        final int decoy = org.telegram.messenger.EdgeConfig.getDecoyAccount();
        if (decoy < 0) {
            return t("Not selected", "Не выбран");
        }
        final org.telegram.messenger.UserConfig config = org.telegram.messenger.UserConfig.getInstance(decoy);
        final org.telegram.tgnet.TLRPC.User user = config.getCurrentUser();
        if (user == null) {
            return t("Not selected", "Не выбран");
        }
        return org.telegram.messenger.ContactsController.formatName(user.first_name, user.last_name);
    }

    private void showDuressCodeDialog() {
        final android.content.Context ctx = getParentActivity() != null ? getParentActivity() : getContext();
        if (ctx == null) {
            return;
        }
        final int textColor = org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_dialogTextBlack);
        final int hintColor = org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_dialogTextHint);

        final android.widget.LinearLayout box = new android.widget.LinearLayout(ctx);
        box.setOrientation(android.widget.LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), 0);

        final android.widget.TextView info = new android.widget.TextView(ctx);
        info.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 14);
        info.setTextColor(org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_dialogTextGray2));
        info.setText(t("The main code turns on the app lock itself, no need to set it in Telegram. Both codes must be different.",
                "Основной код сам включит блокировку приложения, в настройки Telegram идти не нужно. Коды должны отличаться."));
        box.addView(info);

        final android.widget.EditText mainInput = new android.widget.EditText(ctx);
        mainInput.setSingleLine(true);
        mainInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        mainInput.setHint(t("Main code (unlocks everything)", "Основной код (открывает всё)"));
        mainInput.setTextColor(textColor);
        mainInput.setHintTextColor(hintColor);
        box.addView(mainInput);

        final android.widget.EditText hiddenInput = new android.widget.EditText(ctx);
        hiddenInput.setSingleLine(true);
        hiddenInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        hiddenInput.setHint(t("Hidden mode code (opens the decoy)", "Код скрытого режима (открывает подставной)"));
        hiddenInput.setTextColor(textColor);
        hiddenInput.setHintTextColor(hintColor);
        box.addView(hiddenInput);

        final org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(ctx);
        builder.setTitle(t("Codes", "Коды"));
        builder.setView(box);
        builder.setPositiveButton(t("Save", "Сохранить"), (d, w) -> {
            final String main = mainInput.getText().toString().trim();
            final String hidden = hiddenInput.getText().toString().trim();
            if (!main.isEmpty() && org.telegram.messenger.SharedConfig.passcodeHash.isEmpty()) {
                org.telegram.messenger.SharedConfig.enablePasscode(main);
            }
            if (!hidden.isEmpty()) {
                org.telegram.messenger.EdgeConfig.setDuressCode(hidden);
            }
            refreshViews();
        });
        builder.setNeutralButton(t("Remove", "Удалить"), (d, w) -> {
            org.telegram.messenger.EdgeConfig.setDuressCode("");
            refreshViews();
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        builder.show();
    }

    private void showLockSearchDialog() {
        final android.content.Context ctx = getParentActivity() != null ? getParentActivity() : getContext();
        if (ctx == null) {
            return;
        }
        final android.widget.LinearLayout box = new android.widget.LinearLayout(ctx);
        box.setOrientation(android.widget.LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), 0);
        final android.widget.EditText input = new android.widget.EditText(ctx);
        input.setSingleLine(true);
        input.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 16);
        input.setHint(t("Chat name", "Название чата"));
        input.setText(lockQuery);
        input.setTextColor(org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_dialogTextBlack));
        input.setHintTextColor(org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_dialogTextHint));
        box.addView(input);
        final org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(ctx);
        builder.setTitle(t("Search chats", "Поиск по чатам"));
        builder.setView(box);
        builder.setPositiveButton(t("Apply", "Применить"), (d, w) -> {
            lockQuery = input.getText().toString().trim();
            refreshViews();
        });
        builder.setNeutralButton(t("Reset", "Сбросить"), (d, w) -> {
            lockQuery = "";
            refreshViews();
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        builder.show();
    }

    private void showUpdateUrlDialog() {
        final android.content.Context ctx = getParentActivity() != null ? getParentActivity() : getContext();
        if (ctx == null) {
            return;
        }
        final int textColor = org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_dialogTextBlack);
        final android.widget.EditText input = new android.widget.EditText(ctx);
        input.setSingleLine(true);
        input.setHint("https://…/latest.json");
        input.setTextColor(textColor);
        input.setHintTextColor(org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_dialogTextHint));
        input.setText(EdgeConfig.getString("updateUrl", ""));
        input.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(8), AndroidUtilities.dp(22), 0);
        final org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(ctx);
        builder.setTitle(t("Update server", "Сервер обновлений"));
        builder.setView(input);
        builder.setPositiveButton(t("Save", "Сохранить"), (d, w) -> {
            EdgeConfig.setString("updateUrl", input.getText().toString().trim());
            refreshViews();
        });
        builder.setNeutralButton(t("GitHub", "GitHub"), (d, w) -> {
            EdgeConfig.setString("updateUrl", "");
            refreshViews();
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        builder.show();
    }

    private void showDecoyDialog() {
        final android.content.Context ctx = getParentActivity() != null ? getParentActivity() : getContext();
        if (ctx == null) {
            return;
        }
        final java.util.ArrayList<Integer> accounts = new java.util.ArrayList<>();
        final java.util.ArrayList<CharSequence> names = new java.util.ArrayList<>();
        for (int a = 0; a < org.telegram.messenger.UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final org.telegram.messenger.UserConfig config = org.telegram.messenger.UserConfig.getInstance(a);
            if (!config.isClientActivated()) {
                continue;
            }
            final org.telegram.tgnet.TLRPC.User user = config.getCurrentUser();
            accounts.add(a);
            names.add(user != null
                    ? org.telegram.messenger.ContactsController.formatName(user.first_name, user.last_name)
                    : t("Account", "Аккаунт") + " " + (a + 1));
        }
        if (accounts.isEmpty()) {
            return;
        }
        final org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(ctx);
        builder.setTitle(t("Decoy account", "Подставной аккаунт"));
        builder.setItems(names.toArray(new CharSequence[0]), (d, which) -> {
            org.telegram.messenger.EdgeConfig.setDecoyAccount(accounts.get(which));
            refreshViews();
        });
        builder.setNegativeButton(t("Cancel", "Отмена"), null);
        builder.show();
    }

    private String brandHint() {
        final String brand = (android.os.Build.MANUFACTURER + " " + android.os.Build.BRAND).toLowerCase();
        if (brand.contains("samsung")) {
            return t("Samsung: Settings - Apps - EdgeGram - Battery - Unrestricted. Also Settings - Battery - Background usage limits - remove EdgeGram from Sleeping apps.",
                    "Samsung: Настройки - Приложения - EdgeGram - Батарея - «Без ограничений». Ещё Настройки - Батарея - Ограничения фоновой работы - уберите EdgeGram из «Спящих приложений».");
        }
        if (brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")) {
            return t("Xiaomi/Redmi/POCO: Security - Permissions - Autostart - enable EdgeGram. Then Settings - Battery - Battery saver - No restrictions.",
                    "Xiaomi/Redmi/POCO: Безопасность - Разрешения - Автозапуск - включите EdgeGram. Затем Настройки - Батарея - Экономия - «Без ограничений».");
        }
        if (brand.contains("huawei") || brand.contains("honor")) {
            return t("Honor/Huawei: Settings - Battery - App launch - EdgeGram - Manage automatically OFF, then enable Auto-launch, Secondary launch and Run in background.",
                    "Honor/Huawei: Настройки - Батарея - Запуск приложений - EdgeGram - «Управлять автоматически» выключить, включить «Автозапуск», «Косвенный запуск» и «Работа в фоне».");
        }
        if (brand.contains("meizu")) {
            return t("Meizu: Settings - Power - Background app management - EdgeGram - Allow. Also enable Autostart in Security - Permissions.",
                    "Meizu: Настройки - Питание - Управление фоновыми приложениями - EdgeGram - «Разрешить». Ещё включите автозапуск в Безопасность - Разрешения.");
        }
        if (brand.contains("oppo") || brand.contains("realme") || brand.contains("oneplus")) {
            return t("OPPO/realme/OnePlus: Settings - Battery - Battery optimization - EdgeGram - Don't optimize. Also allow Auto-launch in app management.",
                    "OPPO/realme/OnePlus: Настройки - Батарея - Оптимизация батареи - EdgeGram - «Не оптимизировать». Также разрешите автозапуск в управлении приложениями.");
        }
        if (brand.contains("vivo") || brand.contains("iqoo")) {
            return t("vivo/iQOO: Settings - Battery - Power saving - EdgeGram - No restriction. Also enable Auto-start in app permissions.",
                    "vivo/iQOO: Настройки - Батарея - Энергосбережение - EdgeGram - «Не ограничивать». Также включите автозапуск в разрешениях приложения.");
        }
        return t("Allow autostart, background work and remove battery restrictions for EdgeGram in the system settings.",
                "Разрешите автозапуск, работу в фоне и снимите ограничения батареи для EdgeGram в настройках системы.");
    }

    private void fillTools(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Tools", "Инструменты")));
        items.add(UItem.asButton(ID_STT, t("Voice transcription", "Расшифровка голосовых"), EdgeConfig.getSttKey().isEmpty() ? t("Not set up", "Не настроена") : t("On", "Включена")));
        items.add(UItem.asButton(ID_API_KEYS, t("API keys", "API-ключи"), EdgeConfig.hasCustomApiKeys() ? t("Custom", "Свои") : t("Built-in", "Встроенные")));
        items.add(UItem.asButton(ID_OPEN_BASE + PAGE_NOTIFY, t("Notifications", "Уведомления"), t("Check", "Проверить")));
        items.add(UItem.asButton(ID_CHECK_UPDATES, t("Check for updates", "Проверить обновления"),
                t("Version", "Версия") + " " + EdgeUpdater.currentVersionCode(getParentActivity())));
        items.add(UItem.asButton(ID_UPDATE_URL, t("Update server", "Сервер обновлений"),
                EdgeConfig.getString("updateUrl", "").isEmpty() ? t("GitHub", "GitHub") : t("Custom", "Свой")));
        items.add(UItem.asShadow(t("Updates are installed over the app, data stays. A custom URL must return the update JSON.",
                "Обновление ставится поверх приложения, данные сохраняются. Свой URL должен отдавать JSON обновления.")));
        items.add(UItem.asButton(ID_BACKUP_EXPORT, t("Export settings", "Экспорт настроек"), t("Save to a file", "Сохранить в файл")));
        items.add(UItem.asButton(ID_BACKUP_IMPORT, t("Import settings", "Импорт настроек"), t("Restore from a file", "Восстановить из файла")));
        items.add(UItem.asShadow(t("Everything from this screen, including colours and marks. API keys and the speech key are never exported.",
                "Всё с этого экрана, включая цвета и метки. API-ключи и ключ расшифровки не экспортируются.")));
        items.add(UItem.asShadow(t("Up to 10 accounts can be added in the same app.", "В приложение можно добавить до 10 аккаунтов.")));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id >= ID_LOCK_BASE && item.id < ID_LOCK_BASE + 500) {
            final ArrayList<org.telegram.tgnet.TLRPC.Dialog> dialogs =
                    org.telegram.messenger.MessagesController.getInstance(currentAccount).getAllDialogs();
            final int index = item.id - ID_LOCK_BASE;
            if (index >= 0 && index < dialogs.size()) {
                final long dialogId = dialogs.get(index).id;
                org.telegram.messenger.EdgeConfig.setChatLocked(currentAccount, dialogId,
                        !org.telegram.messenger.EdgeConfig.isChatLocked(currentAccount, dialogId));
                refreshViews();
            }
            return;
        }
        if (item.id >= ID_HIDDEN_BASE && item.id < ID_HIDDEN_BASE + org.telegram.messenger.UserConfig.MAX_ACCOUNT_COUNT) {
            final int account = item.id - ID_HIDDEN_BASE;
            org.telegram.messenger.EdgeConfig.setAccountHidden(account, !org.telegram.messenger.EdgeConfig.isAccountHidden(account));
            refreshViews();
            return;
        }
        if (item.id == ID_PIN_SCRAMBLE) {
            org.telegram.messenger.EdgeConfig.toggle(org.telegram.messenger.EdgeConfig.PIN_SCRAMBLE, false);
            refreshViews();
            return;
        }
        if (item.id == ID_DURESS) {
            showDuressCodeDialog();
            return;
        }
        if (item.id == ID_DECOY) {
            showDecoyDialog();
            return;
        }
        if (item.id >= ID_OPEN_BASE && item.id < ID_OPEN_BASE + 20) {
            openSection(item.id - ID_OPEN_BASE);
            return;
        }
        switch (item.id) {
            case ID_KEEP_DELETED:
                EdgeConfig.toggle(EdgeConfig.KEEP_DELETED, true);
                break;
            case ID_KEEP_EDITS:
                EdgeConfig.toggle(EdgeConfig.KEEP_EDITS, true);
                break;
            case ID_GHOST_READ:
                EdgeConfig.toggle(EdgeConfig.GHOST_READ, false);
                break;
            case ID_GHOST_TYPING:
                EdgeConfig.toggle(EdgeConfig.GHOST_TYPING, false);
                break;
            case ID_GHOST_OFFLINE:
                EdgeConfig.toggle(EdgeConfig.GHOST_OFFLINE, false);
                break;
            case ID_GHOST_STORIES:
                EdgeConfig.toggle(EdgeConfig.GHOST_STORIES, false);
                break;
            case ID_GHOST_ON_ACTION:
                EdgeConfig.toggle(EdgeConfig.GHOST_ON_ACTION, true);
                break;
            case ID_SEND_SILENT:
                EdgeConfig.toggle(EdgeConfig.SEND_SILENT, false);
                break;
            case ID_LAST_SEEN:
                EdgeConfig.toggle(EdgeConfig.SAVE_LAST_SEEN, true);
                break;
            case ID_DIM:
                EdgeConfig.toggle(EdgeConfig.DIM_DELETED, true);
                break;
            case ID_SHOW_USER_ID:
                EdgeConfig.toggle(EdgeConfig.SHOW_USER_ID, true);
                break;
            case ID_LOCAL_PREMIUM:
                EdgeConfig.toggle(EdgeConfig.LOCAL_PREMIUM, false);
                break;
            case ID_RESTRICTED:
                EdgeConfig.toggle(EdgeConfig.ALLOW_RESTRICTED, true);
                break;
            case ID_NO_ADS:
                EdgeConfig.toggle(EdgeConfig.NO_ADS, true);
                break;
            case ID_HD:
                EdgeConfig.toggle(EdgeConfig.HD_PHOTOS, false);
                break;
            case ID_SECRET:
                EdgeConfig.toggle(EdgeConfig.SECRET_FREE, true);
                break;
            case ID_TTL:
                EdgeConfig.toggle(EdgeConfig.KEEP_TTL, true);
                break;
            case ID_HIDE_PHONE:
                EdgeConfig.toggle(EdgeConfig.HIDE_PHONE, false);
                break;
            case ID_HIDE_STORIES:
                EdgeConfig.toggle(EdgeConfig.HIDE_STORIES, false);
                break;
            case ID_HIDE_PREMIUM:
                EdgeConfig.toggle(EdgeConfig.HIDE_PREMIUM, false);
                break;
            case ID_TIME_SECONDS:
                EdgeConfig.toggle(EdgeConfig.TIME_SECONDS, false);
                break;
            case ID_NO_ROUNDING:
                EdgeConfig.toggle(EdgeConfig.NO_ROUNDING, false);
                break;
            case ID_STICKER_TIME:
                EdgeConfig.toggle(EdgeConfig.STICKER_NO_TIME, false);
                break;
            case ID_KB_SCROLL:
                EdgeConfig.toggle(EdgeConfig.HIDE_KEYBOARD_SCROLL, false);
                break;
            case ID_GLASS:
                EdgeConfig.toggle(EdgeConfig.GLASS_STROKE, true);
                break;
            case ID_GOOEY:
                EdgeConfig.toggle(EdgeConfig.GOOEY_AVATAR, true);
                break;
            case ID_BOTTOM_TABS: {
                final boolean nowHidden = EdgeConfig.toggle(EdgeConfig.HIDE_BOTTOM_TABS, false);
                if (nowHidden && getParentActivity() != null) {
                    org.telegram.ui.Components.BulletinFactory.of(this).createSimpleBulletin(R.raw.info,
                            t("To bring the tabs back: menu \u22EE on the chat list \u2192 Settings", "Вернуть вкладки: меню \u22EE в списке чатов \u2192 Настройки")).show();
                }
                break;
            }
            case ID_MINI_AVATAR:
                EdgeConfig.toggle(EdgeConfig.MINI_SENDER_AVATAR, false);
                break;
            case ID_MEDIA_PREVIEW:
                EdgeConfig.toggle(EdgeConfig.MEDIA_PREVIEW, true);
                break;
            case ID_TABLET: {
                int m = EdgeConfig.tabletMode();
                m = (m + 1) % 3;
                EdgeConfig.setTabletMode(m);
                android.widget.Toast.makeText(getParentActivity(), t("Restart the app to apply", "Перезапустите приложение, чтобы применить"), android.widget.Toast.LENGTH_SHORT).show();
                break;
            }
            case ID_SPEED_BOOST:
                EdgeConfig.cycleSpeedBoost();
                break;
            case ID_HIDDEN_SEARCH:
                presentFragment(new EdgeHiddenActivity());
                return;
            case ID_STICKER_SIZE:
                EdgeMarks.showStickerSizeDialog(this, this::refreshViews);
                return;
            case ID_MARK_TEXT:
                EdgeMarks.showMarkTextDialog(this, this::refreshViews);
                return;
            case ID_STT:
                EdgeMarks.showSttKeyDialog(this);
                return;
            case ID_API_KEYS:
                EdgeMarks.showApiKeyDialog(this, false);
                return;
            case ID_IOS_PANEL:
                EdgeConfig.toggle(EdgeConfig.IOS_PANEL, false);
                break;
            case ID_SNOW:
                EdgeConfig.toggle(EdgeConfig.SNOWFLAKES, false);
                break;
            case ID_CHECK_UPDATES: {
                final android.content.Context ctx = getParentActivity();
                EdgeUpdater.check(ctx, (info, upToDate) -> {
                    if (ctx == null) {
                        return;
                    }
                    if (info != null && !upToDate) {
                        EdgeUpdater.showDialog(getParentActivity(), info);
                    } else {
                        android.widget.Toast.makeText(ctx, t("You are on the latest version", "У вас последняя версия"),
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            case ID_LOCK_SEARCH: {
                showLockSearchDialog();
                return;
            }
            case ID_UPDATE_URL: {
                showUpdateUrlDialog();
                return;
            }
            case ID_BACKUP_EXPORT: {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(android.content.Intent.EXTRA_TITLE, "edgegram-settings.json");
                startActivityForResult(intent, REQ_BACKUP_EXPORT);
                return;
            }
            case ID_BACKUP_IMPORT: {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                startActivityForResult(intent, REQ_BACKUP_IMPORT);
                return;
            }
            case ID_WAKE_LOCK: {
                org.telegram.messenger.EdgeConfig.toggle(org.telegram.messenger.EdgeConfig.WAKE_LOCK, false);
                EdgeKeepAliveService.start(getParentActivity());
                break;
            }
            case ID_KEEP_ALIVE: {
                final boolean on = EdgeConfig.toggle(EdgeConfig.KEEP_ALIVE, false);
                if (on) {
                    EdgeKeepAliveService.start(getParentActivity());
                } else {
                    EdgeKeepAliveService.stop(getParentActivity());
                }
                break;
            }
            case ID_NOTIF_PERM:
                openNotificationSettings();
                return;
            case ID_BATTERY:
                requestIgnoreBattery();
                return;
            case ID_APP_SETTINGS:
                openAppDetails();
                return;
            case ID_NOTIF_CHANNELS:
                openNotificationSettings();
                return;
            case ID_APPEARANCE:
                presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
                return;
            case ID_CLEAR:
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
                b.setTitle(t("Clear saved history?", "Очистить сохранённую историю?"));
                b.setMessage(t("Marks of deleted messages and the edit history will be removed from this device.", "Метки удалённых сообщений и история правок будут удалены с этого устройства."));
                b.setPositiveButton(t("Clear", "Очистить"), (d, w) -> {
                    EdgeStore.getInstance(currentAccount).clearAll();
                    refreshViews();
                });
                b.setNegativeButton(org.telegram.messenger.LocaleController.getString(R.string.Cancel), null);
                showDialog(b.create());
                return;
            default:
                return;
        }
        refreshViews();
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    /** Preset colors plus a wheel that opens the full HSV picker. */
    private static class PaletteView extends View {
        private final Runnable onChanged;
        private final Runnable onPick;
        private static final int[] COLORS = {
                EdgeConfig.COLOR_RED, 0xFFFF7043, 0xFFFFB300, 0xFF43A047,
                EdgeConfig.COLOR_BLUE, 0xFF7E57C2, EdgeConfig.COLOR_PINK
        };
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);

        PaletteView(Context context, Runnable onChanged, Runnable onPick) {
            super(context);
            this.onChanged = onChanged;
            this.onPick = onPick;
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(AndroidUtilities.dp(3));
        }

        private int count() {
            return COLORS.length + 1;
        }

        private float cell() {
            return (getWidth() - AndroidUtilities.dp(32)) / (float) count();
        }

        private float cx(int i) {
            return AndroidUtilities.dp(16) + cell() * i + cell() / 2f;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            final int w = getWidth();
            final int h = getHeight();
            final float cell = cell();
            final float r = Math.min(cell / 2f - AndroidUtilities.dp(3), AndroidUtilities.dp(21));
            final int selected = EdgeConfig.getMarkColor();
            boolean selectedIsPreset = false;
            for (int i = 0; i < COLORS.length; i++) {
                float x = cx(i);
                paint.setColor(COLORS[i]);
                canvas.drawCircle(x, h / 2f, r, paint);
                if (COLORS[i] == selected) {
                    selectedIsPreset = true;
                    ring.setColor(0xFFFFFFFF);
                    canvas.drawCircle(x, h / 2f, r + AndroidUtilities.dp(4), ring);
                }
            }
            // the wheel: any color
            final float x = cx(COLORS.length);
            int[] sweep = new int[]{
                    Color.HSVToColor(new float[]{0, 1, 1}),
                    Color.HSVToColor(new float[]{90, 1, 1}),
                    Color.HSVToColor(new float[]{180, 1, 1}),
                    Color.HSVToColor(new float[]{270, 1, 1}),
                    Color.HSVToColor(new float[]{360, 1, 1})
            };
            paint.setShader(new android.graphics.SweepGradient(x, h / 2f, sweep, null));
            canvas.drawCircle(x, h / 2f, r, paint);
            paint.setShader(null);
            paint.setColor(0xCC000000);
            canvas.drawCircle(x, h / 2f, r * 0.52f, paint);
            paint.setColor(0xFFFFFFFF);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(AndroidUtilities.dp(2.2f));
            canvas.drawLine(x - r * 0.26f, h / 2f, x + r * 0.26f, h / 2f, paint);
            canvas.drawLine(x, h / 2f - r * 0.26f, x, h / 2f + r * 0.26f, paint);
            paint.setStyle(Paint.Style.FILL);
            if (!selectedIsPreset) {
                ring.setColor(0xFFFFFFFF);
                canvas.drawCircle(x, h / 2f, r + AndroidUtilities.dp(4), ring);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                int i = (int) ((e.getX() - AndroidUtilities.dp(16)) / cell());
                if (i >= 0 && i < COLORS.length) {
                    EdgeConfig.setMarkColor(COLORS[i]);
                    invalidate();
                    if (onChanged != null) {
                        onChanged.run();
                    }
                } else if (i == COLORS.length && onPick != null) {
                    onPick.run();
                }
            }
            return true;
        }
    }
}
