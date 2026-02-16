package ru.feeland.modulesystem.dto.sql;

public class SqlRemoveDTO {

    protected String table;
    protected String keyColumn;
    protected String keyValue;

    public SqlRemoveDTO() {
    }

    public SqlRemoveDTO(String table, String keyColumn, String keyValue) {
        this.table = table;
        this.keyColumn = keyColumn;
        this.keyValue = keyValue;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }
}

