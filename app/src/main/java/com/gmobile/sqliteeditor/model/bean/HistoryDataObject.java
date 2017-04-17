package com.gmobile.sqliteeditor.model.bean;

import com.gmobile.sqliteeditor.orm.dao.model.HistoryData;

/**
 * Created by sg on 2016/11/25.
 */
public class HistoryDataObject extends BaseData {

    HistoryData historyData;

    public HistoryDataObject(HistoryData historyData) {
        this.historyData = historyData;
    }

    @Override
    public String getName() {
        return historyData.getName();
    }

    @Override
    public String getAppName() {
        return historyData.getAppName();
    }

    @Override
    public String getAppPackageName() {
        return historyData.getAppPackageName();
    }

    @Override
    public String getPath() {
        return historyData.getPath();
    }

    @Override
    public Long getSize() {
        return historyData.getSize();
    }

    @Override
    public Long getLastModified() {
        return historyData.getLastModified();
    }

    @Override
    public Long getOpenTime() {
        return historyData.getOpenTime();
    }

    @Override
    public void setName(String name) {

    }


    @Override
    public void setPath(String path) {

    }


    @Override
    public void setAppName(String appName) {

    }


    @Override
    public void setAppPackageName(String appPackageName) {

    }


    @Override
    public void setSize(Long size) {

    }


    @Override
    public void setLastModified(Long lastModified) {

    }


    @Override
    public void setOpenTime(Long openTime) {

    }
}
