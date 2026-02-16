package ru.feeland.modulesystem.dto.sql;

import java.util.LinkedHashMap;
import java.util.Map;

public class SqlRemoveDTO {
    private final String table;
    private final Map<String, Object> where;

    public SqlRemoveDTO(String table) {
        this.table = table;
        this.where = new LinkedHashMap<>();
    }

    public SqlRemoveDTO where(String column, Object value) {
        this.where.put(column, value);
        return this;
    }

    public String getTable() {
        return table;
    }

    public Map<String, Object> getWhere() {
        return where;
    }

    public boolean hasConditions() {
        return !where.isEmpty();
    }
}
