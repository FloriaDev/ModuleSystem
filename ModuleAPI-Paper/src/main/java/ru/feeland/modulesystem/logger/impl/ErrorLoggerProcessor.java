package ru.feeland.modulesystem.logger.impl;

import org.slf4j.Logger;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;

public class ErrorLoggerProcessor extends BaseLoggerProcessor {
    public ErrorLoggerProcessor(BaseModuleSystem plugin, Logger logger) {
        super(plugin, logger);
    }

    public void log(String msg) {
        getLogger().error(prefix() + msg);
    }

    public void log(String format, Object arg) {
        getLogger().error(format, arg);
    }

    public void log(String format, Object... arguments) {
        getLogger().error(format, arguments);
    }

    public void log(String msg, Throwable t) {
        getLogger().error(msg, t);
    }

    @Override
    protected String prefix() {
        return ConsoleColors.RED_BRIGHT + "[ERROR] ";
    }

    @Override
    protected boolean isEnabled() {
        return getPlugin().getConfig(BaseMainConfig.class).getBoolean("logger.error");
    }
}
