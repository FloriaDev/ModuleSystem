package ru.feeland.modulesystem.logger.impl;

import org.slf4j.Logger;
import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;

public class InfoLoggerProcessor extends BaseLoggerProcessor {
    public InfoLoggerProcessor(BaseModuleSystemVelocity plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected String prefix() {
        return ConsoleColors.GREEN_BRIGHT + "[INFO] ";
    }

    @Override
    protected boolean isEnabled() {
        return getPlugin().getConfig(BaseMainConfig.class).getBoolean("logger.info");
    }
}
