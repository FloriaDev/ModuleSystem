package ru.feeland.modulesystem.module;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
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
import ru.feeland.modulesystem.service.impl.scheduler.ModuleSchedulerService;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class BaseModule implements Module {
    private static final int SUFFIX_LENGTH = "Module".length();
    private final BaseModuleSystemVelocity plugin;
    private long createTimeMillis;
    private boolean active = false;
    private List<Config> configs;
    private List<Service> services;
    private List<Command> commands;
    private List<Listener> listeners;
    private final Map<Module, WrapperTask> tasks = new ConcurrentHashMap<>();
    private ModuleSchedulerService moduleSchedulerService;

    public BaseModule(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public BaseModuleSystemVelocity getPlugin() {
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

        listeners.forEach(getPlugin().getServer().getEventManager()::unregisterListeners);
        commands.forEach(cmd -> getPlugin().getServer().getCommandManager().unregister(cmd.getName()));
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

    @Override
    public void addTask(WrapperTask wrapperTask, Module module) {
        if (wrapperTask == null || module == null) {
            return;
        }

        if (!module.isEnabled()) {
            wrapperTask.cancel();
            return;
        }

        tasks.put(module, wrapperTask);
    }

    public void removeTasks(Module module) {
        if (module == null) {
            return;
        }

        WrapperTask task = tasks.remove(module);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}