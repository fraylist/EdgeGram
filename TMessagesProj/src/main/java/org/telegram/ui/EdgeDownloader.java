package org.telegram.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.EdgeConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;

import java.io.File;
import java.util.ArrayList;

/** Downloads every media file of a chat into the Downloads folder. */
public class EdgeDownloader {

    private static final int KIND_PHOTO = 0;
    private static final int KIND_VIDEO = 1;
    private static final int KIND_FILE = 2;
    private static final int KIND_AUDIO = 3;

    private static int kindOf(MessageObject mo) {
        if (mo == null || mo.messageOwner == null) {
            return -1;
        }
        if (mo.isPhoto() || mo.type == MessageObject.TYPE_PHOTO) {
            return KIND_PHOTO;
        }
        if (mo.isVideo() || mo.isRoundVideo() || mo.isGif()) {
            return KIND_VIDEO;
        }
        if (mo.isVoice() || mo.isMusic()) {
            return KIND_AUDIO;
        }
        if (mo.getDocument() != null && !mo.isSticker() && !mo.isAnimatedSticker()) {
            return KIND_FILE;
        }
        return -1;
    }

    private static String t(String en, String ru) {
        return EdgeConfig.tr(en, ru);
    }

    public static void show(ChatActivity activity, ArrayList<MessageObject> messages) {
        if (activity == null || activity.getParentActivity() == null || messages == null) {
            return;
        }
        final Context context = activity.getParentActivity();
        final LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(8), AndroidUtilities.dp(22), 0);

        final CheckBox cbPhoto = check(context, t("Photos", "Фото"), true);
        final CheckBox cbVideo = check(context, t("Videos and GIF", "Видео и GIF"), true);
        final CheckBox cbFile = check(context, t("Files", "Файлы"), false);
        final CheckBox cbAudio = check(context, t("Voice and music", "Голосовые и музыка"), false);
        box.addView(cbPhoto);
        box.addView(cbVideo);
        box.addView(cbFile);
        box.addView(cbAudio);

        TextView info = new TextView(context);
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        info.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        info.setPadding(0, AndroidUtilities.dp(8), 0, 0);
        info.setText(t("Files are saved to the Downloads folder of your phone. Large chats may take a while.",
                "Файлы сохраняются в папку «Загрузки». Большие чаты могут качаться долго."));
        box.addView(info);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(t("Download everything", "Скачать всё из чата"));
        builder.setView(box);
        builder.setPositiveButton(t("Start", "Начать"), (d, w) -> {
            ArrayList<Integer> kinds = new ArrayList<>();
            if (cbPhoto.isChecked()) kinds.add(KIND_PHOTO);
            if (cbVideo.isChecked()) kinds.add(KIND_VIDEO);
            if (cbFile.isChecked()) kinds.add(KIND_FILE);
            if (cbAudio.isChecked()) kinds.add(KIND_AUDIO);
            start(activity, messages, kinds);
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        activity.showDialog(builder.create());
    }

    private static CheckBox check(Context context, String text, boolean checked) {
        CheckBox cb = new CheckBox(context);
        cb.setText(text);
        cb.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        cb.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        cb.setChecked(checked);
        cb.setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(6));
        cb.setGravity(Gravity.CENTER_VERTICAL);
        return cb;
    }

    private static void start(ChatActivity activity, ArrayList<MessageObject> messages, ArrayList<Integer> kinds) {
        final Context context = activity.getParentActivity();
        if (context == null) {
            return;
        }
        final int account = activity.getCurrentAccount();
        final ArrayList<MessageObject> queue = new ArrayList<>();
        for (MessageObject mo : messages) {
            if (mo == null || mo.messageOwner == null || mo.isSending() || mo.getId() <= 0) {
                continue;
            }
            int kind = kindOf(mo);
            if (kind >= 0 && kinds.contains(kind)) {
                queue.add(mo);
            }
        }
        if (queue.isEmpty()) {
            BulletinFactory.of(activity).createSimpleBulletin(R.raw.info,
                    t("Nothing to download here", "Здесь нечего скачивать")).show();
            return;
        }
        for (MessageObject mo : queue) {
            try {
                TLRPC.Document document = mo.getDocument();
                if (document != null) {
                    FileLoader.getInstance(account).loadFile(document, mo, FileLoader.PRIORITY_NORMAL, 0);
                } else if (mo.isPhoto()) {
                    TLRPC.PhotoSize size = FileLoader.getClosestPhotoSizeWithSize(mo.photoThumbs, AndroidUtilities.getPhotoSize());
                    if (size != null) {
                        FileLoader.getInstance(account).loadFile(ImageLocation.getForObject(size, mo.photoThumbsObject), mo, "jpg", FileLoader.PRIORITY_NORMAL, 0);
                    }
                }
            } catch (Throwable ignore) {
            }
        }
        BulletinFactory.of(activity).createSimpleBulletin(R.raw.done,
                t("Downloading ", "Скачиваем ") + queue.size() + t(" files", " файлов")).show();
        saveWhenReady(activity, queue, account, context, 0, 0);
    }

    private static void saveWhenReady(ChatActivity activity, ArrayList<MessageObject> queue, int account, Context context, int saved, int attempts) {
        if (activity == null || activity.getParentActivity() == null) {
            return;
        }
        final ArrayList<MessageObject> pending = new ArrayList<>();
        int savedNow = saved;
        for (MessageObject mo : queue) {
            try {
                File file = FileLoader.getInstance(account).getPathToMessage(mo.messageOwner);
                if (file == null) {
                    file = FileLoader.getInstance(account).getPathToAttach(mo.getDocument() != null ? mo.getDocument() : mo.messageOwner.media);
                }
                if (file != null && file.exists() && file.length() > 0) {
                    int kind = kindOf(mo);
                    String name = null;
                    String mime = "application/octet-stream";
                    TLRPC.Document document = mo.getDocument();
                    if (document != null) {
                        name = FileLoader.getAttachFileName(document);
                        if (!TextUtils.isEmpty(document.mime_type)) {
                            mime = document.mime_type;
                        }
                    } else {
                        name = file.getName();
                        mime = "image/jpeg";
                    }
                    int type = kind == KIND_PHOTO ? 0 : kind == KIND_VIDEO ? 1 : 2;
                    MediaController.saveFile(file.getAbsolutePath(), context, type, name, mime, null, false);
                    savedNow++;
                } else {
                    pending.add(mo);
                }
            } catch (Throwable e) {
                savedNow++;
            }
        }
        if (pending.isEmpty() || attempts > 400) {
            final int total = savedNow;
            BulletinFactory.of(activity).createSimpleBulletin(R.raw.done,
                    t("Saved files: ", "Сохранено файлов: ") + total).show();
            return;
        }
        final int nextSaved = savedNow;
        AndroidUtilities.runOnUIThread(() -> saveWhenReady(activity, pending, account, context, nextSaved, attempts + 1), 1500);
    }
}