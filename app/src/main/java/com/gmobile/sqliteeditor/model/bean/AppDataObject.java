package com.gmobile.sqliteeditor.model.bean;

import com.gmobile.data.FileInfo;
import com.gmobile.sqliteeditor.orm.dao.model.AppData;

/**
 * Created by sg on 2016/11/25.
 */
public class AppDataObject extends BaseData {

    AppData appData;

    private String name;
    private String path;

    public AppDataObject() {
    }

    public AppDataObject(AppData appData) {
        this.appData = appData;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getAppPackageName() {
        if (appData == null) return null;

        return appData.getAppPackageName();
    }

    @Override
    public Long getLastModified() {
        if (appData == null) return (long)0;

        return appData.getLastModified();
    }

    public void setIsSystem(Integer isSystem) {
        if (appData == null) return;

        appData.setIsSystem(isSystem);
    }

    public Integer isSystem() {
        if (appData == null) return 0;

        return appData.getIsSystem();
    }

    public Long getId() {
        if (appData == null) return (long)0;

        return appData.getId();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getAppName() {
        if (appData == null) return null;
        return appData.getAppName();
    }

    @Override
    public void setAppName(String appName) {

    }

    @Override
    public void setAppPackageName(String appPackageName) {

    }

    @Override
    public Long getSize() {
        return null;
    }

    @Override
    public void setSize(Long size) {

    }


    @Override
    public void setLastModified(Long lastModified) {
    }

    @Override
    public Long getOpenTime() {
        return null;
    }

    @Override
    public void setOpenTime(Long openTime) {

    }


}
