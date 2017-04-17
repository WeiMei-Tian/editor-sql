package com.gmobile.sqliteeditor;

import android.app.Application;
import android.support.multidex.MultiDex;

import com.gmobile.data.DataFactory;
import com.gmobile.sqliteeditor.orm.helper.DBCore;

/**
 * Created by sg on 2016/11/24.
 */
public class EditorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);
        DBCore.initDB(this);
        DataFactory.init(this);
    }
}
