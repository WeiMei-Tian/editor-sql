package com.gmobile.sqliteeditor.adapter.listener;

import android.view.View;

import com.gmobile.sqliteeditor.adapter.BaseHolder;
import com.gmobile.sqliteeditor.base.BaseFragment;

/**
 * Created by admin on 2016/11/24.
 */
public abstract class BaseItemListener implements View.OnClickListener,View.OnLongClickListener {

    protected BaseFragment fragment;

    public BaseItemListener(BaseFragment fragment) {
        this.fragment = fragment;
    }


    @Override
    public void onClick(View v) {
        BaseHolder holder = (BaseHolder) v.getTag();
        onClickImp(holder);
    }

    @Override
    public boolean onLongClick(View v) {
        onLongClickImp(v);
        return false;
    }

    protected abstract void onClickImp(BaseHolder v);

    protected abstract void onLongClickImp(View v);
}
