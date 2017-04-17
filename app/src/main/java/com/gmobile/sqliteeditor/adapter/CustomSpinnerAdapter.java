package com.gmobile.sqliteeditor.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;

import java.util.List;


/**
 * Created by admin on 2016/8/1.
 */
public class CustomSpinnerAdapter implements SpinnerAdapter {

    private List<String> mDataList;
    private Context mContext;
    private boolean animClock = false;

    public CustomSpinnerAdapter(List<String> list, Context context) {
        this.mDataList = list;
        this.mContext = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_spinner_drop, null);
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_compress_drop_txt);
        textView.setText(mDataList.get(position));
        if(!animClock){
            FeViewUtils.listItemUpAnimSlow(convertView, position, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animClock = true;
                }
            });
        }
        return convertView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InnerViewHolder innerViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_spinner_compress, null);
            innerViewHolder = new InnerViewHolder(convertView);
            convertView.setTag(innerViewHolder);
        } else {
            innerViewHolder = (InnerViewHolder) convertView.getTag();
        }
        innerViewHolder.getTxt().setText(mDataList.get(position));
//        FeViewUtils.listItemUpAnimSlow(innerViewHolder.getItemView(), position, null);

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private static class InnerViewHolder {

        private TextView txt;
        private View itemView;

        public InnerViewHolder(View itemView) {
            this.itemView = itemView;
            txt = (TextView) itemView.findViewById(R.id.spinner_compress_txt);
        }

        private TextView getTxt() {
            return txt;
        }

        private View getItemView() {
            return itemView;
        }

    }

}
