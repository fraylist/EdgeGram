package org.telegram.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.EdgeConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.EdgePreviewView;

import java.util.ArrayList;

/** Hand-written FAQ for everything EdgeGram adds on top of Telegram. */
public class EdgeFaqActivity extends UniversalFragment {

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_Q = 1;

    /** {type, english, russian} for sections; {type, enQ, ruQ, enA, ruA} for questions. */
    private static final Object[][] DATA = new Object[][]{
            {TYPE_SECTION, "Start here", "С чего начать"},
            {TYPE_Q,
                    "Where are all EdgeGram settings?",
                    "Где найти все настройки EdgeGram?",
                    "Open Telegram settings (bottom right tab or the menu) and tap the red EdgeGram row with the \u00ABDeleted messages, ghost mode and more\u00BB subtitle. You will get a page with six cards: Ghost mode, Secret chats and timers, Spy mode, Customization, Look and privacy, Tools. Each card opens its own page, so nothing is buried deep.",
                    "Откройте настройки Telegram (вкладка справа внизу или меню) и нажмите красную строку EdgeGram с подписью \u00ABУдалённые сообщения, режим призрака и другое\u00BB. Откроется страница с шестью карточками: Режим призрака, Секретные чаты и таймеры, Режим шпиона, Кастомизация, Вид и приватность, Инструменты. Каждая карточка открывает свою страницу, поэтому ничего не спрятано глубоко."},
            {TYPE_Q,
                    "What exactly is different from Telegram?",
                    "Чем именно EdgeGram отличается от Telegram?",
                    "This is Telegram for Android 13.2.2 rebuilt from the official open source, with my own additions: saved deleted messages with a colored mark, edit history, ghost mode, secret chats without limits, timed media that stay, spy mode with last-seen memory, customization (time with seconds, sticker size, mark color, and more), voice transcription and the API key menu. Everything else \u2014 chats, calls, stories, payments \u2014 works like normal Telegram.",
                    "Это Telegram для Android 13.2.2, пересобранный из официального открытого кода, с моими дополнениями: сохранение удалённых сообщений с цветной меткой, история правок, режим призрака, секретные чаты без ограничений, одноразовые медиа, которые остаются, режим шпиона с памятью последнего онлайна, кастомизация (время с секундами, размер стикеров, цвет метки и другое), расшифровка голосовых и меню API-ключей. Всё остальное \u2014 чаты, звонки, истории, платежи \u2014 работает как в обычном Telegram."},
            {TYPE_Q,
                    "Do I lose my chats if I install it over Telegram?",
                    "Пропадут ли мои чаты, если поставить поверх Telegram?",
                    "No. EdgeGram is a separate app with its own package name (org.telegram.messenger.edgegram), so it does not replace Telegram. You sign in again in EdgeGram and the message history comes from the server. Updates of EdgeGram install over the old EdgeGram, and your local settings and saved history stay.",
                    "Нет. EdgeGram \u2014 отдельное приложение со своим пакетом (org.telegram.messenger.edgegram), обычный Telegram оно не заменяет. В EdgeGram вы входите заново, и история сообщений подтягивается с сервера. Обновления EdgeGram ставятся поверх старой версии, локальные настройки и сохранённая история при этом остаются."},

            {TYPE_SECTION, "Deleted messages and edits", "Удалённые сообщения и правки"},
            {TYPE_Q,
                    "How does saving of deleted messages work?",
                    "Как работает сохранение удалённых сообщений?",
                    "When someone deletes a message, Telegram tells your app. EdgeGram remembers that message id on your phone and does not remove the message from the chat. Instead the message stays with the mark you chose (a trash can and the word \u00ABdeleted\u00BB by default) next to the time. The text, photo or file itself is never removed from your device while the option is on.",
                    "Когда кто-то удаляет сообщение, Telegram сообщает об этом приложению. EdgeGram запоминает номер сообщения на вашем телефоне и не убирает его из чата. Сообщение остаётся с выбранной вами меткой (по умолчанию мусорное ведро и слово \u00ABудалено\u00BB) рядом со временем. Текст, фото или файл при включённой настройке с телефона не удаляются."},
            {TYPE_Q,
                    "Why do some messages stay without the mark?",
                    "Почему у некоторых сообщений нет метки?",
                    "EdgeGram can only keep a message that already reached your phone. If a message was deleted before you opened the chat, or while the app was closed and the message never downloaded, there is nothing to keep. Also the mark is added to messages that were deleted from that moment on; messages deleted before you installed EdgeGram have no record.",
                    "EdgeGram может сохранить только то сообщение, которое уже дошло до вашего телефона. Если сообщение удалили раньше, чем вы открыли чат, или пока приложение было закрыто и сообщение не успело загрузиться, сохранять нечего. Кроме того, метка ставится сообщениям, удалённым начиная с момента установки: о том, что удалили до EdgeGram, записей нет."},
            {TYPE_Q,
                    "What does the pencil mark and \u00ABEdit history\u00BB mean?",
                    "Что значит значок карандаша и \u00ABИстория правок\u00BB?",
                    "A pencil (\u270E) next to the time means the message was edited. Long-press such a message and choose \u00ABEdit history\u00BB: you will see every version with a date, the old text in red with a strikethrough and the new text in green. Each edit is stored separately, so you can follow the whole chain of changes.",
                    "Карандаш (\u270E) рядом со временем значит, что сообщение изменяли. Долго нажмите на такое сообщение и выберите \u00ABИстория правок\u00BB: вы увидите все версии с датой, старый текст красным и зачёркнутым, новый \u2014 зелёным. Каждая правка хранится отдельно, поэтому видна вся цепочка изменений."},
            {TYPE_Q,
                    "Why is the edit history empty?",
                    "Почему история правок пустая?",
                    "The history starts from the moment you installed EdgeGram and turned the option on. Edits made earlier were never recorded. Also a message can only be compared when its previous version was already on your phone; if the edit arrived together with the first copy of the message, there is nothing to compare.",
                    "История начинается с момента установки EdgeGram и включения настройки. Правки, сделанные раньше, не записывались. Кроме того, сравнить можно только тогда, когда старая версия уже была на телефоне: если правка пришла вместе с первым вариантом сообщения, сравнивать нечего."},
            {TYPE_Q,
                    "Where is all this stored, and is it private?",
                    "Где всё это хранится и насколько это приватно?",
                    "In a local database on your phone, one per account, inside the app's private files, one file per account. Nothing is uploaded anywhere and no one else can see it. If you uninstall EdgeGram or clear its data, the saved deleted messages and edit history are gone.",
                    "В локальной базе на телефоне, своя для каждого аккаунта, в приватных файлах приложения, отдельный файл на каждый аккаунт. Ничего никуда не отправляется, и никто другой этого не видит. Если удалить EdgeGram или очистить его данные, сохранённые удалённые сообщения и история правок исчезнут."},
            {TYPE_Q,
                    "How do I clear everything that was saved?",
                    "Как очистить всё сохранённое?",
                    "Settings \u2192 EdgeGram \u2192 Spy mode \u2192 \u00ABClear saved history\u00BB. That removes the \u00ABdeleted\u00BB marks and the edit history from this phone, while the chats themselves stay as they are. The same page shows how much is stored: for example \u00AB12 deleted, 4 edits\u00BB.",
                    "Настройки \u2192 EdgeGram \u2192 Режим шпиона \u2192 \u00ABОчистить сохранённую историю\u00BB. Метки \u00ABудалено\u00BB и история правок удалятся с этого телефона, сами чаты останутся как есть. На той же странице видно, сколько записано, например \u00AB12 удалено, 4 правок\u00BB."},

            {TYPE_SECTION, "The \u00ABdeleted\u00BB mark", "Метка \u00ABудалено\u00BB"},
            {TYPE_Q,
                    "How do I change the mark text and color?",
                    "Как изменить текст и цвет метки?",
                    "Settings \u2192 EdgeGram \u2192 Customization. \u00ABMark\u00BB opens a list of ready variants (a trash can with the word, a bare trash can, a cross, a ghost, a skull and so on) plus \u00ABYour own text or emoji\u00BB, where you can type anything up to 24 characters, for example a single bin emoji. The row of colored circles under it sets the color: red, orange, yellow, green, blue, violet, pink or grey.",
                    "Настройки \u2192 EdgeGram \u2192 Кастомизация. Пункт \u00ABМетка удалёнок\u00BB открывает список готовых вариантов (мусорное ведро со словом, просто ведро, крестик, призрак, череп и другие) и \u00ABСвой текст или смайлик\u00BB, где можно ввести что угодно до 24 символов, например одно ведро. Ряд цветных кружков под ним задаёт цвет: красный, оранжевый, жёлтый, зелёный, синий, фиолетовый, розовый или серый."},
            {TYPE_Q,
                    "The preview did not change \u2014 what now?",
                    "Превью не изменилось \u2014 что делать?",
                    "The preview at the top of the Customization page redraws as soon as you tap a switch, a color or save a new mark text. If it still looks old, pull the page back and open it again. Messages already in chats update right away too: the mark is drawn from the current settings, not stored with the message.",
                    "Превью вверху страницы Кастомизация перерисовывается сразу после нажатия на переключатель, цвет или сохранения текста метки. Если оно всё же выглядит старым, вернитесь назад и откройте страницу снова. Сообщения в чатах тоже обновляются сразу: метка рисуется по текущим настройкам, а не хранится вместе с сообщением."},
            {TYPE_Q,
                    "What are semi-transparent deleted messages?",
                    "Что такое полупрозрачные удалёнки?",
                    "With this option on, messages that were deleted by someone look faded, about 60% opacity, so they stand out less and you can tell them apart at a glance. Turn it off in Customization if you prefer them to look exactly like normal messages.",
                    "Если включить, сообщения, удалённые собеседником, выглядят бледнее \u2014 примерно на 60% непрозрачности. Так они меньше бросаются в глаза и сразу понятно, что это удалёнка. Выключите в Кастомизации, если хотите, чтобы они выглядели как обычные."},

            {TYPE_SECTION, "Ghost mode", "Режим призрака"},
            {TYPE_Q,
                    "What does \u00ABDon't mark as read\u00BB do?",
                    "Что делает \u00ABНе отмечать прочитанным\u00BB?",
                    "Your app simply does not send the read receipt to the server. The person on the other side keeps seeing one grey tick instead of two blue ones and does not see that you read the message. Locally the chat looks read for you, so nothing changes in how you use it.",
                    "Приложение просто не отправляет отметку о прочтении на сервер. Собеседник видит одну серую галочку вместо двух синих и не знает, что вы прочитали. Для вас чат выглядит прочитанным, пользоваться им вы будете как обычно."},
            {TYPE_Q,
                    "What is \u00ABRead on action\u00BB?",
                    "Что такое \u00ABЧитать при действиях\u00BB?",
                    "A compromise between normal and ghost mode. The read receipt is blocked, but as soon as you send a message in a chat, that chat is marked as read automatically \u2014 which is what normally happens in Telegram anyway. So you stay unseen only while you are reading and silent, and your chat partner does not wonder why you replied but never \u00ABread\u00BB their messages.",
                    "Компромисс между обычным режимом и призраком. Отметка о прочтении блокируется, но как только вы отправляете сообщение в чат, этот чат автоматически отмечается прочитанным \u2014 в обычном Telegram так и происходит. То есть вас не видно, пока вы только читаете и молчите, и собеседник не удивляется, что вы ответили, но его сообщения якобы не прочитаны."},
            {TYPE_Q,
                    "Always offline, hide typing, silent stories \u2014 what are they?",
                    "Всегда не в сети, скрывать набор, тихие истории \u2014 что это?",
                    "\u00ABAlways offline\u00BB stops your online status from being sent, so contacts see \u00ABlast seen recently\u00BB instead of \u00ABonline\u00BB. \u00ABHide typing\u00BB stops the \u00ABtyping\u2026\u00BB indicator. \u00ABSilent stories\u00BB watches stories without leaving a view mark. All three work only from your side: they change what the app sends, not what the server knows.",
                    "\u00ABВсегда не в сети\u00BB не отправляет ваш статус \u00ABв сети\u00BB, поэтому контакты видят \u00ABбыл(а) недавно\u00BB вместо \u00ABв сети\u00BB. \u00ABСкрывать набор текста\u00BB убирает индикатор \u00ABпечатает\u2026\u00BB. \u00ABТихие истории\u00BB открывают истории без отметки о просмотре. Все три работают только с вашей стороны: меняется то, что отправляет приложение, а не то, что знает сервер."},
            {TYPE_Q,
                    "What is \u00ABSend without sound\u00BB?",
                    "Что такое \u00ABОтправлять без звука\u00BB?",
                    "Every message you send becomes a silent message: the receiver gets it without a notification sound, just like when you hold a chat and choose \u00ABSend without sound\u00BB. Useful at night or in a busy group. It applies to new messages; messages already sent are not affected.",
                    "Каждое ваше сообщение уходит без звука: получатель видит его без звука уведомления, как если бы вы вручную выбрали \u00ABОтправить без звука\u00BB. Удобно ночью или в шумной группе. Действует на новые сообщения, уже отправленные не меняются."},
            {TYPE_Q,
                    "Can I get banned for ghost mode?",
                    "Могут ли забанить за режим призрака?",
                    "The features work by not sending certain requests, which is what any client can do. There is a theoretical risk: Telegram may treat it as unusual behaviour. Nothing is hacked, the account is not modified and the app never pretends to be another device. If you want to be safe, use it sparingly.",
                    "Функции работают за счёт того, что приложение не отправляет часть запросов \u2014 так может вести себя любой клиент. Теоретический риск есть: Telegram может счесть это необычным поведением. Ничего не взламывается, аккаунт не меняется, приложение не выдаёт себя за другое устройство. Если хотите быть осторожнее, пользуйтесь умеренно."},

            {TYPE_SECTION, "Secret chats and timed media", "Секретные чаты и одноразовые медиа"},
            {TYPE_Q,
                    "What does \u00ABSecret chats without limits\u00BB do?",
                    "Что делает \u00ABСекретные чаты без ограничений\u00BB?",
                    "In a secret chat Telegram normally blocks screenshots, forwarding and saving, and sends a notice when you take a screenshot. With this option EdgeGram stops blocking: screenshots and screen recording work, messages can be forwarded and media saved, and the screenshot notice is not sent anymore.",
                    "В секретном чате Telegram обычно запрещает скриншоты, пересылку и сохранение, а при скриншоте отправляет уведомление. С этой настройкой EdgeGram перестаёт блокировать: скриншоты и запись экрана работают, сообщения можно пересылать, медиа сохранять, а уведомление о скриншоте больше не отправляется."},
            {TYPE_Q,
                    "Timed photos and videos are not deleted \u2014 how?",
                    "Одноразовые фото и видео не удаляются \u2014 как это работает?",
                    "A photo or video with a timer normally disappears after you close it, and the server also deletes it. With \u00ABKeep timed photos and videos\u00BB on, EdgeGram does not wipe the local copy and does not send the deletion, so the media stays in the chat and can be opened again. This includes view-once photos, videos and voice messages.",
                    "Фото или видео с таймером обычно исчезает после закрытия, а сервер его удаляет. С включённой настройкой \u00ABНе удалять одноразовые фото и видео\u00BB EdgeGram не стирает копию на телефоне и не отправляет удаление, поэтому медиа остаётся в чате и его можно открыть снова. Это касается фото \u00ABпросмотр один раз\u00BB, видео и голосовых с таймером."},
            {TYPE_Q,
                    "What is the blue \u00ABMark as read\u00BB button?",
                    "Что за синяя кнопка \u00ABПрочитано\u00BB?",
                    "When you open timed media with the option on, a blue button appears at the bottom of the viewer. Nothing is sent to the other side until you press it. Press it when you want the sender to see that the media was watched; then the app sends the usual read receipt. If you close the viewer without pressing, the sender never learns that you opened it.",
                    "Когда вы открываете медиа с таймером при включённой настройке, внизу просмотрщика появляется синяя кнопка. Пока вы её не нажмёте, ничего собеседнику не отправляется. Нажмите, когда хотите, чтобы отправитель узнал о просмотре: тогда уйдёт обычная отметка о прочтении. Если закрыть просмотрщик без нажатия, отправитель не узнает, что вы открыли медиа."},
            {TYPE_Q,
                    "What does the other side see?",
                    "Что видит собеседник?",
                    "With the option on, the media never disappears for them either \u2014 it stays in their chat as a normal watched message after you press the button, or stays unopened from their point of view if you do not. The timer countdown may close the viewer on your side, but the file remains available in the chat.",
                    "При включённой настройке медиа не исчезает и у них: после нажатия кнопки оно остаётся в чате как просмотренное, а без нажатия \u2014 как неоткрытое с их стороны. Таймер может закрыть просмотрщик у вас, но файл остаётся доступным в чате."},

            {TYPE_SECTION, "Spy mode", "Режим шпиона"},
            {TYPE_Q,
                    "Where can I search everything that was hidden from me?",
                    "Где искать всё, что от меня скрыли?",
                    "Settings \u2192 EdgeGram \u2192 Spy mode \u2192 \u00ABWhat was hidden from me\u00BB. There is a search field on top and a switch Everything / Deleted / Edited. Results show the chat, the time of the message and its text, and an edit also shows what the text was before. Tap a result and the chat opens right at that message, where the deleted message is still visible with its mark.",
                    "Настройки \u2192 EdgeGram \u2192 Режим шпиона \u2192 \u00ABСкрытое от меня\u00BB. Сверху поле поиска и переключатель Всё / Удалённые / Правки. В результатах видно чат, время сообщения и текст, а для правки \u2014 ещё и каким текст был раньше. Нажмите на результат \u2014 откроется чат на этом сообщении, и удалённое сообщение будет видно с меткой."},
            {TYPE_Q,
                    "What is \u00ABKeep last seen\u00BB?",
                    "Что такое \u00ABСохранять последний онлайн\u00BB?",
                    "When you see a person online, EdgeGram remembers that exact time locally. If their status is hidden, the profile then shows \u00ABapprox. yesterday 21:14\u00BB instead of the vague \u00ABlast seen recently\u00BB. It only uses what your own app has seen; it does not break anyone's privacy settings and works only for people you actually caught online at least once.",
                    "Когда вы видите человека в сети, EdgeGram запоминает это точное время у себя. Если его статус скрыт, профиль покажет \u00ABпримерно вчера 21:14\u00BB вместо неопределённого \u00ABбыл(а) недавно\u00BB. Используется только то, что видело ваше приложение: настройки приватности не обходятся, и работает это лишь для тех, кого вы хотя бы раз застали в сети."},
            {TYPE_Q,
                    "What is the \u00ABStorage\u00BB row in Spy mode?",
                    "Что показывает строка \u00ABХранилище\u00BB в режиме шпиона?",
                    "It tells how many deleted messages and how many edits are saved on this phone, for example \u00AB24 deleted, 7 edits\u00BB. It is only a counter; the next row removes them all. Removing the history never removes the chats themselves, it only clears the marks and the edit versions.",
                    "Она показывает, сколько удалённых сообщений и правок сохранено на этом телефоне, например \u00AB24 удалено, 7 правок\u00BB. Это только счётчик, следующая строка удаляет всё. Очистка не трогает сами чаты, она убирает лишь метки и версии правок."},

            {TYPE_SECTION, "Customization", "Кастомизация"},
            {TYPE_Q,
                    "Time with seconds and no number rounding",
                    "Время с секундами и без сокращения чисел",
                    "\u00ABTime with seconds\u00BB shows message times as 12:34:56 instead of 12:34, handy for screenshots and for exact moments. \u00ABNo number rounding\u00BB shows 4777 instead of 4.8K in counters, view counts and similar places. Both apply immediately everywhere in chats.",
                    "\u00ABВремя с секундами\u00BB показывает время сообщений как 12:34:56 вместо 12:34 \u2014 удобно для скриншотов и точных моментов. \u00ABБез сокращения чисел\u00BB показывает 4777 вместо 4.8K в счётчиках, просмотрах и похожих местах. Оба пункта применяются сразу во всех чатах."},
            {TYPE_Q,
                    "Tablet mode: why does it need a restart?",
                    "Планшетный режим: почему нужен перезапуск?",
                    "The app decides once, at start, whether it runs as a phone or tablet layout, and builds the whole interface around that decision. Tapping the row cycles Auto \u2192 On \u2192 Off; after the change close the app from the recent apps list and open it again. \u00ABOn\u00BB gives the two-column Telegram layout even on a phone.",
                    "Приложение решает один раз при запуске, работать как телефон или как планшет, и строит вокруг этого весь интерфейс. Нажатие на строку переключает Авто \u2192 Вкл \u2192 Выкл; после смены закройте приложение в списке недавних и откройте снова. \u00ABВкл\u00BB включает двухколоночный интерфейс Telegram даже на телефоне."},
            {TYPE_Q,
                    "Stickers: size and hidden time",
                    "Стикеры: размер и спрятанное время",
                    "\u00ABSticker size\u00BB opens a slider from 7 to 24 dp and sets how big stickers are drawn in chats; the default 14 dp matches Telegram. \u00ABHide time on stickers\u00BB removes the small clock in the corner, so the sticker looks clean unless you select it.",
                    "\u00ABРазмер стикеров\u00BB открывает ползунок от 7 до 24 dp и задаёт, насколько крупно рисовать стикеры в чатах; значение по умолчанию 14 dp совпадает с Telegram. \u00ABСкрывать время на стикерах\u00BB убирает маленькие часы в углу, стикер выглядит чисто, пока вы его не выделите."},
            {TYPE_Q,
                    "Mini avatars and media preview in the chat list",
                    "Мини-аватарки и превью медиа в списке чатов",
                    "\u00ABMini sender avatars\u00BB puts a tiny round photo of the sender before their name in groups, so you see who wrote without opening the chat. \u00ABMedia preview in chat list\u00BB controls the small thumbnails of the last photo or video on the right side of the row; turn it off for a plainer list. Both have a live preview on the \u00ABLook and privacy\u00BB page.",
                    "\u00ABМини-аватарки отправителей\u00BB ставят крошечное круглое фото отправителя перед именем в группах \u2014 видно, кто написал, не открывая чат. \u00ABПревью медиа в списке чатов\u00BB управляет миниатюрами последнего фото или видео справа в строке; выключите для более простого списка. У обоих есть живое превью на странице \u00ABВид и приватность\u00BB."},
            {TYPE_Q,
                    "Layout: tabs, keyboard, gooey avatar, glass outlines",
                    "Раскладка: вкладки, клавиатура, липкий аватар, контуры",
                    "\u00ABHide bottom tabs\u00BB removes the tab bar at the bottom of the chat list and gives a little more screen. \u00ABHide keyboard on scroll\u00BB closes the keyboard as soon as you start scrolling a chat. \u00ABSticky avatar animation\u00BB is the dissolve of the profile photo into the header; turn it off for a plain fade. \u00ABGlass outlines\u00BB toggles the thin light rim around glass panels.",
                    "\u00ABСкрыть нижние вкладки\u00BB убирает панель вкладок внизу списка чатов и даёт чуть больше экрана. \u00ABСкрывать клавиатуру при прокрутке\u00BB закрывает клавиатуру, как только вы начинаете листать чат. \u00ABЛипкая анимация аватара\u00BB \u2014 это растворение фото профиля в шапке; выключите для обычного затухания. \u00ABКонтуры стеклянных элементов\u00BB включает тонкую светлую обводку стеклянных панелей."},
            {TYPE_Q,
                    "Local Premium \u2014 what actually works?",
                    "Локальный Premium \u2014 что реально работает?",
                    "It makes the app treat you as a Premium user on this phone only: Premium badges, stickers, themes and some interface elements unlock. Anything the server checks \u2014 sending files over 2 GB, extra reactions, Premium emoji in messages, stars \u2014 still needs a real subscription. Your account is not modified and nobody else sees it.",
                    "Он заставляет приложение считать вас Premium только на этом телефоне: открываются значки, стикеры, темы Premium и часть элементов интерфейса. Всё, что проверяет сервер \u2014 отправка файлов больше 2 ГБ, дополнительные реакции, Premium-эмодзи в сообщениях, звёзды \u2014 по-прежнему требует настоящей подписки. Аккаунт не меняется, и никто другой этого не видит."},
            {TYPE_Q,
                    "Why are previews in settings needed?",
                    "Зачем превью в настройках?",
                    "So you do not have to guess. The chat preview at the top of Customization shows at once how a normal message, an edited message with the pencil and a deleted message with your mark look. The chat list preview in \u00ABLook and privacy\u00BB shows how your list looks with the current options. Both redraw the moment you change something.",
                    "Чтобы не гадать. Превью чата вверху Кастомизации сразу показывает, как выглядят обычное сообщение, изменённое с карандашом и удалённое с вашей меткой. Превью списка чатов на странице \u00ABВид и приватность\u00BB показывает, как выглядит ваш список с текущими настройками. Оба перерисовываются в момент изменения."},

            {TYPE_SECTION, "Privacy", "Приватность"},
            {TYPE_Q,
                    "Hide my phone number \u2014 where does it help?",
                    "Скрывать мой номер телефона \u2014 где это помогает?",
                    "It replaces your number with dots in the settings header and on your own profile screen, which is exactly where the number is easiest to catch on a screenshot or when someone looks over your shoulder. It is a visual mask, not a privacy setting: other people still see what your Telegram privacy settings allow.",
                    "Он заменяет номер точками в шапке настроек и на экране вашего профиля \u2014 именно там номер проще всего поймать на скриншот или увидеть через плечо. Это визуальная маска, а не настройка приватности: другие люди видят то, что разрешено вашими настройками приватности Telegram."},
            {TYPE_Q,
                    "Hide stories and Premium offers",
                    "Скрыть истории и предложения Premium",
                    "Two quiet switches for a cleaner app. \u00ABHide stories\u00BB removes the stories row from the chat list, \u00ABHide Premium offers\u00BB removes Premium buttons and promos. Both need a restart of the app to fully disappear, because the chat list is built at start.",
                    "Два тихих переключателя для более чистого приложения. \u00ABСкрывать истории\u00BB убирает ленту историй из списка чатов, \u00ABСкрывать предложения Premium\u00BB убирает кнопки и рекламу Premium. Чтобы они полностью исчезли, приложение нужно перезапустить: список чатов строится при запуске."},

            {TYPE_SECTION, "Tools and keys", "Инструменты и ключи"},
            {TYPE_Q,
                    "How do I set up voice transcription?",
                    "Как настроить расшифровку голосовых?",
                    "Settings \u2192 EdgeGram \u2192 Tools \u2192 \u00ABVoice transcription\u00BB. Create a free API key at console.groq.com in the API Keys section, paste it into the field and save. Then long-press any voice message and choose \u00ABTranscribe\u00BB: the text appears in a window where you can copy it.",
                    "Настройки \u2192 EdgeGram \u2192 Инструменты \u2192 \u00ABРасшифровка голосовых\u00BB. Создайте бесплатный ключ на console.groq.com в разделе API Keys, вставьте его в поле и сохраните. После этого долго нажмите на любое голосовое и выберите \u00ABРасшифровать\u00BB: текст откроется в окне, откуда его можно скопировать."},
            {TYPE_Q,
                    "Where does the voice message go when I transcribe it?",
                    "Куда уходит голосовое при расшифровке?",
                    "To the service whose key you entered \u2014 by default Groq. The audio file is uploaded there, recognised and the text comes back; nothing else from the chat is sent. If that is not acceptable for you, do not turn the feature on. You can also point the address and model fields to any other OpenAI-compatible service.",
                    "В сервис, ключ которого вы ввели \u2014 по умолчанию Groq. Аудиофайл загружается туда, распознаётся, текст возвращается; больше ничего из чата не отправляется. Если это для вас неприемлемо, не включайте функцию. Адрес и модель можно поменять на любой другой сервис, совместимый с OpenAI."},
            {TYPE_Q,
                    "Is transcription free?",
                    "Расшифровка бесплатная?",
                    "Groq has a free tier with limits on how much audio you can send per day; for normal use it is enough. I did not check their current limits and terms, so look them up on their site. If the limit runs out, the answer is simply an error message and the audio stays unread.",
                    "У Groq есть бесплатный тариф с ограничением на объём аудио в сутки; для обычного использования его хватает. Их актуальные лимиты и условия я не проверял \u2014 смотрите на их сайте. Когда лимит кончится, вы просто увидите сообщение об ошибке, а аудио останется нерасшифрованным."},
            {TYPE_Q,
                    "What are the API keys and why do I need them?",
                    "Что такое API-ключи и зачем они нужны?",
                    "api_id and api_hash tell Telegram which application you are. My build already contains your own keys, so normally you never need this page. It exists for cases when Telegram limits the key: then you create a new application at my.telegram.org, open \u00ABAPI development tools\u00BB and paste both values here. The app restarts after saving.",
                    "api_id и api_hash сообщают Telegram, какое приложение вы используете. В моей сборке уже вшиты ваши ключи, так что обычно эта страница не нужна. Она на случай, если Telegram ограничит ключ: тогда создайте новое приложение на my.telegram.org, откройте \u00ABAPI development tools\u00BB и вставьте оба значения сюда. После сохранения приложение перезапустится."},
            {TYPE_Q,
                    "I get API_ID_PUBLISHED_FLOOD on login. What is that?",
                    "При входе ошибка API_ID_PUBLISHED_FLOOD. Что это?",
                    "Telegram rejected the application key because it is used by too many builds at once. The fix is exactly the previous answer: get your own api_id and api_hash and enter them, or tell me and I will build a version with your keys inside. The same window opens automatically when this error comes back during login.",
                    "Telegram отклонил ключ приложения, потому что им пользуется слишком много сборок сразу. Решение \u2014 ровно как в прошлом ответе: получить свои api_id и api_hash и ввести их, либо сказать мне, и я соберу версию с вашими ключами внутри. Это же окно открывается само, если ошибка приходит при входе."},

            {TYPE_SECTION, "Accounts and installation", "Аккаунты и установка"},
            {TYPE_Q,
                    "How many accounts fit?",
                    "Сколько аккаунтов помещается?",
                    "Up to ten, against three or five in normal Telegram. They switch from the profile menu, and every account has its own saved history and its own settings of this app. The limit only matters if you really need that many; nothing else changes.",
                    "До десяти, вместо трёх или пяти в обычном Telegram. Переключение \u2014 через меню профиля, у каждого аккаунта своя сохранённая история и свои настройки этого приложения. Ограничение важно, только если вам правда нужно столько; больше ничего не меняется."},
            {TYPE_Q,
                    "Why does the APK not install?",
                    "Почему APK не устанавливается?",
                    "Three usual reasons. First, the build is for arm64 phones only, so a very old 32-bit device will refuse. Second, it must be installed over the same signature: uninstall the previous EdgeGram only as a last resort, because that deletes the local history. Third, some phones ask to allow installing from unknown sources \u2014 allow it for the file manager you use.",
                    "Три обычные причины. Первая: сборка только под arm64, поэтому очень старый 32-битный телефон откажется. Вторая: ставиться нужно поверх той же подписи, удалять предыдущий EdgeGram стоит только в крайнем случае \u2014 вместе с ним исчезнет локальная история. Третья: некоторые телефоны просят разрешить установку из неизвестных источников \u2014 разрешите для файлового менеджера, которым открываете APK."},

            {TYPE_SECTION, "Problems", "Проблемы"},
            {TYPE_Q,
                    "The app closed by itself. What should I send?",
                    "Приложение закрылось само. Что прислать?",
                    "Open the app again, repeat the action that crashed it and immediately export the logs: in the app press the version line in settings twice to reach the debug menu, or find the logs folder in the app data and zip it. Send me that zip \u2014 inside there is a line starting with FATAL and the exact class and line that failed, and I can fix it precisely instead of guessing.",
                    "Откройте приложение снова, повторите действие, на котором оно закрылось, и сразу выгрузите логи: в приложении дважды нажмите на строку с версией в настройках, чтобы попасть в меню отладки, либо найдите папку логов в данных приложения и запакуйте её. Пришлите мне этот архив \u2014 внутри будет строка, начинающаяся с FATAL, с точным классом и строкой, где произошла ошибка, и я исправлю точно, а не наугад."},
            {TYPE_Q,
                    "A setting does not seem to do anything",
                    "Настройка как будто ничего не делает",
                    "Check the order of the options: several of them depend on each other. Ghost mode does nothing readable while \u00ABRead on action\u00BB overrides it on send. \u00ABKeep timed photos\u00BB only helps for media opened after it was switched on. Layout options such as tablet mode, hidden stories and hidden tabs need a restart. And some things simply cannot work, like server-side Premium features.",
                    "Проверьте, как пункты связаны между собой. Режим призрака почти не заметен, пока \u00ABЧитать при действиях\u00BB перекрывает его при отправке. \u00ABНе удалять одноразовые фото\u00BB помогает только для медиа, открытых после включения. Раскладка \u2014 планшетный режим, скрытые истории и вкладки \u2014 требует перезапуска. И часть вещей просто не может работать, например серверные функции Premium."},

            {TYPE_SECTION, "Not implemented", "Чего пока нет"},
            {TYPE_Q,
                    "What is missing compared to Nekogram, AyuGram and Telegraher?",
                    "Чего нет по сравнению с Nekogram, AyuGram и Telegraher?",
                    "Honestly: folders customization (hide \u00ABAll chats\u00BB, folder styles, tab position), custom emoji sets and fonts, own icons for the whole app, saving attachments into a separate folder with a size limit, export and import of the database, PIN screen with scrambled keys and a duress code, session manager, message translation, double-tap actions and message details. Some of these I plan to add next; ask me which one matters most and I will do that first.",
                    "Честно: настройка папок (скрыть \u00ABВсе чаты\u00BB, стили папок, позиция вкладок), свои наборы эмодзи и шрифты, свои иконки во всём приложении, сохранение вложений в отдельную папку с лимитом размера, экспорт и импорт базы, PIN-экран с перемешиванием клавиш и кодом под принуждением, менеджер сессий, перевод сообщений, действия по двойному тапу и детали сообщения. Часть этого я планирую добавить; скажите, что важнее, и начну с этого."},
    };

