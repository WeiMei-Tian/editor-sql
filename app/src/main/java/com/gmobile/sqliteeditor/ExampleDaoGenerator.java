package com.gmobile.sqliteeditor;

import java.io.File;
import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class ExampleDaoGenerator {


    private static final int DB_VERSION = 1;

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(DB_VERSION, "com.gmobile.sqliteeditor.orm.dao.model");
        schema.setDefaultJavaPackageDao("com.gmobile.sqliteeditor.orm.dao.base");

        addAppData(schema);
        addHistoryRecord(schema);
        addMineRecord(schema);

        new DaoGenerator().generateAll(schema, getCurrentProjectPath() + "/app/src/main/java/");
    }

    private static String getCurrentProjectPath() throws IOException {
        File directory = new File("");
        String courseFile = directory.getCanonicalPath();
        String tmp = courseFile.substring(3, courseFile.length());

        String projectPath = "";
        String[] array = tmp.split("\\\\");
        for (String anArray : array) {
            projectPath += "/" + anArray;
        }
        return projectPath;
    }

    private static void addAppData(Schema schema) {
        Entity tableName = schema.addEntity("AppData");

        tableName.addIdProperty().autoincrement();
        tableName.addStringProperty("appName");
        tableName.addStringProperty("appPackageName").unique();
        tableName.addLongProperty("lastModified");
        tableName.addIntProperty("isSystem");
    }

    private static void addHistoryRecord(Schema schema) {
        Entity tableName = schema.addEntity("HistoryData");

        tableName.addIdProperty().autoincrement();
        tableName.addStringProperty("name");
        tableName.addStringProperty("path").unique();
        tableName.addStringProperty("appName");
        tableName.addStringProperty("appPackageName");
        tableName.addLongProperty("size");
        tableName.addLongProperty("lastModified");
        tableName.addLongProperty("openTime");
    }

    private static void addMineRecord(Schema schema) {
        Entity tableName = schema.addEntity("MineData");

        tableName.addIdProperty().autoincrement();
        tableName.addStringProperty("name");
        tableName.addStringProperty("path").unique();
        tableName.addLongProperty("size");
        tableName.addLongProperty("lastModified");
        tableName.addLongProperty("openTime");
    }
}
