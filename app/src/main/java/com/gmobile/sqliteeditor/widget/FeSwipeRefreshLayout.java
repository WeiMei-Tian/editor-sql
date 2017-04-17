package com.gmobile.sqliteeditor.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by liucheng on 2015/12/16.
 */
public class FeSwipeRefreshLayout extends SwipeRefreshLayout {

    public FeSwipeRefreshLayout(Context context) {
        super(context);
    }

    public FeSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
