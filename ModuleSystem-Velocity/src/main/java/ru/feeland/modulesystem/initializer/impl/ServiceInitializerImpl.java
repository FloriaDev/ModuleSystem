package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.aware.PriorityAware;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.ServiceInitializer;
import ru.feeland.modulesystem.service.Service;
import ru.feeland.modulesystem.service.impl.HttpService;
import ru.feeland.modulesystem.service.impl.ModuleService;
import ru.feeland.modulesystem.service.impl.scheduler.SchedulerService;

import java.util.stream.Stream;

public class ServiceInitializerImpl extends BaseInitializer<Service> implements PriorityAware, ServiceInitializer {
    protected ServiceInitializerImpl(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Service> getComponentStream() {
        return Stream.of(
            new HttpService(getPlugin()),
            new ModuleService(getPlugin()),
            new SchedulerService(getPlugin())
        );
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
