package com.gmobile.sqliteeditor.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/**
 * Created by admin on 2016/11/23.
 */
public class ThumbCache {

    private static ThumbCache thumbCache;

    private final Object lock = new Object();
    private LruCache<String, Bitmap> mIconMemoryCache;

    private static final int ICON_MEM_SIZE = 4 * 1024 * 1024;
    private static final int SOFT_CACHE_CAPACITY = 40;


    public static ThumbCache getInstance(){
        if(thumbCache == null){
            thumbCache = new ThumbCache();
        }
        return thumbCache;
    }

    public ThumbCache() {
        createAppIconCache();
    }

    private static final LinkedHashMap<String, SoftReference<Bitmap>> sSoftIconCache =
            new LinkedHashMap<String, SoftReference<Bitmap>>(SOFT_CACHE_CAPACITY, 0.75f, true) {
                @Override
                public SoftReference<Bitmap> put(String key, SoftReference<Bitmap> value) {
                    return super.put(key, value);
                }

                @Override
                protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
                    return size() > SOFT_CACHE_CAPACITY;
                }
            };


    private void createAppIconCache() {
        this.mIconMemoryCache = new LruCache<String, Bitmap>(ICON_MEM_SIZE) {
            @Override
            public int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                //硬引用缓存区满，将一个最不经常使用的oldvalue推入到软引用缓存区
                sSoftIconCache.put(key, new SoftReference<>(oldValue));
            }
        };
    }

    public Bitmap getAppIconFromCache(String key){
        synchronized (lock) {
            final Bitmap bitmap = mIconMemoryCache.get(key);
            if (bitmap != null)
                return bitmap;
        }
        //硬引用缓存区间中读取失败，从软引用缓存区间读取
        synchronized (sSoftIconCache) {
            SoftReference<Bitmap> bitmapReference = sSoftIconCache.get(key);
            if (bitmapReference != null) {
                final Bitmap bitmap2 = bitmapReference.get();
                if (bitmap2 != null)
                    return bitmap2;
                else {
                    sSoftIconCache.remove(key);
                }
            }
        }
        return null;
    }

    public boolean putAppIconCache(String key,Bitmap bitmap){
        if(bitmap != null){
            synchronized (lock){
                mIconMemoryCache.put(key,bitmap);
            }
            return true;
        }
        return false;
    }
}
