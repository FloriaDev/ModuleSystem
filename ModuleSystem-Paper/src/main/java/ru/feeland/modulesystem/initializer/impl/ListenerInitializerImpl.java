package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.aware.PriorityAware;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.ListenerInitializer;
import ru.feeland.modulesystem.listener.Listener;

import java.util.stream.Stream;

/**
 * {@code Initializer}, который производит инициализацию событий
 */
public class ListenerInitializerImpl extends BaseInitializer<Listener> implements ListenerInitializer, PriorityAware {
    protected ListenerInitializerImpl(BaseModuleSystem plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Listener> getComponentStream() {
        return Stream.of(
        );
    }

    /**
     * Возвращает приоритет компонента.
     *
     * @return приоритет компонента
     */
    @Override
    public int getPriority() {
        return 5;
    }
}
