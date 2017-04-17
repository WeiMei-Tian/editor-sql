package com.gmobile.sqliteeditor.factory;

import android.content.Context;
import android.os.Bundle;

import com.gmobile.sqliteeditor.model.AppDataModel;
import com.gmobile.sqliteeditor.model.DataModel;
import com.gmobile.sqliteeditor.model.FileDataModel;
import com.gmobile.sqliteeditor.model.HistoryDataModel;
import com.gmobile.sqliteeditor.model.MineDataModel;

/**
 * Created by admin on 2016/11/22.
 */
public class DataFactory {

    private static DataFactory dataFactory;

    private FileDataModel fileDataModel;
    private AppDataModel appDataModel;
    private HistoryDataModel historyDataModel;
    private MineDataModel mineDataModel;

    private DataFactory() {
    }

    public static DataFactory getInstance() {
        if (dataFactory == null) {
            dataFactory = new DataFactory();
        }
        return dataFactory;
    }

    public DataModel createDataModel(Context context, DataType dataType, Bundle args) {

        DataModel dataModel = null;

        switch (dataType) {
            case appData:
                dataModel = createAppDataModel(context, args);
                break;

            case fileData:
                dataModel = createFileDataModel(context, args);
                break;

            case historyData:
                dataModel = createHistoryDataModel(context, args);
                break;

            case mineData:
                dataModel = createMineDataModel(context, args);
                break;

        }

        dataModel.setArgs(args);
        return dataModel;
    }

    private FileDataModel createFileDataModel(Context context, Bundle args) {
        if (fileDataModel == null) {
            fileDataModel = new FileDataModel(context, args);
        }
        return fileDataModel;
    }

    private AppDataModel createAppDataModel(Context context, Bundle args) {
        if (appDataModel == null) {
            appDataModel = new AppDataModel(context, args);
        }
        return appDataModel;
    }

    private HistoryDataModel createHistoryDataModel(Context context, Bundle args) {
        if (historyDataModel == null) {
            historyDataModel = new HistoryDataModel(context, args);
        }
        return historyDataModel;
    }

    private MineDataModel createMineDataModel(Context context, Bundle args) {
        if (mineDataModel == null) {
            mineDataModel = new MineDataModel(context, args);
        }
        return mineDataModel;
    }
}
