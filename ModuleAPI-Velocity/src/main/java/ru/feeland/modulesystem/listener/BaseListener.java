package ru.feeland.modulesystem.listener;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.logger.Logger;

public abstract class BaseListener implements Listener {
    private final BaseModuleSystemVelocity plugin;
    public static final int SUFFIX_LENGTH = "Listener".length();

    public BaseListener(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        getPlugin().getServer().getEventManager().register(this, plugin);
        Logger.info().log("register listener: " + getClass().getSimpleName());
    }

    @Override
    public BaseModuleSystemVelocity getPlugin() {
        return plugin;
    }

    @Override
    public String getName() {
        String className = getClass().getSimpleName();
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }
}
