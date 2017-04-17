package com.gmobile.sqliteeditor.adapter.listener;

import android.view.View;

import com.gmobile.sqliteeditor.adapter.BaseHolder;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.model.bean.HistoryDataObject;
import com.gmobile.sqliteeditor.model.bean.MineDataObject;
import com.orhanobut.logger.Logger;

/**
 * Created by sg on 2016/11/25.
 */
public class MineItemListener extends BaseItemListener {

    public MineItemListener(BaseFragment fragment) {
        super(fragment);
    }

    @Override
    protected void onClickImp(BaseHolder v) {
        MineDataObject data = (MineDataObject) v.getBaseData();
        String path = data.getPath();

        Logger.d("history_click---" + path + " | " + data.getName());
        Utils.openDB(fragment.getActivity(), path, data.getName());
    }

    @Override
    protected void onLongClickImp(View v) {

    }
}
