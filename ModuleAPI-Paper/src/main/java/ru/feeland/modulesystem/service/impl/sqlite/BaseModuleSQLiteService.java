package ru.feeland.modulesystem.service.impl.sqlite;

import ru.feeland.modulesystem.module.Module;

public abstract class BaseModuleSQLiteService extends BaseSQLiteService{
    private final Module module;

    public BaseModuleSQLiteService(Module module) {
        super(module.getPlugin(), module.getModuleName());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}
