package com.gmobile.sqliteeditor.orm.helper.implement;

import com.gmobile.sqliteeditor.orm.dao.base.HistoryDataDao;
import com.gmobile.sqliteeditor.orm.dao.model.HistoryData;
import com.gmobile.sqliteeditor.orm.helper.BaseHelper;

import java.util.List;

import de.greenrobot.dao.AbstractDao;

/**
 * Created by sg on 2016/11/24.
 */
public class HistoryDataHelper extends BaseHelper<HistoryData, Long> {

    public HistoryDataHelper(AbstractDao dao) {
        super(dao);
    }

    public HistoryData getHistory(String path) {
        return queryBuilder().where(HistoryDataDao.Properties.Path.eq(path)).unique();
    }

    public List<HistoryData> getHistoryList() {
        return queryBuilder().list();
    }
}
