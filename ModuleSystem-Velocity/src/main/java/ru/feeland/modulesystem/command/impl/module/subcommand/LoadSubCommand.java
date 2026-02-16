package ru.feeland.modulesystem.command.impl.module.subcommand;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.builder.CommandValidationBuilder;
import ru.feeland.modulesystem.command.subcommand.BaseSubCommand;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.dto.CommandDTO;
import ru.feeland.modulesystem.dto.module.ModuleInfo;
import ru.feeland.modulesystem.dto.module.ModuleOperationResult;
import ru.feeland.modulesystem.service.impl.ModuleService;

import java.util.List;
import java.util.Map;

public class LoadSubCommand extends BaseSubCommand {
    public LoadSubCommand(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    public boolean command(CommandDTO dto) {
        BaseMessagesConfig baseMessagesConfig = getPlugin().getConfig(BaseMessagesConfig.class);
        String className = dto.args()[0] + "Module";
        ModuleOperationResult load = getPlugin().getService(ModuleService.class).load(className);

        switch (load.getResultType()) {
            case OK -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.successLoad",
                Map.of("module", load.getModuleName())
            ));
            case NO_MODULE -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.noModule",
                Map.of("module", load.getModuleName())
            ));
            case ERROR_ON_LOAD -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.errorOnLoad",
                Map.of("module", load.getModuleName())
            ));
            case NOT_A_MODULE -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.notAModule",
                Map.of("module", load.getModuleName())
            ));
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandDTO dto) {
        if (dto.args().length == 1) {
            final List<String> moduleNames = getPlugin().getService(ModuleService.class).getModuleInfoList()
                .stream()
                .filter(moduleInfo -> !moduleInfo.isLoaded())
                .map(ModuleInfo::getModuleName)
                .toList();
            return CommandValidationBuilder.tabComplete(getPlugin().getConfig(BaseMessagesConfig.class))
                .tabCompleterFilter(
                    moduleNames,
                    dto.args()[0].toLowerCase()
                );
        }

        return List.of();
    }
}
