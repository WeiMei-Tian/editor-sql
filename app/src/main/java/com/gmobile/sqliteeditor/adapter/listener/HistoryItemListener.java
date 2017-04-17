package com.gmobile.sqliteeditor.adapter.listener;

import android.text.TextUtils;
import android.view.View;

import com.gmobile.sqliteeditor.adapter.BaseHolder;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.model.bean.HistoryDataObject;
import com.orhanobut.logger.Logger;

/**
 * Created by sg on 2016/11/25.
 */
public class HistoryItemListener extends BaseItemListener {

    public HistoryItemListener(BaseFragment fragment) {
        super(fragment);
    }

    @Override
    protected void onClickImp(BaseHolder v) {
        HistoryDataObject data = (HistoryDataObject) v.getBaseData();
        String path = data.getPath();

        Logger.d("history_click---" + path + " | " + data.getName());
        Utils.openDB(fragment.getActivity(), path, data.getName());
    }

    @Override
    protected void onLongClickImp(View v) {

    }
}
