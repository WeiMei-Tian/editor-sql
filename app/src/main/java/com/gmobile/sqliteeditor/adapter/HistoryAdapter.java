package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.listener.BaseItemListener;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.model.bean.HistoryDataObject;
import com.gmobile.sqliteeditor.ui.event.EventBusUtil;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by admin on 2016/11/22.
 */
public class HistoryAdapter extends BaseAdapter<HistoryDataObject>{

    public HistoryAdapter(Context context, boolean hasTop, BaseItemListener baseItemListener) {
        super(context, hasTop, baseItemListener);
    }

    @Override
    protected void setDataTypeForArgs(Bundle args) {
        args.putSerializable(Constant.DATA_TYPE_KEY, DataType.historyData);
    }

    @Override
    protected void bindData(final InnerHolder holder, int position) {
        holder.appIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sqleditor_store));
        holder.fileName.setText(datas.get(position).getName());
        if (!TextUtils.isEmpty(datas.get(position).getAppPackageName())){
            holder.fileInfo.setText(datas.get(position).getAppPackageName());
        } else {
            holder.fileInfo.setText(datas.get(position).getPath());
        }
    }

    @Override
    public void gotoClickPath(int position) {

    }

    @Override
    protected void loadFinished() {
        notifyDataSetChanged();
        Log.e("AppViewImp", "type====history");
        EventBusUtil.controlRefreshLayout(DataType.historyData);
    }

}
