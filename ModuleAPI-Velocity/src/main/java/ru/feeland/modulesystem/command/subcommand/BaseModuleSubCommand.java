package ru.feeland.modulesystem.command.subcommand;

import ru.feeland.modulesystem.module.Module;

public abstract class BaseModuleSubCommand extends BaseSubCommand {
    private final Module module;

    public BaseModuleSubCommand(Module module) {
        super(module.getPlugin());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}