package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.aware.PriorityAware;
import ru.feeland.modulesystem.config.Config;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.ConfigInitializer;

import java.util.stream.Stream;

public class ConfigInitializerImpl extends BaseInitializer<Config> implements PriorityAware, ConfigInitializer {
    protected ConfigInitializerImpl(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Config> getComponentStream() {
        return Stream.of(
                new BaseMainConfig(getPlugin()),
                new BaseMessagesConfig(getPlugin())
        );
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
