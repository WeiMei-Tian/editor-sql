package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.listener.BaseItemListener;
import com.gmobile.sqliteeditor.cache.ThumbCache;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.model.bean.MineDataObject;
import com.gmobile.sqliteeditor.orm.dao.model.AppData;
import com.gmobile.sqliteeditor.orm.dao.model.MineData;
import com.gmobile.sqliteeditor.ui.event.EventBusUtil;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by admin on 2016/11/22.
 */
public class MineAdapter extends BaseAdapter<MineDataObject> {

    public MineAdapter(Context context, boolean hasTop, BaseItemListener baseItemListener) {
        super(context, hasTop, baseItemListener);
    }

    @Override
    protected void loadFinished() {
        notifyDataSetChanged();
        Log.e("AppViewImp", "type====mine");
        EventBusUtil.controlRefreshLayout(DataType.mineData);
    }

    @Override
    protected void setDataTypeForArgs(Bundle args) {
        args.putSerializable(Constant.DATA_TYPE_KEY, DataType.mineData);
    }

    @Override
    protected void bindData(final InnerHolder holder, int position) {
        holder.appIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sqleditor_store));
        holder.fileName.setText(datas.get(position).getName());
        holder.fileInfo.setText(datas.get(position).getPath());
        holder.itemMore.setVisibility(View.VISIBLE);
    }

    @Override
    public void gotoClickPath(int position) {

    }

}
