package ru.feeland.modulesystem.service.impl.sqlite.table;

import ru.feeland.modulesystem.module.Module;

public abstract class BaseModuleTable extends BaseTable {
    private final Module module;

    public BaseModuleTable(Module module) {
        super(module.getPlugin());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}
