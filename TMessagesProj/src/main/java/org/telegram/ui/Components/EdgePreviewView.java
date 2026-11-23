package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.EdgeConfig;
import org.telegram.ui.ActionBar.Theme;

/** Live preview cards for the EdgeGram settings: a chat with a deleted message and a chat list row. */
public class EdgePreviewView extends View {

    public static final int MODE_CHAT = 0;
    public static final int MODE_LIST = 1;

    private final int mode;
    private final Paint bubble = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint text = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint small = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint bold = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint markPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    public EdgePreviewView(Context context, int mode) {
        super(context);
        this.mode = mode;
        bold.setTypeface(AndroidUtilities.bold());
    }

    private static int color(int key, int def) {
        try {
            return Theme.getColor(key);
        } catch (Throwable e) {
            return def;
        }
    }

    private static String time() {
        return EdgeConfig.timeWithSeconds() ? "12:34:56" : "12:34";
    }

    private float dp(float v) {
        return AndroidUtilities.dp(v);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mode == MODE_LIST) {
            drawList(canvas);
        } else {
            drawChat(canvas);
        }
    }

    private void drawBubble(Canvas canvas, float left, float top, float right, float bottom, int bgColor) {
        bubble.setColor(bgColor);
        r.set(left, top, right, bottom);
        canvas.drawRoundRect(r, dp(14), dp(14), bubble);
    }

    private void drawChat(Canvas canvas) {
        final int w = getWidth();
        final float pad = dp(16);

        text.setTextSize(dp(14));
        small.setTextSize(dp(11));

        // incoming message
        final int inColor = color(Theme.key_chat_inBubble, 0x33121212);
        final int inText = color(Theme.key_chat_messageTextIn, 0xFF000000);
        final int inTimeColor = color(Theme.key_chat_inTimeText, 0xFF8E8E93);
        final int outTimeColor = color(Theme.key_chat_outTimeText, 0xFF8E8E93);

        String in1 = EdgeConfig.tr("This is how a usual message looks", "Так выглядит обычное сообщение");
        float inW = Math.min(w * 0.74f, text.measureText(in1) + dp(26));
        drawBubble(canvas, pad, dp(10), pad + inW, dp(54), inColor);
        text.setColor(inText);
        canvas.drawText(ellipsize(in1, inW - dp(22)), pad + dp(12), dp(37), text);
        small.setColor(inTimeColor);
        String inTime = time();
        canvas.drawText(inTime, pad + inW - dp(10) - small.measureText(inTime), dp(48), small);

        // outgoing edited message
        final int outColor = color(Theme.key_chat_outBubble, 0xFFE3F2FD);
        final int outText = color(Theme.key_chat_messageTextOut, 0xFF000000);
        String out1 = EdgeConfig.tr("A message that was edited", "Сообщение, которое изменили");
        String edited = "\u270E " + time();
        float outW = Math.min(w * 0.8f, Math.max(text.measureText(out1), small.measureText(edited)) + dp(26));
        float outRight = w - pad;
        drawBubble(canvas, outRight - outW, dp(64), outRight, dp(108), outColor);
        text.setColor(outText);
        canvas.drawText(ellipsize(out1, outW - dp(22)), outRight - outW + dp(12), dp(88), text);
        small.setColor(outTimeColor);
        canvas.drawText(edited, outRight - dp(10) - small.measureText(edited), dp(102), small);

        // deleted message with the chosen mark and color
        final int bubbleAlpha = EdgeConfig.dimDeleted() ? (int) (255 * 0.65f) : 255;
        final int textAlpha = EdgeConfig.dimDeleted() ? (int) (255 * 0.75f) : 255;
        String del = EdgeConfig.tr("A message that was deleted", "Сообщение, которое удалили");
        String mark = EdgeConfig.getMarkText();
        small.setTextSize(dp(11));
        float markW = small.measureText(mark + " " + time());
        float delW = Math.min(w * 0.86f, Math.max(text.measureText(del), markW) + dp(26));
        bubble.setAlpha(bubbleAlpha);
        bubble.setColor(inColor);
        r.set(pad, dp(118), pad + delW, dp(174));
        canvas.drawRoundRect(r, dp(14), dp(14), bubble);
        bubble.setAlpha(255);

        text.setColor(inText);
        text.setAlpha(textAlpha);
        canvas.drawText(ellipsize(del, delW - dp(22)), pad + dp(12), dp(146), text);
        text.setAlpha(255);

        markPaint.setTextSize(dp(11));
        markPaint.setColor(EdgeConfig.getMarkColor());
        markPaint.setAlpha(textAlpha);
        float markX = pad + delW - dp(10) - small.measureText(mark + " " + time());
        canvas.drawText(mark, markX, dp(165), markPaint);
        markPaint.setAlpha(255);
        small.setColor(inTimeColor);
        small.setAlpha(textAlpha);
        canvas.drawText(" " + time(), markX + markPaint.measureText(mark), dp(165), small);
        small.setAlpha(255);
    }

    private String ellipsize(String s, float max) {
        return TextUtils.ellipsize(s, text, max, TextUtils.TruncateAt.END).toString();
    }

    private void drawList(Canvas canvas) {
        final int w = getWidth();
        final float pad = dp(16);
        final int nameColor = color(Theme.key_chats_name, 0xFF000000);
        final int msgColor = color(Theme.key_chats_message, 0xFF8E8E93);
        final int timeColor = color(Theme.key_chats_nameMessage, 0xFF8E8E93);
        final int unreadColor = color(Theme.key_chats_unreadCounter, 0xFF3390EC);
        final int unreadText = color(Theme.key_chats_unreadCounterText, 0xFFFFFFFF);

        // avatar
        accent.setColor(color(Theme.key_avatar_backgroundBlue, 0xFF5B8DEF));
        canvas.drawCircle(pad + dp(22), dp(32), dp(22), accent);

        bold.setTextSize(dp(15));
        bold.setColor(nameColor);
        canvas.drawText(EdgeConfig.tr("Arthur", "Артур"), pad + dp(56), dp(26), bold);

        small.setTextSize(dp(12));

        float msgX = pad + dp(56);
        if (EdgeConfig.miniSenderAvatar()) {
            accent.setColor(color(Theme.key_avatar_backgroundGreen, 0xFF43A047));
            canvas.drawCircle(msgX + dp(7), dp(41), dp(7), accent);
            msgX += dp(19);
        }

        String sender = EdgeConfig.miniSenderAvatar() ? EdgeConfig.tr("Angry user: ", "Angry user: ") : "";
        String msg = sender + EdgeConfig.tr("do you really want that new UI?", "вам правда нужен новый интерфейс?");
        small.setColor(msgColor);
        float available = w - msgX - pad - dp(60);
        canvas.drawText(TextUtils.ellipsize(msg, small, available, TextUtils.TruncateAt.END).toString(), msgX, dp(46), small);

        // thumbnails when the media preview is on
        if (EdgeConfig.mediaPreview()) {
            float tx = w - pad - dp(52);
            bubble.setColor(color(Theme.key_chats_attachMessage, 0xFFDDE3EA));
            for (int i = 0; i < 2; i++) {
                r.set(tx + i * dp(25), dp(32), tx + i * dp(25) + dp(23), dp(55));
                canvas.drawRoundRect(r, dp(4), dp(4), bubble);
            }
        }

        small.setColor(timeColor);
        String t = time();
        boolean hasUnread = !EdgeConfig.mediaPreview();
        float timeRight = w - pad - (hasUnread ? dp(26) : 0);
        canvas.drawText(t, timeRight - small.measureText(t), dp(26), small);

        if (hasUnread) {
            accent.setColor(unreadColor);
            canvas.drawCircle(w - pad - dp(9), dp(22), dp(9), accent);
            small.setColor(unreadText);
            String n = "2";
            canvas.drawText(n, w - pad - dp(9) - small.measureText(n) / 2f, dp(26), small);
        }

        // divider
        bubble.setColor(color(Theme.key_divider, 0x1A000000));
        canvas.drawRect(pad + dp(56), dp(63), w - pad, dp(64), bubble);
    }
}