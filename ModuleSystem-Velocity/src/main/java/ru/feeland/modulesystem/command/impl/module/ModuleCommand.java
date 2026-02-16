package ru.feeland.modulesystem.command.impl.module;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.builder.CommandValidationBuilder;
import ru.feeland.modulesystem.command.BaseCommand;
import ru.feeland.modulesystem.command.impl.module.subcommand.LoadSubCommand;
import ru.feeland.modulesystem.command.impl.module.subcommand.ReloadSubCommand;
import ru.feeland.modulesystem.command.impl.module.subcommand.UnloadSubCommand;
import ru.feeland.modulesystem.command.subcommand.SubCommand;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.dto.CommandDTO;

import java.util.List;
import java.util.stream.Stream;

public class ModuleCommand extends BaseCommand {
    public ModuleCommand(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }
    //<editor-fold desc="CommandMethods" defaultstate="collapsed">
    @Override
    public String getPermission() {
        return getPlugin().getConfig(BaseMainConfig.class).getString("commands.module.permission");
    }

    @Override
    public Stream<SubCommand> getSubCommands() {
        return Stream.of(
            new LoadSubCommand(getPlugin()),
            new ReloadSubCommand(getPlugin()),
            new UnloadSubCommand(getPlugin())
        );
    }

    @Override
    public boolean validateCommand(CommandDTO dto) {
        return CommandValidationBuilder.command(getPlugin().getConfig(BaseMessagesConfig.class))
            .requiresArgs(2, "commands.module.usage")
            .validate(dto);
    }

    @Override
    public boolean validateTabComplete(CommandDTO dto) {
        return CommandValidationBuilder.tabComplete(getPlugin().getConfig(BaseMessagesConfig.class))
            .hasPermission(getPlugin().getConfig(BaseMainConfig.class).getString("commands.module.permission"))
            .validate(dto);
    }
    //</editor-fold>

    @Override
    public boolean command(CommandDTO dto) {
        return handleSubCommand(dto);
    }

    @Override
    public List<String> tabComplete(CommandDTO dto) {
        return handleSubCommandTabComplete(dto);
    }
}