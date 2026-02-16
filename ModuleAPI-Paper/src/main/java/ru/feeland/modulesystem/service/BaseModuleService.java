package ru.feeland.modulesystem.service;

import ru.feeland.modulesystem.module.Module;

public abstract class BaseModuleService extends BaseService {
    private final Module module;

    public BaseModuleService(Module module) {
        super(module.getPlugin());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}