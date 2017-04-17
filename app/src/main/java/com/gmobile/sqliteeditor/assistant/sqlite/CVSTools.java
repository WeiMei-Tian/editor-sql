package com.gmobile.sqliteeditor.assistant.sqlite;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sg on 2016/10/14.
 */
public class CVSTools {

    public static List<String> parseFirst(String cvsFilePath) throws IOException {
        List<String> valuesList = new ArrayList<>();

        InputStream stream = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try {
            stream = new FileInputStream(cvsFilePath);
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);

            String str1;
            while (true) {
                str1 = reader.readLine();
                if (TextUtils.isEmpty(str1)) continue;

                String[] strings = str1.split(",");
                for (String string : strings) {
                    valuesList.add(SQLTools.removeColumnMark(string));
                }
                break;
            }

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return valuesList;
    }

    public static List<String> parseLine(String tableName, String cvsFilePath, List<String> columns,
                                         boolean skipFirstLine, List<Integer> selected) throws IOException {
        List<String> valuesList = new ArrayList<>();

        InputStream stream = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try {
            stream = new FileInputStream(cvsFilePath);
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);

            String str1 = "";
            int line = 0;
            while (true) {

                int index = 0;
                while (index < 5) {
                    index++;
                    str1 = reader.readLine();
                    if (!TextUtils.isEmpty(str1)) break;
                }
                line += 1;

                if (TextUtils.isEmpty(str1)) break;
                if ((skipFirstLine && line == 1)) continue;

                String[] strings = str1.split(",");

                String columnStr = "";
                String valueStr = "";
                String value;
                for (Integer i : selected) {
                    if (TextUtils.isEmpty(columnStr)) {
                        columnStr = SQLTools.processColumnName(columns.get(i));
                    } else {
                        columnStr = columnStr + ", " + SQLTools.processColumnName(columns.get(i));
                    }

                    if (i == 0) {
                        value = SQLTools.removeColumnMark(strings[i]);
                        valueStr = "'" + SQLTools.processValueMark(value) + "'";

                    } else if (i < strings.length) {
                        value = SQLTools.removeColumnMark(strings[i]);
                        valueStr = valueStr + ", " + "'" + SQLTools.processValueMark(value) + "'";

                    } else {

                        valueStr = valueStr + ", " + "''";
                    }
                }
                valuesList.add("INSERT INTO [" + tableName + "] (" + columnStr + ") VALUES (" + valueStr + ")");
            }

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return valuesList;
    }

}
