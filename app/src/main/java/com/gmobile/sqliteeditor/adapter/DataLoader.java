package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataFactory;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.model.DataModel;

/**
 * Created by admin on 2016/11/22.
 */
public class DataLoader extends AsyncTaskLoader<DataModel> {


    private DataModel dataModel;

    public DataLoader(Context context,Bundle bundle) {
        super(context);
        DataType dataType = (DataType) bundle.getSerializable(Constant.DATA_TYPE_KEY);
        dataModel = DataFactory.getInstance().createDataModel(context,dataType,bundle);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public DataModel loadInBackground() {
        dataModel.loadData();
        return dataModel;
    }
}
