package ru.feeland.modulesystem.initializer;

import ru.feeland.modulesystem.aware.InitAware;
import ru.feeland.modulesystem.aware.PluginAware;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Initializer<I> extends InitAware, PluginAware {

    void setComponents(Stream<? extends I> components);

    <T extends I> Optional<T> getComponent(Class<T> clazz);

    <T extends I> void addComponent(T instance);

    boolean removeComponent(String className);

    default void removeComponent(I component) {
        removeComponent(component.getClass().getName());
    }

    List<? extends I> getComponents();
}
