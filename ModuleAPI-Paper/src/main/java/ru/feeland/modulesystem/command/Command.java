package ru.feeland.modulesystem.command;

import org.bukkit.command.TabExecutor;
import ru.feeland.modulesystem.aware.CommandValidateAware;
import ru.feeland.modulesystem.aware.InitAware;
import ru.feeland.modulesystem.aware.NameAware;
import ru.feeland.modulesystem.aware.PluginAware;
import ru.feeland.modulesystem.command.subcommand.SubCommand;
import ru.feeland.modulesystem.dto.CommandDTO;

import java.util.List;
import java.util.stream.Stream;

public interface Command extends TabExecutor, InitAware, PluginAware, CommandValidateAware, NameAware {

    String getPermission();

    List<String> getAliases();

    Stream<SubCommand> getSubCommands();

    default boolean command(CommandDTO dto) {
        return false;
    }

    default List<String> tabComplete(CommandDTO dto) {
        return List.of();
    }
}
