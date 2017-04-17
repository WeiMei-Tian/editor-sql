package com.gmobile.sqliteeditor.model.bean;

import com.gmobile.sqliteeditor.orm.dao.model.MineData;

/**
 * Created by sg on 2016/11/25.
 */
public class MineDataObject extends BaseData {

    MineData mineData;

    public MineDataObject(MineData mineData) {
        this.mineData = mineData;
    }

    @Override
    public String getName() {
        return mineData.getName();
    }

    @Override
    public String getPath() {
        return mineData.getPath();
    }

    @Override
    public Long getSize() {
        return mineData.getSize();
    }

    @Override
    public Long getLastModified() {
        return mineData.getLastModified();
    }

    @Override
    public Long getOpenTime() {
        return mineData.getOpenTime();
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public String getAppName() {
        return null;
    }

    @Override
    public void setAppName(String appName) {

    }

    @Override
    public String getAppPackageName() {
        return null;
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
