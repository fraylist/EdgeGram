package org.telegram.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.EdgeConfig;
import org.telegram.messenger.EdgeStore;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

/** Marks for deleted/edited messages and the edit history dialog. */
public class EdgeMarks {

    public static String decorate(int account, MessageObject message, String time) {
        if (message == null || message.messageOwner == null || time == null) {
            return time;
        }
        try {
            EdgeStore store = EdgeStore.getInstance(account);
            long dialogId = message.getDialogId();
            int id = message.getId();
            String prefix = "";
            if (store.isDeleted(dialogId, id)) {
                prefix = EdgeConfig.getMarkText() + "  ";
            }
            if (store.hasEdits(dialogId, id)) {
                prefix += "\u270E ";
            }
            return prefix.isEmpty() ? time : prefix + time;
        } catch (Throwable e) {
            return time;
        }
    }

    public static boolean isDeleted(int account, MessageObject message) {
        try {
            return message != null && EdgeStore.getInstance(account).isDeleted(message.getDialogId(), message.getId());
        } catch (Throwable e) {
            return false;
        }
    }

    /** Paints the "deleted" mark in the chosen color. */
    public static CharSequence colorize(int account, MessageObject message, CharSequence time) {
        if (message == null || time == null) {
            return time;
        }
        try {
            if (!EdgeStore.getInstance(account).isDeleted(message.getDialogId(), message.getId())) {
                return time;
            }
            String mark = EdgeConfig.getMarkText();
            String s = time.toString();
            int i = s.indexOf(mark);
            if (i < 0) {
                return time;
            }
            SpannableStringBuilder sb = new SpannableStringBuilder(time);
            sb.setSpan(new ForegroundColorSpan(EdgeConfig.getMarkColor()), i, i + mark.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        } catch (Throwable e) {
            return time;
        }
    }

    public static void showMarkTextDialog(BaseFragment fragment, Runnable onChanged) {
        final Context context = fragment.getParentActivity();
        if (context == null) {
            return;
        }
        final String[] presets = EdgeConfig.markPresets();
        final String[] items = new String[presets.length + 1];
        System.arraycopy(presets, 0, items, 0, presets.length);
        items[presets.length] = "\u270F " + EdgeConfig.tr("Your own text or emoji\u2026", "Свой текст или смайлик\u2026");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(EdgeConfig.tr("Mark for deleted messages", "Метка для удалённых сообщений"));
        builder.setItems(items, (dialog, which) -> {
            if (which < presets.length) {
                EdgeConfig.setMarkText(presets[which]);
                if (onChanged != null) {
                    onChanged.run();
                }
            } else {
                showCustomMarkDialog(fragment, onChanged);
            }
        });
        fragment.showDialog(builder.create());
    }

    private static void showCustomMarkDialog(BaseFragment fragment, Runnable onChanged) {
        final Context context = fragment.getParentActivity();
        if (context == null) {
            return;
        }
        final android.widget.EditText edit = new android.widget.EditText(context);
        edit.setSingleLine(true);
        edit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        edit.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        edit.setHintTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        edit.setHint("\uD83D\uDDD1 " + EdgeConfig.tr("deleted", "удалено"));
        edit.setText(EdgeConfig.getMarkText());
        edit.setSelection(edit.getText().length());
        LinearLayout box = new LinearLayout(context);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(8), AndroidUtilities.dp(22), 0);
        box.addView(edit, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(EdgeConfig.tr("Your mark", "Своя метка"));
        builder.setView(box);
        builder.setPositiveButton(LocaleController.getString(org.telegram.messenger.R.string.Save), (d, w) -> {
            String t = edit.getText().toString().trim();
            if (t.length() > 24) {
                t = t.substring(0, 24);
            }
            EdgeConfig.setMarkText(t);
            if (onChanged != null) {
                onChanged.run();
            }
        });
        builder.setNegativeButton(LocaleController.getString(org.telegram.messenger.R.string.Cancel), null);
        fragment.showDialog(builder.create());
    }

    /** Full HSV picker: any hue, saturation and brightness. */
    public static void showColorPicker(BaseFragment fragment, Runnable onChange, Runnable onDone) {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        final android.content.Context context = fragment.getParentActivity();
        final org.telegram.ui.Components.EdgeColorPickerView picker = new org.telegram.ui.Components.EdgeColorPickerView(context);
        picker.setColor(EdgeConfig.getMarkColor());
        picker.setOnColorChanged(color -> {
            EdgeConfig.setMarkColor(color);
            if (onChange != null) {
                onChange.run();
            }
        });
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(6), AndroidUtilities.dp(18), 0);
        box.addView(picker, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(300)));
        TextView hint = new TextView(context);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        hint.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        hint.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(8), AndroidUtilities.dp(4), 0);
        hint.setText(EdgeConfig.tr("Drag the square for shade and brightness, the bar for hue.",
                "Квадрат — оттенок и яркость, полоса — сам цвет."));
        box.addView(hint);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(EdgeConfig.tr("Mark color", "Цвет метки"));
        builder.setView(box);
        builder.setPositiveButton(LocaleController.getString(org.telegram.messenger.R.string.Done), null);
        builder.setNeutralButton(EdgeConfig.tr("Reset", "Сброс"), (d, w) -> {
            EdgeConfig.setMarkColor(EdgeConfig.COLOR_RED);
            if (onChange != null) {
                onChange.run();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> {
            if (onDone != null) {
                onDone.run();
            }
        });
        fragment.showDialog(dialog);
    }

    public static void showMarkColorDialog(BaseFragment fragment, Runnable onChanged) {
        final Context context = fragment.getParentActivity();
        if (context == null) {
            return;
        }
        final int[] colors = {EdgeConfig.COLOR_RED, EdgeConfig.COLOR_BLUE, EdgeConfig.COLOR_PINK};
        final String[] names = {
                "\uD83D\uDD34 " + EdgeConfig.tr("Red", "Красный"),
                "\uD83D\uDD35 " + EdgeConfig.tr("Blue", "Синий"),
                "\uD83C\uDF38 " + EdgeConfig.tr("Pink", "Розовый"),
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(EdgeConfig.tr("Mark color", "Цвет метки"));
        builder.setItems(names, (dialog, which) -> {
            EdgeConfig.setMarkColor(colors[which]);
            if (onChanged != null) {
                onChanged.run();
            }
        });
        fragment.showDialog(builder.create());
    }

    public static String colorName() {
        int c = EdgeConfig.getMarkColor();
        if (c == EdgeConfig.COLOR_BLUE) {
            return EdgeConfig.tr("Blue", "Синий");
        }
        if (c == EdgeConfig.COLOR_PINK) {
            return EdgeConfig.tr("Pink", "Розовый");
        }
        return EdgeConfig.tr("Red", "Красный");
    }

    /** Asks for the user's own api_id / api_hash from my.telegram.org. */
    public static void showApiKeyDialog(BaseFragment fragment, boolean fromError) {
        final Context context = fragment.getParentActivity();
        if (context == null) {
            return;
        }
        final int textColor = Theme.getColor(Theme.key_dialogTextBlack);
        final int gray = Theme.getColor(Theme.key_dialogTextGray2);
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), 0);

        TextView info = new TextView(context);
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        info.setTextColor(gray);
        info.setText(fromError
                ? EdgeConfig.tr("Telegram rejected the built-in app key (API_ID_PUBLISHED_FLOOD). Get your own at my.telegram.org \u2192 API development tools and enter it here.",
                "Telegram отклонил встроенный ключ приложения (API_ID_PUBLISHED_FLOOD). Получите свой на my.telegram.org \u2192 API development tools и введите здесь.")
                : EdgeConfig.tr("Your own keys from my.telegram.org \u2192 API development tools. The app restarts after saving.",
                "Ваши ключи с my.telegram.org \u2192 API development tools. После сохранения приложение перезапустится."));
        box.addView(info);

        final android.widget.EditText idEdit = new android.widget.EditText(context);
        idEdit.setSingleLine(true);
        idEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        idEdit.setHint("api_id");
        idEdit.setTextColor(textColor);
        idEdit.setHintTextColor(gray);
        box.addView(idEdit);

        final android.widget.EditText hashEdit = new android.widget.EditText(context);
        hashEdit.setSingleLine(true);
        hashEdit.setHint("api_hash");
        hashEdit.setTextColor(textColor);
        hashEdit.setHintTextColor(gray);
        box.addView(hashEdit);

        if (EdgeConfig.hasCustomApiKeys()) {
            idEdit.setText(String.valueOf(org.telegram.messenger.BuildVars.APP_ID));
            hashEdit.setText(org.telegram.messenger.BuildVars.APP_HASH);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(EdgeConfig.tr("Your API keys", "Свои API-ключи"));
        builder.setView(box);
        builder.setPositiveButton(LocaleController.getString(org.telegram.messenger.R.string.Save), (d, w) -> {
            try {
                int id = Integer.parseInt(idEdit.getText().toString().trim());
                String hash = hashEdit.getText().toString().trim().toLowerCase();
                if (id <= 0 || !hash.matches("[0-9a-f]{32}")) {
                    throw new NumberFormatException();
                }
                EdgeConfig.saveApiKeys(id, hash);
                EdgeConfig.restartApp(context);
            } catch (Exception e) {
                android.widget.Toast.makeText(context, EdgeConfig.tr("Wrong keys: api_id is a number, api_hash is 32 characters", "Неверные ключи: api_id число, api_hash 32 символа"), android.widget.Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(LocaleController.getString(org.telegram.messenger.R.string.Cancel), null);
        if (EdgeConfig.hasCustomApiKeys()) {
            builder.setNeutralButton(EdgeConfig.tr("Reset", "Сбросить"), (d, w) -> {
                EdgeConfig.resetApiKeys();
                EdgeConfig.restartApp(context);
            });
        }
        fragment.showDialog(builder.create());
    }

    private static final HashMap<String, String> sttCache = new HashMap<>();

    private static void showTextDialog(BaseFragment fragment, String title, String text) {
        final Context context = fragment.getParentActivity();
        if (context == null) {
            return;
        }
        TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        tv.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        tv.setTextIsSelectable(true);
        tv.setText(text);
        tv.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(8), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
        ScrollView sv = new ScrollView(context);
        sv.addView(tv);
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle(title);
        b.setView(sv);
        b.setPositiveButton(LocaleController.getString(org.telegram.messenger.R.string.Close), null);
        b.setNeutralButton(EdgeConfig.tr("Copy", "Копировать"), (d, w) -> {
            AndroidUtilities.addToClipboard(text);
            android.widget.Toast.makeText(context, EdgeConfig.tr("Copied", "Скопировано"), android.widget.Toast.LENGTH_SHORT).show();
        });
        fragment.showDialog(b.create());
    }

    /** Voice message to text through an OpenAI-compatible speech-to-text endpoint (Groq has a free tier). */
    public static void transcribe(BaseFragment fragment, MessageObject message) {
        if (fragment == null || fragment.getParentActivity() == null || message == null) {
            return;
        }
        final Context context = fragment.getParentActivity();
        final String key = EdgeConfig.getSttKey();
        if (key.isEmpty()) {
            showSttKeyDialog(fragment);
            return;
        }
        final String cacheKey = message.getDialogId() + ":" + message.getId();
        final String cached = sttCache.get(cacheKey);
        if (cached != null) {
            showTextDialog(fragment, EdgeConfig.tr("Transcript", "Расшифровка"), cached);
            return;
        }
        final android.widget.Toast wait = android.widget.Toast.makeText(context, EdgeConfig.tr("Transcribing\u2026", "Расшифровываю\u2026"), android.widget.Toast.LENGTH_LONG);
        wait.show();
        final int account = fragment.getCurrentAccount();
        final org.telegram.tgnet.TLRPC.Document doc = message.getDocument();
        new Thread(() -> {
            String result;
            try {
                File file = org.telegram.messenger.FileLoader.getInstance(account).getPathToMessage(message.messageOwner);
                if (message.messageOwner.attachPath != null && new File(message.messageOwner.attachPath).exists()) {
                    file = new File(message.messageOwner.attachPath);
                }
                if (!file.exists() && doc != null) {
                    AndroidUtilities.runOnUIThread(() -> org.telegram.messenger.FileLoader.getInstance(account).loadFile(doc, message, org.telegram.messenger.FileLoader.PRIORITY_NORMAL, 0));
                    for (int i = 0; i < 60 && !file.exists(); i++) {
                        Thread.sleep(500);
                    }
                }
                if (!file.exists()) {
                    throw new Exception(EdgeConfig.tr("Could not download the voice message", "Не удалось скачать голосовое"));
                }
                result = sendStt(file, key);
            } catch (Throwable e) {
                result = null;
                final String err = e.getMessage() == null ? e.toString() : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> {
                    wait.cancel();
                    showTextDialog(fragment, EdgeConfig.tr("Transcription failed", "Не удалось расшифровать"), err);
                });
            }
            if (result != null) {
                final String text = result.isEmpty() ? EdgeConfig.tr("(no speech recognized)", "(речь не распознана)") : result;
                sttCache.put(cacheKey, text);
                AndroidUtilities.runOnUIThread(() -> {
                    wait.cancel();
                    showTextDialog(fragment, EdgeConfig.tr("Transcript", "Расшифровка"), text);
                });
            }
        }, "edge-stt").start();
    }

    private static String sendStt(File file, String key) throws Exception {
        final String boundary = "----edge" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) new URL(EdgeConfig.getSttUrl()).openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(90000);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + key);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        String nl = "\r\n";
        out.writeBytes("--" + boundary + nl + "Content-Disposition: form-data; name=\"model\"" + nl + nl + EdgeConfig.getSttModel() + nl);
        out.writeBytes("--" + boundary + nl + "Content-Disposition: form-data; name=\"response_format\"" + nl + nl + "json" + nl);
        out.writeBytes("--" + boundary + nl + "Content-Disposition: form-data; name=\"file\"; filename=\"voice.ogg\"" + nl + "Content-Type: audio/ogg" + nl + nl);
        FileInputStream in = new FileInputStream(file);
        byte[] buf = new byte[16384];
        int n;
        while ((n = in.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
        in.close();
        out.writeBytes(nl + "--" + boundary + "--" + nl);
        out.flush();
        out.close();
        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (is != null) {
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            is.close();
        }
        String body = bos.toString("UTF-8");
        if (code < 200 || code >= 300) {
            throw new Exception("HTTP " + code + ": " + body);
        }
        return new org.json.JSONObject(body).optString("text", "").trim();
    }

    public static void showStickerSizeDialog(BaseFragment fragment, Runnable onChanged) {
        final Context context = fragment.getParentActivity();
        if (context == null) {
            return;
        }
        final int originalSize = EdgeConfig.stickerSize();
        final android.widget.SeekBar bar = new android.widget.SeekBar(context);
        bar.setMax(24 - 7);
        bar.setProgress(Math.max(0, EdgeConfig.stickerSize() - 7));
        final TextView value = new TextView(context);
        value.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        value.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        value.setText(EdgeConfig.stickerSize() + " dp");
        final org.telegram.ui.Components.EdgeStickerPreviewView preview = new org.telegram.ui.Components.EdgeStickerPreviewView(context);
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(8), AndroidUtilities.dp(22), 0);
        box.addView(preview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(210)));
        box.addView(value);
        box.addView(bar);
        bar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                value.setText((progress + 7) + " dp");
                EdgeConfig.setStickerSize(progress + 7);
                preview.invalidate();
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            }
        });
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle(EdgeConfig.tr("Sticker size", "Размер стикеров"));
        b.setView(box);
        b.setPositiveButton(LocaleController.getString(org.telegram.messenger.R.string.Save), (d, w) -> {
            EdgeConfig.setStickerSize(bar.getProgress() + 7);
            if (onChanged != null) {
                onChanged.run();
            }
        });
        b.setNegativeButton(LocaleController.getString(org.telegram.messenger.R.string.Cancel), (d, w) -> {
            EdgeConfig.setStickerSize(originalSize);
            if (onChanged != null) {
                onChanged.run();
            }
        });
        fragment.showDialog(b.create());
    }

    public static void showSttKeyDialog(BaseFragment fragment) {
        final Context context = fragment.getParentActivity();
        if (context == null) {
            return;
        }
        final int textColor = Theme.getColor(Theme.key_dialogTextBlack);
        final int gray = Theme.getColor(Theme.key_dialogTextGray2);
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), 0);
        TextView info = new TextView(context);
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        info.setTextColor(gray);
        info.setText(EdgeConfig.tr(
                "Free speech recognition: create a free key at console.groq.com \u2192 API Keys and paste it here. Any OpenAI-compatible service also works.",
                "Бесплатная расшифровка: создайте бесплатный ключ на console.groq.com \u2192 API Keys и вставьте сюда. Подойдёт и любой сервис, совместимый с OpenAI."));
        box.addView(info);
        final android.widget.EditText keyEdit = new android.widget.EditText(context);
        keyEdit.setSingleLine(true);
        keyEdit.setHint("API key");
        keyEdit.setTextColor(textColor);
        keyEdit.setHintTextColor(gray);
        keyEdit.setText(EdgeConfig.getSttKey());
        box.addView(keyEdit);
        final android.widget.EditText urlEdit = new android.widget.EditText(context);
        urlEdit.setSingleLine(true);
        urlEdit.setHint("URL");
        urlEdit.setTextColor(textColor);
        urlEdit.setHintTextColor(gray);
        urlEdit.setText(EdgeConfig.getSttUrl());
        box.addView(urlEdit);
        final android.widget.EditText modelEdit = new android.widget.EditText(context);
        modelEdit.setSingleLine(true);
        modelEdit.setHint("model");
        modelEdit.setTextColor(textColor);
        modelEdit.setHintTextColor(gray);
        modelEdit.setText(EdgeConfig.getSttModel());
        box.addView(modelEdit);
        ScrollView sv = new ScrollView(context);
        sv.addView(box);
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle(EdgeConfig.tr("Voice transcription", "Расшифровка голосовых"));
        b.setView(sv);
        b.setPositiveButton(LocaleController.getString(org.telegram.messenger.R.string.Save), (d, w) -> {
            EdgeConfig.putString("sttKey", keyEdit.getText().toString().trim());
            String u = urlEdit.getText().toString().trim();
            String m = modelEdit.getText().toString().trim();
            if (!u.isEmpty()) {
                EdgeConfig.putString("sttUrl", u);
            }
            if (!m.isEmpty()) {
                EdgeConfig.putString("sttModel", m);
            }
        });
        b.setNegativeButton(LocaleController.getString(org.telegram.messenger.R.string.Cancel), null);
        fragment.showDialog(b.create());
    }

    private static String formatDate(int date) {
        try {
            return LocaleController.formatDateAudio(date, false) + " " + LocaleController.getInstance().getFormatterDay().format(new Date(date * 1000L));
        } catch (Throwable e) {
            return new Date(date * 1000L).toString();
        }
    }

    public static void showEdits(BaseFragment fragment, MessageObject message) {
        if (fragment == null || fragment.getParentActivity() == null || message == null) {
            return;
        }
        final Context context = fragment.getParentActivity();
        final ArrayList<EdgeStore.Edit> edits = EdgeStore.getInstance(fragment.getCurrentAccount()).getEdits(message.getDialogId(), message.getId());

        final int textColor = Theme.getColor(Theme.key_dialogTextBlack);
        final int grayColor = Theme.getColor(Theme.key_dialogTextGray2);
        final int blue = Theme.getColor(Theme.key_dialogTextBlue);

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), AndroidUtilities.dp(8));

        if (edits.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText(EdgeConfig.tr("No saved edits", "Правок нет"));
            empty.setTextColor(grayColor);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            list.addView(empty);
        }

        for (int i = edits.size() - 1; i >= 0; i--) {
            EdgeStore.Edit e = edits.get(i);
            TextView head = new TextView(context);
            head.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            head.setTextColor(blue);
            head.setTypeface(AndroidUtilities.bold());
            head.setText("\u270E " + EdgeConfig.tr("Edit", "Правка") + " #" + (i + 1) + "  \u2022  " + formatDate(e.date));
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hp.topMargin = AndroidUtilities.dp(i == edits.size() - 1 ? 4 : 14);
            list.addView(head, hp);

            TextView body = new TextView(context);
            body.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            body.setTextColor(textColor);
            body.setTextIsSelectable(true);
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append(EdgeConfig.tr("Was:", "Было:")).append('\n');
            int a = sb.length();
            sb.append(e.oldText == null ? "" : e.oldText);
            sb.setSpan(new ForegroundColorSpan(0xFFE0524D), a, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new StrikethroughSpan(), a, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new StyleSpan(Typeface.NORMAL), 0, 0, 0);
            sb.append("\n\n").append(EdgeConfig.tr("Became:", "Стало:")).append('\n');
            int b = sb.length();
            sb.append(e.newText == null ? "" : e.newText);
            sb.setSpan(new ForegroundColorSpan(0xFF3FAE4F), b, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            body.setText(sb);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bp.topMargin = AndroidUtilities.dp(4);
            list.addView(body, bp);
        }

        ScrollView scroll = new ScrollView(context);
        scroll.addView(list);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(EdgeConfig.tr("Edit history", "История правок"));
        builder.setView(scroll);
        builder.setPositiveButton(LocaleController.getString(org.telegram.messenger.R.string.Close), null);
        fragment.showDialog(builder.create());
    }
}
