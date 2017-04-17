package com.gmobile.sqliteeditor.assistant;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.gmobile.library.base.view.customview.bottomview.BottomTip;
import com.gmobile.library.base.view.customview.bottomview.BottomViewMgr;
import com.gmobile.library.base.view.customview.dialog.FeDialog;
import com.gmobile.library.base.view.operation.slidingupview.SlidingUpDialog;
import com.gmobile.sqliteeditor.R;

/**
 * Created by sg on 2016/11/23.
 */
public class DialogUtils {

    public static void createMsgBottomDialog(Context context, int msgId) {
        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(context);
        View view = View.inflate(context, R.layout.dialog_textview, null);
        TextView textView = (TextView) view.findViewById(R.id.extra_text);
        textView.setText(msgId);
        builder.setView(view);
        BottomViewMgr.showBottomView(context, builder.create(), false);
    }

    public static BottomTip createLoadingDialog(Context context, String msg) {
        BottomTip.Builder tipBuilder = new BottomTip.Builder(context);
        tipBuilder.setMessage(msg).setButtonVisibility(View.GONE);

        return tipBuilder.create();
    }

    public static void createMsgMiddleMsgDialog(Context context, int msgId) {
        FeDialog.Builder builder = new FeDialog.Builder(context);
        builder.setMessage(msgId);
        builder.setNeutralButton(R.string.okey, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.create().show();
    }
}
