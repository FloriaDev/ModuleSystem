package ru.feeland.modulesystem.command.impl.module.subcommand;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.builder.CommandValidationBuilder;
import ru.feeland.modulesystem.command.subcommand.BaseSubCommand;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.constants.BaseConstants;
import ru.feeland.modulesystem.dto.CommandDTO;
import ru.feeland.modulesystem.dto.module.ModuleInfo;
import ru.feeland.modulesystem.dto.module.ModuleOperationResult;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.impl.ModuleService;

import java.util.List;
import java.util.Map;

public class UnloadSubCommand extends BaseSubCommand {
    public UnloadSubCommand(BaseModuleSystem plugin) {
        super(plugin);
    }
    //<editor-fold desc="CommandMethods" defaultstate="collapsed">
    private void sendUnloadResult(CommandDTO dto, BaseMessagesConfig messages, ModuleOperationResult result) {
        switch (result.getResultType()) {
            case OK -> dto.sender().sendMessage(messages.getComponent(
                "commands.module.successUnload",
                Map.of("module", result.getModuleName())
            ));
            case NOT_LOADED -> dto.sender().sendMessage(messages.getComponent(
                "commands.module.notLoaded",
                Map.of("module", result.getModuleName())
            ));
        }
    }
    //</editor-fold>

    @Override
    public boolean command(CommandDTO dto) {
        BaseMessagesConfig messages = getPlugin().getConfig(BaseMessagesConfig.class);
        ModuleService moduleService = getPlugin().getService(ModuleService.class);
        String arg = dto.args()[0];

        if (arg.equals("all")) {
            boolean any = false;
            for (ModuleInfo info : moduleService.getModuleInfoList()) {
                if (info.isLoaded()) {
                    String className = BaseConstants.BASE_CLASS_NAME + info.getModuleName();
                    ModuleOperationResult result = moduleService.unload(className);
                    sendUnloadResult(dto, messages, result);
                    any = true;
                }
            }

            if (!any) {
                dto.sender().sendMessage(messages.getComponent(
                    "commands.module.noModules"
                ));
                return false;
            }

            dto.sender().sendMessage(messages.getComponent(
                "commands.module.successUnloadAll"
            ));
            return true;
        }

        String className = BaseConstants.BASE_CLASS_NAME + arg;
        ModuleOperationResult result = moduleService.unload(className);
        sendUnloadResult(dto, messages, result);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandDTO dto) {
        if (dto.args().length == 1) {
            List<String> moduleNames = new java.util.ArrayList<>(getPlugin().getService(ModuleService.class)
                .getModuleInfoList()
                .stream()
                .filter(ModuleInfo::isLoaded)
                .map(ModuleInfo::getModuleName)
                .toList());

            moduleNames.add("all");

            return CommandValidationBuilder.tabComplete(getPlugin().getConfig(BaseMessagesConfig.class))
                .tabCompleterFilter(moduleNames, dto.args()[0].toLowerCase());
        }
        return List.of();
    }
}
