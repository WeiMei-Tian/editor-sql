package com.gmobile.sqliteeditor.orm.helper;

import com.gmobile.sqliteeditor.orm.dao.base.DaoSession;
import com.gmobile.sqliteeditor.orm.helper.implement.AppDataHelper;
import com.gmobile.sqliteeditor.orm.helper.implement.HistoryDataHelper;
import com.gmobile.sqliteeditor.orm.helper.implement.MineDataHelper;

/**
 * Created by liucheng on 2015/10/16
 */
public class DbUtils {

    private static AppDataHelper mAppDataHelper;
    private static HistoryDataHelper mHistoryDataHelper;
    private static MineDataHelper mMineDataHelper;

    private static DaoSession session;

    public static AppDataHelper getAppDataHelper() {
        if (mAppDataHelper == null || mAppDataHelper.isNeedReloadDB()) {
            session = DBCore.getDaoSession();
            mAppDataHelper = new AppDataHelper(session.getAppDataDao());
        }
        return mAppDataHelper;
    }

    public static HistoryDataHelper getHistoryDataHelper() {
        if (mHistoryDataHelper == null || mHistoryDataHelper.isNeedReloadDB()) {
            session = DBCore.getDaoSession();
            mHistoryDataHelper = new HistoryDataHelper(session.getHistoryDataDao());
        }
        return mHistoryDataHelper;
    }

    public static MineDataHelper getMineDataHelper() {
        if (mMineDataHelper == null || mMineDataHelper.isNeedReloadDB()) {
            session = DBCore.getDaoSession();
            mMineDataHelper = new MineDataHelper(session.getMineDataDao());
        }
        return mMineDataHelper;
    }


}