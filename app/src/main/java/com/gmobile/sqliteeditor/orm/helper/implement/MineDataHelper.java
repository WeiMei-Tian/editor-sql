package com.gmobile.sqliteeditor.orm.helper.implement;

import com.gmobile.sqliteeditor.orm.dao.base.MineDataDao;
import com.gmobile.sqliteeditor.orm.dao.model.MineData;
import com.gmobile.sqliteeditor.orm.helper.BaseHelper;

import java.util.List;

import de.greenrobot.dao.AbstractDao;

/**
 * Created by liucheng on 2016/11/23.
 */
public class MineDataHelper extends BaseHelper<MineData, Long> {

    public MineDataHelper(AbstractDao dao) {
        super(dao);
    }


    public MineData getMine(String path) {
        return queryBuilder().where(MineDataDao.Properties.Path.eq(path)).unique();
    }

    public List<MineData> getMineList() {
        return queryBuilder().list();
    }


}