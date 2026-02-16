package ru.feeland.modulesystem.listener;

import org.bukkit.Bukkit;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.logger.Logger;

public abstract class BaseListener implements Listener {
    private final BaseModuleSystem plugin;
    public static final int SUFFIX_LENGTH = "Listener".length();

    public BaseListener(BaseModuleSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        Logger.info().log("register listener: " + getClass().getSimpleName());
    }

    @Override
    public BaseModuleSystem getPlugin() {
        return plugin;
    }

    @Override
    public String getName() {
        final String className = getClass().getSimpleName();
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }
}
