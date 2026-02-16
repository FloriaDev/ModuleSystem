package ru.feeland.modulesystem.builder;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.feeland.modulesystem.config.BaseConfig;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.dto.CommandDTO;
import ru.feeland.modulesystem.service.impl.ModuleUtilsService;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class CommandValidationBuilder {
    private final List<Predicate<CommandDTO>> validators = new ArrayList<>();
    private final BaseConfig config;
    private final BaseMessagesConfig messages;
    private final ModuleUtilsService moduleUtilsService;
    private final boolean sendMessages;

    private CommandValidationBuilder(BaseConfig config, boolean sendMessages) {
        this.config = config;
        this.sendMessages = sendMessages;
        this.messages = config.getPlugin().getConfig(BaseMessagesConfig.class);
        this.moduleUtilsService = config.getPlugin().getService(ModuleUtilsService.class);
    }

    public CommandValidationBuilder isPlayer() {
        validators.add(dto -> {
            if (dto.sender() instanceof Player) return true;
            fail(dto, "onlyPlayers", false);
            return false;
        });
        return this;
    }

    public CommandValidationBuilder hasPermission(String permission) {
        validators.add(dto -> {
            if (dto.sender().hasPermission(permission)) return true;
            fail(dto, "noPermission", true);
            return false;
        });
        return this;
    }

    public CommandValidationBuilder cooldown(Map<UUID, Long> cooldowns, long cooldownMillis) {
        validators.add(dto -> {
            if (!(dto.sender() instanceof Player player)) {
                return true;
            }

            long now = System.currentTimeMillis();
            UUID uuid = player.getUniqueId();

            Long lastUsed = cooldowns.get(uuid);
            if (lastUsed != null) {
                long passed = now - lastUsed;
                if (passed < cooldownMillis) {
                    long secondsLeft = (cooldownMillis - passed) / 1000;

                    fail(dto, "cooldown", true, Map.of("cooldown", String.valueOf(secondsLeft)));

                    return false;
                }
            }

            cooldowns.put(uuid, now);
            return true;
        });

        return this;
    }

    public CommandValidationBuilder isOnline(int index) {
        validators.add(dto -> {
            String arg = getArg(dto, index);
            if (arg == null) {
                fail(dto, "noPlayer", true);
                return false;
            }


            Player target = Bukkit.getPlayerExact(arg);
            if (target == null || !target.isOnline()) {
                fail(dto, "noPlayer", true);
                return false;
            }

            if (dto.sender() instanceof Player sender && !sender.canSee(target)) {
                fail(dto, "noPlayer", true);
                return false;
            }

            return true;
        });
        return this;
    }

    public CommandValidationBuilder hasPlayedBefore(int index) {
        validators.add(dto -> {
            String arg = getArg(dto, index);
            if (arg == null) {
                fail(dto, "noPlayer", true);
                return false;
            }

            OfflinePlayer target = getOfflinePlayer(arg);
            if (!target.hasPlayedBefore()) {
                fail(dto, "noPlayer", true);
                return false;
            }

            return true;
        });
        return this;
    }

    public CommandValidationBuilder requiresArgs(String path, int... lengths) {
        validators.add(dto -> {
            int len = dto.args().length;
            if (Arrays.stream(lengths).anyMatch(v -> v == len)) return true;
            fail(dto, path, true);
            return false;
        });
        return this;
    }

    public CommandValidationBuilder requireArgsAtLeast(String path, int min) {
        validators.add(dto -> {
            if (dto.args().length >= min) return true;
            fail(dto, path, true);
            return false;
        });
        return this;
    }

    public CommandValidationBuilder whenArgs(IntPredicate condition, Function<CommandValidationBuilder, CommandValidationBuilder> block) {
        validators.add(dto -> {
            if (!condition.test(dto.args().length)) return true;
            CommandValidationBuilder nested = new CommandValidationBuilder(config, sendMessages);
            block.apply(nested);
            return nested.validate(dto);
        });
        return this;
    }

    public boolean validate(CommandDTO dto) {
        return validators.stream().allMatch(v -> v.test(dto));
    }

    public List<String> tabCompleterFilter(List<String> types, String input) {
        if (types == null || input == null) {
            return List.of();
        }

        return types.stream().filter(opt -> opt != null && opt.toLowerCase().startsWith(input)).collect(Collectors.toList());
    }

    public List<String> tabCompleterFilterOnlinePlayers(CommandSender sender, String input) {
        if (sender instanceof Player player) {
            return Bukkit.getOnlinePlayers().stream()
                .filter(player::canSee)
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .sorted()
                .toList();
        }

        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
            .sorted()
            .toList();
    }

    public List<String> tabCompleterFilterOnlineUUIDs(CommandSender sender, String input) {
        if (sender instanceof Player player) {
            return Bukkit.getOnlinePlayers().stream()
                .filter(player::canSee)
                .map(onlinePlayer -> onlinePlayer.getUniqueId().toString())
                .filter(uuid -> uuid.toLowerCase().startsWith(input.toLowerCase()))
                .sorted()
                .toList();
        }

        return Bukkit.getOnlinePlayers().stream()
            .map(onlinePlayer -> onlinePlayer.getUniqueId().toString())
            .filter(uuid -> uuid.toLowerCase().startsWith(input.toLowerCase()))
            .sorted()
            .toList();
    }

    private String getArg(CommandDTO dto, int index) {
        return dto.args().length > index ? dto.args()[index] : null;
    }

    private void fail(CommandDTO dto, String path, boolean sound) {
        if (dto.sender() instanceof Player player && sound) {
            moduleUtilsService.playSoundNo(player);
        }

        if (!sendMessages) return;

        Component msg = config.hasPath(path) ? config.getComponent(path) : messages.getComponent(path);

        dto.sender().sendMessage(msg);
    }

    private void fail(CommandDTO dto, String path, boolean sound, Map<String, Object> placeholders) {
        if (dto.sender() instanceof Player player && sound) {
            moduleUtilsService.playSoundNo(player);
        }

        if (!sendMessages) return;

        Component msg = config.hasPath(path) ? config.getComponent(path, placeholders) : messages.getComponent(path, placeholders);

        dto.sender().sendMessage(msg);
    }

    private OfflinePlayer getOfflinePlayer(String input) {
        try {
            return Bukkit.getOfflinePlayer(UUID.fromString(input));
        } catch (IllegalArgumentException ignored) {
            return Bukkit.getOfflinePlayer(input);
        }
    }

    public static CommandValidationBuilder command(BaseConfig config) {
        return new CommandValidationBuilder(config, true);
    }

    public static CommandValidationBuilder tabComplete(BaseConfig config) {
        return new CommandValidationBuilder(config, false);
    }
}
