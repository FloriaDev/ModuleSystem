package ru.feeland.modulesystem;

import org.bukkit.plugin.java.JavaPlugin;
import ru.feeland.modulesystem.command.Command;
import ru.feeland.modulesystem.config.Config;
import ru.feeland.modulesystem.initializer.*;
import ru.feeland.modulesystem.listener.Listener;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.Service;

import java.util.Optional;

public abstract class BaseModuleSystem extends JavaPlugin {
    private CoreInitializer coreInitializer;

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

    public <T extends Command> Optional<T> getOptionalCommand(Class<T> clazz) {
        return getOptionalInitializer(CommandInitializer.class).flatMap(initializer -> initializer.getComponent(clazz));
    }

    public <T extends Command> T getCommand(Class<T> clazz) {
        return getInitializer(CommandInitializer.class).getComponent(clazz).orElseThrow(() -> {
            Logger.error().log("Ошибка команды в классе {}", clazz.getSimpleName());
            return new IllegalStateException();
        });
    }

    public <T extends Listener> Optional<T> getOptionalListener(Class<T> clazz) {
        return getOptionalInitializer(ListenerInitializer.class).flatMap(initializer -> initializer.getComponent(clazz));
    }

    public <T extends Listener> T getListener(Class<T> clazz) {
        return getInitializer(ListenerInitializer.class).getComponent(clazz).orElseThrow(() -> {
            Logger.error().log("Ошибка листенера в классе {}", clazz.getSimpleName());
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
