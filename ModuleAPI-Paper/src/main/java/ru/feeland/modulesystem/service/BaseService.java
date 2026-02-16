package ru.feeland.modulesystem.service;

import ru.feeland.modulesystem.BaseModuleSystem;

public class BaseService implements Service {
    private final BaseModuleSystem plugin;

    public BaseService(BaseModuleSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public BaseModuleSystem getPlugin() {
        return plugin;
    }
}
