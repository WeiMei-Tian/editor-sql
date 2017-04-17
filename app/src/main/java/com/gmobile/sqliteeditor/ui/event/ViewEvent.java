package com.gmobile.sqliteeditor.ui.event;

import android.os.Bundle;

/**
 * Created by admin on 2016/11/23.
 */
public class ViewEvent {

    private EvenType mType;
    private Bundle mArgs;

    public ViewEvent(EvenType type, Bundle args) {
        this.mType = type;
        this.mArgs = args;
    }

    public EvenType getType() {
        return mType;
    }

    public void setType(EvenType type) {
        this.mType = type;
    }

    public Bundle getArgs() {
        return mArgs;
    }

    public void setArgs(Bundle args) {
        this.mArgs = args;
    }

    /**
     * 更新界面事件类型.
     * KeyCodeBack  back
     * updateToolbarFileInfo  更新toolbar内容
     * SetItemThumb           设置缩略图
     * updateTabTitle         tab title 更新
     */
    public enum EvenType {
        controlAppReFreshLayout,
        controlFileReFreshLayout,
        hideTop,
        gotoFileClickPosition,
        backPath,
        gotoAppClickPosition,
        sqlTabItemClick,
        controlAdMob,
        controlSqlPayTip,
        sqlTabDataItemClick,
        sqlTabDataFilterItemClick,
        refreshTable,
        hideLoading,
        refreshTableData,
        sqlSortData,
        isEditTableItem
    }

    /**
     * 更新界面参数的关键字.
     */
    public static class Keys {
        public static final String SQL_TAB_ITEM_CLICK = "sql_item_click";
        public static final String ACTION = "action";
        public static final String REFRESH_TYPE = "refresh_type";
        public static final String SQL_SORT = "sql_sort";
        public static final String SQL_SORT_DESC_ASC = "sql_sort_desc_asc";

    }

}
