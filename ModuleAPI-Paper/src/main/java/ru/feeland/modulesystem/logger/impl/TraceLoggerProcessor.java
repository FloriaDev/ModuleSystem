package ru.feeland.modulesystem.logger.impl;

import org.slf4j.Logger;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;

public class TraceLoggerProcessor extends BaseLoggerProcessor {
    public TraceLoggerProcessor(BaseModuleSystem plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected String prefix() {
        return ConsoleColors.PURPLE_BRIGHT + "[TRACE] ";
    }

    @Override
    protected boolean isEnabled() {
        return getPlugin().getConfig(BaseMainConfig.class).getBoolean("logger.trace");
    }
}
