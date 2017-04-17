package com.gmobile.sqliteeditor.model;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.gmobile.library.base.assistant.utils.FileUtils;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.model.bean.FileData;
import com.gmobile.sqliteeditor.ui.event.FileDataType;

import java.io.File;
import java.util.Collections;

/**
 * Created by admin on 2016/11/22.
 */
public class FileDataModel extends DataModel<FileData> {

    private int position;
    private String currentPath;
    FileData fileData;

    public FileDataModel(Context context,Bundle bundle) {
        super(context,bundle);
    }

    @Override
    public void loadData() {

        FileDataType fileDataType = (FileDataType) args.getSerializable(Constant.FILE_DATA_TYPE);
        switch (fileDataType){
            case init:
                getSdCardFileInfo();
                break;
            case gotoPath:
                position = args.getInt(Constant.CLICK_POSITION,-1);
                gotoClickPosition(position);
                break;
            case backPath:
                currentPath = args.getString(Constant.BACK_PATH);
                backToPath(currentPath);
                break;
            case refresh:
                currentPath = args.getString(Constant.BACK_PATH);
                refreshCurrentPath(currentPath);
                break;
        }

    }

    private void refreshCurrentPath(String currentPath) {
        datas.clear();
        listFile(currentPath);
    }

    private void backToPath(String currentPath) {
        Log.e("backToPath","=="+currentPath);
        datas.clear();
        getParentFiles(currentPath);
    }

    private void getParentFiles(String currentPath) {

        File file = new File(currentPath);
        String parentPath = FileUtils.getParentPath(file.getAbsolutePath());
        listFile(parentPath);
    }

    private void gotoClickPosition(int position) {
        if(datas.size() > 0){
            currentPath = datas.get(position).getPath();
            datas.clear();
            listFile(currentPath);
        }
    }

    private void getSdCardFileInfo() {
        datas.clear();
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            listFile(currentPath);
        }
    }

    private void listFile(String path){
        File file = new File(path);
        File[] files = file.listFiles();

        if (files == null) return;

        for (File file1 : files) {
            file = file1;
            fileData = new FileData();
            fileData = new FileData();
            fileData.setName(file.getName());
            fileData.setPath(file.getAbsolutePath());
            fileData.setIsFolder(file.isDirectory());
            fileData.setLastModified(file.lastModified());
            datas.add(fileData);
        }
        Collections.sort(datas,new DataComparator());
    }
}
