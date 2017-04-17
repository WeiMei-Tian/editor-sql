package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.model.bean.sqlite.ColumnField;
import com.gmobile.sqliteeditor.ui.activity.SqlTabDatasActivity;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


/**
 * Created by admin on 2016/9/22.
 */
public class SortTypeAdapter extends RecyclerView.Adapter<SortTypeAdapter.MenuHolder> implements View.OnClickListener {

    private Context mContext;
    private List<ColumnField> mDatas;
    private int index;
    private String asStr,desStr;
    private String txt;

    public SortTypeAdapter(Context mContext, List<ColumnField> columnFields) {
        this.mContext = mContext;
        this.mDatas = columnFields;
        asStr = mContext.getResources().getString(R.string.table_datas_as);
        desStr = mContext.getResources().getString(R.string.table_datas_desc);
    }

    @Override
    public MenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MenuHolder menuHolder;
        RelativeLayout itemLayout;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        itemLayout = (RelativeLayout) inflater.inflate(R.layout.common_menu_item, null);
        menuHolder = new MenuHolder(itemLayout, this);
        return menuHolder;
    }

    @Override
    public void onBindViewHolder(MenuHolder holder, int position) {

        index = position / 2;

        if (position % 2 == 0) {
            txt = asStr.replace("&",mDatas.get(index).getFieldName());
            holder.name.setText(txt);
            holder.lineLayout.setVisibility(View.VISIBLE);
            holder.itemLayout.setTag(mDatas.get(index).getFieldName() + 1);
        } else {
            txt = desStr.replace("&",mDatas.get(index).getFieldName());
            holder.name.setText(txt);
            holder.lineLayout.setVisibility(View.GONE);
            holder.itemLayout.setTag(mDatas.get(index).getFieldName() + 0);
        }

//        if(PreferenceUtils.getPrefString(mContext,SqlTabDatasActivity.SORT_TYPE,
//                mContext.getResources().getString(mDatas[0])).equals(mContext.getResources().getString(mDatas[position]))){
//            holder.img.setVisibility(View.VISIBLE);
//        }else {
        holder.img.setVisibility(View.GONE);
//        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size() > 0 ? mDatas.size() * 2 : 0;
    }

    @Override
    public void onClick(View v) {

        Bundle bundle = new Bundle();
        String columName = (String) v.getTag();


//        PreferenceUtils.setPrefString(mContext, SqlTabDatasActivity.SORT_TYPE, mContext.getResources().getString(mDatas[position]));
        notifyDataSetChanged();
        boolean isDesc = columName.endsWith("0");
        columName = columName.substring(0, columName.length() - 1);
        bundle.putString(ViewEvent.Keys.SQL_SORT, columName);
        bundle.putBoolean(ViewEvent.Keys.SQL_SORT_DESC_ASC, isDesc);
        EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.sqlSortData, bundle));
        if (((SqlTabDatasActivity) mContext).getSortDialog() != null) {
            ((SqlTabDatasActivity) mContext).getSortDialog().dismiss();
        }

    }

    static class MenuHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView img;
        private RelativeLayout itemLayout;
        private RelativeLayout lineLayout;


        public MenuHolder(View itemView, View.OnClickListener onClickListener) {
            super(itemView);
            this.lineLayout = (RelativeLayout) itemView.findViewById(R.id.dividingLine_layout);
            this.itemLayout = (RelativeLayout) itemView.findViewById(R.id.menu_item);
            this.name = (TextView) itemView.findViewById(R.id.menu_item_name);
            this.img = (ImageView) itemView.findViewById(R.id.menu_checked);

            itemLayout.setOnClickListener(onClickListener);
            itemLayout.setTag(this);
        }
    }
}
