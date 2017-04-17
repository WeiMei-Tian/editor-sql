package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;
import com.gmobile.sqliteeditor.constant.SqlConstant;
import com.gmobile.sqliteeditor.model.bean.sqlite.TableDataField;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.gmobile.sqliteeditor.widget.BorderTextView;
import com.gmobile.sqliteeditor.widget.SqlViewTabRow;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/9/20.
 */
public class TabDatasAdapter extends RecyclerView.Adapter<TabDatasAdapter.MyHolder> {

    private Context mContext;
    private List<Integer> mWidthList;
    private int mClickPosition = -1;
    private List<Map<String, TableDataField>> mDatas;
    private List<String> mTitles;
    private int FOOT_VIEW = 0;
    private float scale = 0;
    private List<Integer> heightList = new ArrayList<>();
    private int dataSource;
    private int lastColumnWidth;
    private int columnSize;

    public TabDatasAdapter(List<Integer> widthList, List<String> mTitles, Context context, int dataSource, boolean isVertical) {
        this.mWidthList = widthList;
        this.mContext = context;
        this.mTitles = mTitles;
        this.dataSource = dataSource;
        columnSize = mTitles.size();
        initWidthList(isVertical);
        initHeight();
    }

    private void initHeight() {
        for (int i = 0;i<mWidthList.size();i++){
            heightList.add(FeViewUtils.dpToPx(28));
        }
    }

    private void initWidthList(boolean isVertical) {

        int width;
        int maxWidth = 0;
        int screenWidth;
        if(isVertical){
            screenWidth = FeViewUtils.getScreenWidth(mContext);
        }else {
            screenWidth = FeViewUtils.getScreenHeight(mContext);
        }

        for (int i =0;i<columnSize;i++){

            width = (mWidthList.get(i) + FeViewUtils.dpToPx(32));
            if( width >= 600){
                mWidthList.set(i,600);
            }else {
                mWidthList.set(i, width);
            }

            maxWidth += mWidthList.get(i);

            if(i == (columnSize - 1)){
                if(screenWidth > maxWidth){
                    lastColumnWidth = screenWidth - (maxWidth - mWidthList.get(columnSize - 1));
                    Log.e("width", "没有满屏幕" + "====maxwidth===" + maxWidth + "======" + mWidthList.get(columnSize - 1) + "======" + "=====lastEidth===" + lastColumnWidth);
                    mWidthList.set(columnSize-1,lastColumnWidth);
                }
            }

        }

    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(mContext).inflate(R.layout.sql_tab_view_item, null));
    }

    @Override
    public int getItemViewType(int position) {

        if(position >= mDatas.size()){
            return FOOT_VIEW;
        }
        return 1;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {

        holder.tableRow.setScale(scale);
        if(getItemViewType(position) == FOOT_VIEW){
            holder.tableLayout.removeAllViews();
            holder.tableRow.removeAllViews();
            TextView textView = new TextView(mContext);
            textView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setHeight(FeViewUtils.dpToPx(36));
            holder.tableRow.addView(textView);
            holder.tableLayout.addView(holder.tableRow);
        }else {
            holder.tableLayout.removeAllViews();
            holder.tableLayout.setTag(position);
            holder.tableRow.removeAllViews();
            Map<String, TableDataField> map = mDatas.get(position);
            BorderTextView textView;
            TableDataField tableDataField;
            for (int col = 0; col < mTitles.size(); col++) {
                textView = holder.tableRow.getTextView(col);
                tableDataField = map.get(mTitles.get(col));
                textView.setText(tableDataField.getFieldData());
                holder.tableRow.addView(textView);
            }

            if(dataSource != SqlConstant.TABLE_DATAS_SQL){
                if (position == mClickPosition) {
                    holder.tableLayout.setBackgroundColor(mContext.getResources().getColor(R.color.fab_bg));
                } else {
                    holder.tableLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                }
            }

            ViewGroup viewGroup = (ViewGroup) holder.tableRow.getParent();
            if (viewGroup != null) {
                viewGroup.removeAllViews();
            }
            holder.tableLayout.addView(holder.tableRow);
        }

    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() + 2 : 0;
    }

    public void setDatas(List<Map<String, TableDataField>> datas) {
        this.mDatas = datas;
        notifyDataSetChanged();
    }

    public void clearEditType() {
        mClickPosition = -1;
        notifyDataSetChanged();
    }

    public void setClickPosition(int position){
        mClickPosition = position;
    }

    public void scaleWidth(float scale) {
        this.scale = scale;
        notifyDataSetChanged();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public TableLayout tableLayout;
        public SqlViewTabRow tableRow;

        public MyHolder(View itemView) {
            super(itemView);
            tableLayout = (TableLayout) itemView.findViewById(R.id.sql_tabview_item);
            tableRow = new SqlViewTabRow(mContext, mWidthList,scale,heightList);
            tableLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    mClickPosition = (int) tableLayout.getTag();
                    bundle.putInt(ViewEvent.Keys.SQL_TAB_ITEM_CLICK, mClickPosition);
                    if(dataSource == SqlConstant.TABLE_DATAS_NORMAL){
                        EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.sqlTabDataItemClick, bundle));
                    }else if(dataSource == SqlConstant.TABLE_DATAS_FILTER){
                        EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.sqlTabDataFilterItemClick, bundle));
                    }
                }
            });
        }
    }

}
