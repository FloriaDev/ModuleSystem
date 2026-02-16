package ru.feeland.modulesystem.initializer;

import org.apache.commons.lang3.StringUtils;
import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.aware.AvoidInitializer;
import ru.feeland.modulesystem.aware.DestroyAware;
import ru.feeland.modulesystem.aware.InitAware;
import ru.feeland.modulesystem.aware.PriorityAware;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseInitializer<I> implements Initializer<I> {
    private final BaseModuleSystemVelocity plugin;

    private List<I> components = new ArrayList<>();

    protected BaseInitializer(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public BaseModuleSystemVelocity getPlugin() {
        return plugin;
    }

    @Override
    public void init() {
        setComponents(getComponentStream());

        final Set<? extends I> components = getComponents().stream()
                .filter(InitAware.class::isInstance)
                .collect(Collectors.toSet());
        final Set<? extends I> priorityComponents = components.stream()
                .filter(PriorityAware.class::isInstance)
                .collect(Collectors.toSet());

        priorityComponents.stream()
                .sorted(Comparator.comparing(component -> ((PriorityAware) component).getPriority()))
                .forEach(component -> ((InitAware) component).init());

        components.removeAll(priorityComponents);
        components.forEach(component -> ((InitAware) component).init());
    }

    protected abstract Stream<? extends I> getComponentStream();

    @Override
    public void setComponents(Stream<? extends I> components) {
        List<I> newComponents = components.collect(Collectors.toList());
        if (this.components.isEmpty()) {
            this.components = new ArrayList<>(newComponents);
        } else {
            newComponents.stream()
                    .filter(component -> !this.components.contains(component))
                    .filter(component -> !(component instanceof AvoidInitializer))
                    .forEach(this.components::add);
        }
    }

    @Override
    public <T extends I> Optional<T> getComponent(Class<T> clazz) {
        return components.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst();
    }

    @Override
    public <T extends I> void addComponent(T instance) {
        boolean added = false;
        for (int i = 0; i < components.size(); i++) {
            final I obj = components.get(i);
            if (instance.getClass().isInstance(obj)) {
                if (obj instanceof DestroyAware destroyAware) {
                    destroyAware.destroy();
                }
                components.set(i, instance);
                added = true;
            }
        }
        if (!added) {
            components.add(instance);
        }
    }

    @Override
    public boolean removeComponent(String className) {
        return components.removeIf(t -> {
            if (StringUtils.equals(t.getClass().getName(), className)) {
                if (t instanceof DestroyAware destroyAware) {
                    destroyAware.destroy();
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void removeComponent(I component) {
        if (components.remove(component) && component instanceof DestroyAware destroyAware) {
            destroyAware.destroy();
        }
    }

    @Override
    public List<? extends I> getComponents() {
        return components;
    }
}
