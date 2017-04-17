package com.gmobile.sqliteeditor.model;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.gmobile.sqliteeditor.model.bean.HistoryDataObject;
import com.gmobile.sqliteeditor.orm.dao.model.AppData;
import com.gmobile.sqliteeditor.orm.dao.model.HistoryData;
import com.gmobile.sqliteeditor.orm.helper.DbUtils;

import java.io.File;
import java.util.List;

/**
 * Created by sg on 2016/11/24.
 */
public class HistoryDataModel extends DataModel<HistoryDataObject> {

    public HistoryDataModel(Context context, Bundle bundle) {
        super(context, bundle);
    }

    @Override
    public void loadData() {
        List<HistoryData> bList = DbUtils.getHistoryDataHelper().getHistoryList();
        for (HistoryData data : bList) {
            File curFile = new File(data.getPath());
            if (curFile.exists()) {
                datas.add(new HistoryDataObject(data));
            } else {
                if (data.getPath().startsWith("/data/data")){
                    continue;
                }

                DbUtils.getHistoryDataHelper().delete(data);
            }
        }
    }
}
