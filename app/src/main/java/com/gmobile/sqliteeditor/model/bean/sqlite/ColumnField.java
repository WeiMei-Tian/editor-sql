package com.gmobile.sqliteeditor.model.bean.sqlite;

/**
 * Created by sg on 2016/9/21.
 */
public class ColumnField {

    private String fieldName;
    private String fieldType;
    private int notNull;
    private int pk;
    private String def;
    private String columnValue;
    private boolean isAutoIncr;

    public ColumnField() {
    }

    public ColumnField(ColumnField columnField) {
        fieldName = columnField.getFieldName();
        fieldType = columnField.getFieldType();
        notNull = columnField.getNotNull();
        pk = columnField.getPk();
        def = columnField.getDef();
        columnValue = columnField.getColumnValue();
        isAutoIncr = columnField.isAutoIncr();
    }

    public boolean isAutoIncr() {
        return isAutoIncr;
    }

    public void setIsAutoIncr(boolean isAutoIncr) {
        this.isAutoIncr = isAutoIncr;
    }

    public String getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(String columnValue) {
        this.columnValue = columnValue;
    }

    public String getDef()
    {
        return this.def;
    }

    public String getFieldName()
    {
        return this.fieldName;
    }

    public String getFieldType()
    {
        return this.fieldType;
    }

    public int getNotNull()
    {
        return this.notNull;
    }

    public int getPk()
    {
        return this.pk;
    }

    public void setDef(String paramString)
    {
        this.def = paramString;
    }

    public void setFieldName(String paramString)
    {
        this.fieldName = paramString;
    }

    public void setFieldType(String paramString)
    {
        this.fieldType = paramString;
    }

    public void setNotNull(int paramInt)
    {
        this.notNull = paramInt;
    }

    public void setPk(int paramInt)
    {
        this.pk = paramInt;
    }

    @Override
    public String toString() {
        return "ColumnField{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", notNull=" + notNull +
                ", pk=" + pk +
                ", def='" + def + '\'' +
                ", columnValue='" + columnValue + '\'' +
                ", isAutoIncr=" + isAutoIncr +
                '}';
    }
}
