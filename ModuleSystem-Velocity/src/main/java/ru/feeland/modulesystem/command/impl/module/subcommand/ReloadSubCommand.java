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

public class ReloadSubCommand extends BaseSubCommand {
    public ReloadSubCommand(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    @Override
    public boolean command(CommandDTO dto) {
        BaseMessagesConfig baseMessagesConfig = getPlugin().getConfig(BaseMessagesConfig.class);
        String className = BaseConstants.BASE_CLASS_NAME + dto.args()[0];
        ModuleOperationResult reload = getPlugin().getService(ModuleService.class).reload(className);

        switch (reload.getResultType()) {
            case OK -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.successReload",
                Map.of("module", reload.getModuleName())
            ));
            case NO_MODULE -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.noModule",
                Map.of("module", reload.getModuleName())
            ));
            case ERROR_ON_LOAD -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.errorOnLoad",
                Map.of("module", reload.getModuleName())
            ));
            case NOT_A_MODULE -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.notAModule",
                Map.of("module", reload.getModuleName())
            ));
            case NOT_LOADED -> dto.source().sendMessage(baseMessagesConfig.getComponent(
                "commands.module.notLoaded",
                Map.of("module", reload.getModuleName())
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