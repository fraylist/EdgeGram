package org.telegram.ui;

import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.telegram.messenger.EdgeConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/** Per-chat biometric lock: a cover over the chat that asks for the fingerprint before showing it. */
public class EdgeChatLock {

    public static void apply(ChatActivity fragment, FrameLayout content, int account, long dialogId, String title) {
        if (fragment == null || content == null) {
            return;
        }
        final Activity activity = fragment.getParentActivity();
        if (activity == null) {
            return;
        }
        if (!EdgeConfig.isChatLocked(account, dialogId) || EdgeConfig.isChatUnlockedNow(account, dialogId)) {
            return;
        }
        final int lockAvailable = BiometricManager.from(activity).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG);
        if (lockAvailable != BiometricManager.BIOMETRIC_SUCCESS) {
            // no fingerprint on the device: never lock the user out of the chat
            EdgeConfig.markChatUnlocked(account, dialogId);
            return;
        }

        final FrameLayout cover = new FrameLayout(activity);
        cover.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cover.setClickable(true);

        final LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        final ImageView icon = new ImageView(activity);
        icon.setImageResource(R.drawable.permission_locked);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon));
        box.addView(icon, LayoutHelper.createLinear(56, 56, Gravity.CENTER_HORIZONTAL));

        final TextView text = new TextView(activity);
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        text.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        text.setGravity(Gravity.CENTER);
        text.setText(EdgeConfig.tr("Chat is locked", "Чат заблокирован"));
        box.addView(text, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 12, 0, 0));

        final TextView hint = new TextView(activity);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        hint.setGravity(Gravity.CENTER);
        hint.setText(EdgeConfig.tr("Tap to unlock", "Нажмите, чтобы разблокировать"));
        box.addView(hint, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 4, 0, 0));

        cover.addView(box, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        content.addView(cover, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        cover.setOnClickListener(v -> prompt(fragment, account, dialogId, title, cover));
        cover.post(() -> prompt(fragment, account, dialogId, title, cover));
    }

    private static void prompt(ChatActivity fragment, int account, long dialogId, String title, FrameLayout cover) {
        final Activity activity = fragment == null ? null : fragment.getParentActivity();
        if (activity == null || !(activity instanceof FragmentActivity) || cover.getParent() == null) {
            return;
        }
        final FragmentActivity fragmentActivity = (FragmentActivity) activity;
        final BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(EdgeConfig.tr("Unlock chat", "Разблокировать чат"))
                .setSubtitle(title == null ? "" : title)
                .setNegativeButtonText(EdgeConfig.tr("Cancel", "Отмена"))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();
        final BiometricPrompt prompt = new BiometricPrompt(fragmentActivity,
                ContextCompat.getMainExecutor(fragmentActivity),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        EdgeConfig.markChatUnlocked(account, dialogId);
                        remove(cover);
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        remove(cover);
                        fragment.finishFragment();
                    }
                });
        try {
            prompt.authenticate(info);
        } catch (Throwable e) {
            remove(cover);
        }
    }

    private static void remove(View cover) {
        if (cover != null && cover.getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) cover.getParent()).removeView(cover);
        }
    }
}
