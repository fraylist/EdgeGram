# Что изменено относительно Telegram

База: **Telegram for Android 13.2.2** (репозиторий `DrKLO/Telegram`, коммит `21f7d10`).

Этот файл нужен для обновлений: когда выйдет новая версия Telegram, по нему видно, что и где мы дописывали, чтобы перенести изменения на новую базу.

Всего: **20 новых файлов** и **57 изменённых** файлов апстрима.

## Новые файлы

| Файл | Что делает |
|---|---|
| `messenger/MashaConfig.java` | Все настройки EdgeGram в SharedPreferences, фильтр «призрачных» запросов, ключи API, метка удалёнок |
| `messenger/MashaStore.java` | Локальная база: сохранённые удалённые сообщения и история правок |
| `ui/MashaSettingsActivity.java` | Экран настроек EdgeGram: главная с разделами и подстраницы |
| `ui/MashaFaqActivity.java` | FAQ с поиском, оглавлением и превью-иллюстрациями |
| `ui/MashaMarks.java` | Метка «удалено», подсветка, диалоги истории правок, расшифровки, размера стикеров, ключей API |
| `ui/Components/MashaPreviewView.java` | Превью чата и списка чатов для настроек |
| `ui/Components/MashaStickerPreviewView.java` | Превью размера стикера |
| `res/drawable-nodpi/masha_sticker.png` | Картинка для превью стикера |
| `res/mipmap-*/mg_icon_*.png` | Передний и задний слой иконки с котиками |
| `.github/workflows/build.yml` | Сборка APK и публикация релиза |
| `HOW_TO_BUILD.md` | Инструкция по сборке |

## Изменённые файлы апстрима

**Настройки и ключи**
- `BuildVars.java` — вписаны `APP_ID` и `APP_HASH`.
- `ApplicationLoader.java` — `MashaConfig.applyApiKeys()` при старте.
- `gradle.properties`, `TMessagesProj_App/build.gradle` — подпись EdgeGram, суффикс пакета `.mashagram`.
- `TMessagesProj_App/google-services.json` — добавлен клиент для пакета `org.telegram.messenger.mashagram`.

**Удалённые сообщения и правки**
- `MessagesController.java` — не удалять сообщения при `updateDeleteMessages` (и при push-удалении), запись правок в `MashaStore`, `mashaReadDialogNow` и `mashaMarkTtlRead`.
- `MessagesStorage.java` — `mashaResolveDialogs`, `mashaGetMessage`, отказ от очистки таймер-медиа.
- `NotificationCenter.java` — событие `mashaMessagesDeleted`.
- `ChatActivity.java` — пропуск удаления из UI, пункт «История правок», «Расшифровать».
- `ChatMessageCell.java` — метка и цвет метки, затемнение удалёнок, скрытие времени на стикерах, размер стикеров.

**Призрак и приватность**
- `MashaConfig.shouldDropRequest` + `tgnet/ConnectionsManager.java` — блокировка read-запросов, «печатает», oффлайн-статуса, просмотров историй; тихая отправка.
- `SendMessagesHelper.java` — «читать при действиях», тихие сообщения, подавление уведомлений о скриншотах.
- `MediaController.java` — не отправлять «сделал скриншот» в секретных чатах.
- `UserConfig.java` — лимит аккаунтов до 10, локальный Premium.
- `jni/tgnet/Defines.h`, `jni/tgnet/ConnectionsManager.cpp` — `MAX_ACCOUNT_COUNT 10` и экземпляры 5–9.

**Секретные чаты и медиа**
- `MessageObject.java`, `TLRPC.java` — снятие запрета пересылки и сохранения при включённой настройке.
- `SecretMediaViewer.java` — кнопка «Прочитано», отключаемый `FLAG_SECURE`.
- `SecretVoicePlayer.java`, `PhotoViewer.java`, `ProfileActivity.java`, `ChatActivity.java` — тот же флаг.

**Интерфейс и кастомизация (часть идей сверена с Nekogram)**
- `LocaleController.java` — время с секундами, без сокращения чисел, память последнего онлайна.
- `AndroidUtilities.java` — планшетный режим, размеры фото.
- `DialogCell.java` — мини-аватарки отправителей, превью медиа.
- `MainTabsActivity.java` — скрытие нижней панели вкладок.
- `DialogsActivity.java` — пункт «Настройки» в меню, когда вкладки скрыты.
- `ThemeActivity.java` — скругление пузырей до 30.
- `GlassRimRenderer.java` — отключаемые контуры стекла.
- `SettingsActivity.java`, `UserInfoActivity.java` — строка EdgeGram, скрытие номера.
- `LoginActivity.java` — диалог своих API-ключей при `API_ID_PUBLISHED_FLOOD`.
- `res/values*/strings.xml` — имя приложения EdgeGram во всех локалях.
- `res/mipmap-anydpi-v26/ic_launcher*.xml`, `res/mipmap-*/ic_launcher*.png` — новая иконка.
- `TMessagesProj/build.gradle` — сборка только под arm64.

## Как обновить на новую версию Telegram

1. Добавить апстрим и получить новую версию:
   ```
   git remote add upstream https://github.com/DrKLO/Telegram.git
   git fetch upstream master --depth 1
   ```
2. Создать ветку от нового коммита апстрима и перенести новые файлы `Masha*` (они не конфликтуют, это отдельные классы).
3. Пройти по списку изменённых файлов выше и перенести правки. Смотреть, что именно менялось, удобно так:
   ```
   git diff 21f7d10 HEAD -- <путь к файлу>
   ```
4. Обновить `APP_VERSION_CODE` и `APP_VERSION_NAME` в `gradle.properties`.
5. Собрать, исправить ошибки компиляции (обычно это переименованные методы в новых версиях).
6. Поставить тег `v<версия>` — GitHub Actions соберёт APK и приложит к релизу.
