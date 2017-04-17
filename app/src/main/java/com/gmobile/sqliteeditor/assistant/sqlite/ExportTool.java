package com.gmobile.sqliteeditor.assistant.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;


import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.constant.SqlConstant;
import com.gmobile.sqliteeditor.model.bean.sqlite.ColumnField;
import com.gmobile.sqliteeditor.model.bean.sqlite.TableDataField;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by sg on 2016/10/17.
 */
public class ExportTool {

    private static String NL = "\n";

    public static int exportDatabase(String fileName, String parentPath) throws NoSuchFieldException {

        if (!SQLManager.getSQLHelper().isDBOk()) {
            return Constant.FAIL;
        }

        File file = null;
        FileWriter localFileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            file = new File(parentPath, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            localFileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(localFileWriter);

            exportAllTable(SQLManager.getSQLHelper()._db, bufferedWriter);
            exportAllData(SQLManager.getSQLHelper()._db, bufferedWriter);
            exportAllIndex(SQLManager.getSQLHelper()._db, bufferedWriter);
            exportView(SQLManager.getSQLHelper()._db, bufferedWriter);
            exportTrigger(SQLManager.getSQLHelper()._db, bufferedWriter);

            return Constant.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();

            if (file != null && file.exists()) {
                file.delete();
            }
            return Constant.FAIL;

        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (localFileWriter != null) {
                try {
                    localFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int exportTable(String tableName, String parentPath, String fileName) throws NoSuchFieldException {

        if (!SQLManager.getSQLHelper().isDBOk()) {
            return Constant.FAIL;
        }

        File file = null;
        FileWriter localFileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            file = new File(parentPath, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            localFileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(localFileWriter);

            exportSingleTable(SQLManager.getSQLHelper()._db, tableName, bufferedWriter);
            exportSingleTableData(SQLManager.getSQLHelper()._db, tableName, bufferedWriter);
            exportSingleIndex(SQLManager.getSQLHelper()._db, tableName, bufferedWriter);

            return Constant.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();

            if (file != null && file.exists()) {
                file.delete();
            }
            return Constant.FAIL;
        } finally {

            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (localFileWriter != null) {
                try {
                    localFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void exportSingleTable(SQLiteDatabase db, String tableName, BufferedWriter writer) throws IOException {

        String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null) return;

        cursor.moveToFirst();
        String sqlStr = cursor.getString(0);
        cursor.close();

        writer.write(sqlStr + ";" + NL + NL);
    }

    private static void exportAllTable(SQLiteDatabase db, BufferedWriter writer) throws IOException {
        String sql = "SELECT name, sql FROM sqlite_master WHERE type='table'";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null) return;

        String table;
        while (cursor.moveToNext()) {
            //遍历出表名
            table = cursor.getString(0);
            if (table.equals(Constant.SQLITE_MASTER)
                    || table.equals(Constant.SQLITE_SEQUENCE)
                    || table.equals(Constant.ANDROID_METADATA))
                continue;

            writer.write(cursor.getString(1) + ";" + NL + NL);
        }
        cursor.close();
    }

    private static void exportAllData(SQLiteDatabase db, BufferedWriter writer) throws IOException, NoSuchFieldException {

        List<String> tableList = SQLManager.getSQLHelper().getTableNameList();
        for (String table : tableList) {
            if ((table.equals(Constant.SQLITE_SEQUENCE))
                    || (table.equals(Constant.ANDROID_METADATA))
                    || (table.equals(Constant.SQLITE_MASTER)))
                continue;

            exportSingleTableData(db, table, writer);
        }
    }

    private static void exportSingleTableData(SQLiteDatabase db, String tableName, BufferedWriter writer) throws IOException {
        String sql = "SELECT * FROM '" + tableName + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null) return;

        String[] columnName = cursor.getColumnNames();
        while (cursor.moveToNext()) {

            String columnStr = "";
            String valueStr = "";
            String value;
            String name;
            for (int i = 0; i < columnName.length; i++) {
                name = columnName[i];

                if (TextUtils.isEmpty(columnStr)) {
                    columnStr = SQLTools.processColumnName(name);
                } else {
                    columnStr = columnStr + ", " + SQLTools.processColumnName(name);
                }

                try {
                    value = SQLTools.processValueMark(cursor.getString(cursor.getColumnIndex(name)));
                    if (TextUtils.isEmpty(value)) {
                        value = null;
                    } else {
                        value = "'" + value + "'";
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(name));
                    if (bytes != null) {
                        value = "X'" + SQLTools.byteArrayToHexString(bytes) + "'";
                    } else {
                        value = null;
                    }
                }

                if (i == 0) {
                    valueStr = value;
                } else {
                    valueStr = valueStr + ", " + value;
                }
            }
            writer.write("INSERT INTO [" + tableName + "] (" + columnStr + ") VALUES (" + valueStr + ");" + NL);
        }
        writer.write(NL);
        cursor.close();
    }

    private static void exportSingleIndex(SQLiteDatabase db, String tableName, BufferedWriter writer) throws IOException {
        String sql = "SELECT sql FROM sqlite_master WHERE type = 'index' AND tbl_name='" + tableName + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            if (cursor.getString(0) == null)
                continue;

            writer.write(cursor.getString(0) + ";" + NL + NL);
        }
        cursor.close();
    }

    private static void exportAllIndex(SQLiteDatabase db, BufferedWriter writer) throws IOException {
        String sql = "SELECT sql FROM sqlite_master WHERE type = 'index'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            if (cursor.getString(0) == null)
                continue;

            writer.write(cursor.getString(0) + ";" + NL + NL);
        }
        cursor.close();
    }

    private static void exportView(SQLiteDatabase db, BufferedWriter writer) throws IOException {
        String sql = "SELECT sql FROM sqlite_master WHERE type = 'view'";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            if (cursor.getString(0) == null)
                continue;

            writer.write(cursor.getString(0) + ";" + NL + NL);
        }
        cursor.close();
    }

    private static void exportTrigger(SQLiteDatabase db, BufferedWriter writer) throws IOException {
        String sql = "select sql from sqlite_master where type = 'trigger'";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            if (cursor.getString(0) == null)
                continue;

            writer.write(cursor.getString(0) + ";" + NL + NL);
        }
        cursor.close();

    }

    public static int importTab(String sqlFilePath) throws NoSuchFieldException {

        if (!SQLManager.getSQLHelper().isDBOk()) {
            return Constant.FAIL;
        }

        List sqlList = null;
        try {
            sqlList = SQLTools.parseSQLFile(sqlFilePath);
        } catch (Exception e) {
            e.printStackTrace();

        }

        if (sqlList != null && sqlList.size() > 0) {
            try {
                Iterator localIterator = sqlList.iterator();
                SQLManager.getSQLHelper()._db.beginTransaction();
                while (true) {
                    if (!localIterator.hasNext()) {
                        SQLManager.getSQLHelper()._db.setTransactionSuccessful();
                        break;
                    }

                    String str = (String) localIterator.next();
                    Log.e("sg", "--str---" + str);
                    if (str.trim().startsWith("--"))
                        continue;

                    SQLManager.getSQLHelper()._db.execSQL(str);
                }

                return Constant.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SQLManager.getSQLHelper()._db.endTransaction();
            }
        }

        return Constant.FAIL;
    }

    public static int exportCsv(String tableName, String parentPath, String fileName, List<ColumnField> columnFields, boolean writeHeader, List<String> exportColumns) throws NoSuchFieldException {

        if (!SQLManager.getSQLHelper().isDBOk()) {
            return SqlConstant.FAIL;
        }

        File file = null;
        FileWriter fileWriter = null;
        try {
            file = new File(parentPath, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);

            if (writeHeader) {
                int size;
                if (exportColumns == null) {
                    size = columnFields.size();
                    for (int i = 0; i < size; i++) {
                        fileWriter.write("\"" + columnFields.get(i).getFieldName() + "\"");
                        if (i == size - 1) {
                            fileWriter.write(NL);
                        } else {
                            fileWriter.write(",");
                        }
                    }
                } else {
                    size = exportColumns.size();
                    for (int i = 0; i < size; i++) {
                        fileWriter.write("\"" + exportColumns.get(i) + "\"");
                        if (i == size - 1) {
                            fileWriter.write(NL);
                        } else {
                            fileWriter.write(",");
                        }
                    }
                }
            }

            String selection;
            if (exportColumns == null) {
                selection = "*";
            } else {
                StringBuilder bf = new StringBuilder();
                int size = exportColumns.size();
                for (int i = 0; i < size; i++) {
                    bf.append(SQLTools.processColumnName(exportColumns.get(i)));
                    if (i != size - 1) {
                        bf.append(",");
                    }
                }
                selection = bf.toString();
            }

            String sql = "SELECT " + selection + " FROM '" + tableName + "'";
            Cursor cursor = SQLManager.getSQLHelper()._db.rawQuery(sql, null);
            if (cursor == null) return SqlConstant.FAIL;

            String[] columnName = cursor.getColumnNames();
            while (cursor.moveToNext()) {

                String columnStr = "";
                String valueStr = "";
                String value;
                String name;
                for (int i = 0; i < columnName.length; i++) {
                    name = columnName[i];

                    if (TextUtils.isEmpty(columnStr)) {
                        columnStr = SQLTools.processColumnName(name);
                    } else {
                        columnStr = columnStr + ", " + SQLTools.processColumnName(name);
                    }

                    try {
                        value = SQLTools.processValueMark(cursor.getString(cursor.getColumnIndex(name)));
                        if (TextUtils.isEmpty(value)) {
                            value = "";
                        } else {
                            value = "\"" + cursor.getString(cursor.getColumnIndex(name)) + "\"";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        byte[] bytes = cursor.getBlob(cursor.getColumnIndex(name));
                        if (bytes != null) {
                            value = "X'" + SQLTools.byteArrayToHexString(bytes) + "'";
                        } else {
                            value = null;
                        }
                    }

                    if (i == 0) {
                        valueStr = value;
                    } else {
                        valueStr = valueStr + "," + value;
                    }
                }

                Logger.d("column--" + columnStr + "--value--" + valueStr);
                fileWriter.write(valueStr + NL);
            }
            cursor.close();

            return SqlConstant.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();

            if (file != null && file.exists()) {
                file.delete();
            }
            return SqlConstant.FAIL;

        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int isColumnCountSame(String table, String csvFilePath) throws NoSuchFieldException {
        if (!SQLManager.getSQLHelper().isDBOk()) {
            return Constant.FAIL;
        }

        try {
            List<String> columns = SQLManager.getSQLHelper().getColumnFieldNameList(table);
            List<String> list = CVSTools.parseFirst(csvFilePath);

            if (columns.size() != list.size()) {
                return Constant.IMPORT_COUNT_DIFFERENT;
            }

            return Constant.SUCCESS;

        } catch (Exception e) {
            return Constant.FAIL;
        }
    }

    public static int importCsv(String table, String csvFilePath, boolean skipFirstLine, List<String> importColumns) throws NoSuchFieldException {
        if (!SQLManager.getSQLHelper().isDBOk()) {
            return SqlConstant.FAIL;
        }

        try {
            List<String> allColumns = SQLManager.getSQLHelper().getColumnFieldNameList(table);
            if (allColumns == null || allColumns.isEmpty()) return SqlConstant.FAIL;

            List<Integer> selected = new ArrayList<>();
            for (int i = 0; i < allColumns.size(); i++) {
                if(importColumns != null){
                    for (String column : importColumns) {
                        if (allColumns.get(i).equals(column)) {
                            selected.add(i);
                            break;
                        }
                    }
                } else {
                    selected.add(i);
                }
            }

            List<String> valuesList = CVSTools.parseLine(table, csvFilePath, allColumns, skipFirstLine, selected);
            if (valuesList != null) {

                SQLManager.getSQLHelper()._db.beginTransaction();

                for (String value : valuesList) {
                    SQLManager.getSQLHelper()._db.execSQL(value);
                }

                SQLManager.getSQLHelper()._db.setTransactionSuccessful();
            }

            return SqlConstant.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            return SqlConstant.FAIL;
        } finally {
            try {
                SQLManager.getSQLHelper()._db.endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int exportHtml(Context context, String tableName, String parentPath, String fileName,
                                 List<ColumnField> columnFields, boolean writeHeader, List<String> exportColumns) throws NoSuchFieldException {
        if (!SQLManager.getSQLHelper().isDBOk()) {
            return SqlConstant.FAIL;
        }

        File file = null;
        FileWriter fileWriter = null;
        try {
            file = new File(parentPath, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);

            InputStream stream = context.getResources().getAssets().open("table.html");
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);

            String str;
            while (true) {
                str = reader.readLine();

                if (TextUtils.isEmpty(str)) {
                    reader.close();
                    streamReader.close();
                    stream.close();
                    break;
                }

                if (str.trim().startsWith("<title>")) {
                    fileWriter.write("<title>" + fileName + "</title>");
                    continue;
                }

                if (str.trim().startsWith("<&>")) {

                    if (writeHeader) {
                        fileWriter.write("<tr>");

                        int size;
                        if (exportColumns == null) {
                            size = columnFields.size();

                            for (int i = 0; i < size; i++) {
                                fileWriter.write("<th>");
                                fileWriter.write(columnFields.get(i).getFieldName());
                                fileWriter.write("</th>");
                                if (i == size - 1) {
                                    fileWriter.write(NL);
                                }
                            }
                        } else {
                            size = exportColumns.size();
                            for (int i = 0; i < size; i++) {
                                fileWriter.write("<th>");
                                fileWriter.write(exportColumns.get(i));
                                fileWriter.write("</th>");
                                if (i == size - 1) {
                                    fileWriter.write(NL);
                                }
                            }
                        }

                        fileWriter.write("</tr>");
                    }

                    String selection;
                    if (exportColumns == null) {
                        selection = "*";
                    } else {
                        StringBuilder bf = new StringBuilder();
                        int size = exportColumns.size();
                        for (int i = 0; i < size; i++) {
                            bf.append(SQLTools.processColumnName(exportColumns.get(i)));
                            if (i != size - 1) {
                                bf.append(",");
                            }
                        }
                        selection = bf.toString();
                    }

                    String sql = "SELECT " + selection + " FROM '" + tableName + "'";
                    Logger.d("export--html---" + sql);
                    Cursor cursor = SQLManager.getSQLHelper()._db.rawQuery(sql, null);
                    if (cursor == null) return SqlConstant.FAIL;

                    String[] columnName = cursor.getColumnNames();
                    while (cursor.moveToNext()) {

                        String value;
                        fileWriter.write("<tr>");
                        for (String aColumnName : columnName) {

                            try {
                                value = cursor.getString(cursor.getColumnIndex(aColumnName));
                                if (TextUtils.isEmpty(value)) {
                                    value = "";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                                byte[] bytes = cursor.getBlob(cursor.getColumnIndex(aColumnName));
                                if (bytes != null) {
                                    value = "BLOB (size: " + bytes.length + ")";
                                } else {
                                    value = "";
                                }
                            }

                            fileWriter.write("<td>" + value + "</td>");
                        }
                        fileWriter.write("</tr>" + NL);
                    }
                    cursor.close();
                    continue;
                }

                fileWriter.write(str);
            }

            return SqlConstant.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();

            if (file != null && file.exists()) {
                file.delete();
            }
            return SqlConstant.FAIL;

        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int exportCsvByResult(String parentPath, String fileName,
                                        boolean writeHeader, List<Map<String, TableDataField>> dataList,
                                        List<String> exportColumns) {

        if (dataList == null || dataList.size() <= 0) return SqlConstant.FAIL;

        File file = null;
        FileWriter fileWriter = null;
        try {
            file = new File(parentPath, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);


            Set<String> keySet = dataList.get(0).keySet();
            String[] columns = new String[keySet.size()];
            keySet.toArray(columns);
            int len = columns.length;

            if (writeHeader) {
                if (exportColumns == null) {
                    for (int i = 0; i < len; i++) {

                        fileWriter.write("\"" + columns[i] + "\"");
                        if (i == len - 1) {
                            fileWriter.write(NL);
                        } else {
                            fileWriter.write(",");
                        }
                    }
                } else {

                    int exportLen = exportColumns.size();
                    for (int i = 0; i < exportLen; i++) {
                        fileWriter.write("\"" + exportColumns.get(i) + "\"");
                        if (i == exportLen - 1) {
                            fileWriter.write(NL);
                        } else {
                            fileWriter.write(",");
                        }
                    }
                }
            }

            String columnName;
            TableDataField field;
            String value;
            String valueStr = "";

            if (exportColumns == null) {
                for (Map<String, TableDataField> mapData : dataList) {
                    for (int i = 0; i < len; i++) {
                        columnName = columns[i];
                        field = mapData.get(columnName);

                        if (TextUtils.isEmpty(field.getFieldData())) {
                            value = "";
                        } else {
                            value = "\"" + field.getFieldData() + "\"";
                        }

                        if (i == 0) {
                            valueStr = value;
                        } else {
                            valueStr = valueStr + "," + value;
                        }
                    }

                    Logger.d("--value--" + valueStr);
                    fileWriter.write(valueStr + NL);
                }

            } else {

                for (String column : exportColumns) {
                    for (Map<String, TableDataField> mapData : dataList) {
                        if (mapData.get(column) == null) continue;

                        field = mapData.get(column);

                        if (TextUtils.isEmpty(field.getFieldData())) {
                            value = "";
                        } else {
                            value = field.getFieldData();
                        }

                        fileWriter.write("<td>" + value + "</td>");
                    }

                    fileWriter.write("</tr>" + NL);
                }
            }


            fileWriter.close();
            return SqlConstant.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();

            if (file != null && file.exists()) {
                file.delete();
            }
            return SqlConstant.FAIL;

        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int exportHtmlByResult(Context context, String parentPath, String fileName,
                                         boolean writeHeader, List<Map<String, TableDataField>> dataList,
                                         List<String> exportColumns) {

        File file = null;
        FileWriter fileWriter = null;
        try {
            file = new File(parentPath, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);

            InputStream stream = context.getResources().getAssets().open("table.html");
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);

            String str;
            while (true) {
                str = reader.readLine();

                if (TextUtils.isEmpty(str)) {
                    reader.close();
                    streamReader.close();
                    stream.close();
                    break;
                }

                if (str.trim().startsWith("<title>")) {
                    fileWriter.write("<title>" + fileName + "</title>");
                    continue;
                }

                if (str.trim().startsWith("<&>")) {

                    if (writeHeader) {
                        fileWriter.write("<tr>");
                        if (exportColumns == null) {
                            Set<String> keySet = dataList.get(0).keySet();
                            String[] columns = new String[keySet.size()];
                            keySet.toArray(columns);
                            int len = columns.length;

                            for (int i = 0; i < len; i++) {
                                fileWriter.write("<th>" + columns[i] + "</th>");
                                if (i == len - 1) {
                                    fileWriter.write(NL);
                                }
                            }

                        } else {

                            int len = exportColumns.size();
                            for (int i = 0; i < len; i++) {
                                fileWriter.write("<th>" + exportColumns.get(i) + "</th>");
                                if (i == len - 1) {
                                    fileWriter.write(NL);
                                }
                            }
                        }
                        fileWriter.write("</tr>");
                    }

                    String columnName;
                    TableDataField field;
                    String value;

                    if (exportColumns == null) {

                        Set<String> keySet = dataList.get(0).keySet();
                        for (Map<String, TableDataField> mapData : dataList) {
                            for (String column : keySet) {
                                columnName = column;
                                field = mapData.get(columnName);

                                if (TextUtils.isEmpty(field.getFieldData())) {
                                    value = "";
                                } else {
                                    value = field.getFieldData();
                                }

                                fileWriter.write("<td>" + value + "</td>");
                            }

                            fileWriter.write("</tr>" + NL);
                        }
                    } else {
                        for (String column : exportColumns) {
                            for (Map<String, TableDataField> mapData : dataList) {
                                if (mapData.get(column) == null) continue;

                                field = mapData.get(column);

                                if (TextUtils.isEmpty(field.getFieldData())) {
                                    value = "";
                                } else {
                                    value = field.getFieldData();
                                }

                                fileWriter.write("<td>" + value + "</td>");
                            }

                            fileWriter.write("</tr>" + NL);
                        }
                    }

                    continue;
                }

                fileWriter.write(str);
            }
            return SqlConstant.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();

            if (file != null && file.exists()) {
                file.delete();
            }
            return SqlConstant.FAIL;

        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}