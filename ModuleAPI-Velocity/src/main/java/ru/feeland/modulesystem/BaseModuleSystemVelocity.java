package ru.feeland.modulesystem;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import ru.feeland.modulesystem.config.Config;
import ru.feeland.modulesystem.initializer.ConfigInitializer;
import ru.feeland.modulesystem.initializer.CoreInitializer;
import ru.feeland.modulesystem.initializer.Initializer;
import ru.feeland.modulesystem.initializer.ServiceInitializer;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.Service;

import java.nio.file.Path;
import java.util.Optional;

public abstract class BaseModuleSystemVelocity {
    private CoreInitializer coreInitializer;
    private final ProxyServer server;
    private final org.slf4j.Logger logger;
    private final Path dataFolder;

    public BaseModuleSystemVelocity(ProxyServer server, org.slf4j.Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    public ProxyServer getServer() {
        return server;
    }

    public org.slf4j.Logger getLogger() {
        return logger;
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    public <T extends Initializer> Optional<T> getOptionalInitializer(Class<T> clazz) {
        return coreInitializer.getComponent(clazz);
    }

    public <T extends Initializer> T getInitializer(Class<T> clazz) {
        return getOptionalInitializer(clazz).orElseThrow(() -> {
            Logger.error().log("Ошибка инициализатора в классе {}", clazz.getSimpleName());
            return new IllegalStateException();
        });
    }

    public <T extends Service> Optional<T> getOptionalService(Class<T> clazz) {
        return getOptionalInitializer(ServiceInitializer.class).flatMap(initializer -> initializer.getComponent(clazz));
    }

    public <T extends Service> T getService(Class<T> clazz) {
        return getInitializer(ServiceInitializer.class).getComponent(clazz).orElseThrow(() -> {
            Logger.error().log("Ошибка сервиса в классе {}", clazz.getSimpleName());
            return new IllegalStateException();
        });
    }

    public <T extends Config> Optional<T> getOptionalConfig(Class<T> clazz) {
        return getOptionalInitializer(ConfigInitializer.class).flatMap(initializer -> initializer.getComponent(clazz));
    }

    public <T extends Config> T getConfig(Class<T> clazz) {
        return getInitializer(ConfigInitializer.class).getComponent(clazz).orElseThrow(() -> {
            Logger.error().log("Ошибка конфигурации в классе {}", clazz.getSimpleName());
            return new IllegalStateException();
        });
    }

    public CoreInitializer getCoreInitializer() {
        return coreInitializer;
    }

    public void setCoreInitializer(CoreInitializer coreInitializer) {
        this.coreInitializer = coreInitializer;
    }
}

