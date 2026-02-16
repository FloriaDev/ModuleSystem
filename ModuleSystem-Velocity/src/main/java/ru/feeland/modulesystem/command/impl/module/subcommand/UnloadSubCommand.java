package ru.feeland.modulesystem.command.impl.module.subcommand;

import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.builder.CommandValidationBuilder;
import ru.feeland.modulesystem.command.subcommand.BaseSubCommand;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.constants.BaseConstants;
import ru.feeland.modulesystem.dto.CommandDTO;
import ru.feeland.modulesystem.dto.module.ModuleInfo;
import ru.feeland.modulesystem.dto.module.ModuleOperationResult;
import ru.feeland.modulesystem.service.impl.ModuleService;

import java.util.List;
import java.util.Map;

public class UnloadSubCommand extends BaseSubCommand {
    public UnloadSubCommand(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    public boolean command(CommandDTO dto) {
        BaseMessagesConfig baseMessagesConfig = getPlugin().getConfig(BaseMessagesConfig.class);
        String className = BaseConstants.BASE_CLASS_NAME + dto.args()[0];
        ModuleOperationResult unload = getPlugin().getService(ModuleService.class).unload(className);

        switch (unload.getResultType()) {
            case OK -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.successUnload",
                Map.of("module", unload.getModuleName())
            ));
            case NOT_LOADED -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.notLoaded",
                Map.of("module", unload.getModuleName())
            ));
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandDTO dto) {
        if (dto.args().length == 1) {
            final List<String> moduleNames = getPlugin().getService(ModuleService.class).getModuleInfoList()
                .stream()
                .filter(ModuleInfo::isLoaded)
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