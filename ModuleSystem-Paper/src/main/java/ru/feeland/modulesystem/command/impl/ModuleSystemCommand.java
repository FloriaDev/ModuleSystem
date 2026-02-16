package ru.feeland.modulesystem.command.impl;

import ru.feeland.modulesystem.initializer.impl.ConfigInitializerImpl;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.builder.CommandValidationBuilder;
import ru.feeland.modulesystem.command.BaseCommand;
import ru.feeland.modulesystem.config.Config;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.dto.CommandDTO;

import java.util.List;

public class ModuleSystemCommand extends BaseCommand {
    public ModuleSystemCommand(BaseModuleSystem plugin) {
        super(plugin);
    }
    //<editor-fold desc="CommandMethods" defaultstate="collapsed">
    @Override
    public String getPermission() {
        return getPlugin().getConfig(BaseMainConfig.class).getString("commands.modulesystem.permission");
    }

    @Override
    public List<String> getAliases() {
        return List.of("msc");
    }

    @Override
    public boolean validateCommand(CommandDTO dto) {
        return CommandValidationBuilder.command(getPlugin().getConfig(BaseMessagesConfig.class))
            .requiresArgs("commands.reload.usage", 1)
            .validate(dto);
    }

    @Override
    public boolean validateTabComplete(CommandDTO dto) {
        return CommandValidationBuilder.tabComplete(getPlugin().getConfig(BaseMessagesConfig.class))
            .hasPermission(getPlugin().getConfig(BaseMainConfig.class).getString("commands.modulesystem.permission"))
            .validate(dto);
    }
    //</editor-fold>

    @Override
    public boolean command(CommandDTO dto) {
        List<? extends Config> configs = getPlugin().getInitializer(ConfigInitializerImpl.class).getComponents();
        BaseMessagesConfig baseMessagesConfig = getPlugin().getConfig(BaseMessagesConfig.class);

        if (dto.args().length > 0 && dto.args()[0].equalsIgnoreCase("reload")) {
            configs.forEach(Config::reload);
            dto.sender().sendMessage(baseMessagesConfig.getComponent("commands.reload.success"));
            return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandDTO dto) {
        if (dto.args().length == 1) {
            return CommandValidationBuilder.tabComplete(getPlugin().getConfig(BaseMessagesConfig.class))
                    .tabCompleterFilter(List.of("reload"), dto.args()[0]);
        }
        return List.of();
    }
}
