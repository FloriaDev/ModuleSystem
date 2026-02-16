package ru.feeland.modulesystem.aware;

import ru.feeland.modulesystem.dto.CommandDTO;

public interface CommandValidateAware {

    default boolean validateCommand(CommandDTO dto) {
        return true;
    }

    default boolean validateTabComplete(CommandDTO dto) {
        return true;
    }
}
