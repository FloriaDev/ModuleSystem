package ru.feeland.modulesystem.module;

import org.bukkit.event.HandlerList;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.aware.InitAware;
import ru.feeland.modulesystem.command.Command;
import ru.feeland.modulesystem.config.Config;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.initializer.CommandInitializer;
import ru.feeland.modulesystem.initializer.ConfigInitializer;
import ru.feeland.modulesystem.initializer.ListenerInitializer;
import ru.feeland.modulesystem.initializer.ServiceInitializer;
import ru.feeland.modulesystem.listener.Listener;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.Service;
import ru.feeland.modulesystem.service.impl.CommandSystemService;
import ru.feeland.modulesystem.service.impl.scheduler.ModuleSchedulerService;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class BaseModule implements Module {
    private static final int SUFFIX_LENGTH = "Module".length();
    private final BaseModuleSystem plugin;
    private long createTimeMillis;
    private boolean active = false;
    private List<Config> configs;
    private List<Service> services;
    private List<Command> commands;
    private List<Listener> listeners;
    private final Map<Module, List<WrapperTask>> tasks = new ConcurrentHashMap<>();
    private ModuleSchedulerService moduleSchedulerService;

    public BaseModule(BaseModuleSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public BaseModuleSystem getPlugin() {
        return plugin;
    }

    @Override
    public String getConfigName() {
        final String className = getClass().getSimpleName();
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }

    @Override
    public String getModuleName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isEnabled() {
        return getPlugin().getConfig(BaseMainConfig.class).getBoolean("modules." + getConfigName() + ".enabled");
    }

    @Override
    public void destroy() {
        final ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            try {
                urlClassLoader.close();
            } catch (IOException e) {
                Logger.error().log("caught exception on disable loader", e);
            }
        }

        removeTasks(this);

        configs.forEach(getPlugin().getInitializer(ConfigInitializer.class)::removeComponent);
        services.forEach(getPlugin().getInitializer(ServiceInitializer.class)::removeComponent);
        commands.forEach(getPlugin().getInitializer(CommandInitializer.class)::removeComponent);
        listeners.forEach(getPlugin().getInitializer(ListenerInitializer.class)::removeComponent);

        listeners.forEach(HandlerList::unregisterAll);
        commands.forEach(cmd -> getPlugin().getService(CommandSystemService.class).unregisterCommand(cmd.getName()));
    }

    @Override
    public void init() {
        if (!isEnabled()) return;
        if (active) return;
        active = true;

        configs = getConfigs().toList();
        services = getServices().toList();
        commands = getCommands().toList();
        listeners = getListeners().toList();

        configs.forEach(getPlugin().getInitializer(ConfigInitializer.class)::addComponent);
        services.forEach(getPlugin().getInitializer(ServiceInitializer.class)::addComponent);
        commands.forEach(getPlugin().getInitializer(CommandInitializer.class)::addComponent);
        listeners.forEach(getPlugin().getInitializer(ListenerInitializer.class)::addComponent);

        configs.forEach(initConsumer());
        services.forEach(initConsumer());
        commands.forEach(initConsumer());
        listeners.forEach(initConsumer());
    }

    protected Consumer<Object> initConsumer() {
        return obj -> {if (obj instanceof InitAware initAware) initAware.init();};
    }

    @Override
    public void setCreateTimeMillis(long millis) {
        this.createTimeMillis = millis;
    }

    @Override
    public long getCreateTimeMillis() {
        return createTimeMillis;
    }

    @Override
    public ModuleSchedulerService getSchedulerService() {
        if (moduleSchedulerService == null) {
            moduleSchedulerService = new ModuleSchedulerService(this);
        }
        return moduleSchedulerService;
    }

    public void addTask(WrapperTask task, Module module) {
        tasks.computeIfAbsent(module, m -> new CopyOnWriteArrayList<>()).add(task);
    }

    public void removeTasks(Module module) {
        List<WrapperTask> moduleTasks = tasks.remove(module);
        if (moduleTasks != null) {
            moduleTasks.forEach(task -> {
                if (!task.isCancelled()) {
                    task.cancel();
                }
            });
        }
    }
}