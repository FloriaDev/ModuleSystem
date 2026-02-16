package ru.feeland.modulesystem.logger.impl;

import org.slf4j.Logger;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;

public class DebugLoggerProcessor extends BaseLoggerProcessor {
    public DebugLoggerProcessor(BaseModuleSystem plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected String prefix() {
        return ConsoleColors.BLUE_BRIGHT + "[DEBUG] ";
    }

    @Override
    protected boolean isEnabled() {
        return getPlugin().getConfig(BaseMainConfig.class).getBoolean("logger.debug");
    }
}
