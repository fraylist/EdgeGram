package org.telegram.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.EdgeConfig;
import org.telegram.messenger.EdgeStore;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;
import java.util.Date;

/** Search across deleted messages and edit history. */
public class EdgeHiddenActivity extends UniversalFragment {

    private EditText searchField;
    private FrameLayout searchWrap;
    private ImageView clearButton;
    private String query = "";
    private int mode = 0;
    private int generation = 0;
    private ArrayList<EdgeStore.Hit> results = new ArrayList<>();
    private boolean loading;

    private static String t(String en, String ru) {
        return EdgeConfig.tr(en, ru);
    }

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        fragmentView = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        return fragmentView;
    }

    @Override
    protected RecyclerListView getListViewForSimpleGlass() {
        return listView;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Override
    protected CharSequence getTitle() {
        return t("Hidden messages", "Скрытое от меня");
    }

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);
        if (searchWrap == null) {
            buildSearchBar(view.getContext());
        }
        runSearch();
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        super.onInsets(left, top, right, bottom);
        positionSearchBar();
    }

    private void buildSearchBar(Context context) {
        FrameLayout wrap = new FrameLayout(context);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(22));
        wrap.setBackground(bg);

        searchField = new EditText(context);
        searchField.setSingleLine(true);
        searchField.setBackground(null);
        searchField.setHint(t("Search deleted and edited", "Поиск по удалённому и правкам"));
        searchField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        searchField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        searchField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        searchField.setPadding(AndroidUtilities.dp(14), 0, AndroidUtilities.dp(38), 0);
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchField.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        searchField.setGravity(Gravity.CENTER_VERTICAL);
        wrap.addView(searchField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        clearButton = new ImageView(context);
        clearButton.setImageResource(R.drawable.msg_clear_input);
        clearButton.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        clearButton.setVisibility(View.GONE);
        clearButton.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        clearButton.setOnClickListener(v -> {
            searchField.setText("");
            AndroidUtilities.hideKeyboard(searchField);
        });
        wrap.addView(clearButton, LayoutHelper.createFrame(38, 38, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 4, 0));

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                clearButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                runSearch();
            }
        });
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                AndroidUtilities.hideKeyboard(searchField);
                return true;
            }
            return false;
        });

        searchWrap = wrap;
        contentView.addView(wrap, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.TOP | Gravity.FILL_HORIZONTAL, 12, ActionBar.getCurrentActionBarHeight(), 12, 0));
        positionSearchBar();
    }

    private void positionSearchBar() {
        if (searchWrap == null || listView == null) {
            return;
        }
        final int barTop = mSystemInsets.top + ActionBar.getCurrentActionBarHeight() + AndroidUtilities.dp(6);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) searchWrap.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(44));
            lp.gravity = Gravity.TOP;
        }
        lp.topMargin = barTop;
        lp.leftMargin = AndroidUtilities.dp(12);
        lp.rightMargin = AndroidUtilities.dp(12);
        searchWrap.setLayoutParams(lp);
        listView.setClipToPadding(false);
        listView.setPadding(0, barTop + AndroidUtilities.dp(50), 0, listView.getPaddingBottom());
    }

    private void runSearch() {
        final int gen = ++generation;
        final String q = query;
        final int m = mode;
        loading = true;
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(false);
        }
        new Thread(() -> {
            ArrayList<EdgeStore.Hit> found;
            try {
                found = EdgeStore.getInstance(currentAccount).search(q, m, 120);
            } catch (Throwable e) {
                found = new ArrayList<>();
            }
            final ArrayList<EdgeStore.Hit> result = found;
            AndroidUtilities.runOnUIThread(() -> {
                if (gen != generation) {
                    return;
                }
                loading = false;
                results = result;
                if (listView != null && listView.adapter != null) {
                    listView.adapter.update(false);
                }
            });
        }, "edge-hidden-search").start();
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(t("What is hidden", "Что скрывают")));
        items.add(UItem.asSlideView(new String[]{
                t("Everything", "Всё"),
                t("Deleted", "Удалённые"),
                t("Edited", "Правки")
        }, mode, idx -> {
            mode = idx;
            runSearch();
        }));
        items.add(UItem.asShadow(t("Search runs on this phone only, nothing is sent anywhere. Tap a result to open the chat at that message.",
                "Поиск идёт только по этому телефону, никуда не отправляется. Нажмите на результат, чтобы открыть чат на этом сообщении.")));

        if (loading && results.isEmpty()) {
            items.add(UItem.asHeader(t("Searching…", "Ищу…")));
            items.add(UItem.asShadow(null));
            return;
        }

        items.add(UItem.asHeader(results.isEmpty()
                ? t("Nothing found", "Ничего не найдено")
                : t("Found: ", "Найдено: ") + results.size()));

        for (int i = 0; i < results.size(); i++) {
            EdgeStore.Hit hit = results.get(i);
            UItem custom = UItem.asCustom(1000 + i, buildCell(hit));
            custom.intValue = hit.kind == 1 && !TextUtils.isEmpty(hit.oldText) ? 96 : 78;
            items.add(custom);
        }
        items.add(UItem.asShadow(null));
    }

    private View buildCell(EdgeStore.Hit hit) {
        final Context context = getContext();
        final LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(8), AndroidUtilities.dp(16), AndroidUtilities.dp(8));

        final TextView title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setTypeface(AndroidUtilities.bold());
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setText(chatName(hit.uid) + "  ·  " + dateText(hit.date) + "  ·  "
                + (hit.kind == 0 ? t("deleted", "удалено") : t("edited", "изменено")));
        box.addView(title);

        final TextView body = new TextView(context);
        body.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        body.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        body.setMaxLines(1);
        body.setEllipsize(TextUtils.TruncateAt.END);
        body.setText(TextUtils.isEmpty(hit.text) ? t("(no text)", "(без текста)") : hit.text);
        box.addView(body);

        if (hit.kind == 1 && !TextUtils.isEmpty(hit.oldText)) {
            final TextView old = new TextView(context);
            old.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            old.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            old.setMaxLines(1);
            old.setEllipsize(TextUtils.TruncateAt.END);
            old.setText(t("was: ", "было: ") + hit.oldText);
            box.addView(old);
        }

        return box;
    }

    private String chatName(long dialogId) {
        try {
            MessagesController mc = MessagesController.getInstance(currentAccount);
            if (dialogId > 0) {
                TLRPC.User user = mc.getUser(dialogId);
                if (user != null) {
                    String name = ContactsController.formatName(user.first_name, user.last_name);
                    return TextUtils.isEmpty(name) ? String.valueOf(dialogId) : name;
                }
                return String.valueOf(dialogId);
            } else {
                TLRPC.Chat chat = mc.getChat(-dialogId);
                if (chat != null && !TextUtils.isEmpty(chat.title)) {
                    return chat.title;
                }
                return String.valueOf(-dialogId);
            }
        } catch (Throwable e) {
            return String.valueOf(dialogId);
        }
    }

    private String dateText(int date) {
        try {
            return LocaleController.getInstance().getFormatterDay().format(new Date(date * 1000L));
        } catch (Throwable e) {
            return "";
        }
    }

    private void openChat(EdgeStore.Hit hit) {
        try {
            if (DialogObject.isEncryptedDialog(hit.uid)) {
                return;
            }
            Bundle args = new Bundle();
            if (hit.uid > 0) {
                args.putLong("user_id", hit.uid);
            } else {
                args.putLong("chat_id", -hit.uid);
            }
            if (hit.mid > 0) {
                args.putInt("message_id", hit.mid);
            }
            presentFragment(new ChatActivity(args));
        } catch (Throwable e) {
            org.telegram.messenger.FileLog.e(e);
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        final int index = item.id - 1000;
        if (index >= 0 && index < results.size()) {
            openChat(results.get(index));
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}