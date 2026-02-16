package ru.feeland.modulesystem.service.impl.sqlite.table;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;

public abstract class BaseTable implements Table{
    public static final int SUFFIX_LENGTH = "Table".length();
    private final BaseModuleSystemVelocity plugin;

    public BaseTable(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public BaseModuleSystemVelocity getPlugin() {
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
