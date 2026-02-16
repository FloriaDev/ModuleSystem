package ru.feeland.modulesystem.builder;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import ru.feeland.modulesystem.config.BaseConfig;
import ru.feeland.modulesystem.config.impl.BaseMessagesConfig;
import ru.feeland.modulesystem.dto.CommandDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandValidationBuilder {
    private final List<Predicate<CommandDTO>> predicates = new ArrayList<>();
    private final BaseConfig config;
    private final BaseMessagesConfig messagesConfig;
    private final boolean command;

    private CommandValidationBuilder(BaseConfig config, boolean command) {
        this.config = config;
        this.command = command;
        this.messagesConfig = config.getPlugin().getConfig(BaseMessagesConfig.class);
    }

    public CommandValidationBuilder isPlayer() {
        predicates.add(dto -> {
            if (dto.source() instanceof Player) return true;

            sendMessage(dto, "onlyPlayers");
            return false;
        });
        return this;
    }

    public CommandValidationBuilder hasPermission(String permission) {
        predicates.add(dto -> {
            if (dto.source().hasPermission(permission)) return true;

            sendMessage(dto, "noPermission");
            return false;
        });
        return this;
    }

    public CommandValidationBuilder requiresArgs(int exact, String path) {
        predicates.add(dto -> {
            if (dto.args().length == exact) return true;
            sendMessage(dto, path);
            return false;
        });
        return this;
    }

    public CommandValidationBuilder requiresArgsAtLeast(int min, String path) {
        predicates.add(dto -> {
            if (dto.args().length >= min) return true;
            sendMessage(dto, path);
            return false;
        });
        return this;
    }

    public List<String> tabCompleterFilter(List<String> types, String input) {
        if (types == null || input == null) {
            return List.of();
        }

        return types.stream()
            .filter(opt -> opt != null && opt.toLowerCase().startsWith(input))
            .collect(Collectors.toList());
    }

    public boolean validate(CommandDTO dto) {
        for (Predicate<CommandDTO> predicate : predicates) {
            boolean test = predicate.test(dto);
            if (!test) {
                return false;
            }
        }
        return true;
    }

    private void sendMessage(CommandDTO dto, String path) {
        if (!command) return;
        CommandSource commandSender = dto.source();
        Component component = config.getComponent(path);
        if (component.children().isEmpty()) {
            component = messagesConfig.getComponent(path);
        }
        if (!component.children().isEmpty()) {
            commandSender.sendMessage(component);
        }
    }

    public static CommandValidationBuilder command(BaseConfig config) {
        return new CommandValidationBuilder(config, true);
    }

    public static CommandValidationBuilder tabComplete(BaseConfig config) {
        return new CommandValidationBuilder(config, false);
    }
}