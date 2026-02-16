package ru.feeland.modulesystem.listener;

import ru.feeland.modulesystem.module.Module;

public abstract class BaseModuleListener extends BaseListener {
    private final Module module;

    public BaseModuleListener(Module module) {
        super(module.getPlugin());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}