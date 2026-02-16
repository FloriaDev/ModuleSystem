package ru.feeland.modulesystem.service.impl.sqlite.table;

import ru.feeland.modulesystem.aware.NameAware;
import ru.feeland.modulesystem.aware.PluginAware;
import ru.feeland.modulesystem.aware.sql.CreateTableAware;
import ru.feeland.modulesystem.aware.sql.LoadCacheTableAware;

public interface Table extends CreateTableAware, LoadCacheTableAware, PluginAware, NameAware {
}
