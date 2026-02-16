package ru.feeland.modulesystem.dto.sql;

import java.util.Map;

public class SqlInsertDTO {
    private final String table;
    private final String keyColumn;
    private final Object keyValue;
    private final Map<String, Object> columnValues;
    private final boolean returnGeneratedKeys;

    public SqlInsertDTO(String table, String keyColumn, Object keyValue, Map<String, Object> columnValues, boolean returnGeneratedKeys) {
        this.table = table;
        this.keyColumn = keyColumn;
        this.keyValue = keyValue;
        this.columnValues = columnValues;
        this.returnGeneratedKeys = returnGeneratedKeys;
    }

    public SqlInsertDTO(String table, String keyColumn, Object keyValue, Map<String, Object> columnValues) {
        this(table, keyColumn, keyValue, columnValues, false);
    }

    public boolean isReturnGeneratedKeys() {
        return returnGeneratedKeys;
    }

    public Map<String, Object> getColumnValues() {
        return columnValues;
    }

    public Object getKeyValue() {
        return keyValue;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public String getTable() {
        return table;
    }
}

