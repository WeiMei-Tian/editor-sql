package com.gmobile.sqliteeditor.assistant;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.gmobile.library.base.assistant.utils.TimeUtils;
import com.gmobile.sqliteeditor.cache.ThumbCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by liucheng on 2015/11/2.
 * <p/>
 * 管理文件上所有图标
 */
public class ThumbWorkManger {

    private static ThumbWorkManger instance;
    private Context context;
    private PackageManager packageManager;

    private ExecutorService mThumbPool = null;

    public static ThumbWorkManger getInstance(Context context) {
        if (instance == null) {
            instance = new ThumbWorkManger(context);
        }
        return instance;
    }

    private ThumbWorkManger(Context context) {
        this.mThumbPool = new ThreadPoolExecutor(5, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new PriorityBlockingQueue<Runnable>());
        this.context = context;
        packageManager = context.getPackageManager();
    }

    private static class BaseThumbThread implements Comparable<BaseThumbThread> {

        private long mTimeFlag;

        public BaseThumbThread() {
            this.mTimeFlag = TimeUtils.getCurrentTime();
        }

        @Override
        public int compareTo(@NonNull BaseThumbThread another) {
            return (int) another.getTimeFlag() - (int) mTimeFlag;
        }

        public long getTimeFlag() {
            return mTimeFlag;
        }
    }

    public void getAppIcon(String name, ImageView appIcon,String key,String packageName) {
        mThumbPool.execute(new IconThread(name, appIcon,key,packageName));
    }

    private class IconThread extends BaseThumbThread implements Runnable {

        private String mName;
        private ImageView mAppIcon;
        private String key;
        private ApplicationInfo applicationInfo;
        private String packageName;

        public IconThread(String name, ImageView appIcon,String key,String packageName) {
            super();
            this.mName = name;
            this.mAppIcon = appIcon;
            this.key = key;
            this.packageName = packageName;
        }

        @Override
        public void run() {
            Observable.just(mAppIcon)
                    .map(new Func1<ImageView, Bitmap>() {
                        @Override
                        public Bitmap call(ImageView imageView) {
                            if (imageView != null) {
                                try {
                                    applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                                    Bitmap bm;
                                    Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
                                    bm = ((BitmapDrawable) appIcon).getBitmap();
                                    if(bm != null){
                                        return bm;
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    })
                    .filter(new Func1<Bitmap, Boolean>() {
                        @Override
                        public Boolean call(Bitmap bitmap) {
                            return bitmap != null &&
                                    mAppIcon.getTag().equals(mName);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Bitmap>() {
                        @Override
                        public void call(Bitmap bitmap) {
                            ThumbCache.getInstance().putAppIconCache(key, bitmap);
                            mAppIcon.setImageBitmap(bitmap);
                            mAppIcon.setVisibility(View.VISIBLE);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });
        }
    }

}
