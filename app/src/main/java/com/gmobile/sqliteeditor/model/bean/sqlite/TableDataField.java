package com.gmobile.sqliteeditor.model.bean.sqlite;

/**
 * Created by sg on 2016/9/21.
 */
public class TableDataField {

    String fieldName;
    String fieldData;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldData() {
        return fieldData;
    }

    public void setFieldData(String fieldData) {
        this.fieldData = fieldData;
    }

    @Override
    public String toString() {
        return "TableDataField{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldData='" + fieldData + '\'' +
                '}';
    }

}
