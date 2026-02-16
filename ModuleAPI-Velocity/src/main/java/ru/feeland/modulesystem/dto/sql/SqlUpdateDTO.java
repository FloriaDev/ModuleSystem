package ru.feeland.modulesystem.dto.sql;

import java.util.Map;

public class SqlUpdateDTO extends SqlRemoveDTO {

    protected Map<String, Object> columnValues;

    public SqlUpdateDTO() {
        super();
    }

    public SqlUpdateDTO(String table, String keyColumn, String keyValue, Map<String, Object> columnValues) {
        super(table, keyColumn, keyValue);
        this.columnValues = columnValues;
    }

    public Map<String, Object> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(Map<String, Object> columnValues) {
        this.columnValues = columnValues;
    }
}

