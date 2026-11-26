# Выпуск релиза EdgeGram

Порядок действий для каждого релиза. Все шаги выполняются локально в `C:\Mashagram\MashaGram`.

## 1. Версия

В `gradle.properties` увеличить `APP_VERSION_CODE`. Итоговый versionCode в APK = `APP_VERSION_CODE * 10 + 9`.

## 2. Сборка

```
set JAVA_HOME=C:\Mashagram\tools\jdk17
set ANDROID_HOME=C:\Mashagram\tools\sdk

gradle :TMessagesProj_App:assembleAfatRelease -PedgeAbi=arm64-v8a
copy app.apk EdgeGram-<version>-r<NN>-arm64.apk

gradle :TMessagesProj_App:assembleAfatRelease -PedgeAbi=armeabi-v7a,arm64-v8a,x86,x86_64
copy app.apk EdgeGram-<version>-r<NN>-universal.apk
```

Путь к APK: `TMessagesProj_App/build/outputs/apk/afat/release/app.apk`.

## 3. Проверка

```
adb install -r EdgeGram-<version>-r<NN>-arm64.apk
adb shell dumpsys package org.telegram.messenger.edgegram | findstr versionCode
```

## 4. Манифест обновлений

Обновить `latest.json` в корне репозитория:

```json
{
  "versionCode": 73149,
  "versionName": "13.2.2 r14",
  "changelog": "Коротко, что нового",
  "force": false,
  "minVersion": <версии ниже этого значения обновляются принудительно>,
  "arm64": "https://github.com/fraylist/EdgeGram/releases/download/v<tag>/EdgeGram-<version>-r<NN>-arm64.apk",
  "universal": "https://github.com/fraylist/EdgeGram/releases/download/v<tag>/EdgeGram-<version>-r<NN>-universal.apk"
}
```

`force: true` — принудительно для всех, кто ниже `versionCode`.
`minVersion` — «аварийный рубильник» для старых сборок.

Приложение берёт манифест по умолчанию отсюда:
`https://raw.githubusercontent.com/fraylist/EdgeGram/main/latest.json`
Свой URL задаётся в EdgeGram → Инструменты → «Сервер обновлений».

## 5. Коммит и пуш

```
git add -A
git commit -m "Release r<NN>: ..."
git push https://fraylist:<token>@github.com/fraylist/EdgeGram.git HEAD:main
```

## 6. Релиз на GitHub

Через API: POST `/repos/fraylist/EdgeGram/releases` с `tag_name`, `name`, `body`,
затем загрузить оба APK в `https://uploads.github.com/repos/fraylist/EdgeGram/releases/<id>/assets?name=<file>`.

Тело релиза: подробно, с разделами «Что нового», «Исправления», «Файлы».

## 7. Проверка манифеста

```
curl -s https://raw.githubusercontent.com/fraylist/EdgeGram/main/latest.json
```

`versionCode` должен совпадать с новым APK, ссылки — открываться.
