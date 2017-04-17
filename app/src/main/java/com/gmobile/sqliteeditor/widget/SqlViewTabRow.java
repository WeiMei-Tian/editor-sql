package com.gmobile.sqliteeditor.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/9/20.
 */
public class SqlViewTabRow extends TableRow {

    private List<BorderTextView> mTextViewList = new ArrayList<>();
    private Context mContext;
    private List<Integer> widthList;
    private List<Integer> heightList;
    private float scale = 0;


    public SqlViewTabRow(Context context, List<Integer> widthList, float scale, List<Integer> heightList) {
        super(context);
        this.mContext = context;
        this.scale = scale;
        this.widthList = widthList;
        this.heightList = heightList;
    }

    public BorderTextView getTextView(int position){

        BorderTextView textView;
        if (position < mTextViewList.size()) {
            textView = mTextViewList.get(position);
        } else {
            textView = (BorderTextView) LayoutInflater.from(mContext).inflate(R.layout.sql_table_ceil_item,null);
            textView.setTextSize(14);
            textView.setWidth(widthList.get(position));
            textView.setHeight(heightList.get(position));
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setPadding(FeViewUtils.dpToPx(12), 10,FeViewUtils.dpToPx(12), 10);//此处设置padding会挤压测量好的text显示，所以在设置宽度的时候要在测量的基础上预留上padding空间
            mTextViewList.add(textView);
        }

        ViewGroup viewGroup = (ViewGroup) textView.getParent();
        if(viewGroup != null){
            viewGroup.removeView(textView);
        }
        return textView;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    public void setScale(float scale){

        if(this.scale != scale){
            this.scale = scale;
            int width;
            TextView textView;
            for (int i = 0;i < mTextViewList.size();i++){
                textView = mTextViewList.get(i);
                width = textView.getWidth();
                textView.setWidth((int) (width * (1 + scale)));
                widthList.set(i, (int) (width * (1 + scale)));
            }
        }
    }

}
