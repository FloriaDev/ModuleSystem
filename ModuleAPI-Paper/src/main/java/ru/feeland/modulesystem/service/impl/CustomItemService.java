package ru.feeland.modulesystem.service.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.BaseConfig;
import ru.feeland.modulesystem.service.BaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomItemService extends BaseService {
    public CustomItemService(BaseModuleSystem plugin) {
        super(plugin);
    }

    public ItemStack createItem(String id, Material material, Component name, List<Component> lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(name);
            itemMeta.lore(lore);

            itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(getPlugin(), "custom_item"),
                PersistentDataType.STRING,
                id
            );

            if (enchanted) {
                itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public ItemStack createRecipeItem(String id, Material material, Component name, List<Component> lore, boolean enchanted, boolean custom) {
        ItemStack item = new ItemStack(material);
        if (!custom) return item;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(name);
            itemMeta.lore(lore);

            itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(getPlugin(), "custom_item"),
                PersistentDataType.STRING,
                id
            );

            if (enchanted) {
                itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public ItemStack createMenusItem(String path, BaseConfig menusConfig) {
        ItemStack item = new ItemStack(menusConfig.getMaterial(path + ".material"));
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(menusConfig.getComponent(path + ".name"));
            itemMeta.lore(menusConfig.getComponentList(path + ".lore"));
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public ItemStack createFilterItem(String path, String currentFilter, BaseConfig config) {
        Material material = config.getMaterial(path + ".material");
        Component name = config.getComponent(path + ".name");

        Component activeSymbol   = config.getComponent(path + ".activeSymbol");
        Component inactiveSymbol = config.getComponent(path + ".inactiveSymbol");

        List<Component> filterNames = config.getComponentList(path + ".filters");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);

        List<Component> lore = new ArrayList<>();

        for (Component filterName : filterNames) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(filterName);
            boolean isActive = plainText.equals(currentFilter);

            Component symbol = isActive ? activeSymbol : inactiveSymbol;

            Component line = Component.empty()
                .append(symbol)
                .append(Component.text(" "))
                .append(filterName)
                .decoration(TextDecoration.ITALIC, false);

            lore.add(line);
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createCosmeticItem(
        BaseConfig cosmeticsConfig,
        BaseConfig mainConfig,
        Player player,
        String selectedCosmetic,
        String path,
        String cosmetic,
        String currency,
        String selectTextPath,
        String noPermissionPath,
        String leftClickPath,
        String rightClickPath
    ) {
        int price = cosmeticsConfig.getInt(path + ".price");
        Material material = cosmeticsConfig.getMaterial(path + ".material");
        Component name = cosmeticsConfig.getComponent(path + ".name");
        List<Component> lore = cosmeticsConfig.getComponentList(path + ".lore");

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null) {
            itemMeta.displayName(name);

            String permission = cosmeticsConfig.getString(path + ".permission");
            String loreTextKey;
            if (!player.hasPermission(permission) && !selectedCosmetic.equals(cosmetic)) {
                loreTextKey = noPermissionPath;
            } else {
                loreTextKey = cosmetic.equalsIgnoreCase(selectedCosmetic)
                    ? selectTextPath
                    : leftClickPath;
            }

            if (cosmetic.equals(selectedCosmetic)) {
                itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            lore.add(mainConfig.getComponent(loreTextKey, Map.of("price", price, "currency", currency)));
            lore.add(mainConfig.getComponent(rightClickPath));

            itemMeta.lore(lore);
            item.setItemMeta(itemMeta);
        }

        return item;
    }
}