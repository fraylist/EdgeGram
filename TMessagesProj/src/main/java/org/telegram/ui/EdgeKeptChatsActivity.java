package org.telegram.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.EdgeKeptDialogs;
import org.telegram.messenger.EdgeStore;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Date;

/** Chats kept after the server removed them: read-only history, delete forever, stored size. */
public class EdgeKeptChatsActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.EdgeKeptChats));
        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        final ScrollView scrollView = new ScrollView(context);
        final LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(list, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));
        ((FrameLayout) fragmentView).addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        rebuild(list, context);
        return fragmentView;
    }

    private void rebuild(LinearLayout list, Context context) {
        list.removeAllViews();
        final ArrayList<EdgeStore.KeptDialog> kept = EdgeKeptDialogs.list(currentAccount);
        if (kept.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText(LocaleController.getString(R.string.EdgeKeptChatsEmpty));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            empty.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(48), AndroidUtilities.dp(24), AndroidUtilities.dp(24));
            list.addView(empty, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            return;
        }

        TextView summary = new TextView(context);
        summary.setText(LocaleController.formatString(R.string.EdgeKeptChatsSummary, kept.size()));
        summary.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        summary.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        summary.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        list.addView(summary, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        for (int i = 0; i < kept.size(); i++) {
            final EdgeStore.KeptDialog record = kept.get(i);
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 2));

            TextView name = new TextView(context);
            name.setText(record.name != null ? record.name : String.valueOf(record.did));
            name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            name.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), 0);
            row.addView(name, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            TextView info = new TextView(context);
            info.setText(android.text.format.DateFormat.getDateFormat(context).format(new Date(record.date * 1000L))
                    + " · " + (record.channel ? "канал" : "группа"));
            info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            info.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(2), AndroidUtilities.dp(16), AndroidUtilities.dp(6));
            row.addView(info, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            TextView delete = new TextView(context);
            delete.setText(LocaleController.getString(R.string.EdgeKeptDeleteForever));
            delete.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            delete.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
            delete.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(6), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
            delete.setOnClickListener(v -> {
                MessagesController.getInstance(currentAccount).deleteDialog(record.did, 0);
                rebuild(list, context);
            });
            row.addView(delete, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            params.bottomMargin = AndroidUtilities.dp(8);
            list.addView(row, params);
        }
    }
}
