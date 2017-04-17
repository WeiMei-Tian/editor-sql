package com.gmobile.sqliteeditor.assistant.sqlite;

import android.text.TextUtils;

import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.model.bean.sqlite.NewColumnField;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by sg on 2016/10/13.
 */
public class SQLTools {

    public static List<String> parseSQLFile(String sqlFilePath) throws IOException {

        List<String> sqlList = new ArrayList<>();

        FileReader localFileReader = new FileReader(sqlFilePath);

        BufferedReader localBufferedReader = new BufferedReader(localFileReader);
        String str1 = "";
        String str2;
        while (true) {
            str2 = localBufferedReader.readLine();

            if (str2 == null) {
                localBufferedReader.close();
                localFileReader.close();
                return sqlList;
            }

            if (str2.startsWith("--")) {
                continue;
            }

            str1 = str1 + str2;
            if (str1.endsWith(";")) {
                sqlList.add(str1.substring(0, str1.length() - 1));
                str1 = "";
            }
        }
    }

    public static String byteArrayToHexString(byte[] paramArrayOfByte) {
        String str = "";
        for (int i = 0; ; i++) {
            if (i >= paramArrayOfByte.length)
                return str;
            str = str + Integer.toString(256 + (0xFF & paramArrayOfByte[i]), 16).substring(1);
        }
    }

    private static byte[] hexStringToByteArray(String paramString) {
        int i = paramString.length();
        byte[] arrayOfByte = new byte[i / 2];
        for (int j = 0; ; j += 2) {
            if (j >= i)
                return arrayOfByte;
            arrayOfByte[(j / 2)] = (byte) ((Character.digit(paramString.charAt(j), 16) << 4) + Character.digit(paramString.charAt(j + 1), 16));
        }
    }

    public static String processColumnName(String columnName) {
        return "[" + columnName + "]";
    }


    //由于一般会出现双重嵌套，但是不确定嵌套组合，故各执行两次
    public static String removeColumnMark(String oldStr) {
        if (TextUtils.isEmpty(oldStr)) return oldStr;

        oldStr = oldStr.replaceAll("^[\\'|\\[|\"|\\(]", "");
        oldStr = oldStr.replaceAll("^[\\'|\\[|\"|\\(]", "");
        oldStr = oldStr.replaceAll("[\\'|\\]|\"|\\)]$", "");
        oldStr = oldStr.replaceAll("[\\'|\\]|\"|\\)]$", "");

        return oldStr;
    }

    public static String addColumnMark(String oldStr){
        return "[" + oldStr + "]";
    }

    public static String processValueMark(String oldStr) {
        if (TextUtils.isEmpty(oldStr)) return oldStr;

        oldStr = oldStr.replaceAll("[\\']", "''");

        return oldStr;
    }

    public static String getColumnSqlStr(NewColumnField columnField) {

        return "'" + columnField.getFieldName() + "' "
                + columnField.getFieldType()
                + " " + (columnField.getSize() <= 0 ? "" : " ( " + columnField.getSize() + " ) ")
                + " " + getConditionStr(columnField);
    }

    public static String getConditionStr(NewColumnField columnField) {
        String conditionStr = "";

        if (columnField.isPrimaryKey()) {
            conditionStr = Constant.PRIMARY_KEY_VALUE + " ";
        }

        if (columnField.isAutoIncrement()) {
            conditionStr = conditionStr + Constant.AUTOINCREMENT_VALUE + " ";
        }

        if (columnField.isNotNull()) {
            conditionStr = conditionStr + Constant.NOT_NULL_VALUE + " ";
        }

        if (columnField.isUnique()) {
            conditionStr = conditionStr + Constant.UNIQUE_KEY_VALUE + " ";
        }

        if (columnField.isDefaultKey()) {
            conditionStr = conditionStr + Constant.DEFAULT_VALUE + " ('" + columnField.getDefaultValue() + "') ";
        }

        if (columnField.isForeignKey()) {
            conditionStr = conditionStr + Constant.FOREIGN_KEY_VALUE + " '" + columnField.getFkTable() + "' ('" + columnField.getFkField() + "') ";
        }

        return conditionStr;
    }

    public static List<String> getConditionList(NewColumnField columnField) {
        List<String> conditions = new LinkedList<>();
        if (columnField.isPrimaryKey()) {
            conditions.add(Constant.PRIMARY_VALUE);
            conditions.add(Constant.KEY_VALUE);
        }

        if (columnField.isAutoIncrement()) {
            conditions.add(Constant.AUTOINCREMENT_VALUE);
        }

        if (columnField.isNotNull()) {
            conditions.add(Constant.NOT_VALUE);
            conditions.add(Constant.NULL_VALUE);
        }

        if (columnField.isUnique()) {
            conditions.add(Constant.UNIQUE_KEY_VALUE);
        }

        if (columnField.isDefaultKey()) {
            conditions.add(Constant.DEFAULT_VALUE);
            conditions.add("('" + columnField.getDefaultValue() + "')");
        }

        if (columnField.isForeignKey()) {
            conditions.add(Constant.FOREIGN_KEY_VALUE);
            conditions.add("'" + columnField.getFkTable() + "'");
            conditions.add("('" + columnField.getFkField() + "')");
        }

        return conditions;
    }
}
