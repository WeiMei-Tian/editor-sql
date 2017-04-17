package com.gmobile.sqliteeditor.model;

import android.content.Context;
import android.os.Bundle;

import com.gmobile.sqliteeditor.model.bean.MineDataObject;
import com.gmobile.sqliteeditor.orm.dao.model.HistoryData;
import com.gmobile.sqliteeditor.orm.dao.model.MineData;
import com.gmobile.sqliteeditor.orm.helper.DbUtils;

import java.io.File;
import java.util.List;

/**
 * Created by sg on 2016/11/24.
 */
public class MineDataModel extends DataModel<MineDataObject> {

    public MineDataModel(Context context, Bundle bundle) {
        super(context, bundle);
    }

    @Override
    public void loadData() {
        List<MineData> bList = DbUtils.getMineDataHelper().getMineList();
        for (MineData data : bList) {
            File curFile = new File(data.getPath());
            if (curFile.exists()) {
                datas.add(new MineDataObject(data));
            } else {
                DbUtils.getMineDataHelper().delete(data);
            }
        }
    }
}
