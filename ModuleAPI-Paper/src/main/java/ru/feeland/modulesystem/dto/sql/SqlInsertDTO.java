package ru.feeland.modulesystem.dto.sql;

import java.util.LinkedHashMap;
import java.util.Map;

public class SqlInsertDTO {
    private final String table;
    private final Map<String, Object> columns;
    private boolean returnGeneratedKeys;

    public SqlInsertDTO(String table) {
        this.table = table;
        this.columns = new LinkedHashMap<>();
    }

    public SqlInsertDTO set(String column, Object value) {
        this.columns.put(column, value);
        return this;
    }

    public String getTable() {
        return table;
    }

    public Map<String, Object> getColumns() {
        return columns;
    }

    public boolean isReturnGeneratedKeys() {
        return returnGeneratedKeys;
    }

    public SqlInsertDTO returnGeneratedKeys(boolean returnGeneratedKeys) {
        this.returnGeneratedKeys = returnGeneratedKeys;
        return this;
    }
}
