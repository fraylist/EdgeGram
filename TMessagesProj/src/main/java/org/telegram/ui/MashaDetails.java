package org.telegram.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MashaConfig;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.Vector;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Date;

/** Message details: ids, exact dates, sender and who read it. */
public class MashaDetails {

    private static String dateText(int date) {
        if (date == 0) {
            return "—";
        }
        try {
            return LocaleController.getInstance().getFormatterDayWithSeconds().format(new Date(date * 1000L))
                    + "  (" + date + ")";
        } catch (Throwable e) {
            return String.valueOf(date);
        }
    }

    private static TextView label(Context context) {
        TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        try {
            tv.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        } catch (Throwable ignore) {
        }
        tv.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(1));
        return tv;
    }

    private static TextView value(Context context) {
        TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        try {
            tv.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        } catch (Throwable ignore) {
        }
        tv.setTextIsSelectable(true);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        return tv;
    }

    public static void show(BaseFragment fragment, MessageObject message) {
        if (fragment == null || fragment.getParentActivity() == null || message == null || message.messageOwner == null) {
            return;
        }
        final Context context = fragment.getParentActivity();
        final int account = fragment.getCurrentAccount();
        final TLRPC.Message owner = message.messageOwner;
        final long dialogId = message.getDialogId();

        final LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(4), AndroidUtilities.dp(22), AndroidUtilities.dp(8));

        final StringBuilder copy = new StringBuilder();
        final ArrayList<String> lines = new ArrayList<>();

        final StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(message.getId()).append('\n');
        sb.append("chat: ").append(dialogId).append('\n');

        final TLRPC.User from = owner.from_id != null ? MessagesController.getInstance(account).getUser(MessageObject.getPeerId(owner.from_id)) : null;
        String senderLabel = "";
        long senderId = 0;
        String senderName = "";
        if (from != null) {
            senderId = from.id;
            senderName = ContactsController.formatName(from.first_name, from.last_name);
            if (TextUtils.isEmpty(senderName)) {
                senderName = UserObject.getUserName(from);
            }
            final String username = UserObject.getPublicUsername(from);
            sb.append("from: ").append(senderName).append(" | id ").append(senderId);
            if (!TextUtils.isEmpty(username)) {
                sb.append(" | @").append(username);
            }
            sb.append('\n');
        }
        sb.append("date: ").append(dateText(owner.date)).append('\n');
        if (owner.edit_date != 0) {
            sb.append("edited: ").append(dateText(owner.edit_date)).append('\n');
        }
        if (owner.fwd_from != null) {
            final long fwd = owner.fwd_from.from_id != null ? MessageObject.getPeerId(owner.fwd_from.from_id) : 0;
            sb.append("forwarded: ").append(owner.fwd_from.date != 0 ? dateText(owner.fwd_from.date) : "—");
            if (fwd != 0) {
                sb.append(" | from id ").append(fwd);
            }
            sb.append('\n');
        }
        sb.append(message.isOutOwner() ? "direction: outgoing" : "direction: incoming").append('\n');
        if (message.isOutOwner()) {
            sb.append("read: ").append(message.isUnread() ? "no" : "yes").append('\n');
        }

        final String dump = sb.toString();

        TextView info = new TextView(context);
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        try {
            info.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        } catch (Throwable ignore) {
        }
        info.setTextIsSelectable(true);
        info.setTypeface(android.graphics.Typeface.MONOSPACE);
        info.setText(dump);
        box.addView(info);


        final boolean canAskReaders = message.isOutOwner() && dialogId < 0 && message.getId() > 0 && MessagesController.getInstance(account).getChat(-dialogId) != null;
        if (canAskReaders) {
            TextView readers = new TextView(context);
            readers.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            readers.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            readers.setPadding(0, AndroidUtilities.dp(12), 0, 0);
            readers.setText(MashaConfig.tr("Show who read ↓", "Показать, кто прочитал ↓"));
            readers.setTypeface(AndroidUtilities.bold());
            final LinearLayout list = new LinearLayout(context);
            list.setOrientation(LinearLayout.VERTICAL);
            readers.setOnClickListener(v -> {
                readers.setVisibility(View.GONE);
                loadReaders(fragment, account, dialogId, message.getId(), list);
            });
            box.addView(readers);
            box.addView(list);
        }

        ScrollView scroll = new ScrollView(context);
        scroll.addView(box);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(MashaConfig.tr("Message details", "Детали сообщения"));
        builder.setView(scroll);
        builder.setPositiveButton(LocaleController.getString(R.string.Close), null);
        builder.setNeutralButton(MashaConfig.tr("Copy", "Копировать"), (d, w) -> {
            AndroidUtilities.addToClipboard(dump);
            android.widget.Toast.makeText(context, MashaConfig.tr("Copied", "Скопировано"), android.widget.Toast.LENGTH_SHORT).show();
        });
        fragment.showDialog(builder.create());
    }

    private static void loadReaders(BaseFragment fragment, int account, long dialogId, int mid, LinearLayout list) {
        final TLRPC.TL_messages_getMessageReadParticipants req = new TLRPC.TL_messages_getMessageReadParticipants();
        req.peer = MessagesController.getInstance(account).getInputPeer(dialogId);
        req.msg_id = mid;
        if (req.peer == null) {
            return;
        }
        ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            final Context context = fragment.getParentActivity();
            if (context == null || list == null) {
                return;
            }
            if (error != null) {
                TextView tv = value(context);
                tv.setText(error.text == null ? error.toString() : error.text);
                list.addView(tv);
                return;
            }
            if (!(response instanceof Vector)) {
                TextView tv = value(context);
                tv.setText(MashaConfig.tr("Read receipts are not available for this chat", "В этом чате нельзя узнать, кто прочитал"));
                list.addView(tv);
                return;
            }
            final ArrayList<TLRPC.TL_readParticipantDate> rows = new ArrayList<>();
            for (Object o : ((Vector) response).objects) {
                if (o instanceof TLRPC.TL_readParticipantDate) {
                    rows.add((TLRPC.TL_readParticipantDate) o);
                }
            }
            if (rows.isEmpty()) {
                TextView tv = value(context);
                tv.setText(MashaConfig.tr("Nobody read it yet", "Пока никто не прочитал"));
                list.addView(tv);
                return;
            }
            for (TLRPC.TL_readParticipantDate row : rows) {
                final TLRPC.User user = MessagesController.getInstance(account).getUser(row.user_id);
                String name = user != null ? ContactsController.formatName(user.first_name, user.last_name) : String.valueOf(row.user_id);
                if (TextUtils.isEmpty(name)) {
                    name = String.valueOf(row.user_id);
                }
                TextView tv = label(context);
                tv.setText(name + "  ·  id " + row.user_id);
                list.addView(tv);
                TextView when = value(context);
                when.setText(dateText(row.date));
                list.addView(when);
            }
        }));
    }
}
