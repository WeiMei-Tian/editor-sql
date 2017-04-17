package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.gmobile.sqliteeditor.adapter.listener.BaseItemListener;
import com.gmobile.sqliteeditor.assistant.FeThumbUtils;
import com.gmobile.sqliteeditor.assistant.ThumbWorkManger;
import com.gmobile.sqliteeditor.cache.ThumbCache;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.model.bean.AppDataObject;
import com.gmobile.sqliteeditor.ui.event.EventBusUtil;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.litesuits.go.SmartExecutor;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by admin on 2016/11/22.
 */
public class AppAdapter extends BaseAdapter<AppDataObject> {

    private PackageManager packageManager;
    private ApplicationInfo applicationInfo;
    private String packageName;

    private int mCurPage = Constant.PAGE_ROOT;

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    public AppAdapter(Context context, boolean hasTop, BaseItemListener baseItemListener) {
        super(context, hasTop, baseItemListener);
        packageManager = context.getPackageManager();
    }

    @Override
    protected void setDataTypeForArgs(Bundle args) {
        args.putSerializable(Constant.DATA_TYPE_KEY, DataType.appData);
    }

    @Override
    protected void bindData(final InnerHolder holder, int position) {
        AppDataObject object = datas.get(position);
        if (TextUtils.isEmpty(object.getPath())){
            holder.fileName.setText(datas.get(position).getAppName());
            holder.fileInfo.setText(datas.get(position).getAppPackageName());
            setAppIcon(holder, position);
        } else {
            holder.fileName.setText(datas.get(position).getName());
            holder.fileInfo.setText(datas.get(position).getAppPackageName());
            setFileIcon(holder, position);
        }

        holder.itemMore.setVisibility(View.VISIBLE);

        holder.appIcon.setTag(object.getAppName());
        holder.setBaseData(datas.get(position));
        holder.setClickPosition(position);
    }

    private void setFileIcon(InnerHolder holder, int position) {
        Bitmap bitmap = FeThumbUtils.getDefaultThumb(context, FeThumbUtils.getMiMeType(datas.get(position).getName()));
        holder.appIcon.setImageBitmap(bitmap);
    }

    private void setAppIcon(final InnerHolder holder, int position){
        packageName = datas.get(position).getAppPackageName();
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
            holder.appIcon.setImageDrawable(appIcon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final String key = packageName + position;
        Bitmap appIconBitMap = ThumbCache.getInstance().getAppIconFromCache(key);

        if (appIconBitMap == null) {
            ThumbWorkManger.getInstance(context).getAppIcon(datas.get(position).getName(),holder.appIcon,key,packageName);
        }else {
            holder.appIcon.setImageBitmap(appIconBitMap);
        }

    }

    @Override
    public void gotoClickPath(int position) {

    }

    @Override
    protected void loadFinished() {
        notifyDataSetChanged();
        Log.e("AppViewImp", "type====App");
        EventBusUtil.controlRefreshLayout(DataType.appData);
    }
}
