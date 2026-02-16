package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.aware.PriorityAware;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.ListenerInitializer;
import ru.feeland.modulesystem.listener.Listener;

import java.util.stream.Stream;

public class ListenerInitializerImpl extends BaseInitializer<Listener> implements ListenerInitializer, PriorityAware {
    protected ListenerInitializerImpl(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Listener> getComponentStream() {
        return Stream.of(
        );
    }

    @Override
    public int getPriority() {
        return 5;
    }
}
