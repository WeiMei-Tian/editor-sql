package com.gmobile.sqliteeditor.ui.presenter;

import android.os.Bundle;
import android.util.Log;

import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.ui.AppFragment;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by admin on 2016/11/23.
 */
public class AppViewImp extends BaseViewImp<AppFragment> {

    private DataType mDataType;

    public AppViewImp(AppFragment appFragment, DataType dataType) {
        super(appFragment);
        mDataType = dataType;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventOk(ViewEvent viewEvent) {

        ViewEvent.EvenType evenType = viewEvent.getType();
        Bundle args = viewEvent.getArgs();
        DataType dataType = (DataType) args.getSerializable(Constant.DATA_TYPE_KEY);

        switch (evenType) {
            case controlAppReFreshLayout:
                controlRefreshLayout(dataType);
                break;

            case hideTop:
                hideTop();
                break;

            case gotoAppClickPosition:
                if (dataType != mDataType) {
                    return;
                }
                fragment.gotoClickPath(args.getInt(Constant.CLICK_POSITION));
                break;
        }
    }

    private void controlRefreshLayout(DataType dataType) {
        Log.e("AppViewImp", "type====" + dataType);
        fragment.controlReFreshLayout();

        switch (dataType) {
            case appData:

                break;

            case historyData:

                break;

            case mineData:

                break;
        }
    }

    private void hideTop(){
        fragment.hideTop();
    }

}
