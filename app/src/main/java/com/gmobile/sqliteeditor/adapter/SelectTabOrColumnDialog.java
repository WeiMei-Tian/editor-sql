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

import java.util.List;


/**
 * Created by admin on 2016/9/22.
 */
public class SelectTabOrColumnDialog extends RecyclerView.Adapter<SelectTabOrColumnDialog.MenuHolder> implements View.OnClickListener{

    private Context mContext;
    private List<String> mDatas;
    private MyListener myListener;

    public void setMyListener(MyListener myListener) {
        this.myListener = myListener;
    }

    public interface MyListener{
        void click(int position);
    }

    public SelectTabOrColumnDialog(Context mContext, List<String> columnFields) {
        this.mContext = mContext;
        this.mDatas = columnFields;
    }

    @Override
    public MenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MenuHolder menuHolder;
        RelativeLayout itemLayout;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        itemLayout = (RelativeLayout) inflater.inflate(R.layout.table_add_column_select_table, null);
        menuHolder = new MenuHolder(itemLayout, this);
        return menuHolder;
    }

    @Override
    public void onBindViewHolder(MenuHolder holder, int position) {

        holder.name.setText(mDatas.get(position));
        holder.img.setVisibility(View.GONE);
        holder.itemLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onClick(View v) {

        if(myListener != null){
            myListener.click((Integer) v.getTag());
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
        }
    }
}
