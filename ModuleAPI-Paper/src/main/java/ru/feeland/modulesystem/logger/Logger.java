package ru.feeland.modulesystem.logger;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.logger.impl.*;

public class Logger {
    public static TraceLoggerProcessor TRACE_LOGGER_PROCESSOR;
    public static DebugLoggerProcessor DEBUG_LOGGER_PROCESSOR;
    public static InfoLoggerProcessor INFO_LOGGER_PROCESSOR;
    public static WarnLoggerProcessor WARN_LOGGER_PROCESSOR;
    public static ErrorLoggerProcessor ERROR_LOGGER_PROCESSOR;

    private static BaseModuleSystem plugin;

    public static void init(BaseModuleSystem plugin) {
        if (Logger.plugin != null) return;
        Logger.plugin = plugin;

        TRACE_LOGGER_PROCESSOR = new TraceLoggerProcessor(plugin, logger());
        DEBUG_LOGGER_PROCESSOR = new DebugLoggerProcessor(plugin, logger());
        INFO_LOGGER_PROCESSOR = new InfoLoggerProcessor(plugin, logger());
        WARN_LOGGER_PROCESSOR = new WarnLoggerProcessor(plugin, logger());
        ERROR_LOGGER_PROCESSOR = new ErrorLoggerProcessor(plugin, logger());
    }

    public static org.slf4j.Logger logger() {
        return plugin.getSLF4JLogger();
    }

    public static String getName() {
        return logger().getName();
    }

    public static LoggerProcessor trace() {
        return TRACE_LOGGER_PROCESSOR;
    }

    public static LoggerProcessor debug() {
        return DEBUG_LOGGER_PROCESSOR;
    }

    public static LoggerProcessor info() {
        return INFO_LOGGER_PROCESSOR;
    }

    public static LoggerProcessor warn() {
        return WARN_LOGGER_PROCESSOR;
    }

    public static LoggerProcessor error() {
        return ERROR_LOGGER_PROCESSOR;
    }
}
