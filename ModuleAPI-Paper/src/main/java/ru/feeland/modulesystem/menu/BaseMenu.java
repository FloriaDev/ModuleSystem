package ru.feeland.modulesystem.menu;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.BaseConfig;
import ru.feeland.modulesystem.service.impl.CustomItemService;
import ru.feeland.modulesystem.service.impl.filter.BaseFilterService;
import ru.feeland.modulesystem.service.impl.scheduler.SchedulerService;

import java.util.List;

public abstract class BaseMenu implements Menu {
    private final BaseModuleSystem plugin;
    private final Player player;
    private Gui gui;
    private PaginatedGui paginatedGui;

    public BaseMenu(BaseModuleSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    protected Gui createGui(Component title, int rows, boolean disableAllInteractions) {
        if (disableAllInteractions) {
            this.gui = Gui.gui()
                .title(title)
                .rows(rows)
                .disableAllInteractions()
                .create();
        } else {
            this.gui = Gui.gui()
                .title(title)
                .rows(rows)
                .create();
        }
        return this.gui;
    }

    protected PaginatedGui createPaginatedGui(Component title, int rows, int pageSize, boolean disableAllInteractions) {
        if (disableAllInteractions) {
            this.paginatedGui = Gui.paginated()
                .title(title)
                .rows(rows)
                .pageSize(pageSize)
                .disableAllInteractions()
                .create();
        } else {
            this.paginatedGui = Gui.paginated()
                .title(title)
                .rows(rows)
                .pageSize(pageSize)
                .create();
        }
        return this.paginatedGui;
    }

    protected void addMenuItems(String itemsPath, BaseConfig config) {
        CustomItemService customItemService = getPlugin().getService(CustomItemService.class);
        ConfigurationSection itemsSection = config.getSection(itemsPath);
        if (itemsSection == null) return;

        for (String itemKey : itemsSection.getKeys(false)) {
            String itemPath = itemsPath + "." + itemKey;
            String type = config.getString(itemPath + ".type");
            int slot = config.getInt(itemPath + ".slot");
            String command = config.getString(itemPath + ".command");
            List<InventoryAction> actions = config.getInventoryActionList(itemPath + ".actions");
            List<ClickType> clicks = config.getClickTypeList(itemPath + ".clicks");
            switch (type) {
                case "skull" -> {
                    String owner = config.getString(itemPath + ".owner");
                    String texture = config.getString(itemPath + ".texture");
                    GuiItem skullGuiItem = ItemBuilder.skull(customItemService.createMenusItem(itemPath, config))
                        .owner(Bukkit.getOfflinePlayer(owner))
                        .texture(texture)
                        .asGuiItem(event -> {
                            if (!command.isEmpty()) {
                                for (InventoryAction action : actions) {
                                    if (event.getAction() == action) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                                for (ClickType clickType : clicks) {
                                    if (event.getClick() == clickType) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                            }
                        });
                    gui.setItem(slot, skullGuiItem);
                }
                default -> {
                    GuiItem guiItem = ItemBuilder.from(customItemService.createMenusItem(itemPath, config))
                        .asGuiItem(event -> {
                            if (!command.isEmpty()) {
                                for (InventoryAction action : actions) {
                                    if (event.getAction() == action) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                                for (ClickType clickType : clicks) {
                                    if (event.getClick() == clickType) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                            }
                        });
                    gui.setItem(slot, guiItem);
                }
            }
        }
    }

    protected void addPaginatedMenuItems(
        String itemsPath,
        BaseFilterService filterService,
        BaseConfig config
    ) {
        CustomItemService customItemService = getPlugin().getService(CustomItemService.class);
        ConfigurationSection itemsSection = config.getSection(itemsPath);
        if (itemsSection == null) return;
        for (String itemKey : itemsSection.getKeys(false)) {
            String itemPath = itemsPath + "." + itemKey;
            String type = config.getString(itemPath + ".type");
            int slot = config.getInt(itemPath + ".slot");
            String command = config.getString(itemPath + ".command");
            List<InventoryAction> actions = config.getInventoryActionList(itemPath + ".actions");
            List<ClickType> clicks = config.getClickTypeList(itemPath + ".clicks");
            switch (type) {
                case "skull" -> {
                    String owner = config.getString(itemPath + ".owner");
                    String texture = config.getString(itemPath + ".texture");
                    GuiItem skullGuiItem = ItemBuilder.skull(customItemService.createMenusItem(itemPath, config))
                        .owner(Bukkit.getOfflinePlayer(owner))
                        .texture(texture)
                        .asGuiItem(event -> {
                            if (!command.isEmpty()) {
                                for (InventoryAction action : actions) {
                                    if (event.getAction() == action) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                                for (ClickType clickType : clicks) {
                                    if (event.getClick() == clickType) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                            }
                        });
                    paginatedGui.setItem(slot, skullGuiItem);
                }
                case "previous" -> {
                    GuiItem guiItem = ItemBuilder.from(customItemService.createMenusItem(itemPath, config))
                        .asGuiItem(event -> {
                            for (InventoryAction action : actions) {
                                if (event.getAction() == action) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.previous();
                                }
                            }
                            for (ClickType clickType : clicks) {
                                if (event.getClick() == clickType) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.previous();
                                }
                            }
                        });
                    paginatedGui.setItem(slot, guiItem);
                }
                case "next" -> {
                    GuiItem guiItem = ItemBuilder.from(customItemService.createMenusItem(itemPath, config))
                        .asGuiItem(event -> {
                            for (InventoryAction action : actions) {
                                if (event.getAction() == action) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.next();
                                }
                            }
                            for (ClickType clickType : clicks) {
                                if (event.getClick() == clickType) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.next();
                                }
                            }
                        });
                    paginatedGui.setItem(slot, guiItem);
                }
                case "filter" -> {
                    GuiItem filterItem = ItemBuilder.from(customItemService.createFilterItem(
                        itemPath,
                        filterService.getCurrentFilter().toString(),
                        config
                    )).asGuiItem(event -> {
                        for (InventoryAction action : actions) {
                            if (event.getAction() == action) {
                                if (!command.isEmpty()) {
                                    getPlayer().performCommand(command);
                                }
                            }
                        }
                        for (ClickType clickType : clicks) {
                            if (event.getClick() == clickType) {
                                if (!command.isEmpty()) {
                                    getPlayer().performCommand(command);
                                }
                            }
                        }
                        if (event.getClick().isLeftClick()) {
                            filterService.switchFilterNext();
                        }
                        if (event.getClick().isRightClick()) {
                            filterService.switchFilterPrevious();
                        }
                        updateGui();
                    });
                    paginatedGui.setItem(slot, filterItem);
                }
                default -> {
                    GuiItem guiItem = ItemBuilder.from(customItemService.createMenusItem(itemPath, config))
                        .asGuiItem(event -> {
                            if (!command.isEmpty()) {
                                for (InventoryAction action : actions) {
                                    if (event.getAction() == action) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                                for (ClickType clickType : clicks) {
                                    if (event.getClick() == clickType) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                            }
                        });
                    paginatedGui.setItem(slot, guiItem);
                }
            }
        }
    }

    protected void addPaginatedMenuItems(
        String itemsPath,
        BaseConfig config
    ) {
        CustomItemService customItemService = getPlugin().getService(CustomItemService.class);
        ConfigurationSection itemsSection = config.getSection(itemsPath);
        if (itemsSection == null) return;
        for (String itemKey : itemsSection.getKeys(false)) {
            String itemPath = itemsPath + "." + itemKey;
            String type = config.getString(itemPath + ".type");
            int slot = config.getInt(itemPath + ".slot");
            String command = config.getString(itemPath + ".command");
            List<InventoryAction> actions = config.getInventoryActionList(itemPath + ".actions");
            List<ClickType> clicks = config.getClickTypeList(itemPath + ".clicks");
            switch (type) {
                case "skull" -> {
                    String owner = config.getString(itemPath + ".owner");
                    String texture = config.getString(itemPath + ".texture");
                    GuiItem skullGuiItem = ItemBuilder.skull(customItemService.createMenusItem(itemPath, config))
                        .owner(Bukkit.getOfflinePlayer(owner))
                        .texture(texture)
                        .asGuiItem(event -> {
                            if (!command.isEmpty()) {
                                for (InventoryAction action : actions) {
                                    if (event.getAction() == action) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                                for (ClickType clickType : clicks) {
                                    if (event.getClick() == clickType) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                            }
                        });
                    paginatedGui.setItem(slot, skullGuiItem);
                }
                case "previous" -> {
                    GuiItem guiItem = ItemBuilder.from(customItemService.createMenusItem(itemPath, config))
                        .asGuiItem(event -> {
                            for (InventoryAction action : actions) {
                                if (event.getAction() == action) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.previous();
                                }
                            }
                            for (ClickType clickType : clicks) {
                                if (event.getClick() == clickType) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.previous();
                                }
                            }
                        });
                    paginatedGui.setItem(slot, guiItem);
                }
                case "next" -> {
                    GuiItem guiItem = ItemBuilder.from(customItemService.createMenusItem(itemPath, config))
                        .asGuiItem(event -> {
                            for (InventoryAction action : actions) {
                                if (event.getAction() == action) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.next();
                                }
                            }
                            for (ClickType clickType : clicks) {
                                if (event.getClick() == clickType) {
                                    if (!command.isEmpty()) {
                                        getPlayer().performCommand(command);
                                    }
                                    paginatedGui.next();
                                }
                            }
                        });
                    paginatedGui.setItem(slot, guiItem);
                }
                default -> {
                    GuiItem guiItem = ItemBuilder.from(customItemService.createMenusItem(itemPath, config))
                        .asGuiItem(event -> {
                            if (!command.isEmpty()) {
                                for (InventoryAction action : actions) {
                                    if (event.getAction() == action) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                                for (ClickType clickType : clicks) {
                                    if (event.getClick() == clickType) {
                                        getPlayer().performCommand(command);
                                    }
                                }
                            }
                        });
                    paginatedGui.setItem(slot, guiItem);
                }
            }
        }
    }

    @Override
    public BaseModuleSystem getPlugin() {
        return plugin;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Gui getGui() {
        return this.gui;
    }

    @Override
    public PaginatedGui getPaginatedGui() {
        return this.paginatedGui;
    }

    public abstract void drawItems();

    @Override
    public void open() {
        getPlugin().getService(SchedulerService.class).runOnceRegion(
            getPlayer().getLocation(),
            () -> {
                if (gui != null) {
                    gui.open(getPlayer());
                } else if (paginatedGui != null) {
                    paginatedGui.open(getPlayer());
                }
            }
        );
    }

    @Override
    public void updateGui() {
        if (gui != null) {
            gui.clearItems();
        }
        if (paginatedGui != null) {
            paginatedGui.clearPageItems(true);
        }
        drawItems();
        if (gui != null) {
            gui.update();
        }
        if (paginatedGui != null) {
            paginatedGui.update();
        }
    }
}