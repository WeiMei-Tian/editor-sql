package com.gmobile.sqliteeditor.model;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.gmobile.data.DataFactory;
import com.gmobile.data.FileInfo;
import com.gmobile.data.constant.DataConstant;
import com.gmobile.data.constant.ResultConstant;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.model.bean.AppDataObject;
import com.gmobile.sqliteeditor.orm.dao.model.AppData;
import com.gmobile.sqliteeditor.orm.helper.DbUtils;
import com.gmobile.sqliteeditor.ui.event.AppDataType;
import com.gmobile.sqliteeditor.ui.event.FileDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/11/22.
 */
public class AppDataModel extends DataModel<AppDataObject> {

    private static PackageManager mPm;

    public AppDataModel(Context context, Bundle bundle) {
        super(context, bundle);
        mPm = context.getPackageManager();
    }

    @Override
    public void loadData() {
        AppDataType appDataType = (AppDataType) args.getSerializable(Constant.FILE_DATA_TYPE);
        if (appDataType == null) return;

        switch (appDataType) {
            case gointo:
                int position = args.getInt(Constant.CLICK_POSITION, -1);
                gotoClickPosition(position);
                break;

            case init:
            case goback:
                getAllAppNames();
                break;

            case refreshData:
//                currentPath = args.getString(Constant.BACK_PATH);
//                refreshCurrentPath(currentPath);
                break;
        }
    }


    private void refreshCurrentPath(String currentPath) {
        datas.clear();
    }

    private void gotoClickPosition(int position) {
        String pkgName = datas.get(position).getAppPackageName();
        String dbDirPath = "/data/data/" + pkgName + "/databases";
        Log.e("sg", "root----dir-----" + dbDirPath);

        //TODO 应在主线程做root
        if (Utils.isRoot(context)) {
            FileInfo fileInfo = DataFactory.createObject(DataConstant.MEMORY_DATA_ID, dbDirPath);

            datas.clear();
            if (fileInfo.exists() == ResultConstant.EXIST){
                List<FileInfo> children = fileInfo.getList(false);//去掉隐藏文件
                if (children != null) {
                    datas = listFiles(pkgName, children);
                }
            }
        }
    }

    public List<AppDataObject> listFiles(String pkgName, List<FileInfo> children) {
        List<AppDataObject> objects = new ArrayList<>();
        AppDataObject object;
        for (FileInfo f : children) {
            object = new AppDataObject();
            object.setName(f.getName());
            object.setPath(f.getPath());
            object.setAppPackageName(pkgName);
            object.setLastModified(f.getLastModified());
            object.setSize(f.getSize());
            objects.add(object);
        }

        return objects;
    }

    public void getAllAppNames() {

        List<AppData> bList = DbUtils.getAppDataHelper().getAppList();
        if (bList != null && bList.size() > 0) {
            Log.e("sg", "读取App缓存");
            for (AppData d : bList) {
                datas.add(new AppDataObject(d));
            }
            return;
        }

        Log.e("sg", "现取App数据");
        List<PackageInfo> aiList;
        try {
            aiList = mPm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        AppData appData;
        AppDataObject appDataObject;
        List<AppData> cacheList = new ArrayList<>();
        boolean isSystem;
        for (PackageInfo packageInfo : aiList) {
            isSystem = isSystemApp(packageInfo.applicationInfo);
            if (isSystem){
                continue;
            }

            appData = initApp(packageInfo, isSystem);
            if (appData != null) {
                cacheList.add(appData);
                appDataObject = new AppDataObject(appData);
                datas.add(appDataObject);
            }
        }

        DbUtils.getAppDataHelper().saveOrUpdate(cacheList);
    }

    private AppData initApp(PackageInfo packageInfo, boolean isSystem) {
        AppData appData = new AppData();
        appData.setAppName(packageInfo.applicationInfo.loadLabel(mPm).toString());
        appData.setAppPackageName(packageInfo.packageName);
        appData.setLastModified(packageInfo.firstInstallTime);
        appData.setIsSystem(isSystem ? Constant.SYSTEM_APP : Constant.USER_APP);

        return appData;
    }

    private boolean isSystemApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return false;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return false;
        }
        return true;
    }
}
