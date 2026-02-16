package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.aware.PriorityAware;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.logger.Logger;

import java.util.stream.Stream;

public class PrimaryInitializer extends BaseInitializer implements PriorityAware {
    protected PrimaryInitializer(BaseModuleSystem plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        Logger.init(getPlugin());
    }

    @Override
    protected Stream getComponentStream() {
        return Stream.empty();
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
