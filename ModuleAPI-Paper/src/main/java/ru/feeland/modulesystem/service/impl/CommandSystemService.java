package ru.feeland.modulesystem.service.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.BaseService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class CommandSystemService extends BaseService {
    private Map<String, Command> knownCommands;

    public CommandSystemService(BaseModuleSystem plugin) {
        super(plugin);
        initializeCommandSystem();
    }

    private void initializeCommandSystem() {
        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            knownCommands = Bukkit.getCommandMap().getKnownCommands();
        } catch (Exception e) {
            Logger.error().log("caught exception", e);
        }
    }

    public void registerCommand(String name) {
        if (knownCommands == null) {
            Logger.error().log("knownCommands не инициализирована");
            return;
        }

        String lowerName = name.toLowerCase();

        PluginCommand command = getPlugin().getCommand(lowerName);

        if (command == null) {
            try {
                Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(
                    String.class,
                    Plugin.class
                );
                constructor.setAccessible(true);
                command = constructor.newInstance(lowerName, getPlugin());
            } catch (Exception e) {
                Logger.error().log("Не удалось создать команду {}", lowerName, e);
                return;
            }
        }

        knownCommands.remove(lowerName);
        knownCommands.entrySet().removeIf(entry -> {
            Command cmd = entry.getValue();
            if (cmd instanceof PluginCommand pc) {
                return pc.getName().equalsIgnoreCase(lowerName) || pc.getPlugin() == getPlugin() && pc.getAliases().contains(
                    lowerName);
            }
            return false;
        });

        knownCommands.put(lowerName, command);

        for (String alias : command.getAliases()) {
            String lowerAlias = alias.toLowerCase();
            knownCommands.put(lowerAlias, command);
        }

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    public void registerCommand(String name, List<String> aliases) {
        if (knownCommands == null) {
            Logger.error().log("knownCommands не инициализирована");
            return;
        }
        String lowerName = name.toLowerCase();

        PluginCommand command = getPlugin().getCommand(lowerName);

        if (command == null) {
            try {
                Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(
                    String.class,
                    Plugin.class
                );
                constructor.setAccessible(true);
                command = constructor.newInstance(lowerName, getPlugin());
            } catch (Exception e) {
                Logger.error().log("Не удалось создать команду {}", lowerName, e);
                return;
            }
        }

        if (aliases != null && !aliases.isEmpty()) {
            List<String> cleanAliases = aliases.stream()
                .map(String::toLowerCase)
                .filter(a -> !a.equals(lowerName))
                .distinct()
                .toList();
            command.setAliases(cleanAliases);
        } else {
            command.setAliases(List.of());
        }

        knownCommands.remove(lowerName);
        knownCommands.entrySet().removeIf(entry -> {
            Command cmd = entry.getValue();
            if (cmd instanceof PluginCommand pc) {
                return pc.getName().equalsIgnoreCase(lowerName) || pc.getPlugin() == getPlugin() && pc.getAliases().contains(
                    lowerName);
            }
            return false;
        });

        knownCommands.put(lowerName, command);

        for (String alias : command.getAliases()) {
            String lowerAlias = alias.toLowerCase();
            knownCommands.put(lowerAlias, command);
        }

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    public void unregisterCommand(String name) {
        if (knownCommands == null) {
            Logger.error().log("knownCommands не инициализирована");
            return;
        }

        knownCommands.remove(name.toLowerCase());
        knownCommands.remove(getPlugin().getName().toLowerCase() + ":" + name.toLowerCase());
        knownCommands.values().removeIf(c -> c.getName().equalsIgnoreCase(name)
            || c.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(name)));

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }
}