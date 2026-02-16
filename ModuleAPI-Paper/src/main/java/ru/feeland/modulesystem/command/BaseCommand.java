package ru.feeland.modulesystem.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.aware.CommandValidateAware;
import ru.feeland.modulesystem.command.subcommand.SubCommand;
import ru.feeland.modulesystem.dto.CommandDTO;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.impl.CommandSystemService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseCommand implements Command {
    public static final int SUFFIX_LENGTH = "Command".length();
    private final BaseModuleSystem plugin;
    protected final Map<String, SubCommand> subCommands = new HashMap<>();

    public BaseCommand(BaseModuleSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        String commandName = getName().toLowerCase();

        PluginCommand command = getPlugin().getCommand(commandName);

        if (command == null) {
            getPlugin().getService(CommandSystemService.class).registerCommand(commandName, getAliases());
            command = getPlugin().getCommand(commandName);
            if (command == null) {
                Logger.warn().log("unknown command: {}", commandName);
                return;
            }
        }

        Logger.info().log("register command: {}", commandName);
        command.setPermission(getPermission());
        command.setExecutor(this);
        command.setTabCompleter(this);
        getSubCommands().forEach(subCommand -> subCommands.put(subCommand.getName().toLowerCase(), subCommand));
    }

    @Override
    public String getName() {
        final String className = getClass().getSimpleName();
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public BaseModuleSystem getPlugin() {
        return plugin;
    }

    protected boolean handleSubCommand(CommandDTO dto) {
        if (!subCommands.isEmpty()) {
            if (dto.args().length == 0) {
                return false;
            }

            SubCommand subCommand = subCommands.get(dto.args()[0].toLowerCase());
            if (subCommand == null) {
                return false;
            }

            String[] subArgs = new String[dto.args().length - 1];
            System.arraycopy(dto.args(), 1, subArgs, 0, dto.args().length - 1);
            if (!subCommand.validateCommand(dto)) return false;
            return subCommand.command(new CommandDTO(dto.sender(), dto.command(), dto.label(), subArgs));
        }
        return false;
    }

    protected List<String> handleSubCommandTabComplete(CommandDTO dto) {
        if (!subCommands.isEmpty()) {
            if (dto.args().length == 1) {
                String input = dto.args()[0].toLowerCase();
                return subCommands.keySet().stream()
                        .filter(opt -> opt.startsWith(input))
                        .collect(Collectors.toList());
            }

            SubCommand subCommand = subCommands.get(dto.args()[0].toLowerCase());
            if (subCommand == null) {
                return List.of();
            }

            String[] subArgs = new String[dto.args().length - 1];
            System.arraycopy(dto.args(), 1, subArgs, 0, dto.args().length - 1);
            if (!subCommand.validateTabComplete(dto)) return List.of();
            return subCommand.tabComplete(new CommandDTO(dto.sender(), dto.command(), dto.label(), subArgs));
        }
        return List.of();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        CommandDTO dto = new CommandDTO(sender, command, label, args);
        if (!validateCommand(dto)) return false;
        return command(dto);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        CommandDTO dto = new CommandDTO(sender, command, label, args);
        if (!validateTabComplete(dto)) return List.of();
        return tabComplete(dto);
    }

    @Override
    public Stream<SubCommand> getSubCommands() {
        return Stream.empty();
    }
}