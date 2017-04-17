package com.gmobile.sqliteeditor.adapter.listener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gmobile.library.base.view.operation.ToastManger;
import com.gmobile.sqliteeditor.adapter.BaseHolder;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLManager;
import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.model.bean.FileData;
import com.gmobile.sqliteeditor.ui.activity.DbTablesActivity;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by admin on 2016/11/24.
 */
public class FileItemListener extends BaseItemListener {

    public FileItemListener(BaseFragment fragment) {
        super(fragment);
    }

    @Override
    protected void onClickImp(BaseHolder holder) {

        FileData fileData = (FileData) holder.getBaseData();
        int position = holder.getClickPosition();
        Bundle bundle = new Bundle();
        if(position == 0){
            EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.backPath,bundle));
        }else if(position == -1){
            return;
        }else {
            if(fileData.isFolder()){
                bundle.putInt(Constant.CLICK_POSITION,position);
                EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.gotoFileClickPosition,bundle));
            }else {
                Log.e("FileItemListener","====file===name=="+fileData.getName());
                Utils.openDB(fragment.getActivity(),fileData.getPath(),fileData.getName());
            }
        }

    }

    @Override
    protected void onLongClickImp(View v) {

    }
}
