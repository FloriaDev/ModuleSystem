package ru.feeland.modulesystem.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.command.subcommand.SubCommand;
import ru.feeland.modulesystem.dto.CommandDTO;
import ru.feeland.modulesystem.logger.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public abstract class BaseCommand implements Command {
    public static final int SUFFIX_LENGTH = "Command".length();
    private final BaseModuleSystemVelocity plugin;
    protected final Map<String, SubCommand> subCommands = new HashMap<>();

    public BaseCommand(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        String commandName = getName().toLowerCase();
        List<String> aliases = getAliases();
        CommandManager commandManager = getPlugin().getServer().getCommandManager();
        CommandMeta meta = commandManager.metaBuilder(commandName)
            .aliases(aliases.toArray(new String[0]))
            .build();
        commandManager.register(meta, this);
        Logger.info().log("register command: {} (aliases: {})", commandName, aliases);
        getSubCommands().forEach(sub -> subCommands.put(sub.getName().toLowerCase(), sub));
    }

    @Override
    public String getName() {
        String className = getClass().getSimpleName();
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
    public BaseModuleSystemVelocity getPlugin() {
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

            String[] subArgs = Arrays.copyOfRange(dto.args(), 1, dto.args().length);

            if (!subCommand.validateCommand(dto)) {
                return false;
            }

            return subCommand.command(new CommandDTO(dto.source(), subArgs, dto.alias()));
        }
        return false;
    }

    protected List<String> handleSubCommandTabComplete(CommandDTO dto) {
        if (!subCommands.isEmpty()) {
            if (dto.args().length == 1) {
                String input = dto.args()[0].toLowerCase();
                return subCommands.keySet().stream()
                    .filter(opt -> opt.startsWith(input))
                    .toList();
            }

            SubCommand subCommand = subCommands.get(dto.args()[0].toLowerCase());
            if (subCommand == null) {
                return List.of();
            }

            String[] subArgs = Arrays.copyOfRange(dto.args(), 1, dto.args().length);

            if (!subCommand.validateTabComplete(dto)) {
                return List.of();
            }

            return subCommand.tabComplete(new CommandDTO(dto.source(), subArgs, dto.alias()));
        }
        return List.of();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        String alias = invocation.alias();

        CommandDTO dto = new CommandDTO(source, args, alias);

        if (!validateCommand(dto)) {
            return;
        }

        command(dto);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        String alias = invocation.alias();

        CommandDTO dto = new CommandDTO(source, args, alias);

        if (!validateTabComplete(dto)) {
            return CompletableFuture.completedFuture(List.of());
        }

        return CompletableFuture.completedFuture(tabComplete(dto));
    }

    @Override
    public Stream<SubCommand> getSubCommands() {
        return Stream.empty();
    }
}