package com.gmobile.sqliteeditor.ui.presenter;

import com.gmobile.sqliteeditor.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by admin on 2016/11/24.
 */
public class BaseViewImp<T extends BaseFragment> {

    protected T fragment;

    public BaseViewImp(T fragment) {
        this.fragment = fragment;
        EventBus.getDefault().register(this);
    }

    public void unRegisterEvent(){
        EventBus.getDefault().unregister(this);

    }
}
