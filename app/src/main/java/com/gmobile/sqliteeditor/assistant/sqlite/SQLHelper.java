package com.gmobile.sqliteeditor.assistant.sqlite;

import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.model.bean.sqlite.ColumnField;
import com.gmobile.sqliteeditor.model.bean.sqlite.NewColumnField;
import com.gmobile.sqliteeditor.model.bean.sqlite.TableDataField;
import com.orhanobut.logger.Logger;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by sg on 2016/9/19.
 */
public class SQLHelper {

    private String mCurDBFilePath;
    public SQLiteDatabase _db;
    public boolean isDataBase = false;
    public static final String ROW_ID = "rowid";

    public SQLHelper(String _dbFilePath, DatabaseErrorHandler errHandler) {
        this.mCurDBFilePath = _dbFilePath;
        try {
            _db = SQLiteDatabase.openDatabase(mCurDBFilePath, null, 0, errHandler);
            isDataBase = true;
        } catch (Exception e) {
            isDataBase = false;
        }
    }

    public int createTable(String tableName, NewColumnField columnField) {
        List<String> list = getColumnFieldNameList(tableName);

        String conditionStr = SQLTools.getColumnSqlStr(columnField);

        String sqlBase;
        if (list == null || list.size() <= 0) {
            sqlBase = "CREATE TABLE '" + tableName + "' ( " + conditionStr + " )";
            try {
                _db.execSQL(sqlBase);
                return Constant.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            sqlBase = "ALTER TABLE '" + tableName + "' ADD COLUMN " + conditionStr;
            try {
                _db.execSQL(sqlBase);
                return Constant.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                return addColumn(tableName, columnField);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Constant.FAIL;
    }

    public int addColumn(String tableName, NewColumnField columnField) throws Exception {
        String tmpTableName = "@filexpert123@";
        dropTable(tmpTableName);

        try {
            _db.beginTransaction();

            List<String> list = getColumnFieldNameList(tableName);
            String oldColumns = "";
            for (String str : list) {
                if (TextUtils.isEmpty(oldColumns)) {
                    oldColumns = "'" + str + "'";
                } else {
                    oldColumns = oldColumns + "," + "'" + str + "'";
                }
            }

            //1:获取原始表的sql
            String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";

            Cursor cursor = _db.rawQuery(sql, null);
            if (cursor == null) return Constant.FAIL;

            if (!cursor.moveToFirst()) {
                cursor.close();
                return Constant.FAIL;
            }

            String sqlStr = cursor.getString(0);
            cursor.close();

            if (TextUtils.isEmpty(sqlStr)) return Constant.FAIL;

            CreateTable select = (CreateTable) CCJSqlParserUtil.parse(sqlStr);
            List<ColumnDefinition> columnsNames = select.getColumnDefinitions();

            ColumnDefinition newDefinition = new ColumnDefinition();
            newDefinition.setColumnName(columnField.getFieldName());
            newDefinition.setColumnSpecStrings(SQLTools.getConditionList(columnField));

            ColDataType type = new ColDataType();
            type.setDataType(columnField.getFieldType());
            newDefinition.setColDataType(type);
            columnsNames.add(newDefinition);

            Table table = select.getTable();
            table.setName("'" + tmpTableName + "'");
            select.setTable(table);
            Logger.d(select);

            sql = select.toString();
            _db.execSQL(sql);

            sql = "INSERT INTO '" + tmpTableName + "' (" + oldColumns + ") SELECT * FROM '" + tableName + "'";
            _db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS '" + tableName + "'";
            _db.execSQL(sql);

            sql = "ALTER TABLE '" + tmpTableName + "' RENAME TO '" + tableName + "'";
            _db.execSQL(sql);

            _db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
            return Constant.FAIL;

        } finally {
            SQLManager.getSQLHelper()._db.endTransaction();
        }


        return Constant.SUCCESS;
    }

    public int deleteTableData(String tableName, Map<String, TableDataField> map) {

        if (!isDBOk()) {
            return Constant.FAIL;
        }

        String where;
        TableDataField rowField;

        if (map.containsKey(ROW_ID)) {
            rowField = map.get(ROW_ID);
            if (rowField == null) {
                return Constant.FAIL;
            }
            where = ROW_ID + "=?";
        } else {

            String id = "";
            List<ColumnField> list = getAutoincrementColumn(tableName);
            for (ColumnField field : list) {
                if (field.isAutoIncr()) {
                    id = field.getFieldName();
                    break;
                }
            }
            rowField = map.get(id);
            if (rowField == null) {
                return Constant.FAIL;
            }
            where = id + "=?";
            Logger.d(where);
        }

        try {
            _db.execSQL("DELETE FROM [" + tableName + "] WHERE " + where, new String[]{rowField.getFieldData()});
            return Constant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Constant.FAIL;
    }


    public Map<String, TableDataField> selectOneDataFromTable(String tableName, List<ColumnField> selectColList) {

        if (!isDBOk()) {
            return null;
        }

        String values = "";
        String name, value;
        int index = 0;
        int argsSize = 0;

        //获取字段中包括自增的信息
        List<ColumnField> autoColumnList = getAutoincrementColumn(tableName);

        List<String> autoColumnName = new ArrayList<>();
        if (autoColumnList.size() > 0) {
            for (int i = 0; i < autoColumnList.size(); i++) {
                ColumnField columnField = autoColumnList.get(i);
                if (columnField.isAutoIncr()) {
                    autoColumnName.add(columnField.getFieldName());
                }
            }
        }

        List<ColumnField> nowColumns = new ArrayList<>();
        nowColumns.addAll(selectColList);

        //过滤掉包含自增的字段进行查找
        for (int i = 0; i < selectColList.size(); i++) {
            ColumnField columnField = selectColList.get(i);
            if (autoColumnName.contains(columnField.getFieldName())) {
                nowColumns.remove(columnField);
            }
        }

        for (int i = 0; i < nowColumns.size(); i++) {
            value = nowColumns.get(i).getColumnValue();
            if (value != null) {
                argsSize++;
            }
        }

        String[] selectArgs = new String[argsSize];

        argsSize = 0;
        for (int i = 0; i < nowColumns.size(); i++) {
            name = SQLTools.addColumnMark(nowColumns.get(i).getFieldName());
            value = nowColumns.get(i).getColumnValue();

            if (value != null) {

                selectArgs[argsSize++] = value;
                index++;

                if (index == 1) {
                    values += (name + "=?");
                } else {
                    values += (" AND " + name + "=?");
                }
            } else {
                if (index == 0) {
                    index++;
                    values += (name + " IS NULL");
                } else {
                    values += (" and " + name + " IS NULL");
                }
            }
        }

        String sql = "select " + ROW_ID + ",* from '" + tableName + "' where " + values;
        Log.e("abc", "=====select==" + sql);

        Cursor cursor = _db.rawQuery(sql, !(argsSize == 0) ? selectArgs : null);
        if (cursor == null) return null;

        String[] columnNames = cursor.getColumnNames();
        TableDataField tableField;
        Map<String, TableDataField> itemTableDataList = null;

        while (cursor.moveToNext()) {

            itemTableDataList = new LinkedHashMap<>();
            for (String namee : columnNames) {

                tableField = new TableDataField();
                tableField.setFieldName(namee);

                try {
                    tableField.setFieldData(cursor.getString(cursor.getColumnIndex(namee)));
                } catch (Exception e) {
                    e.printStackTrace();
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(namee));
                    if (bytes != null) {
                        tableField.setFieldData("BLOB (size: " + bytes.length + ")");
                    }
                }

                itemTableDataList.put(namee, tableField);
                Log.e("sg", "----DATA---" + tableField.getFieldData());
            }

        }
        cursor.close();

        return itemTableDataList;
    }

    public int insertData2(String tableName, List<ColumnField> columnFields) {

        if (!isDBOk()) {
            return Constant.FAIL;
        }

        String columnName;
        String name = "";
        String value = "";
        String[] bindArgs = new String[columnFields.size()];

        for (int i = 0; i < columnFields.size(); i++) {
            columnName = SQLTools.addColumnMark(columnFields.get(i).getFieldName());
            if (TextUtils.isEmpty(name)) {
                name = columnName;
            } else {
                name = name + ", " + columnName;
            }

            if (TextUtils.isEmpty(value)) {
                value = "?";
            } else {
                value = value + "," + "?";
            }

            bindArgs[i] = columnFields.get(i).getColumnValue();
        }

        try {
            Logger.d("INSERT INTO [" + tableName + "] (" + name + ") VALUES(" + value + ")");
            _db.execSQL("INSERT INTO [" + tableName + "] (" + name + ") VALUES(" + value + ")", bindArgs);
            return Constant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Constant.FAIL;
    }


    public int updateTableData(String tableName, Map<String, TableDataField> map, List<ColumnField> nowColumns) {

        if (!isDBOk()) {
            return Constant.FAIL;
        }

        String name;
        String names = "";

        String[] bindArgs = new String[nowColumns.size() + 1];
        for (int i = 0; i < nowColumns.size(); i++) {
            name = SQLTools.addColumnMark(nowColumns.get(i).getFieldName());
            bindArgs[i] = nowColumns.get(i).getColumnValue();

            if (TextUtils.isEmpty(names)) {
                names = name + "=?";
            } else {
                names = names + " , " + name + "=?";
            }
        }

        String where;
        TableDataField rowField;

        if (map.containsKey(ROW_ID)) {
            rowField = map.get(ROW_ID);
            if (rowField == null) {
                return Constant.FAIL;
            }
            where = ROW_ID + "=?";

        } else {

            String id = "";
            List<ColumnField> list = getAutoincrementColumn(tableName);
            for (ColumnField field : list) {
                if (field.isAutoIncr()) {
                    id = field.getFieldName();
                    break;
                }
            }
            rowField = map.get(id);
            if (rowField == null) {
                return Constant.FAIL;
            }
            where = id + "=?";
            Logger.d(where);
        }

        bindArgs[bindArgs.length - 1] = rowField.getFieldData();
        try {
            Logger.d("UPDATE [" + tableName + "] SET " + names + " WHERE " + where);
            _db.execSQL("UPDATE [" + tableName + "] SET " + names + " WHERE " + where, bindArgs);
            return Constant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Constant.FAIL;
    }

    public List<String> getTableNameList() {
        List<String> tables = new ArrayList<>();

        if (!isDBOk()) {
            return tables;
        }

        Cursor cursor = _db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name", null);
        if (cursor == null) return tables;

        while (cursor.moveToNext()) {
            //遍历出表名
            String name = cursor.getString(0);
            tables.add(name);
        }
        cursor.close();

        return tables;
    }

    public List<ColumnField> getColumnFieldList(String tableName) {

        List<ColumnField> columnFields = new ArrayList<>();

        if (!isDBOk()) {
            return columnFields;
        }

        Cursor cursor = _db.rawQuery("PRAGMA table_info('" + tableName + "')", null);
        if (cursor == null) return columnFields;

        while (cursor.moveToNext()) {
            ColumnField localField = new ColumnField();
            localField.setFieldName(cursor.getString(1));
            localField.setFieldType(cursor.getString(2));
            localField.setNotNull(cursor.getInt(3));
            localField.setDef(cursor.getString(4));
            localField.setPk(cursor.getInt(5));
            columnFields.add(localField);

            Logger.d(localField.toString());
        }
        cursor.close();

        return columnFields;

    }

    public List<String> getColumnFieldNameList(String tableName) {

        List<String> columnFields = new ArrayList<>();
        if (!isDBOk()) {
            return columnFields;
        }
        Cursor cursor = _db.rawQuery("PRAGMA table_info('" + tableName + "')", null);
        if (cursor == null) return columnFields;

        while (cursor.moveToNext()) {
            columnFields.add(cursor.getString(1));
        }
        cursor.close();

        return columnFields;

    }

    public List<Map<String, TableDataField>> getTableDataList(String tableName, int start, int end, String orderByColumName, boolean isDesc) {

        List<Map<String, TableDataField>> allTableDataList = new LinkedList<>();

        if (!isDBOk()) {
            return allTableDataList;
        }

        String limit = "";
        String order = "";
        if (end > 0) {
            limit = " limit " + start + " ," + end;
        }

        if (orderByColumName != null) {
            order += " ORDER BY " + orderByColumName + (isDesc ? " DESC" : " ASC");
        }

        String sql = "SELECT " + ROW_ID + ",* FROM '" + tableName + "'" + limit + order;
        Cursor cursor = _db.rawQuery(sql, null);
        if (cursor == null) return allTableDataList;

        String[] columnName = cursor.getColumnNames();
        TableDataField tableField;
        Map<String, TableDataField> itemTableDataList;

        while (cursor.moveToNext()) {
            itemTableDataList = new LinkedHashMap<>();

            for (String name : columnName) {
                tableField = new TableDataField();

                tableField.setFieldName(name);

                try {
                    tableField.setFieldData(cursor.getString(cursor.getColumnIndex(name)));
                } catch (Exception e) {
                    e.printStackTrace();
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(name));
                    if (bytes != null) {
                        tableField.setFieldData("BLOB (size: " + bytes.length + ")");
                    }
                }
                itemTableDataList.put(name, tableField);
            }

            allTableDataList.add(itemTableDataList);
        }
        cursor.close();
        return allTableDataList;
    }

    public List<Map<String, TableDataField>> getFilterData(String tableName, List<ColumnField> nowColumns, String columnName, String selectArg, boolean isAll) {
        List<Map<String, TableDataField>> allTableDataList = new LinkedList<>();
        if (!isDBOk()) {
            return allTableDataList;
        }

        String sql = "SELECT " + ROW_ID + ",* FROM '" + tableName + "' WHERE ";
        Cursor cursor;
        int index = 0;
        if (isAll) {

            String name;
            String selectArgs = "";
            String[] condition = new String[nowColumns.size()];
            for (int i = 0; i < nowColumns.size(); i++) {
                name = SQLTools.addColumnMark(nowColumns.get(i).getFieldName());
                if (index == 0) {
                    selectArgs += name + " like ? ";
                } else {
                    selectArgs += " or " + name + " like ? ";
                }
                index++;

                condition[i] = "%" + selectArg + "%";
            }

            sql = sql + selectArgs;
            cursor = _db.rawQuery(sql, condition);
        } else {
            sql = sql + SQLTools.addColumnMark(columnName) + " like ?";
            cursor = _db.rawQuery(sql, new String[]{"%" + selectArg + "%"});
        }

        if (cursor == null) return allTableDataList;

        String[] columnNames = cursor.getColumnNames();
        TableDataField tableField;
        Map<String, TableDataField> itemTableDataList;

        while (cursor.moveToNext()) {

            itemTableDataList = new LinkedHashMap<>();
            for (String name : columnNames) {
                tableField = new TableDataField();

                tableField.setFieldName(name);

                try {
                    tableField.setFieldData(cursor.getString(cursor.getColumnIndex(name)));
                } catch (Exception e) {
                    e.printStackTrace();
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(name));
                    if (bytes != null) {
                        tableField.setFieldData("BLOB (size: " + bytes.length + ")");
                    }
                }

                itemTableDataList.put(name, tableField);
                Logger.d(tableField.toString());
            }

            allTableDataList.add(itemTableDataList);
        }
        cursor.close();

        return allTableDataList;
    }

    public int dropTable(String name) {

        if (!isDBOk()) {
            return Constant.FAIL;
        }

        try {
            String sql = "DROP TABLE '" + name + "'";
            _db.execSQL(sql);

            return Constant.SUCCESS;
        } catch (Exception e) {
        }

        return Constant.FAIL;
    }

    public int dropView(String viewName) {
        if (!isDBOk()) {
            return Constant.FAIL;
        }

        try {
            String sql = "DROP VIEW '" + viewName + "'";
            _db.execSQL(sql);

            return Constant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Constant.FAIL;
    }

    public List<NewColumnField> getNewColumnFieldList(String tableName) {

        List<NewColumnField> columnFieldList = new LinkedList<>();
        if (!isDBOk()) {
            return columnFieldList;
        }

        String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        Cursor cursor = _db.rawQuery(sql, null);
        if (cursor == null) return columnFieldList;

        if (!cursor.moveToFirst()) {
            cursor.close();
            return columnFieldList;
        }
        String sqlStr = cursor.getString(0);
        Log.e("sg", "--sql-------------" + sqlStr);
        cursor.close();

        if (TextUtils.isEmpty(sqlStr)) return columnFieldList;

        try {
            CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(sqlStr);
            List<ColumnDefinition> columnList = createTable.getColumnDefinitions();

            NewColumnField field;
            List<String> specList;
            for (ColumnDefinition definition : columnList) {

                field = new NewColumnField();
                field.setFieldName(SQLTools.removeColumnMark(definition.getColumnName()));
                field.setFieldType(SQLTools.removeColumnMark(definition.getColDataType().getDataType()));

                specList = definition.getColumnSpecStrings();
                if (specList != null) {
                    String tmpStr;
                    int next;
                    int conditionSize = specList.size();
                    for (int i = 0; i < conditionSize; i++) {
                        next = i + 1;
                        tmpStr = specList.get(i).toUpperCase();

                        if (tmpStr.equals(Constant.PRIMARY_VALUE)) {
                            field.setPrimaryKey(true);
                        }

                        if (tmpStr.equals(Constant.AUTOINCREMENT_VALUE)) {
                            field.setAutoIncrement(true);
                        }

                        if (tmpStr.equals(Constant.NOT_VALUE) &&
                                specList.get(next).toUpperCase().equals(Constant.NULL_VALUE)) {
                            field.setNotNull(true);
                        }

                        if (tmpStr.equals(Constant.UNIQUE_KEY_VALUE)) {
                            field.setUnique(true);
                        }

                        if (tmpStr.equals(Constant.DEFAULT_VALUE)) {
                            field.setDefaultKey(true);
                            if (next < conditionSize && !TextUtils.isEmpty(specList.get(next))) {
                                field.setDefaultValue(SQLTools.removeColumnMark(specList.get(next)));
                            }
                        }

                        if (tmpStr.equals(Constant.FOREIGN_KEY_VALUE)) {
                            field.setForeignKey(true);

                            if (next < conditionSize && !TextUtils.isEmpty(specList.get(next))) {
                                field.setFkTable(SQLTools.removeColumnMark(specList.get(next)));
                            }

                            if (next + 1 < conditionSize && !TextUtils.isEmpty(specList.get(next + 1))) {
                                field.setFkField(SQLTools.removeColumnMark(specList.get(next + 1)));
                            }
                        }

                    }
                }

                columnFieldList.add(field);
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        return columnFieldList;
    }

    public List<ColumnField> getAutoincrementColumn(String tableName) {
        List<ColumnField> columnFieldList = new LinkedList<>();

        if (!isDBOk()) {
            return columnFieldList;
        }

        String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        Cursor cursor = _db.rawQuery(sql, null);
        if (cursor == null) return columnFieldList;

        if (!cursor.moveToFirst()) {
            cursor.close();
            return columnFieldList;
        }

        String sqlStr = cursor.getString(0);
        cursor.close();

        if (TextUtils.isEmpty(sqlStr)) return columnFieldList;

        try {
            CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(sqlStr);
            List<ColumnDefinition> columnList = createTable.getColumnDefinitions();

            ColumnField field;
            List<String> specList;
            for (ColumnDefinition definition : columnList) {

                field = new ColumnField();
                field.setFieldName(SQLTools.removeColumnMark(definition.getColumnName()));
                field.setFieldType(SQLTools.removeColumnMark(definition.getColDataType().getDataType()));

                specList = definition.getColumnSpecStrings();
                if (specList != null) {
                    String tmpStr;
                    int next;
                    int conditionSize = specList.size();
                    for (int i = 0; i < conditionSize; i++) {
                        next = i + 1;
                        tmpStr = specList.get(i).toUpperCase();

                        if (tmpStr.equals(Constant.PRIMARY_VALUE)) {
                            field.setPk(1);
                        }

                        if (tmpStr.equals(Constant.AUTOINCREMENT_VALUE)) {
                            field.setIsAutoIncr(true);
                        }

                        if (tmpStr.equals(Constant.NOT_VALUE) &&
                                specList.get(next).toUpperCase().equals(Constant.NULL_VALUE)) {
                            field.setNotNull(1);
                        }

                        if (tmpStr.equals(Constant.DEFAULT_VALUE)) {
                            if (next < conditionSize && !TextUtils.isEmpty(specList.get(next))) {
                                field.setDef(SQLTools.removeColumnMark(specList.get(next)));
                            }
                        }
                    }
                }

                columnFieldList.add(field);
                Logger.d("--------" + field.toString());
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        return columnFieldList;
    }

    private boolean isAutoIncrement(ColumnDefinition definition) {
        List<String> specList = definition.getColumnSpecStrings();

        if (specList != null) {
            String tmpStr;
            int conditionSize = specList.size();
            for (int i = 0; i < conditionSize; i++) {
                tmpStr = specList.get(i).toUpperCase();

                if (tmpStr.equals(Constant.AUTOINCREMENT_VALUE)) {
                    return true;
                }
            }
        }

        return false;
    }

    public int deleteAllDataFromTable(String tableName) {
        if (!isDBOk()) {
            return Constant.FAIL;
        }

        try {
            String sql = "delete from '" + tableName + "'";
            _db.execSQL(sql);

            return Constant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Constant.FAIL;
    }

    /**
     * 修改表中的列
     *
     * @param tableName    表名
     * @param oldFieldName 待操作列名
     * @param columnField  修改后的列及属性（删除操作此项为空）
     * @return int
     */
    public Bundle processColumn(String tableName, String oldFieldName, NewColumnField columnField) {
        String tmpTableName = "@filexpert123@";
        dropTable(tmpTableName);

        Bundle resultBundle = new Bundle();

        boolean isDelete = columnField == null;
        if (!isDBOk()) {
            resultBundle.putInt(Constant.RESULT, Constant.FAIL);
            return resultBundle;
        }

        if (isColumnForeignKey(tableName, oldFieldName)) {
            resultBundle.putInt(Constant.RESULT, Constant.FAIL_EDIT_COLUMN_FK);
            return resultBundle;
        }

        try {
            _db.beginTransaction();

            //1:获取原始表的sql
            String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";

            Cursor cursor = _db.rawQuery(sql, null);
            if (cursor == null) {
                resultBundle.putInt(Constant.RESULT, Constant.FAIL);
                return resultBundle;
            }

            if (!cursor.moveToFirst()) {
                cursor.close();
                resultBundle.putInt(Constant.RESULT, Constant.FAIL);
                return resultBundle;
            }

            String sqlStr = cursor.getString(0);
            cursor.close();

            if (TextUtils.isEmpty(sqlStr)) {
                resultBundle.putInt(Constant.RESULT, Constant.FAIL);
                return resultBundle;
            }

            CreateTable select = (CreateTable) CCJSqlParserUtil.parse(sqlStr);
            List<ColumnDefinition> columnsNames = select.getColumnDefinitions();

            int position = -1;
            if (columnsNames.size() <= 0) {
                resultBundle.putInt(Constant.RESULT, Constant.FAIL_DELETE_COLUMN_EMPTY);
                return resultBundle;
            }

            String autoColumn = "";
            String columnName;
            int size = columnsNames.size();
            for (int i = 0; i < size; i++) {
                columnName = columnsNames.get(i).getColumnName();
                if (isAutoIncrement(columnsNames.get(i))) {
                    autoColumn = columnName;
                }

                if (SQLTools.removeColumnMark(columnName).equals(oldFieldName)) {
                    position = i;
                }
            }

            if (position > -1) {
                if (isDelete) {
                    if (size == 1) {
                        resultBundle.putInt(Constant.RESULT, Constant.FAIL_DELETE_COLUMN_EMPTY);
                        return resultBundle;
                    }

                    columnsNames.remove(position);
                    select.setColumnDefinitions(columnsNames);
                } else {
                    ColumnDefinition definition = columnsNames.get(position);
                    definition.setColumnName(columnField.getFieldName());
                    definition.getColDataType().setDataType(columnField.getFieldType());
                    definition.setColumnSpecStrings(SQLTools.getConditionList(columnField));
                }
            }

            Table table = select.getTable();
            table.setName("'" + tmpTableName + "'");
            select.setTable(table);

            //2-3:修改表名，并创建临时表
            sql = select.toString();
            Logger.d(sql);
            _db.execSQL(sql);

            List<String> list = getColumnFieldNameList(tableName);
            String oldColumns = "";
            String tmpColumns = "";
            for (String str : list) {

                if (str.equals(SQLTools.removeColumnMark(autoColumn)) && !TextUtils.isEmpty(autoColumn)) {
                    continue;
                }

                if (str.equals(oldFieldName)) {
                    if (isDelete) {
                        continue;
                    } else {
                        if (TextUtils.isEmpty(tmpColumns)) {
                            tmpColumns = columnField.getFieldName();
                        } else {
                            tmpColumns = tmpColumns + " , " + columnField.getFieldName();
                        }
                    }
                } else {
                    if (!isDelete) {
                        if (TextUtils.isEmpty(tmpColumns)) {
                            tmpColumns = str;
                        } else {
                            tmpColumns = tmpColumns + " , " + str;
                        }
                    }
                }

                if (TextUtils.isEmpty(oldColumns)) {
                    oldColumns = "'" + str + "'";
                } else {
                    oldColumns = oldColumns + " , " + "'" + str + "'";
                }

                if (isDelete) {
                    tmpColumns = oldColumns;
                }
            }


            //3:查询原表数据，如有数据，则插入新表中
            if (!TextUtils.isEmpty(tmpColumns)) {
                sql = "INSERT INTO '" + tmpTableName + "' (" + tmpColumns + ") SELECT " + oldColumns + " FROM '" + tableName + "'";
                Logger.d(sql);
                _db.execSQL(sql);
            }

            //4-1:查询原表是否有索引
            sql = "SELECT sql FROM sqlite_master WHERE type = 'index' AND tbl_name='" + tableName + "'";
            Cursor indexCursor = _db.rawQuery(sql, null);
            List<String> indexStrList = new ArrayList<>();
            if (indexCursor != null) {
                while (indexCursor.moveToNext()) {

                    if (indexCursor.getString(0) == null)
                        continue;

                    indexStrList.add(indexCursor.getString(0));
                }
                indexCursor.close();
            }

            //4-2:查询原表是否有视图
            sql = "SELECT sql FROM sqlite_master WHERE type = 'view'";
            Cursor viewCursor = _db.rawQuery(sql, null);
            List<String> viewStrList = new ArrayList<>();
            if (viewCursor != null) {
                while (viewCursor.moveToNext()) {

                    if (viewCursor.getString(0) == null)
                        continue;

                    viewStrList.add(viewCursor.getString(0));
                    Log.e("sg", "--viewStr--" + viewCursor.getString(0));
                }
                viewCursor.close();
            }

            //5:删除原表
            sql = "DROP TABLE IF EXISTS '" + tableName + "'";
            _db.execSQL(sql);

            //6:重命名临时表为原表名
            sql = "ALTER TABLE '" + tmpTableName + "' RENAME TO '" + tableName + "'";
            _db.execSQL(sql);

            //7:如果步骤4查出有视图，索引等，则在这步进行创建
            boolean isColumnNameChanged = isDelete || !(oldFieldName.equals(columnField.getFieldName()));

            if (indexStrList.size() > 0) {
                for (String indexStr : indexStrList) {
                    try {
                        if (isColumnNameChanged) {
                            indexStr = processIndexStr(indexStr, oldFieldName, isDelete ? null : columnField.getFieldName());
                            if (indexStr == null) continue;
                        }

                        Logger.d(indexStr);

                        _db.execSQL(indexStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (viewStrList.size() > 0) {
                for (String viewStr : viewStrList) {
                    try {
                        if (isColumnNameChanged) {
                            viewStr = processViewStr(viewStr, oldFieldName, isDelete ? null : columnField.getFieldName());
                            if (viewStr == null) continue;
                        }

                        Logger.d(viewStr);

                        _db.execSQL(viewStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            SQLManager.getSQLHelper()._db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();

            String msg = e.getMessage();
            msg = msg.replace(tmpTableName, tableName);

            resultBundle.putInt(Constant.RESULT, Constant.FAIL);
            resultBundle.putString(Constant.ERROR_MSG, msg);
            return resultBundle;

        } finally {
            try {
                SQLManager.getSQLHelper()._db.endTransaction();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        resultBundle.putInt(Constant.RESULT, Constant.SUCCESS);
        return resultBundle;
    }

    /**
     * 只针对CRETAE句式
     *
     * @param sqlStr        索引create 句式
     * @param specialColumn 需要修改/删除的字段
     * @param newColumn     修改的话，新的字段
     * @return 修改后的句式
     */
    private String processIndexStr(String sqlStr, String specialColumn, String newColumn) {

        boolean isDelete = newColumn == null;
        try {
            CreateIndex select = (CreateIndex) CCJSqlParserUtil.parse(sqlStr);
            Index index = select.getIndex();
            List<String> columnsNames = index.getColumnsNames();

            int position = -1;
            if (columnsNames != null && columnsNames.size() > 0) {
                int size = columnsNames.size();

                for (int i = 0; i < size; i++) {
                    if (columnsNames.get(i).contains(specialColumn)) {
                        position = i;
                        break;
                    }
                }

                if (position > -1) {
                    columnsNames.remove(position);
                    if (!isDelete) {
                        columnsNames.add(SQLTools.processColumnName(newColumn));
                    }
                    if (size <= 0) {
                        return null;
                    }

                    index.setColumnsNames(columnsNames);
                    select.setIndex(index);
                }
            }

            Log.e("---process--index---", select.toString());
            return select.toString();

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        return sqlStr;
    }

    private String processViewStr(String sqlStr, String specialColumn, String newColumn) {

        boolean isDelete = newColumn == null;

        try {
            CreateView select = (CreateView) CCJSqlParserUtil.parse(sqlStr);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            List<SelectItem> selectItems = plainSelect.getSelectItems();

            int position = -1;
            if (selectItems != null && selectItems.size() > 0) {
                int size = selectItems.size();

                for (int i = 0; i < size; i++) {
                    if (selectItems.get(i).toString().contains(specialColumn)) {
                        position = i;
                        break;
                    }
                }

                if (position > -1) {
                    if (isDelete) {
                        if (size == 1) {
                            return null;
                        }

                        selectItems.remove(position);
                        plainSelect.setSelectItems(selectItems);
                        select.setSelectBody(plainSelect);
                        dropView(select.getView().toString());

                    } else {

                        SelectExpressionItem item = (SelectExpressionItem) selectItems.get(position);
                        Column value = (Column) item.getExpression();
                        value.setColumnName(SQLTools.processColumnName(newColumn));

                        plainSelect.setSelectItems(selectItems);
                        select.setSelectBody(plainSelect);

                        dropView(SQLTools.removeColumnMark(select.getView().toString()));
                    }
                }
            }

            Log.e("---process--view---", select.toString());
            return select.toString();

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        return sqlStr;
    }

    public boolean isColumnForeignKey(String table, String column) {
        String sql = "SELECT name, sql FROM sqlite_master WHERE type='table'";

        Cursor cursor = _db.rawQuery(sql, null);
        if (cursor == null) return true;

        List<String> tableStrList = new ArrayList<>();
        while (cursor.moveToNext()) {
            if (cursor.getString(0) == null
                    || cursor.getString(0).equals(Constant.SQLITE_MASTER)
                    || cursor.getString(0).equals(Constant.SQLITE_SEQUENCE)
                    || cursor.getString(0).equals(Constant.ANDROID_METADATA))
                continue;

            tableStrList.add(cursor.getString(1));
        }
        cursor.close();

        if (tableStrList.size() <= 0) return false;

        CreateTable select;
        List<ColumnDefinition> columnList;
        //遍历所有表
        for (String sqlStr : tableStrList) {
            try {
                select = (CreateTable) CCJSqlParserUtil.parse(sqlStr);
                columnList = select.getColumnDefinitions();
                if (columnList == null || columnList.size() <= 0) continue;

                List<String> specList;
                String fkTable;
                String fkField;
                //遍历表的每列
                for (ColumnDefinition definition : columnList) {
                    specList = definition.getColumnSpecStrings();
                    if (specList != null) {

                        String tmpStr;
                        int conditionSize = specList.size();
                        //遍历列的每个限制条件
                        for (int i = 0; i < conditionSize; i++) {
                            tmpStr = specList.get(i).toUpperCase();

                            boolean isTable = false;
                            if (tmpStr.equals(Constant.FOREIGN_KEY_VALUE)) {

                                if (i + 1 < conditionSize && !TextUtils.isEmpty(specList.get(i + 1))) {
                                    fkTable = SQLTools.removeColumnMark(specList.get(i + 1));
                                    isTable = fkTable.equals(table);
                                }

                                if (isTable && i + 2 < conditionSize && !TextUtils.isEmpty(specList.get(i + 2))) {
                                    fkField = SQLTools.removeColumnMark(specList.get(i + 2));

                                    if (fkField.equals(column)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (JSQLParserException e) {
                e.printStackTrace();

                if (sqlStr.matches(".+REFERENCES.+" + table + ".+" + column + ".+")) {
                    return true;
                }
            }
        }

        return false;
    }

    public int execSQLStr(String sqlStr) {
        if (!isDBOk()) {
            return Constant.FAIL;
        }

        SQLiteStatement statement = _db.compileStatement(sqlStr);

        try {
            return statement.executeUpdateDelete();
        } finally {
            statement.close();
        }
    }

    public List<Map<String, TableDataField>> execQuerySQLStr(String sqlStr) {
        List<Map<String, TableDataField>> allTableDataList = new LinkedList<>();

        if (!isDBOk()) {
            return allTableDataList;
        }

        Cursor cursor = _db.rawQuery(sqlStr, null);
        if (cursor == null) return allTableDataList;

        TableDataField tableField;
        Map<String, TableDataField> itemTableDataList;

        String[] columnName = cursor.getColumnNames();
        while (cursor.moveToNext()) {

            itemTableDataList = new LinkedHashMap<>();
            for (String name : columnName) {
                tableField = new TableDataField();
                tableField.setFieldName(name);

                String value = "";
                try {
                    value = cursor.getString(cursor.getColumnIndex(name));
                } catch (Exception e) {
                    e.printStackTrace();
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(name));
                    if (bytes != null) {
                        value = "BLOB (size: " + bytes.length + ")";
                    }
                }

                tableField.setFieldData(value);
                itemTableDataList.put(name, tableField);
            }
            allTableDataList.add(itemTableDataList);
        }
        cursor.close();

        return allTableDataList;
    }

    public boolean renameTable(String table, String newTable){
        try{
            _db.execSQL("ALTER TABLE '" + table + "' RENAME TO '" + newTable + "'");
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDBOk() {
        if (_db != null && _db.isOpen()) {
            return true;
        }

        if (!TextUtils.isEmpty(mCurDBFilePath)) {
            try {
                _db = SQLiteDatabase.openDatabase(mCurDBFilePath, null, 0);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public void close() {
        try {
            if (_db != null && _db.isOpen()) {
                _db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
