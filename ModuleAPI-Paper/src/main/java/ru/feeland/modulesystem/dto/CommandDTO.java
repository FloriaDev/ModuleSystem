package ru.feeland.modulesystem.dto;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public record CommandDTO(
        CommandSender sender,
        Command command,
        String label,
        String[] args
) { }
