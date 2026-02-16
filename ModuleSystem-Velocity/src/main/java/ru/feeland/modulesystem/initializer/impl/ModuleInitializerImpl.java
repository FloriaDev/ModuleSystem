package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.ModuleInitializer;
import ru.feeland.modulesystem.module.Module;
import ru.feeland.modulesystem.service.impl.ModuleService;

import java.util.stream.Stream;

public class ModuleInitializerImpl extends BaseInitializer<Module> implements ModuleInitializer {
    protected ModuleInitializerImpl(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Module> getComponentStream() {
        return Stream.of();
    }

    @Override
    public void init() {
        getPlugin().getService(ModuleService.class).loadAll();
    }
}