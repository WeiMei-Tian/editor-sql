package com.gmobile.sqliteeditor.orm.helper.implement;

import com.gmobile.sqliteeditor.orm.dao.base.AppDataDao;
import com.gmobile.sqliteeditor.orm.dao.model.AppData;
import com.gmobile.sqliteeditor.orm.helper.BaseHelper;

import java.util.List;

import de.greenrobot.dao.AbstractDao;

/**
 * Created by sg on 2016/11/24.
 */
public class AppDataHelper extends BaseHelper<AppData, Long> {

    public AppDataHelper(AbstractDao dao) {
        super(dao);
    }

    public AppData getApp(String packageName) {
        return queryBuilder().where(AppDataDao.Properties.AppPackageName.eq(packageName)).unique();
    }

    public List<AppData> getAppList() {
        return queryBuilder().list();
    }
}
