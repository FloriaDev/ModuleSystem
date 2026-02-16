package ru.feeland.modulesystem.service.impl.sqlite;

import org.apache.commons.lang3.StringUtils;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.aware.sql.CreateTableAware;
import ru.feeland.modulesystem.aware.sql.LoadCacheTableAware;
import ru.feeland.modulesystem.dto.sql.SqlInsertDTO;
import ru.feeland.modulesystem.dto.sql.SqlRemoveDTO;
import ru.feeland.modulesystem.dto.sql.SqlUpdateDTO;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.BaseService;
import ru.feeland.modulesystem.service.impl.sqlite.table.Table;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class BaseSQLiteService extends BaseService implements SQLiteService {
    public static final int SUFFIX_LENGTH = "SQLiteService".length();
    protected List<Table> tables;
    protected Connection connection;
    private final String moduleName;

    public BaseSQLiteService(BaseModuleSystem plugin, String moduleName) {
        super(plugin);
        this.moduleName = moduleName;
    }

    @Override
    public String getName() {
        final String className = getClass().getSimpleName();
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }

    @Override
    public void createTables() {
        tables = createTableInstances();
        getTables().forEach(CreateTableAware::createTable);
    }

    @Override
    public void loadCaches() {
        getTables().forEach(LoadCacheTableAware::loadCache);
    }

    @Override
    public Optional<Table> getOptionalTable(String name) {
        return tables.stream()
            .filter(table -> StringUtils.equals(table.getName(), name))
            .findFirst();
    }

    @Override
    public Table getTable(String name) {
        return getOptionalTable(name).orElseThrow(() -> {
            Logger.error().log("Ошибка таблицы в классе {}", getClass().getSimpleName());
            return new IllegalStateException();
        });
    }

    @Override
    public <T> Optional<T> getOptionalTable(Class<T> castClass) {
        return tables.stream().filter(castClass::isInstance).map(castClass::cast).findFirst();
    }

    @Override
    public <T> T getTable(Class<T> castClass) {
        return getOptionalTable(castClass).orElseThrow(() -> {
            Logger.error().log("Ошибка таблицы в классе {}", getClass().getSimpleName());
            return new IllegalStateException();
        });
    }

    @Override
    public List<Table> getTables() {
        return tables;
    }

    protected abstract List<Table> createTableInstances();

    public void connect() {
        File dataFolder = new File(getPlugin().getDataFolder(), "modules" + File.separator + moduleName);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, getName() + ".db");
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            createTables();
            loadCaches();

            Logger.info().log("Подключение к базе {} успешно установлено", getName());
        } catch (ClassNotFoundException | SQLException e) {
            Logger.error().log("Ошибка при подключении к базе {}", getName());
            Logger.error().log("", e);
            getPlugin().getServer().getPluginManager().disablePlugin(getPlugin());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            Logger.error().log("Ошибка при закрытии подключения к базе {}", getName());
            Logger.error().log("", e);
        }
    }

    @Override
    public CompletableFuture<Integer> saveToDatabase(SqlInsertDTO dto, String... conflictColumns) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> columns = dto.getColumns();
            if (columns.isEmpty()) return -1;

            String columnNames = String.join(", ", columns.keySet());
            String placeholders = columns.keySet().stream().map(c -> "?").collect(Collectors.joining(", "));
            String conflictTarget = String.join(", ", conflictColumns);

            String conflictUpdates = columns.keySet().stream()
                .map(c -> c + " = excluded." + c)
                .collect(Collectors.joining(", "));

            String sql = """
            INSERT INTO %s (%s)
            VALUES (%s)
            ON CONFLICT(%s) DO UPDATE SET %s
            """.formatted(
                dto.getTable(),
                columnNames,
                placeholders,
                conflictTarget,
                conflictUpdates
            );

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int idx = 1;
                for (Object value : columns.values()) {
                    ps.setObject(idx++, value);
                }
                ps.executeUpdate();
                return 1;
            } catch (SQLException e) {
                Logger.error().log("Ошибка UPSERT в таблицу {}", dto.getTable());
                Logger.error().log("", e);
                return -1;
            }
        });
    }


    @Override
    public CompletableFuture<Boolean> updateInDatabase(SqlUpdateDTO dto) {
        return CompletableFuture.supplyAsync(() -> {
            if (!dto.hasSetColumns() || !dto.hasWhere()) return false;

            String setClause = dto.getSetColumns().keySet().stream()
                .map(col -> col + " = ?")
                .collect(Collectors.joining(", "));
            String whereClause = dto.getWhere().keySet().stream()
                .map(col -> col + " = ?")
                .collect(Collectors.joining(" AND "));

            String sql = "UPDATE " + dto.getTable() + " SET " + setClause + " WHERE " + whereClause;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int idx = 1;
                for (Object value : dto.getSetColumns().values()) {
                    ps.setObject(idx++, value);
                }
                for (Object value : dto.getWhere().values()) {
                    ps.setObject(idx++, value);
                }

                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                Logger.error().log("Ошибка UPDATE в таблице {}", dto.getTable());
                Logger.error().log("", e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removeFromDatabase(SqlRemoveDTO dto) {
        return CompletableFuture.supplyAsync(() -> {
            if (!dto.hasConditions()) {
                Logger.error().log("DELETE без WHERE запрещён: {}", dto.getTable());
                return false;
            }

            String whereClause = dto.getWhere().keySet().stream()
                .map(col -> col + " = ?")
                .collect(Collectors.joining(" AND "));
            String sql = "DELETE FROM " + dto.getTable() + " WHERE " + whereClause;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int idx = 1;
                for (Object value : dto.getWhere().values()) {
                    ps.setObject(idx++, value);
                }

                int deleted = ps.executeUpdate();

                return deleted > 0;
            } catch (SQLException e) {
                Logger.error().log("Ошибка DELETE в таблице {}", dto.getTable());
                Logger.error().log("", e);
                return false;
            }
        });
    }

    public Connection getConnection() {
        return connection;
    }
}