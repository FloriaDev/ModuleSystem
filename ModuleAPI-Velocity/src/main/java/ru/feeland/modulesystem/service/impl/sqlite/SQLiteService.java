package ru.feeland.modulesystem.service.impl.sqlite;

import ru.feeland.modulesystem.aware.NameAware;
import ru.feeland.modulesystem.dto.sql.SqlInsertDTO;
import ru.feeland.modulesystem.dto.sql.SqlRemoveDTO;
import ru.feeland.modulesystem.dto.sql.SqlUpdateDTO;
import ru.feeland.modulesystem.service.impl.sqlite.table.Table;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SQLiteService extends NameAware {

    void createTables();

    void loadCaches();

    Optional<Table> getOptionalTable(String name);

    Table getTable(String name);

    <T> Optional<T> getOptionalTable(Class<T> castClass);

    <T> T getTable(Class<T> castClass);

    List<Table> getTables();

    void connect();

    void close();

    CompletableFuture<Integer> saveToDatabase(SqlInsertDTO dto);

    CompletableFuture<Boolean> updateInDatabase(SqlUpdateDTO dto);

    CompletableFuture<Boolean> removeFromDatabase(SqlRemoveDTO dto);
}
