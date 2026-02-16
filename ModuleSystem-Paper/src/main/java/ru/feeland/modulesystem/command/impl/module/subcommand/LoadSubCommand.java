package ru.feeland.modulesystem.command.impl.module.subcommand;

import ru.feeland.modulesystem.BaseModuleSystem;
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
    public LoadSubCommand(BaseModuleSystem plugin) {
        super(plugin);
    }
    //<editor-fold desc="CommandMethods" defaultstate="collapsed">
    private void sendLoadResult(CommandDTO dto, BaseMessagesConfig messages, ModuleOperationResult result) {
        switch (result.getResultType()) {
            case OK -> dto.sender().sendMessage(messages.getComponent(
                "commands.module.successLoad",
                Map.of("module", result.getModuleName())
            ));
            case NO_MODULE -> dto.sender().sendMessage(messages.getComponent(
                "commands.module.noModule",
                Map.of("module", result.getModuleName())
            ));
            case ERROR_ON_LOAD -> dto.sender().sendMessage(messages.getComponent(
                "commands.module.errorOnLoad",
                Map.of("module", result.getModuleName())
            ));
            case NOT_A_MODULE -> dto.sender().sendMessage(messages.getComponent(
                "commands.module.notAModule",
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
                if (!info.isLoaded()) {
                    ModuleOperationResult result = moduleService.load(info.getModuleName());
                    sendLoadResult(dto, messages, result);
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
                "commands.module.successReloadAll"
            ));
            return true;
        }

        ModuleOperationResult result = moduleService.load(arg);
        sendLoadResult(dto, messages, result);
        return true;
    }


    @Override
    public List<String> tabComplete(CommandDTO dto) {
        if (dto.args().length == 1) {
            final List<String> moduleNames = new java.util.ArrayList<>(getPlugin().getService(ModuleService.class).getModuleInfoList()
                .stream()
                .filter(moduleInfo -> !moduleInfo.isLoaded())
                .map(ModuleInfo::getModuleName)
                .toList());

            moduleNames.add("all");

            return CommandValidationBuilder.tabComplete(getPlugin().getConfig(BaseMessagesConfig.class))
                .tabCompleterFilter(moduleNames, dto.args()[0].toLowerCase());
        }

        return List.of();
    }
}
