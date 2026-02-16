package ru.feeland.modulesystem.dto;

import com.velocitypowered.api.command.CommandSource;

public record CommandDTO(
    CommandSource source,
    String[] args,
    String alias
) {
}
