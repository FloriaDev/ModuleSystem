package ru.feeland.modulesystem.command.subcommand;

import ru.feeland.modulesystem.aware.CommandValidateAware;
import ru.feeland.modulesystem.aware.NameAware;
import ru.feeland.modulesystem.dto.CommandDTO;

import java.util.List;

public interface SubCommand extends CommandValidateAware, NameAware {

    boolean command(CommandDTO dto);

    List<String> tabComplete(CommandDTO dto);
}