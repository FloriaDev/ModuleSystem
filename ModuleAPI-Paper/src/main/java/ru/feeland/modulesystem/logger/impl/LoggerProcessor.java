package ru.feeland.modulesystem.logger.impl;

public interface LoggerProcessor {

    void log(String msg);

    void log(String format, Object arg);

    void log(String format, Object... arguments);

    void log(String msg, Throwable t);
}
