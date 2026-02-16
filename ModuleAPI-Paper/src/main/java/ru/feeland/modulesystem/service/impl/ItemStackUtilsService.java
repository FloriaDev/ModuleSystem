package ru.feeland.modulesystem.service.impl;

import org.bukkit.inventory.ItemStack;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.service.BaseService;

import java.util.Base64;

public class ItemStackUtilsService extends BaseService {
    public ItemStackUtilsService(BaseModuleSystem plugin) {
        super(plugin);
    }

    public String serializeItemStack(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public ItemStack deserializeItemStack(String data) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(data));
    }

    public String serializeItemStacks(ItemStack [] items) {

        Base64.Encoder encoder = Base64.getEncoder();
        StringBuilder s = new StringBuilder();

        for (ItemStack item : items) {
            s.append(encoder.encodeToString(item.serializeAsBytes()));
            s.append(",");
        }

        return s.toString();
    }

    public ItemStack [] deserializeItemStacks(String base64) {

        Base64.Decoder decoder = Base64.getDecoder();
        String[] base64Items = base64.split(",");
        ItemStack[] items = new ItemStack[base64Items.length];

        for (int i = 0; i < base64Items.length; i++) {
            items[i] = ItemStack.deserializeBytes(decoder.decode(base64Items[i]));
        }

        return items;
    }
}

