package ru.feeland.modulesystem.config;

import ru.feeland.modulesystem.aware.NameAware;
import ru.feeland.modulesystem.aware.PluginAware;

public interface Config extends PluginAware, NameAware {

    void reload();
}
