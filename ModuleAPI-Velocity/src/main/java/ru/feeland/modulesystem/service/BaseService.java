package ru.feeland.modulesystem.service;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;

public class BaseService implements Service {
    private final BaseModuleSystemVelocity plugin;

    public BaseService(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public BaseModuleSystemVelocity getPlugin() {
        return plugin;
    }
}
