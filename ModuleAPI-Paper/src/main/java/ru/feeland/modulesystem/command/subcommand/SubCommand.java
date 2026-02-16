package ru.feeland.modulesystem.command.subcommand;

import ru.feeland.modulesystem.aware.CommandValidateAware;
import ru.feeland.modulesystem.aware.NameAware;
import ru.feeland.modulesystem.dto.CommandDTO;

import java.util.List;

public interface SubCommand extends CommandValidateAware, NameAware {

    default boolean command(CommandDTO dto) {
        return false;
    }

    default List<String> tabComplete(CommandDTO dto) {
        return List.of();
    }
}