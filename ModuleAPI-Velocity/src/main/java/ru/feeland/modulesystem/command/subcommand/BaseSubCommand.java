package ru.feeland.modulesystem.command.subcommand;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;

public abstract class BaseSubCommand implements SubCommand {
    public static final int SUFFIX_LENGTH = "SubCommand".length();
    private final BaseModuleSystemVelocity plugin;

    public BaseSubCommand(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        final String className = getClass().getSimpleName();
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }

    public BaseModuleSystemVelocity getPlugin() {
        return plugin;
    }
}