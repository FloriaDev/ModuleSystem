package ru.feeland.modulesystem.dto.sql;

import java.util.LinkedHashMap;
import java.util.Map;

public class SqlUpdateDTO {
    private final String table;
    private final Map<String, Object> setColumns;
    private final Map<String, Object> where;

    public SqlUpdateDTO(String table) {
        this.table = table;
        this.setColumns = new LinkedHashMap<>();
        this.where = new LinkedHashMap<>();
    }

    public SqlUpdateDTO set(String column, Object value) {
        this.setColumns.put(column, value);
        return this;
    }

    public SqlUpdateDTO where(String column, Object value) {
        this.where.put(column, value);
        return this;
    }

    public String getTable() {
        return table;
    }

    public Map<String, Object> getSetColumns() {
        return setColumns;
    }

    public Map<String, Object> getWhere() {
        return where;
    }

    public boolean hasSetColumns() {
        return !setColumns.isEmpty();
    }

    public boolean hasWhere() {
        return !where.isEmpty();
    }
}
