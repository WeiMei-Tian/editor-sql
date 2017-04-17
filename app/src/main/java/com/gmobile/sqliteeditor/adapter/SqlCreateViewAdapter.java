package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;
import com.gmobile.sqliteeditor.model.bean.sqlite.NewColumnField;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.gmobile.sqliteeditor.widget.CustomImage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by admin on 2016/9/20.
 */
public class SqlCreateViewAdapter extends RecyclerView.Adapter<SqlCreateViewAdapter.MyHolder> {

    private Context mContext;
    private int mClickPosition = -1;
    private List<NewColumnField> mDates;
    private String[] mTitles;
    private boolean mIsInformation = false;

    public SqlCreateViewAdapter(List<NewColumnField> data, String[] mTitles, Context context, boolean isInformation) {
        this.mContext = context;
        this.mTitles = mTitles;
        this.mDates = data;
        this.mIsInformation = isInformation;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(mContext).inflate(R.layout.sql_tab_view_item, null));
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.tableLayout.removeAllViews();
        holder.tableLayout.setTag(position);
        holder.tableRow.removeAllViews();
        NewColumnField newColumnField = mDates.get(position);
        TextView textView = null;

        for (int i = 0;i<mTitles.length;i++){
            switch (i){
                case 0:
                    textView = new CustomImage(mContext,false,null);
                    textView.setText(newColumnField.getFieldName());
                    break;
                case 1:
                    textView = new CustomImage(mContext,false,null);
                    textView.setText(newColumnField.getFieldType());
                    break;
                case 2:
                    textView = new CustomImage(mContext,newColumnField.isCheck(),null);
                    break;
                case 3:
                    textView = new CustomImage(mContext,newColumnField.isNotNull(),null);
                    break;
                case 4:
                    textView = new CustomImage(mContext,newColumnField.isUnique(),null);
                    break;
                case 5:
                    textView = new CustomImage(mContext,newColumnField.isDefaultKey(),null);
                    break;
                case 6:
                    textView = new CustomImage(mContext,newColumnField.isForeignKey(),null);
                    break;
                case 7:
                    textView = new CustomImage(mContext,false,null);
                    textView.setText(newColumnField.getDefaultValue());
                    break;
                case 8:
                    textView = new CustomImage(mContext,newColumnField.isPrimaryKey(),null);
                    break;
                case 9:
                    textView = new CustomImage(mContext,newColumnField.isAutoIncrement(),null);
                    break;
                case 10:
                    textView = new CustomImage(mContext,false,null);
                    textView.setText(newColumnField.getFkTable());
                    break;
                case 11:
                    textView = new CustomImage(mContext,false,null);
                    textView.setText(newColumnField.getFkField());
                    break;
            }
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setSingleLine(true);
            textView.setTextColor(mContext.getResources().getColor(R.color.table_text_color));
            if( (i>0 && i<= 5) || i>9){
                textView.setWidth(FeViewUtils.dpToPx(85));
            }else {
                textView.setWidth(FeViewUtils.dpToPx(120));
            }
            textView.setPadding(25, 10, 25, 10);
            holder.tableRow.addView(textView);
        }

        if (position == mClickPosition) {
            holder.tableLayout.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
        } else {
            holder.tableLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }

        ViewGroup viewGroup = (ViewGroup) holder.tableRow.getParent();
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        }
        holder.tableLayout.addView(holder.tableRow);
    }

    @Override
    public int getItemCount() {
        return mDates != null ? mDates.size() : 0;
    }

    public void setDatas(List<NewColumnField> datas) {
        this.mDates = datas;
        notifyDataSetChanged();
    }

    public void clearEditType() {
        mClickPosition = -1;
        notifyDataSetChanged();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public TableLayout tableLayout;
        public TableRow tableRow;

        public MyHolder(View itemView) {
            super(itemView);
            tableLayout = (TableLayout) itemView.findViewById(R.id.sql_tabview_item);
            tableRow = new TableRow(mContext);
            tableRow.setMinimumWidth(FeViewUtils.dpToPx(100));
            tableLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mIsInformation){
                        mClickPosition = -1;
                    }else {
                        mClickPosition = (int) tableLayout.getTag();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt(ViewEvent.Keys.SQL_TAB_ITEM_CLICK, mClickPosition);
                    EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.sqlTabItemClick, bundle));
                    notifyDataSetChanged();
                }
            });
        }
    }

}