    private static final int ID_TOC_BASE = 100000;
    private final ArrayList<Integer> tocDataIndexes = new ArrayList<>();
    private final ArrayList<Integer> tocPositions = new ArrayList<>();

    private EditText searchField;
    private FrameLayout searchWrap;
    private ImageView clearButton;
    private String query = "";

    private boolean matches(Object[] row) {
        if (query.isEmpty()) {
            return true;
        }
        final String q = query.toLowerCase();
        for (int i = 1; i < row.length; i++) {
            Object o = row[i];
            if (o instanceof String && ((String) o).toLowerCase().contains(q)) {
                return true;
            }
        }
        return false;
    }

    private void buildSearchBar(Context context) {
        FrameLayout wrap = new FrameLayout(context);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(22));
        wrap.setBackground(bg);

        searchField = new EditText(context);
        searchField.setSingleLine(true);
        searchField.setBackground(null);
        searchField.setHint(EdgeConfig.tr("Search questions", "Поиск по вопросам"));
        searchField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        searchField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        searchField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        searchField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        searchField.setPadding(AndroidUtilities.dp(14), 0, AndroidUtilities.dp(38), 0);
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchField.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        searchField.setGravity(Gravity.CENTER_VERTICAL);
        wrap.addView(searchField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        clearButton = new ImageView(context);
        clearButton.setImageResource(R.drawable.msg_clear_input);
        clearButton.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        clearButton.setVisibility(View.GONE);
        clearButton.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        clearButton.setOnClickListener(v -> {
            searchField.setText("");
            AndroidUtilities.hideKeyboard(searchField);
        });
        wrap.addView(clearButton, LayoutHelper.createFrame(38, 38, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 4, 0));

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                clearButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                if (listView != null && listView.adapter != null) {
                    listView.adapter.update(false);
                }
            }
        });
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                AndroidUtilities.hideKeyboard(searchField);
                return true;
            }
            return false;
        });

        searchWrap = wrap;
        contentView.addView(wrap, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.TOP | Gravity.FILL_HORIZONTAL, 12, ActionBar.getCurrentActionBarHeight(), 12, 0));
        positionSearchBar();
    }

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        fragmentView = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        return fragmentView;
    }

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);
        if (searchWrap == null) {
            buildSearchBar(view.getContext());
        }
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        super.onInsets(left, top, right, bottom);
        positionSearchBar();
    }

    private void positionSearchBar() {
        if (searchWrap == null || listView == null) {
            return;
        }
        final int barTop = mSystemInsets.top + ActionBar.getCurrentActionBarHeight() + AndroidUtilities.dp(6);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) searchWrap.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(44));
            lp.gravity = Gravity.TOP;
        }
        lp.topMargin = barTop;
        lp.leftMargin = AndroidUtilities.dp(12);
        lp.rightMargin = AndroidUtilities.dp(12);
        searchWrap.setLayoutParams(lp);
        listView.setClipToPadding(false);
        listView.setPadding(0, barTop + AndroidUtilities.dp(50), 0, listView.getPaddingBottom());
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
        return EdgeConfig.tr("FAQ and help", "FAQ и помощь");
    }

    private static String pick(Object[] row, int indexEn, int indexRu) {
        return EdgeConfig.tr((String) row[indexEn], (String) row[indexRu]);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (query.isEmpty()) {
            items.add(UItem.asHeader(EdgeConfig.tr("How to read this page", "Как читать эту страницу")));
            items.add(UItem.asShadow(EdgeConfig.tr(
                    "Every feature EdgeGram adds to Telegram has an answer here. Tap a question \u2014 the answer opens in a window and can be selected and copied. Use the search field above when you are looking for a specific setting.",
                    "Здесь есть ответ про каждую функцию, которую EdgeGram добавляет к Telegram. Нажмите на вопрос \u2014 ответ откроется в окне, его можно выделить и скопировать. Если ищете конкретную настройку, воспользуйтесь поиском сверху.")));
        }
        if (query.isEmpty()) {
            items.add(UItem.asHeader(EdgeConfig.tr("Contents", "Оглавление")));
            for (int i = 0; i < DATA.length; i++) {
                if ((Integer) DATA[i][0] == TYPE_SECTION) {
                    items.add(UItem.asButton(ID_TOC_BASE + i, "\u2192  " + pick(DATA[i], 1, 2)));
                }
            }
            items.add(UItem.asShadow(EdgeConfig.tr("Tap a section to jump to it.", "Нажмите на раздел, чтобы перейти к нему.")));
        }
        tocDataIndexes.clear();
        tocPositions.clear();
        int found = 0;
        boolean sectionHasItems = false;
        boolean pendingSection = false;
        int pendingSectionIndex = -1;
        for (int i = 0; i < DATA.length; i++) {
            Object[] row = DATA[i];
            int type = (Integer) row[0];
            if (type == TYPE_SECTION) {
                pendingSection = true;
                pendingSectionIndex = i;
                sectionHasItems = false;
            } else if (matches(row)) {
                if (pendingSection) {
                    tocDataIndexes.add(pendingSectionIndex);
                    tocPositions.add(items.size());
                    items.add(UItem.asHeader(pick(DATA[pendingSectionIndex], 1, 2)));
                    pendingSection = false;
                }
                items.add(UItem.asButton(i, "\u2022 " + pick(row, 1, 2)));
                found++;
                sectionHasItems = true;
            }
        }
        if (found == 0) {
            items.add(UItem.asHeader(EdgeConfig.tr("Nothing found", "Ничего не найдено")));
            items.add(UItem.asShadow(EdgeConfig.tr(
                    "Try another word, for example \u00ABdeleted\u00BB, \u00ABghost\u00BB, \u00ABsticker\u00BB or \u00ABkeys\u00BB. If the answer is really missing, send me your question and I will add it.",
                    "Попробуйте другое слово, например \u00ABудал\u00BB, \u00ABпризрак\u00BB, \u00ABстикер\u00BB или \u00ABключ\u00BB. Если ответа действительно нет, пришлите вопрос, и я его добавлю.")));
        }
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id >= ID_TOC_BASE && item.id < ID_TOC_BASE + 10000) {
            jumpToSection(item.id - ID_TOC_BASE);
            return;
        }
        if (item.id < 0 || item.id >= DATA.length) {
            return;
        }
        Object[] row = DATA[item.id];
        if ((Integer) row[0] != TYPE_Q) {
            return;
        }
        showAnswer(row);
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private void jumpToSection(int dataIndex) {
        for (int i = 0; i < tocDataIndexes.size(); i++) {
            if (tocDataIndexes.get(i) == dataIndex) {
                final int pos = tocPositions.get(i);
                if (listView != null && listView.layoutManager instanceof androidx.recyclerview.widget.LinearLayoutManager) {
                    ((androidx.recyclerview.widget.LinearLayoutManager) listView.layoutManager)
                            .scrollToPositionWithOffset(pos, AndroidUtilities.dp(70));
                }
                return;
            }
        }
    }

    /** Returns the preview mode for the answers where a picture really helps: 0 none, 1 chat, 2 chat list. */
    private static int illustrationFor(Object[] row) {
        final String en = (String) row[1];
        if (en.contains("Mini avatars") || en.contains("Where are all EdgeGram settings")) {
            return EdgePreviewView.MODE_LIST;
        }
        if (en.contains("saving of deleted messages")
                || en.contains("pencil mark")
                || en.contains("semi-transparent deleted")
                || en.contains("change the mark text")
                || en.contains("Time with seconds")
                || en.contains("What are previews in settings")) {
            return EdgePreviewView.MODE_CHAT;
        }
        return -1;
    }

    private void showAnswer(Object[] row) {
        final String question = pick(row, 1, 2);
        final String answer = pick(row, 3, 4);
        if (getParentActivity() == null) {
            return;
        }
        final LinearLayout box = new LinearLayout(getParentActivity());
        box.setOrientation(LinearLayout.VERTICAL);

        final int illustration = illustrationFor(row);
        if (illustration >= 0) {
            EdgePreviewView preview = new EdgePreviewView(getParentActivity(), illustration);
            preview.invalidate();
            box.addView(preview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(illustration == EdgePreviewView.MODE_LIST ? 90 : 186)));
            TextView caption = new TextView(getParentActivity());
            caption.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            caption.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            caption.setText(EdgeConfig.tr("Illustration of how this looks in the app", "Иллюстрация: как это выглядит в приложении"));
            caption.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(2), AndroidUtilities.dp(22), AndroidUtilities.dp(6));
            box.addView(caption);
        }

        TextView text = new TextView(getParentActivity());
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        text.setLineSpacing(AndroidUtilities.dp(3), 1f);
        text.setTextIsSelectable(true);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.setText(answer);
        text.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(8), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
        ScrollView scroll = new ScrollView(getParentActivity());
        scroll.addView(text);
        box.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(illustration >= 0 ? 320 : 380)));
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(question);
        builder.setView(box);
        builder.setPositiveButton(LocaleController.getString(R.string.Close), null);
        showDialog(builder.create());
    }
}