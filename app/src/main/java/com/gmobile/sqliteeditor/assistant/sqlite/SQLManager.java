package com.gmobile.sqliteeditor.assistant.sqlite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import com.gmobile.library.base.assistant.utils.PreferenceUtils;
import com.gmobile.sqliteeditor.constant.Constant;

import java.io.File;

/**
 * Created by sg on 2016/10/19.
 */
public class SQLManager {

    private static SQLHelper SQLHelper;

    private SQLManager() {
    }

    public static SQLHelper getSQLHelper(Context context) {
        if (SQLHelper == null) {
            SQLHelper = new SQLHelper(PreferenceUtils.getPrefString(context, Constant.SQL_PATH, ""), new MyDbErrorHandler());
        }

        return SQLHelper;
    }

    public static SQLHelper getSQLHelper() throws NoSuchFieldException {
        if (SQLHelper == null){
            throw new NoSuchFieldException("SQLHELPER HAS NOT INIT");
        }
        return SQLHelper;
    }

    public static int createDatabase(String dir, String fileName) {
        try {
            SQLiteDatabase.openOrCreateDatabase(dir + File.separator + fileName, null).close();
            return Constant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Constant.FAIL;
    }


    static class MyDbErrorHandler implements DatabaseErrorHandler {

        @Override
        public void onCorruption(SQLiteDatabase dbObj) {

        }
    }

    public static void destroyManager() {
        if (SQLHelper != null) {
            SQLHelper.close();
            SQLHelper = null;
        }
    }

}
