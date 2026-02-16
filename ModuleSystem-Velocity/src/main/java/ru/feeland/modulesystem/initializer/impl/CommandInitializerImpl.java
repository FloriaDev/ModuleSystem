package ru.feeland.modulesystem.initializer.impl;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.aware.PriorityAware;
import ru.feeland.modulesystem.command.Command;
import ru.feeland.modulesystem.command.impl.ModuleSystemCommand;
import ru.feeland.modulesystem.command.impl.module.ModuleCommand;
import ru.feeland.modulesystem.initializer.BaseInitializer;
import ru.feeland.modulesystem.initializer.CommandInitializer;

import java.util.stream.Stream;

public class CommandInitializerImpl extends BaseInitializer<Command> implements CommandInitializer, PriorityAware {
    protected CommandInitializerImpl(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    protected Stream<? extends Command> getComponentStream() {
        return Stream.of(
            new ModuleCommand(getPlugin()),
            new ModuleSystemCommand(getPlugin())
        );
    }

    @Override
    public int getPriority() {
        return 4;
    }
}
