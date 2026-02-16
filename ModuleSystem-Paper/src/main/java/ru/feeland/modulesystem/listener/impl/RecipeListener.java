package ru.feeland.modulesystem.listener.impl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.listener.BaseListener;
import ru.feeland.modulesystem.service.impl.RecipeService;

public class RecipeListener extends BaseListener {
    public RecipeListener(BaseModuleSystem plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        getPlugin().getService(RecipeService.class).handlePrepareItemCraftEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickCraft(InventoryClickEvent event) {
        getPlugin().getService(RecipeService.class).handleInventoryClickEvent(event);
    }
}