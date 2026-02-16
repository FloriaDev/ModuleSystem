package ru.feeland.modulesystem.logger.impl;

import org.slf4j.Logger;
import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;

public class WarnLoggerProcessor extends BaseLoggerProcessor {
    public WarnLoggerProcessor(BaseModuleSystemVelocity plugin, Logger logger) {
        super(plugin, logger);
    }

    public void log(String msg) {
        getLogger().warn(prefix() + msg);
    }

    public void log(String format, Object arg) {
        getLogger().warn(format, arg);
    }

    public void log(String format, Object... arguments) {
        getLogger().warn(format, arguments);
    }

    public void log(String msg, Throwable t) {
        getLogger().warn(msg, t);
    }

    @Override
    protected String prefix() {
        return ConsoleColors.YELLOW_BRIGHT + "[WARN] ";
    }

    @Override
    protected boolean isEnabled() {
        return getPlugin().getConfig(BaseMainConfig.class).getBoolean("logger.warn");
    }
}
