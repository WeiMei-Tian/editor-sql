package com.gmobile.sqliteeditor.constant;

/**
 * Created by admin on 2016/11/23.
 */
public class Constant {

    public static final String DATA_TYPE_KEY = "data_type_key";
    public static final String HAS_TOP = "has_top";

    public static final int SYSTEM_APP = 1;
    public static final int USER_APP = 2;

    public static final String CLICK_POSITION = "click_position";
    public static final String BACK_PATH = "back_path";
    public static final String FILE_DATA_TYPE = "file_data_type";
    public static final String START_PATH = "start_path";
    public static final String OPERATION_TITLE = "operation_title";
    public static final String CONFIRM_PATH = "confirm_path";

    public static final String SQL_PATH = "sqlpath";

    public static final String PRIMARY_VALUE = "PRIMARY";
    public static final String KEY_VALUE = "KEY";
    public static final String PRIMARY_KEY_VALUE = "PRIMARY KEY";
    public static final String NOT_NULL_VALUE = "NOT NULL";
    public static final String NOT_VALUE = "NOT";
    public static final String NULL_VALUE = "NULL";
    public static final String UNIQUE_KEY_VALUE = "UNIQUE";
    public static final String DEFAULT_VALUE = "DEFAULT";
    public static final String FOREIGN_KEY_VALUE = "REFERENCES";

    public static final String AUTOINCREMENT_VALUE = "AUTOINCREMENT";

    //Result
    public static final int SUCCESS = 1;
    public static final int FAIL_DELETE_COLUMN_EMPTY = 2;//此表只有这一个字段，古不能删除
    public static final int FAIL = 3;
    public static final int FAIL_EDIT_COLUMN_FK = 4;//此字段是别的表的外键，故不能进行删除修改操作
    public static final int IMPORT_COUNT_DIFFERENT = 5;//导入csv文件时，字段数不一致

    public static final String INTEGER_TYPE = "INTEGER";
    public static final String REAL_TYPE = "REAL";
    public static final String TEXT_TYPE = "TEXT";
    public static final String BLOB_TYPE = "BLOB";
    public static final String UNRESOLVED_TYPE = "UNRESOLVED";
    public static final String DATE_TYPE = "DATE";
    public static final String TIMESTAMP_TYPE = "TIMESTAMP";
    public static final String VARCHAR_TYPE = "VARCHAR";

    public static final String SQLITE_MASTER = "sqlite_master";
    public static final String SQLITE_SEQUENCE = "sqlite_sequence";
    public static final String ANDROID_METADATA = "android_metadata";

    public static final int TABLE_DATAS_SQL = 1;
    public static final int TABLE_DATAS_NORMAL = 2;
    public static final int TABLE_DATAS_FILTER = 3;

    public static final String CONTENT_PREFIX = "content://";
    public static final String SYSTEM_FILE_PREFIX = "file://";

    public static final String HAS_NET_ACCOUNT = "has_net_account";
    public static final String SRC_DATA_ID = "src_data_id";

    public static final String RESULT = "result";
    public static final String ERROR_MSG = "error_msg";
    public static final String SHOW_DESC_WRITE_STORAGE_PER_DIALOG = "show_desc_write_storage_per_dialog";
    public static final String SHOW_DESC_ACCCOUNT_PERMISSON_DIALOG = "show_desc_acccount_permisson_dialog";

    public static final String FragmentKey = "fragmentKey";

    public static final int PAGE_ROOT = 1;
    public static final int PAGE_CONTENT = 2;

    public static final String CUR_APP_DB_PATH = "curAppDbPath";
    public static final String CUR_APP_TMP_DB_PATH = "curAppTmpDbPath";

}
