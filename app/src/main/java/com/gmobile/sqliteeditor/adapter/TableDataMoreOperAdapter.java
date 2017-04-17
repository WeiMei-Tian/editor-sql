package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;


/**
 * Created by admin on 2016/9/22.
 */
public class TableDataMoreOperAdapter extends RecyclerView.Adapter<TableDataMoreOperAdapter.MenuHolder> implements View.OnClickListener{

    private Context mContext;
    private onMenuClickListener onMenuItemClickListener;

    public void setOnMenuItemClickListener(onMenuClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    private final int mDatas[] = {
            R.string.sql_data_more_sort,
            R.string.sql_data_more_clear_data,
            R.string.sql_data_more_export,
            R.string.sql_data_more_import,
            R.string.sql_data_more_information,
    };

    public TableDataMoreOperAdapter(Context mContext) {
        this.mContext = mContext;
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
        holder.name.setText(mContext.getResources().getString(mDatas[position]));
        holder.img.setVisibility(View.GONE);
        holder.lineLayout.setVisibility(View.GONE);
        holder.itemLayout.setTag(mDatas[position]);
    }

    @Override
    public int getItemCount() {
        return mDatas.length;
    }

    @Override
    public void onClick(View v) {

        if(onMenuItemClickListener != null){
            int strRes = (int) v.getTag();
            onMenuItemClickListener.onMenuCkick(strRes);
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

    public interface onMenuClickListener{

        void onMenuCkick(int strRes);
    }
}
