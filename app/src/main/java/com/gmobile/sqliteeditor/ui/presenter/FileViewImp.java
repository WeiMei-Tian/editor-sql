package com.gmobile.sqliteeditor.ui.presenter;

import android.os.Bundle;
import android.util.Log;

import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.ui.FileFragment;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by admin on 2016/11/23.
 */
public class FileViewImp extends BaseViewImp<FileFragment>{


    public FileViewImp(FileFragment appFragment) {
        super(appFragment);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOk(ViewEvent viewEvent){

        ViewEvent.EvenType evenType = viewEvent.getType();
        Bundle args = viewEvent.getArgs();
        int position = args.getInt(Constant.CLICK_POSITION);

        switch (evenType){
            case controlFileReFreshLayout:
//                Log.e("FileViewImp","controlFileReFreshLayout");
                fragment.controlReFreshLayout();
                fragment.refreshTopPath(position);
                break;
            case hideTop:
                fragment.hideTop();
                break;
            case gotoFileClickPosition:
//                Log.e("FileViewImp","gotoFileClickPosition");
                fragment.gotoClickPath(position);
                break;
            case backPath:
//                Log.e("FileViewImp","backPath");
                fragment.onBackPressed();
                break;
        }
    }

    public void unRegisterEvent(){
        EventBus.getDefault().unregister(this);
    }
}
