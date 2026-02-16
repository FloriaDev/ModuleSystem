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

/**
 * Интерфейс, представляющий модуль плагина.
 * Каждый модуль может содержать команды, сервисы, листенеры и конфигурации.
 *
 */
public interface Module extends PluginAware, InitAware, DestroyAware, CreateTimeMillisAware {

    /**
     * Возвращает имя модуля в конфиге.
     *
     * @return имя модуля для конфига
     */
    String getConfigName();

    /**
     * Возвращает имя модуля.
     *
     * @return имя модуля
     */
    String getModuleName();

    /**
     * Проверяет, включен ли модуль в конфигурации.
     *
     * @return true, если модуль включен, false в противном случае
     */
    boolean isEnabled();

    /**
     * Возвращает {@link Stream} команд, принадлежащих этому модулю.
     *
     * @return {@link Stream} команд модуля
     */
    Stream<Command> getCommands();

    /**
     * Возвращает {@link Stream} листенеров, принадлежащих этому модулю.
     *
     * @return {@link Stream} листенеров модуля
     */
    Stream<Listener> getListeners();

    /**
     * Возвращает {@link Stream} конфигураций, принадлежащих этому модулю.
     *
     * @return {@link Stream} конфигураций модуля
     */
    Stream<Config> getConfigs();

    /**
     * Возвращает {@link Stream} сервисов, принадлежащих этому модулю.
     *
     * @return {@link Stream} сервисов модуля
     */
    Stream<Service> getServices();

    ModuleSchedulerService getSchedulerService();

    void addTask(WrapperTask task, Module module);
}