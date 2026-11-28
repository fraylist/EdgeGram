# Как проверить релиз EdgeGram

Каждый релиз подписан ключом EdgeGram. Проверка состоит из двух шагов: контрольная сумма файла
и подпись списка сумм.

## 1. Контрольная сумма

В релизе лежит `SHA256SUMS.txt` и отдельные файлы `*.apk.sha256`.

Linux / macOS:

```sh
sha256sum -c SHA256SUMS.txt
```

Windows (PowerShell):

```powershell
Get-FileHash .\EdgeGram-13.2.2-r15-arm64.apk -Algorithm SHA256
```

Сравни вывод со строкой в `SHA256SUMS.txt` и с полем `sha256` в `latest.json`:
приложение само сверяет его перед установкой и отказывается ставить файл при несовпадении.

## 2. Сертификат подписи APK

```sh
apksigner verify --print-certs EdgeGram-13.2.2-r15-arm64.apk
```

Ожидаемые значения (они же в `CERTIFICATES.txt` релиза):

```
CN=EdgeGram, O=EdgeGram, L=Moscow, C=RU
SHA-256: 5a85ecbaafba76b8954c4e188d9a12d8dbde5f90f266f9101a267680f65ff54b
SHA-1:   a1776d0ff08a1573f1e6a12ba6027bbc388006df
```

Если отпечаток другой — файл собран не нами, устанавливать нельзя.

## 3. Подпись контрольных сумм

`SHA256SUMS.txt` подписан ключом, публичная часть — в `docs/release-key.pub`.

```sh
minisign -Vm SHA256SUMS.txt -p release-key.pub
```

Ожидаемый ответ: `Signature and comment signature verified` и `Trusted comment: ...`.

## Что это даёт

- APK нельзя подменить: Android не установит обновление с другой подписью.
- Испорченный при скачивании файл отсекается сверкой SHA-256 (в приложении и вручную).
- Список сумм нельзя подменить без приватного ключа подписи.
