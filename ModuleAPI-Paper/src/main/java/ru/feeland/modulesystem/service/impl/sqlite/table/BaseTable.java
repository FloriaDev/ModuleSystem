package ru.feeland.modulesystem.service.impl.sqlite.table;

import ru.feeland.modulesystem.BaseModuleSystem;

public abstract class BaseTable implements Table{
    public static final int SUFFIX_LENGTH = "Table".length();
    private final BaseModuleSystem plugin;

    public BaseTable(BaseModuleSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public BaseModuleSystem getPlugin() {
        return plugin;
    }

    @Override
    public String getName() {
        final String className = getClass().getSimpleName();
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }

    public abstract void createTable();

    public abstract void loadCache();
}
