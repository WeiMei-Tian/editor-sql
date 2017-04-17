package com.gmobile.sqliteeditor.ui.view;


/**
 * Created by admin on 2016/11/23.
 */
public interface FileView {

    void controlReFreshLayout();

    void hideTop();

    void gotoClickPath(int position);

    void refreshTopPath(int position);
}
