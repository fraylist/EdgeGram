package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.EdgeConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

/** Live preview of a sticker at the size chosen in the settings. */
public class EdgeStickerPreviewView extends View {

    private final Paint bubble = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint image = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF r = new RectF();
    private Bitmap bitmap;

    public EdgeStickerPreviewView(Context context) {
        super(context);
    }

    private Bitmap bitmap() {
        if (bitmap == null) {
            try {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.edge_sticker);
            } catch (Throwable ignore) {
            }
        }
        return bitmap;
    }

    /** The same proportion the chat uses: 0.5 of the screen at 14 dp, growing to 24 dp. */
    private float scaleFactor() {
        return 1f + (EdgeConfig.stickerSize() - 14) / 15f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Bitmap bm = bitmap();
        if (bm == null) {
            return;
        }
        final int w = getWidth();
        final int h = getHeight();
        final float label = AndroidUtilities.dp(26);
        float side = AndroidUtilities.dp(96) * scaleFactor();
        side = Math.min(side, Math.min(w * 0.62f, h - label - AndroidUtilities.dp(16)));
        if (side <= 0) {
            return;
        }
        final float left = (w - side) / 2f;
        final float top = (h - label - side) / 2f;
        r.set(left, top, left + side, top + side);
        canvas.drawBitmap(bm, null, r, image);

        final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setTextSize(AndroidUtilities.dp(13));
        text.setTextAlign(Paint.Align.CENTER);
        try {
            text.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        } catch (Throwable e) {
            text.setColor(0xFF8E8E93);
        }
        canvas.drawText(EdgeConfig.stickerSize() + " dp", w / 2f, top + side + AndroidUtilities.dp(19), text);
    }
}