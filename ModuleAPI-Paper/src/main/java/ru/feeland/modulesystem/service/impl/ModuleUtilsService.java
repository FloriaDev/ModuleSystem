package ru.feeland.modulesystem.service.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.service.BaseService;

import java.util.Map;
import java.util.regex.Pattern;

public class ModuleUtilsService extends BaseService {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Pattern LEGACY_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");
    private static final Map<Character, String> LEGACY_TO_MM = Map.ofEntries(
        Map.entry('0', "<black>"), Map.entry('1', "<dark_blue>"), Map.entry('2', "<dark_green>"),
        Map.entry('3', "<dark_aqua>"), Map.entry('4', "<dark_red>"), Map.entry('5', "<dark_purple>"),
        Map.entry('6', "<gold>"), Map.entry('7', "<gray>"), Map.entry('8', "<dark_gray>"),
        Map.entry('9', "<blue>"), Map.entry('a', "<green>"), Map.entry('b', "<aqua>"),
        Map.entry('c', "<red>"), Map.entry('d', "<light_purple>"), Map.entry('e', "<yellow>"),
        Map.entry('f', "<white>"), Map.entry('k', "<obf>"), Map.entry('l', "<bold>"),
        Map.entry('m', "<strikethrough>"), Map.entry('n', "<underlined>"), Map.entry('o', "<italic>"),
        Map.entry('r', "<reset>")
    );

    public ModuleUtilsService(BaseModuleSystem plugin) {
        super(plugin);
    }

    public void playSoundYes(CommandSender sender) {
        if (sender instanceof Player player) {
            BaseMainConfig baseMainConfig = getPlugin().getConfig(BaseMainConfig.class);
            player.playSound(
                player.getLocation(),
                baseMainConfig.getSound("sounds.soundYes.sound"),
                baseMainConfig.getSoundCategory("sounds.soundYes.soundCategory"),
                baseMainConfig.getFloat("sounds.soundYes.volume"),
                baseMainConfig.getFloat("sounds.soundYes.pitch")
            );
        }
    }

    public void playSoundYes(Player player) {
        BaseMainConfig baseMainConfig = getPlugin().getConfig(BaseMainConfig.class);
        player.playSound(
            player.getLocation(),
            baseMainConfig.getSound("sounds.soundYes.sound"),
            baseMainConfig.getSoundCategory("sounds.soundYes.soundCategory"),
            baseMainConfig.getFloat("sounds.soundYes.volume"),
            baseMainConfig.getFloat("sounds.soundYes.pitch")
        );
    }

    public void playSoundNo(CommandSender sender) {
        if (sender instanceof Player player) {
            BaseMainConfig baseMainConfig = getPlugin().getConfig(BaseMainConfig.class);
            player.playSound(
                player.getLocation(),
                baseMainConfig.getSound("sounds.soundNo.sound"),
                baseMainConfig.getSoundCategory("sounds.soundNo.soundCategory"),
                baseMainConfig.getFloat("sounds.soundNo.volume"),
                baseMainConfig.getFloat("sounds.soundNo.pitch")
            );
        }
    }

    public void playSoundNo(Player player) {
        BaseMainConfig baseMainConfig = getPlugin().getConfig(BaseMainConfig.class);
        player.playSound(
            player.getLocation(),
            baseMainConfig.getSound("sounds.soundNo.sound"),
            baseMainConfig.getSoundCategory("sounds.soundNo.soundCategory"),
            baseMainConfig.getFloat("sounds.soundNo.volume"),
            baseMainConfig.getFloat("sounds.soundNo.pitch")
        );
    }

    public String convertLegacyColors(String input) {
        return LEGACY_PATTERN.matcher(input).replaceAll(m -> {
            char code = m.group(1).toLowerCase().charAt(0);
            return LEGACY_TO_MM.getOrDefault(code, m.group(0));
        });
    }

    public Component formatComponent(
        String input,
        boolean legacy,
        boolean miniMessage,
        boolean disableItalic
    ) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }

        Component component;

        if (legacy) {
            input = convertLegacyColors(input);
        }

        if (miniMessage) {
            component = MINI.deserialize(input);
        } else {
            component = Component.text(input);
        }

        if (disableItalic) {
            component = component.decoration(TextDecoration.ITALIC, false);
        }

        return component;
    }
}
