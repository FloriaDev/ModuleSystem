package ru.feeland.modulesystem.config;

import ru.feeland.modulesystem.module.Module;

public abstract class BaseModuleConfig extends BaseConfig {
    private final Module module;

    public BaseModuleConfig(Module module) {
        super(module.getPlugin(), module.getModuleName());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}