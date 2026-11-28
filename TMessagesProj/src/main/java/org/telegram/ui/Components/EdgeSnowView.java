package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import java.util.Random;

/** Light snowfall over the chat list and chats. Touches pass through. */
public class EdgeSnowView extends View {

    private static final int COUNT = 42;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final float[] px = new float[COUNT];
    private final float[] py = new float[COUNT];
    private final float[] radius = new float[COUNT];
    private final float[] speed = new float[COUNT];
    private final float[] alpha = new float[COUNT];
    private final float[] drift = new float[COUNT];

    private long lastFrame;

    public EdgeSnowView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setEnabled(false);
        setWillNotDraw(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        for (int i = 0; i < COUNT; i++) {
            px[i] = random.nextFloat();
            py[i] = random.nextFloat();
            radius[i] = AndroidUtilities.dp(1.2f + random.nextFloat() * 2.2f);
            speed[i] = 0.06f + random.nextFloat() * 0.16f;
            alpha[i] = 0.35f + random.nextFloat() * 0.5f;
            drift[i] = (random.nextFloat() - 0.5f) * 0.06f;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        final long now = System.currentTimeMillis();
        if (lastFrame == 0) {
            lastFrame = now;
        }
        float dt = Math.min(64, now - lastFrame) / 1000f;
        lastFrame = now;

        final int background = Theme.getColor(Theme.key_windowBackgroundWhite);
        final boolean light = AndroidUtilities.computePerceivedBrightness(background) > 0.6f;
        paint.setColor(light ? 0xFF2B3A4A : 0xFFFFFFFF);

        for (int i = 0; i < COUNT; i++) {
            py[i] += speed[i] * dt;
            px[i] += drift[i] * dt;
            if (py[i] > 1.05f) {
                py[i] = -0.05f;
                px[i] = random.nextFloat();
            }
            if (px[i] < -0.05f) {
                px[i] = 1.05f;
            } else if (px[i] > 1.05f) {
                px[i] = -0.05f;
            }
            paint.setAlpha((int) (alpha[i] * 255));
            canvas.drawCircle(px[i] * width, py[i] * height, radius[i], paint);
        }
        postInvalidateOnAnimation();
    }
}
