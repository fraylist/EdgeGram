package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MashaConfig;
import org.telegram.messenger.MashaStore;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.MashaPreviewView;
import org.telegram.ui.Components.MashaStickerPreviewView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

/** EdgeGram settings: a main page with sections and one sub-page for each section. */
public class MashaSettingsActivity extends UniversalFragment {
    public static final int PAGE_MAIN = 0;
    public static final int PAGE_GHOST = 1;
    public static final int PAGE_SPY = 2;
    public static final int PAGE_CUSTOM = 3;
    public static final int PAGE_SECRET = 4;
    public static final int PAGE_LOOK = 5;
    public static final int PAGE_TOOLS = 6;

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

    private final int page;
    private PaletteView palette;
    private MashaPreviewView chatPreview;
    private MashaPreviewView listPreview;
    private MashaStickerPreviewView stickerPreview;

    public MashaSettingsActivity() {
        this(PAGE_MAIN);
    }

    public MashaSettingsActivity(int page) {
        this.page = page;
    }

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        palette = new PaletteView(context, this::refreshViews);
        chatPreview = new MashaPreviewView(context, MashaPreviewView.MODE_CHAT);
        listPreview = new MashaPreviewView(context, MashaPreviewView.MODE_LIST);
        stickerPreview = new MashaStickerPreviewView(context);
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
            default:
                return "EdgeGram";
        }
    }

    private static String t(String en, String ru) {
        return MashaConfig.tr(en, ru);
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
                t("Others do not see that you read their messages", "Собеседники не видят, что вы прочитали сообщение"), MashaConfig.ghostRead()));
        items.add(check(ID_GHOST_ON_ACTION, t("Read on action", "Читать при действиях"),
                t("When you send a message, the chat is marked as read automatically", "При отправке сообщения чат автоматически отмечается прочитанным"), MashaConfig.ghostOnAction()));
        items.add(UItem.asShadow(null));
        items.add(check(ID_GHOST_TYPING, t("Hide typing", "Скрывать набор текста"),
                t("The \"typing\" status is not sent", "Статус «печатает» не отправляется"), MashaConfig.ghostTyping()));
        items.add(check(ID_GHOST_OFFLINE, t("Always offline", "Всегда не в сети"),
                t("Your online status is never sent", "Статус «в сети» не отправляется"), MashaConfig.ghostOffline()));
        items.add(check(ID_GHOST_STORIES, t("Silent stories", "Тихие истории"),
                t("Stories are watched without a view mark", "Истории просматриваются без отметки о просмотре"), MashaConfig.ghostStories()));
        items.add(UItem.asShadow(null));
        items.add(check(ID_SEND_SILENT, t("Send without sound", "Отправлять без звука"),
                t("All your messages are sent silently by default", "Все ваши сообщения по умолчанию уходят без звука"), MashaConfig.sendSilent()));
        items.add(UItem.asShadow(t("Without \"Read on action\" you stay unseen even after replying.", "Без «Читать при действиях» вы остаётесь непрочитавшим даже после ответа.")));
    }

    private void fillSpy(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Spy mode", "Режим шпиона")));
        items.add(check(ID_KEEP_DELETED, t("Keep deleted messages", "Сохранять удалённые сообщения"),
                t("They stay in the chat with a mark", "Остаются в чате с пометкой"), MashaConfig.keepDeleted()));
        items.add(check(ID_KEEP_EDITS, t("Keep edit history", "Сохранять историю правок"),
                t("Long-press an edited message \u2192 \"Edit history\"", "Долгое нажатие на изменённое сообщение \u2192 «История правок»"), MashaConfig.keepEdits()));
        items.add(UItem.asShadow(null));
        items.add(check(ID_LAST_SEEN, t("Keep last seen", "Сохранять последний онлайн"),
                t("Remembers the last exact time you saw a person online and shows it as approximate when their status is hidden", "Запоминает последнее точное время, когда вы видели человека в сети, и показывает его как примерное, если статус скрыт"), MashaConfig.get(MashaConfig.SAVE_LAST_SEEN, true)));
        items.add(UItem.asShadow(null));
        items.add(UItem.asButton(ID_STATS, t("Storage", "Хранилище"), statsText()));
        items.add(UItem.asButton(ID_CLEAR, t("Clear saved history", "Очистить сохранённую историю")));
        items.add(UItem.asShadow(t("Only messages already loaded on this device can be kept. Clearing removes the \"deleted\" marks and edit history.", "Сохраняются только сообщения, уже загруженные на это устройство. Очистка убирает метки «удалено» и историю правок.")));
    }

    private String statsText() {
        try {
            int[] st = MashaStore.getInstance(currentAccount).counts();
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

    private void fillCustom(ArrayList<UItem> items) {
        if (chatPreview == null) {
            chatPreview = new MashaPreviewView(getContext(), MashaPreviewView.MODE_CHAT);
        }
        chatPreview.invalidate();
        items.add(UItem.asHeader(t("Preview", "Превью")));
        items.add(UItem.asCustomShadow(chatPreview, 186));
        items.add(UItem.asHeader(t("Deleted messages", "Удалённые сообщения")));
        items.add(check(ID_DIM, t("Semi-transparent deleted", "Полупрозрачные удалёнки"),
                t("Deleted messages are dimmed", "Удалённые сообщения затемняются"), MashaConfig.dimDeleted()));
        items.add(UItem.asButton(ID_MARK_TEXT, t("Mark", "Метка удалёнок"), MashaConfig.getMarkText()));
        if (palette == null) {
            palette = new PaletteView(getContext(), this::refreshViews);
        }
        items.add(UItem.asCustom(palette, 70));
        items.add(UItem.asShadow(t("Pick a color for the mark. You can set any text or an emoji, e.g. a trash can.", "Выберите цвет метки. Текст можно задать любой, в том числе смайлик, например мусорное ведро.")));

        items.add(UItem.asHeader(t("Interface", "Интерфейс")));
        items.add(check(ID_TIME_SECONDS, t("Time with seconds", "Время с секундами"),
                t("Message time is shown as 12:34:56", "Время сообщений показывается как 12:34:56"), MashaConfig.timeWithSeconds()));
        items.add(check(ID_NO_ROUNDING, t("No number rounding", "Без сокращения чисел"),
                t("4.8K becomes 4777", "4.8K превращается в 4777"), MashaConfig.noRounding()));
        items.add(cycle(ID_TABLET, t("Tablet mode", "Планшетный режим"), tabletLabel()));
        items.add(check(ID_STICKER_TIME, t("Hide time on stickers", "Скрывать время на стикерах"),
                t("No clock in the corner of stickers", "Без часов в углу стикеров"), MashaConfig.stickerNoTime()));
        items.add(check(ID_SHOW_USER_ID, t("Show user ID in profile", "Показывать ID пользователя в профиле"),
                t("Adds an item with the numeric ID to the profile menu, tap it to copy", "Добавляет в меню профиля пункт с числовым ID, нажатие копирует его"), MashaConfig.showUserId()));
        items.add(check(ID_MINI_AVATAR, t("Mini sender avatars", "Мини-аватарки отправителей"),
                t("Small avatar next to the sender name in the chat list", "Маленький аватар рядом с именем отправителя в списке чатов"), MashaConfig.miniSenderAvatar()));
        items.add(check(ID_MEDIA_PREVIEW, t("Media preview in chat list", "Превью медиа в списке чатов"),
                t("Show thumbnails of the last photo or video", "Показывать миниатюры последнего фото или видео"), MashaConfig.mediaPreview()));
        if (stickerPreview == null) {
            stickerPreview = new MashaStickerPreviewView(getContext());
        }
        stickerPreview.invalidate();
        items.add(UItem.asCustomShadow(stickerPreview, 210));
        items.add(UItem.asButton(ID_STICKER_SIZE, t("Sticker size", "Размер стикеров"), MashaConfig.stickerSize() + " dp"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("Layout", "Раскладка")));
        items.add(check(ID_BOTTOM_TABS, t("Hide bottom tabs", "Скрыть нижние вкладки"),
                t("The tab bar at the bottom of the chat list disappears", "Нижняя панель вкладок в списке чатов пропадает"), MashaConfig.hideBottomTabs()));
        items.add(check(ID_KB_SCROLL, t("Hide keyboard on scroll", "Скрывать клавиатуру при прокрутке"),
                t("The keyboard closes when you scroll the chat", "Клавиатура закрывается при прокрутке чата"), MashaConfig.hideKeyboardOnScroll()));
        items.add(check(ID_GOOEY, t("\"Sticky\" avatar animation", "«Липкая» анимация аватара"),
                t("Profile avatar melts into the header when you scroll", "Аватар профиля «растворяется» в шапке при прокрутке"), MashaConfig.gooeyAvatar()));
        items.add(check(ID_GLASS, t("Glass outlines", "Контуры стеклянных элементов"),
                t("Thin light outline around glass panels", "Тонкая светлая обводка у стеклянных панелей"), MashaConfig.glassStroke()));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(t("Useful", "Полезное")));
        items.add(check(ID_NO_ADS, t("Hide ads", "Отключить рекламу"),
                t("Sponsored messages are not shown", "Спонсорские сообщения не показываются"), MashaConfig.noAds()));
        items.add(check(ID_RESTRICTED, t("Allow saving and copying", "Разрешить сохранение и копирование"),
                t("Works in chats with restricted content", "Работает и в чатах с запретом копирования"), MashaConfig.allowRestricted()));
        items.add(check(ID_LOCAL_PREMIUM, t("Local Premium", "Локальный Premium"),
                t("Unlocks Premium interface locally. Server features still need a real subscription", "Открывает Premium-интерфейс локально. Серверные функции требуют настоящей подписки"), MashaConfig.localPremium()));
        items.add(UItem.asShadow(null));
    }

    private void openSection(int index) {
        if (index == 8) {
            presentFragment(new MashaFaqActivity());
        } else {
            presentFragment(new MashaSettingsActivity(index));
        }
    }

    private static UItem cycle(int id, String title, String value) {
        return UItem.asButton(id, title, value);
    }

    private String tabletLabel() {
        switch (MashaConfig.tabletMode()) {
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
                t("Screenshots, forwarding and saving are allowed; screenshot notices are not sent", "Скриншоты, пересылка и сохранение разрешены, уведомления о скриншотах не отправляются"), MashaConfig.secretFree()));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader(t("Timed media", "Медиа с таймером")));
        items.add(check(ID_TTL, t("Keep timed photos and videos", "Не удалять одноразовые фото и видео"),
                t("They stay on your device. A \"Mark as read\" button appears in the viewer", "Остаются у вас. В просмотрщике появляется кнопка «Прочитано»"), MashaConfig.keepTtl()));
        items.add(UItem.asShadow(t("Nothing is marked as read until you press the button, so the sender does not see that you opened it.", "Пока вы не нажмёте кнопку, отправитель не узнает, что вы открыли медиа.")));
    }

    private void fillLook(ArrayList<UItem> items) {
        if (listPreview == null) {
            listPreview = new MashaPreviewView(getContext(), MashaPreviewView.MODE_LIST);
        }
        listPreview.invalidate();
        items.add(UItem.asHeader(t("Chat list preview", "Превью списка чатов")));
        items.add(UItem.asCustomShadow(listPreview, 80));
        items.add(UItem.asShadow(t("This is how your chat list looks with the current settings.", "Так выглядит ваш список чатов с текущими настройками.")));
        items.add(UItem.asHeader(t("Privacy", "Приватность")));
        items.add(check(ID_HIDE_PHONE, t("Hide my phone number", "Скрывать мой номер телефона"),
                t("Masks the number in settings and profile", "Скрывает номер в настройках и профиле"), MashaConfig.hidePhone()));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader(t("Interface", "Интерфейс")));
        items.add(check(ID_HIDE_STORIES, t("Hide stories", "Скрывать истории"),
                t("Removes the stories row. Takes effect after restart", "Убирает ленту историй. Вступит в силу после перезапуска"), MashaConfig.hideStories()));
        items.add(check(ID_HIDE_PREMIUM, t("Hide Premium offers", "Скрывать предложения Premium"),
                t("Removes Premium buttons and promos. Takes effect after restart", "Убирает кнопки и рекламу Premium. Вступит в силу после перезапуска"), MashaConfig.hidePremium()));
        items.add(UItem.asButton(ID_APPEARANCE, t("Telegram themes and corners", "Темы и скругления Telegram")));
        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader(t("Media", "Медиа")));
        items.add(check(ID_HD, t("Larger photos", "Фото в большем размере"),
                t("Photos are sent in up to 2560 px. Takes effect after restart", "Фото отправляются до 2560 px. Вступит в силу после перезапуска"), MashaConfig.hdPhotos()));
        items.add(UItem.asShadow(null));
    }

    private void fillTools(ArrayList<UItem> items) {
        items.add(UItem.asHeader(t("Tools", "Инструменты")));
        items.add(UItem.asButton(ID_STT, t("Voice transcription", "Расшифровка голосовых"), MashaConfig.getSttKey().isEmpty() ? t("Not set up", "Не настроена") : t("On", "Включена")));
        items.add(UItem.asButton(ID_API_KEYS, t("API keys", "API-ключи"), MashaConfig.hasCustomApiKeys() ? t("Custom", "Свои") : t("Built-in", "Встроенные")));
        items.add(UItem.asShadow(t("Up to 10 accounts can be added in the same app.", "В приложение можно добавить до 10 аккаунтов.")));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id >= ID_OPEN_BASE && item.id < ID_OPEN_BASE + 20) {
            openSection(item.id - ID_OPEN_BASE);
            return;
        }
        switch (item.id) {
            case ID_KEEP_DELETED:
                MashaConfig.toggle(MashaConfig.KEEP_DELETED, true);
                break;
            case ID_KEEP_EDITS:
                MashaConfig.toggle(MashaConfig.KEEP_EDITS, true);
                break;
            case ID_GHOST_READ:
                MashaConfig.toggle(MashaConfig.GHOST_READ, false);
                break;
            case ID_GHOST_TYPING:
                MashaConfig.toggle(MashaConfig.GHOST_TYPING, false);
                break;
            case ID_GHOST_OFFLINE:
                MashaConfig.toggle(MashaConfig.GHOST_OFFLINE, false);
                break;
            case ID_GHOST_STORIES:
                MashaConfig.toggle(MashaConfig.GHOST_STORIES, false);
                break;
            case ID_GHOST_ON_ACTION:
                MashaConfig.toggle(MashaConfig.GHOST_ON_ACTION, true);
                break;
            case ID_SEND_SILENT:
                MashaConfig.toggle(MashaConfig.SEND_SILENT, false);
                break;
            case ID_LAST_SEEN:
                MashaConfig.toggle(MashaConfig.SAVE_LAST_SEEN, true);
                break;
            case ID_DIM:
                MashaConfig.toggle(MashaConfig.DIM_DELETED, true);
                break;
            case ID_SHOW_USER_ID:
                MashaConfig.toggle(MashaConfig.SHOW_USER_ID, true);
                break;
            case ID_LOCAL_PREMIUM:
                MashaConfig.toggle(MashaConfig.LOCAL_PREMIUM, false);
                break;
            case ID_RESTRICTED:
                MashaConfig.toggle(MashaConfig.ALLOW_RESTRICTED, true);
                break;
            case ID_NO_ADS:
                MashaConfig.toggle(MashaConfig.NO_ADS, true);
                break;
            case ID_HD:
                MashaConfig.toggle(MashaConfig.HD_PHOTOS, false);
                break;
            case ID_SECRET:
                MashaConfig.toggle(MashaConfig.SECRET_FREE, true);
                break;
            case ID_TTL:
                MashaConfig.toggle(MashaConfig.KEEP_TTL, true);
                break;
            case ID_HIDE_PHONE:
                MashaConfig.toggle(MashaConfig.HIDE_PHONE, false);
                break;
            case ID_HIDE_STORIES:
                MashaConfig.toggle(MashaConfig.HIDE_STORIES, false);
                break;
            case ID_HIDE_PREMIUM:
                MashaConfig.toggle(MashaConfig.HIDE_PREMIUM, false);
                break;
            case ID_TIME_SECONDS:
                MashaConfig.toggle(MashaConfig.TIME_SECONDS, false);
                break;
            case ID_NO_ROUNDING:
                MashaConfig.toggle(MashaConfig.NO_ROUNDING, false);
                break;
            case ID_STICKER_TIME:
                MashaConfig.toggle(MashaConfig.STICKER_NO_TIME, false);
                break;
            case ID_KB_SCROLL:
                MashaConfig.toggle(MashaConfig.HIDE_KEYBOARD_SCROLL, false);
                break;
            case ID_GLASS:
                MashaConfig.toggle(MashaConfig.GLASS_STROKE, true);
                break;
            case ID_GOOEY:
                MashaConfig.toggle(MashaConfig.GOOEY_AVATAR, true);
                break;
            case ID_BOTTOM_TABS: {
                final boolean nowHidden = MashaConfig.toggle(MashaConfig.HIDE_BOTTOM_TABS, false);
                if (nowHidden && getParentActivity() != null) {
                    org.telegram.ui.Components.BulletinFactory.of(this).createSimpleBulletin(R.raw.info,
                            t("To bring the tabs back: menu \u22EE on the chat list \u2192 Settings", "Вернуть вкладки: меню \u22EE в списке чатов \u2192 Настройки")).show();
                }
                break;
            }
            case ID_MINI_AVATAR:
                MashaConfig.toggle(MashaConfig.MINI_SENDER_AVATAR, false);
                break;
            case ID_MEDIA_PREVIEW:
                MashaConfig.toggle(MashaConfig.MEDIA_PREVIEW, true);
                break;
            case ID_TABLET: {
                int m = MashaConfig.tabletMode();
                m = (m + 1) % 3;
                MashaConfig.setTabletMode(m);
                android.widget.Toast.makeText(getParentActivity(), t("Restart the app to apply", "Перезапустите приложение, чтобы применить"), android.widget.Toast.LENGTH_SHORT).show();
                break;
            }
            case ID_STICKER_SIZE:
                MashaMarks.showStickerSizeDialog(this, this::refreshViews);
                return;
            case ID_MARK_TEXT:
                MashaMarks.showMarkTextDialog(this, this::refreshViews);
                return;
            case ID_STT:
                MashaMarks.showSttKeyDialog(this);
                return;
            case ID_API_KEYS:
                MashaMarks.showApiKeyDialog(this, false);
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
                    MashaStore.getInstance(currentAccount).clearAll();
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

    /** Row of color circles for the deleted mark. */
    private static class PaletteView extends View {
        private final Runnable onChanged;
        private static final int[] COLORS = {
                MashaConfig.COLOR_RED, 0xFFFF7043, 0xFFFFB300, 0xFF43A047,
                MashaConfig.COLOR_BLUE, 0xFF7E57C2, MashaConfig.COLOR_PINK, 0xFF8E9AA8
        };
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);

        PaletteView(Context context, Runnable onChanged) {
            super(context);
            this.onChanged = onChanged;
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(AndroidUtilities.dp(3));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            int count = COLORS.length;
            float cell = (w - AndroidUtilities.dp(32)) / (float) count;
            float r = Math.min(cell / 2f - AndroidUtilities.dp(4), AndroidUtilities.dp(22));
            int selected = MashaConfig.getMarkColor();
            for (int i = 0; i < count; i++) {
                float cx = AndroidUtilities.dp(16) + cell * i + cell / 2f;
                paint.setColor(COLORS[i]);
                canvas.drawCircle(cx, h / 2f, r, paint);
                if (COLORS[i] == selected) {
                    ring.setColor(0xFFFFFFFF);
                    canvas.drawCircle(cx, h / 2f, r + AndroidUtilities.dp(4), ring);
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                int count = COLORS.length;
                float cell = (getWidth() - AndroidUtilities.dp(32)) / (float) count;
                int i = (int) ((e.getX() - AndroidUtilities.dp(16)) / cell);
                if (i >= 0 && i < count) {
                    MashaConfig.setMarkColor(COLORS[i]);
                    invalidate();
                    if (onChanged != null) {
                        onChanged.run();
                    }
                }
            }
            return true;
        }
    }
}
