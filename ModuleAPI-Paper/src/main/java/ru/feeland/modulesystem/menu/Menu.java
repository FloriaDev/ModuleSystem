package ru.feeland.modulesystem.menu;


import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;
import ru.feeland.modulesystem.aware.PluginAware;

public interface Menu extends PluginAware {

    Player getPlayer();

    void drawItems();

    default Gui getGui() {
        return null;
    }

    default PaginatedGui getPaginatedGui() {
        return null;
    }

    void updateGui();

    void open();
}
