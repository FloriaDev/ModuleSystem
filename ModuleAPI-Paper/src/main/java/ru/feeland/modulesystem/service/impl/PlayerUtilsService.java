package ru.feeland.modulesystem.service.impl;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.service.BaseService;

public class PlayerUtilsService extends BaseService {
    public PlayerUtilsService(BaseModuleSystem plugin) {
        super(plugin);
    }

    public boolean removeExp(Player player, int levelRemove) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        int currentLevel = player.getLevel();
        if (currentLevel < levelRemove) {
            player.sendMessage(getPlugin().getConfig(BaseMessagesConfig.class).getComponent("noLeveL"));
            getPlugin().getService(ModuleUtilsService.class).playSoundNo(player);
            return false;
        }

        player.setLevel(player.getLevel() - levelRemove);
        return true;
    }
}
