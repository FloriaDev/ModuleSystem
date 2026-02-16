package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.CoreInitializer;
import ru.feeland.modulesystem.initializer.Initializer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * {@code Initializer}, который производит инициализацию остальных компонентов слоя инициализации
 */
public class CoreInitializerImpl extends BaseInitializer<Initializer> implements CoreInitializer {
    private static final Map<BaseModuleSystem, CoreInitializerImpl> INSTANCE_MAP = new HashMap<>();

    private CoreInitializerImpl(BaseModuleSystem plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Initializer> getComponentStream() {
        return Stream.of(
                new PrimaryInitializer(getPlugin()),
                new ConfigInitializerImpl(getPlugin()),
                new ServiceInitializerImpl(getPlugin()),
                new ModuleInitializerImpl(getPlugin()),
                new CommandInitializerImpl(getPlugin()),
                new ListenerInitializerImpl(getPlugin())
        );
    }

    public static CoreInitializerImpl of(BaseModuleSystem plugin) {
        return INSTANCE_MAP.computeIfAbsent(plugin, k -> new CoreInitializerImpl(plugin));
    }
}
