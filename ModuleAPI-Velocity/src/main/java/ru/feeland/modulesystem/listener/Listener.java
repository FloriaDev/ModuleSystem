package ru.feeland.modulesystem.listener;

import ru.feeland.modulesystem.aware.InitAware;
import ru.feeland.modulesystem.aware.NameAware;
import ru.feeland.modulesystem.aware.PluginAware;

public interface Listener extends PluginAware, InitAware, NameAware {
}
