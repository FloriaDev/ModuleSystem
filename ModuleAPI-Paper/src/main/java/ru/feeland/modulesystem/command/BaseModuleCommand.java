package ru.feeland.modulesystem.command;

import ru.feeland.modulesystem.module.Module;

public abstract class BaseModuleCommand extends BaseCommand {
    private final Module module;

    public BaseModuleCommand(Module module) {
        super(module.getPlugin());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}