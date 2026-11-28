package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

/** HSV color picker: a saturation/value square plus a hue bar. */
public class EdgeColorPickerView extends View {

    public interface OnColorChanged {
        void onColorChanged(int color);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF svRect = new RectF();
    private final RectF hueRect = new RectF();
    private final float[] hsv = new float[]{0f, 1f, 1f};

    private OnColorChanged listener;
    private boolean dragSv;
    private boolean dragHue;

    public EdgeColorPickerView(Context context) {
        super(context);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(AndroidUtilities.dp(2.5f));
        stroke.setColor(0xFFFFFFFF);
    }

    public void setOnColorChanged(OnColorChanged l) {
        listener = l;
    }

    public void setColor(int color) {
        Color.colorToHSV(color, hsv);
        invalidate();
    }

    public int getColor() {
        return Color.HSVToColor(hsv);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void layoutParts() {
        final float pad = AndroidUtilities.dp(10);
        final float hueH = AndroidUtilities.dp(22);
        final float gap = AndroidUtilities.dp(14);
        float w = getWidth() - pad * 2;
        float h = getHeight() - pad * 2 - hueH - gap;
        float size = Math.min(w, h);
        float left = (getWidth() - size) / 2f;
        svRect.set(left, pad, left + size, pad + size);
        hueRect.set(left, svRect.bottom + gap, left + size, svRect.bottom + gap + hueH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        layoutParts();
        final int hueColor = Color.HSVToColor(new float[]{hsv[0], 1f, 1f});

        paint.setShader(new LinearGradient(svRect.left, svRect.top, svRect.right, svRect.top,
                Color.WHITE, hueColor, Shader.TileMode.CLAMP));
        canvas.drawRect(svRect, paint);
        paint.setShader(new LinearGradient(svRect.left, svRect.top, svRect.left, svRect.bottom,
                Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP));
        canvas.drawRect(svRect, paint);
        paint.setShader(null);

        final float cx = svRect.left + hsv[1] * svRect.width();
        final float cy = svRect.top + (1f - hsv[2]) * svRect.height();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(AndroidUtilities.dp(3f));
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, AndroidUtilities.dp(9), paint);
        paint.setStrokeWidth(AndroidUtilities.dp(1.5f));
        paint.setColor(0x55000000);
        canvas.drawCircle(cx, cy, AndroidUtilities.dp(11), paint);
        paint.setStyle(Paint.Style.FILL);

        int[] hueColors = new int[]{
                Color.HSVToColor(new float[]{0, 1, 1}),
                Color.HSVToColor(new float[]{60, 1, 1}),
                Color.HSVToColor(new float[]{120, 1, 1}),
                Color.HSVToColor(new float[]{180, 1, 1}),
                Color.HSVToColor(new float[]{240, 1, 1}),
                Color.HSVToColor(new float[]{300, 1, 1}),
                Color.HSVToColor(new float[]{360, 1, 1})
        };
        paint.setShader(new LinearGradient(hueRect.left, hueRect.top, hueRect.right, hueRect.top,
                hueColors, null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(hueRect, hueRect.height() / 2f, hueRect.height() / 2f, paint);
        paint.setShader(null);

        final float hx = hueRect.left + (hsv[0] / 360f) * hueRect.width();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(AndroidUtilities.dp(3f));
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(hx, hueRect.centerY(), hueRect.height() / 2f - AndroidUtilities.dp(1f), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragSv = svRect.contains(e.getX(), e.getY());
                dragHue = hueRect.contains(e.getX(), e.getY());
                if (!dragSv && !dragHue) {
                    return false;
                }
                apply(e.getX(), e.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragSv || dragHue) {
                    apply(e.getX(), e.getY());
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragSv = dragHue = false;
                return true;
        }
        return super.onTouchEvent(e);
    }

    private void apply(float x, float y) {
        if (dragHue) {
            float t = (x - hueRect.left) / hueRect.width();
            hsv[0] = Math.max(0f, Math.min(1f, t)) * 360f;
        } else if (dragSv) {
            float sx = (x - svRect.left) / svRect.width();
            float sy = (y - svRect.top) / svRect.height();
            hsv[1] = Math.max(0f, Math.min(1f, sx));
            hsv[2] = 1f - Math.max(0f, Math.min(1f, sy));
        }
        invalidate();
        if (listener != null) {
            listener.onColorChanged(getColor());
        }
    }
}