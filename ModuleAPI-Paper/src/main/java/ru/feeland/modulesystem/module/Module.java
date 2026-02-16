package ru.feeland.modulesystem.module;


import ru.feeland.modulesystem.aware.CreateTimeMillisAware;
import ru.feeland.modulesystem.aware.DestroyAware;
import ru.feeland.modulesystem.aware.InitAware;
import ru.feeland.modulesystem.aware.PluginAware;
import ru.feeland.modulesystem.command.Command;
import ru.feeland.modulesystem.config.Config;
import ru.feeland.modulesystem.listener.Listener;
import ru.feeland.modulesystem.service.Service;
import ru.feeland.modulesystem.service.impl.scheduler.ModuleSchedulerService;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

import java.util.stream.Stream;

public interface Module extends PluginAware, InitAware, DestroyAware, CreateTimeMillisAware {

    String getConfigName();

    String getModuleName();

    boolean isEnabled();

    Stream<Command> getCommands();

    Stream<Listener> getListeners();

    Stream<Config> getConfigs();

    Stream<Service> getServices();

    ModuleSchedulerService getSchedulerService();

    void addTask(WrapperTask task, Module module);
}