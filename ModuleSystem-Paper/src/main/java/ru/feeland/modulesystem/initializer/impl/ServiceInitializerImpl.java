package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.aware.PriorityAware;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.ServiceInitializer;
import ru.feeland.modulesystem.service.Service;
import ru.feeland.modulesystem.service.impl.*;
import ru.feeland.modulesystem.service.impl.filter.impl.PurchaseFilterService;
import ru.feeland.modulesystem.service.impl.scheduler.SchedulerService;

import java.util.stream.Stream;

public class ServiceInitializerImpl extends BaseInitializer<Service> implements PriorityAware, ServiceInitializer {
    protected ServiceInitializerImpl(BaseModuleSystem plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Service> getComponentStream() {
        return Stream.of(
            new PurchaseFilterService(getPlugin()),
            new CommandSystemService(getPlugin()),
            new CustomItemService(getPlugin()),
            new HttpService(getPlugin()),
            new ItemStackUtilsService(getPlugin()),
            new ModuleService(getPlugin()),
            new ModuleUtilsService(getPlugin()),
            new SchedulerService(getPlugin()),
            new PlayerUtilsService(getPlugin())
        );
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
