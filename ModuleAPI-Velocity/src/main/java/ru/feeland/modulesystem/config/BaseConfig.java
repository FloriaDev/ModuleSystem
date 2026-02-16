package ru.feeland.modulesystem.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public abstract class BaseConfig implements Config {
    public static final int SUFFIX_LENGTH = "Config".length();

    private final BaseModuleSystemVelocity plugin;
    private final Map<String, Object> cache = new HashMap<>();
    private final Path configPath;
    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode rootNode;

    public BaseConfig(BaseModuleSystemVelocity plugin) {
        this.plugin = plugin;
        this.configPath = getPlugin().getDataFolder().resolve(getName() + ".yml");
        this.loader = YamlConfigurationLoader.builder().path(configPath).build();
        loadConfigFromResources(getName() + ".yml");
        reload();
    }

    public BaseConfig(BaseModuleSystemVelocity plugin, String moduleName) {
        this.plugin = plugin;
        Path moduleFolder = getPlugin().getDataFolder().resolve("modules").resolve(moduleName);
        this.configPath = moduleFolder.resolve(getName() + ".yml");
        this.loader = YamlConfigurationLoader.builder().path(configPath).build();

        String resourcePath = getName() + ".yml";
        loadConfigFromModuleJar(moduleFolder, moduleName, resourcePath);

        reload();
    }

    @Override
    public String getName() {
        final String className = getClass().getSimpleName()
            .replace("Base", "")
            .replace("Main", "Config");
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }

    @Override
    public BaseModuleSystemVelocity getPlugin() {
        return plugin;
    }

    private void loadConfigFromResources(String resourceName) {
        if (Files.exists(configPath)) return;

        try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in != null) {
                Files.createDirectories(configPath.getParent());
                Files.copy(in, configPath);
            }
        } catch (IOException e) {
            Logger.error().log("Не удалось скопировать дефолтный конфиг " + resourceName, e);
        }
    }

    private void loadConfigFromModuleJar(Path moduleFolder, String moduleName, String resourcePath) {
        if (Files.exists(configPath)) return;

        try {
            Files.createDirectories(moduleFolder);
        } catch (IOException e) {
            Logger.error().log("Не удалось создать папку модуля", e);
            return;
        }

        Path modulesDir = moduleFolder.getParent();
        if (!Files.exists(modulesDir) || !Files.isDirectory(modulesDir)) return;

        try (var stream = Files.list(modulesDir)) {
            Optional<Path> moduleJar = stream
                .filter(p -> p.getFileName().toString().equalsIgnoreCase(moduleName + ".jar"))
                .findFirst();

            if (moduleJar.isEmpty()) return;

            try (JarFile jar = new JarFile(moduleJar.get().toFile())) {
                ZipEntry entry = jar.getEntry(resourcePath);
                if (entry != null) {
                    try (InputStream in = jar.getInputStream(entry)) {
                        Files.createDirectories(configPath.getParent());
                        Files.copy(in, configPath);
                    }
                }
            }
        } catch (IOException e) {
            Logger.error().log("Ошибка при извлечении конфига модуля " + moduleName, e);
        }
    }

    protected String getConfigName() {
        return configPath.getFileName().toString();
    }

    private void logMissingPath(String path) {
        String callerInfo = getCallerInfo();
        Logger.warn().log("[Config] В файле '" + getConfigName() + "' путь '" + path + "' отсутствует. Вызвано из: " + callerInfo);
    }

    private void logInvalidValue(String path, Object badValue, Object defaultValue) {
        String callerInfo = getCallerInfo();
        Logger.warn().log("[Config] В файле '" + getConfigName() + "' путь '" + path + "' имеет неверное значение '" +
            (badValue != null ? badValue : "null") + "': использую дефолт '" + defaultValue + "'. Вызвано из: " + callerInfo);
    }

    private String getCallerInfo() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 5) {
            StackTraceElement caller = stack[5];
            return caller.getFileName() + ":" + caller.getLineNumber();
        }
        return "неизвестный источник";
    }

    @Override
    public void reload() {
        cache.clear();
        try {
            rootNode = loader.load();
            loadToCache("", rootNode);
        } catch (ConfigurateException e) {
            Logger.error().log("Не удалось загрузить конфиг " + configPath, e);
        }
    }

    private void loadToCache(String prefix, CommentedConfigurationNode node) {
        for (Map.Entry<Object, CommentedConfigurationNode> entry : node.childrenMap().entrySet()) {
            String key = entry.getKey().toString();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            CommentedConfigurationNode child = entry.getValue();

            if (!child.virtual()) {
                if (child.childrenMap().isEmpty()) {
                    cache.put(fullKey, child.raw());
                } else {
                    cache.put(fullKey, child);
                    loadToCache(fullKey, child);
                }
            }
        }
    }

    public Object get(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
        }
        return value;
    }

    public void set(String path, Object value) {
        try {
            CommentedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));
            if (value == null) {
                node.set(null);
                cache.remove(path);
            } else {
                node.set(value);
                cache.put(path, value);
            }
            loader.save(rootNode);
        } catch (ConfigurateException e) {
            Logger.error().log("Не удалось сохранить значение в конфиг", e);
        }
    }

    public String getString(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return "";
        }
        if (value instanceof String) return (String) value;
        logInvalidValue(path, value, "");
        return "";
    }

    public String getString(String path, Map<String, Object> placeholders) {
        String str = getString(path);
        for (Map.Entry<String, Object> e : placeholders.entrySet()) {
            str = str.replace("%" + e.getKey() + "%", e.getValue() != null ? e.getValue().toString() : "");
        }
        return str;
    }

    public Component getComponent(String path) {
        String str = getString(path);
        if (str.isEmpty()) return Component.empty();
        try {
            return MiniMessage.miniMessage().deserialize(str).decoration(TextDecoration.ITALIC, false);
        } catch (Exception e) {
            logInvalidValue(path, str, "empty component");
            return Component.empty();
        }
    }

    public Component getComponent(String path, Map<String, Object> placeholders) {
        String str = getString(path);
        if (str.isEmpty()) return Component.empty();

        String processed = str;
        for (Map.Entry<String, Object> e : placeholders.entrySet()) {
            String replacement = e.getValue() instanceof Component
                ? MiniMessage.miniMessage().serialize((Component) e.getValue())
                : String.valueOf(e.getValue());
            processed = processed.replace("%" + e.getKey() + "%", replacement);
        }

        try {
            return MiniMessage.miniMessage().deserialize(processed).decoration(TextDecoration.ITALIC, false);
        } catch (Exception ex) {
            logInvalidValue(path, processed, "empty component");
            return Component.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return Collections.emptyList();
        }
        if (value instanceof List) {
            return (List<String>) value;
        }
        logInvalidValue(path, value, "empty list");
        return Collections.emptyList();
    }

    public List<Component> getComponentList(String path) {
        return getComponentList(path, Collections.emptyMap());
    }

    public List<Component> getComponentList(String path, Map<String, Object> placeholders) {
        List<String> lines = getStringList(path);
        List<Component> result = new ArrayList<>();

        for (String rawLine : lines) {
            if (rawLine == null || rawLine.isBlank()) continue;

            String line = rawLine;
            String currentColorTag = "";

            int lastOpen = -1, lastClose = -1;
            for (int i = line.lastIndexOf('<'); i >= 0; i = line.lastIndexOf('<', i - 1)) {
                int close = line.indexOf('>', i);
                if (close != -1) {
                    String tag = line.substring(i + 1, close);
                    if (!tag.startsWith("/") && !tag.contains("<") && !tag.contains(">")) {
                        lastOpen = i;
                        lastClose = close;
                        break;
                    }
                }
            }
            if (lastOpen != -1) {
                currentColorTag = line.substring(lastOpen, lastClose + 1);
            }

            for (Map.Entry<String, Object> e : placeholders.entrySet()) {
                line = line.replace("%" + e.getKey() + "%", String.valueOf(e.getValue()));
            }

            line = line.replace("<newline>", "\n");
            for (String part : line.split("\n")) {
                if (part.isBlank()) continue;
                String colored = currentColorTag + part;
                try {
                    result.add(MiniMessage.miniMessage().deserialize(colored).decoration(TextDecoration.ITALIC, false));
                } catch (Exception ex) {
                    logInvalidValue(path, colored, "skipping invalid part");
                }
            }
        }
        return result;
    }

    public int getInt(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0;
        }
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            logInvalidValue(path, value, 0);
            return 0;
        }
    }

    public long getLong(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0L;
        }
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            logInvalidValue(path, value, 0L);
            return 0L;
        }
    }

    public double getDouble(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0.0;
        }
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            logInvalidValue(path, value, 0.0);
            return 0.0;
        }
    }

    public float getFloat(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0.0f;
        }
        if (value instanceof Number) return ((Number) value).floatValue();
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {
            logInvalidValue(path, value, 0.0f);
            return 0.0f;
        }
    }

    public boolean getBoolean(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return false;
        }
        if (value instanceof Boolean) return (Boolean) value;
        try {
            return Boolean.parseBoolean(String.valueOf(value));
        } catch (Exception e) {
            logInvalidValue(path, value, false);
            return false;
        }
    }

    public List<String> getSectionKeys(String path) {
        Object obj = cache.get(path);
        if (obj instanceof CommentedConfigurationNode node && !node.virtual()) {
            return node.childrenMap().keySet().stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    public boolean contains(String path) {
        return cache.containsKey(path) || !rootNode.node((Object[]) path.split("\\.")).virtual();
    }
}