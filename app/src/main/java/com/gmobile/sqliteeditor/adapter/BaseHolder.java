package com.gmobile.sqliteeditor.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gmobile.sqliteeditor.model.bean.BaseData;

/**
 * Created by admin on 2016/11/23.
 */
public class BaseHolder<T extends BaseData> extends RecyclerView.ViewHolder {

    protected T baseData;
    protected int position;

    public BaseHolder(View itemView) {
        super(itemView);
    }

    public int getClickPosition() {
        return position;
    }

    public void setClickPosition(int position) {
        this.position = position;
    }

    public T getBaseData() {
        return baseData;
    }

    public void setBaseData(T baseData) {
        this.baseData = baseData;
    }
}
