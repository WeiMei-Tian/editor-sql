package com.gmobile.sqliteeditor.ui.event;

import android.os.Bundle;

import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by sg on 2016/11/28.
 */
public class EventBusUtil {

    public static void controlRefreshLayout(DataType type){
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.DATA_TYPE_KEY, type);
        EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.controlAppReFreshLayout, bundle));
    }

}
