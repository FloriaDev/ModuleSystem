package ru.feeland.modulesystem.command.impl;

import net.kyori.adventure.text.Component;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.command.BaseCommand;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.dto.CommandDTO;
import ru.feeland.modulesystem.dto.module.ModuleInfo;
import ru.feeland.modulesystem.service.impl.ModuleService;

import java.util.List;
import java.util.Map;

public class ModulesCommand extends BaseCommand {
    public ModulesCommand(BaseModuleSystem plugin) {
        super(plugin);
    }
    //<editor-fold desc="CommandMethods" defaultstate="collapsed">
    @Override
    public String getPermission() {
        return getPlugin().getConfig(BaseMainConfig.class).getString("commands.modules.permission");
    }
    //</editor-fold>

    @Override
    public boolean command(CommandDTO dto) {
        BaseMessagesConfig messages = getPlugin().getConfig(BaseMessagesConfig.class);
        List<ModuleInfo> modules = getPlugin().getService(ModuleService.class).getModuleInfoList();

        if (modules.isEmpty()) {
            dto.sender().sendMessage(messages.getComponent("commands.modules.noModules"));
            return true;
        }

        dto.sender().sendMessage(messages.getComponent("commands.modules.header"));

        for (ModuleInfo module : modules) {
            String moduleName = module.getModuleName();

            Component nameComponent = messages.getComponent(
                "commands.modules.nameFormat",
                Map.of("module", moduleName)
            );

            Component loadOrUnloadComponent = module.isLoaded()
                ? messages.getComponent(
                    "commands.modules.unloadButton",
                    Map.of("module", moduleName)
                )
                : messages.getComponent(
                    "commands.modules.loadButton",
                    Map.of("module", moduleName)
                );

            Component reloadComponent = module.isLoaded()
                ? messages.getComponent("commands.modules.reloadButton", Map.of("module", moduleName))
                : Component.empty();

            dto.sender().sendMessage(nameComponent
                .append(loadOrUnloadComponent)
                .append(reloadComponent)
            );
        }
        return true;
    }
}
