package ru.feeland.modulesystem.config;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.potion.PotionEffectType;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public abstract class BaseConfig implements Config {
    public static final int SUFFIX_LENGTH = "Config".length();
    private final BaseModuleSystem plugin;
    private final Map<String, Object> cache = new HashMap<>();
    private final File configFile;
    private final YamlConfiguration config;

    public BaseConfig(BaseModuleSystem plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), getName() + ".yml");
        plugin.saveResource(getName() + ".yml", false);
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadToCache("", config);
    }

    public BaseConfig(BaseModuleSystem plugin, String moduleName) {
        File moduleFolder = new File(plugin.getDataFolder(), "modules" + File.separator + moduleName);
        this.plugin = plugin;
        this.configFile = new File(moduleFolder, getName() + ".yml");
        this.config = loadConfigFromModuleJar(moduleFolder, moduleName, getName() + ".yml");
        loadToCache("", config);
    }

    @Override
    public String getName() {
        final String className = getClass().getSimpleName().replace("Base", "").replace("Main", "Config");
        return className.substring(0, className.length() - SUFFIX_LENGTH).toLowerCase();
    }

    protected YamlConfiguration loadConfigFromModuleJar(File moduleFolder, String moduleName, String resourcePath) {
        File targetFile = new File(
            moduleFolder,
            resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath
        );

        if (targetFile.exists()) {
            return YamlConfiguration.loadConfiguration(targetFile);
        }

        if (!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }

        File modulesDir = moduleFolder.getParentFile();

        File moduleJar = null;
        if (modulesDir.exists() && modulesDir.isDirectory()) {
            File[] foundJars = modulesDir.listFiles((dir, name) -> name.equalsIgnoreCase(moduleName + ".jar"));

            if (foundJars != null && foundJars.length > 0) {
                moduleJar = foundJars[0];
            }
        }
        if (moduleJar != null) {
            try (JarFile jar = new JarFile(moduleJar)) {
                ZipEntry jarEntry = jar.getEntry(resourcePath);

                if (jarEntry != null) {
                    try (InputStream in = jar.getInputStream(jarEntry); OutputStream out = Files.newOutputStream(
                        targetFile.toPath())) {
                        in.transferTo(out);
                    } catch (Exception e) {
                        Logger.error().log("caught exception", e);
                    }
                }
            } catch (IOException e) {
                Logger.error().log("caught exception", e);
            }
        }
        return YamlConfiguration.loadConfiguration(targetFile);
    }

    protected String getConfigName() {
        return configFile.getName();
    }

    private void logMissingPath(String path) {
        String callerInfo = getCallerInfo();
        plugin.getLogger().warning("[Config] В файле '" + getConfigName() + "' путь '" + path + "' отсутствует. Вызвано из: " + callerInfo);
    }

    private void logInvalidValue(String path, Object badValue, Object defaultValue) {
        String callerInfo = getCallerInfo();
        plugin.getLogger().warning("[Config] В файле '" + getConfigName() + "' путь '" + path + "' имеет неверное значение '" + (badValue != null ? badValue.toString() : "null") + "': использую дефолт '" + defaultValue + "'. Вызвано из: " + callerInfo);
    }

    private String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 5) {
            StackTraceElement caller = stackTrace[5];
            int lineNumber = caller.getLineNumber();
            String className = caller.getClassName();
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
            return simpleClassName + ".java:" + lineNumber;
        }
        return "неизвестный источник";
    }

    public boolean hasPath(String path) {
        return config.contains(path);
    }

    @Override
    public void reload() {
        cache.clear();

        try {
            config.load(configFile);
        } catch (Exception e) {
            Logger.error().log("Не удалось перезагрузить конфиг", e);
            return;
        }

        loadToCache("", config);
    }

    protected void loadToCache(String prefix, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = section.get(key);
            if (value instanceof ConfigurationSection subSection) {
                cache.put(fullKey, subSection);
                loadToCache(fullKey, subSection);
            } else {
                cache.put(fullKey, value);
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
        config.set(path, value);

        if (value == null) {
            cache.remove(path);
        } else {
            cache.put(path, value);
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            Logger.error().log("caught exception", e);
        }
    }

    public String getString(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        } else {
            logInvalidValue(path, value, "");
            return "";
        }
    }

    public String getString(String path, Map<String, Object> placeholders) {
        String str = getString(path);
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            Object value = entry.getValue();
            str = str.replace(placeholder, value != null ? value.toString() : "");
        }
        return str;
    }

    public Component getComponent(String path) {
        String str = getString(path);
        if (str.isEmpty()) {
            return Component.empty();
        }
        try {
            return MiniMessage.miniMessage().deserialize(str).decoration(TextDecoration.ITALIC, false);
        } catch (Exception e) {
            logInvalidValue(path, str, "empty component");
            return Component.empty();
        }
    }

    public Component getComponent(String path, Player player) {
        String str = getString(path);
        if (str.isEmpty()) {
            return Component.empty();
        }
        str = PlaceholderAPI.setPlaceholders(player, str);

        return MiniMessage.miniMessage().deserialize(str).decoration(TextDecoration.ITALIC, false);
    }

    public Component getComponent(String path, Map<String, Object> placeholders, Player player) {
        String str = getString(path);
        if (str.isEmpty()) {
            return Component.empty();
        }
        str = PlaceholderAPI.setPlaceholders(player, str);

        String processedStr = str;
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String key = "%" + entry.getKey() + "%";
            Object value = entry.getValue();
            String replacement = value instanceof Component ? MiniMessage.miniMessage().serialize((Component) value) : value.toString();
            processedStr = processedStr.replace(key, replacement);
        }

        return MiniMessage.miniMessage().deserialize(processedStr).decoration(TextDecoration.ITALIC, false);
    }

    public Component getComponent(String path, Map<String, Object> placeholders) {
        String str = getString(path);
        if (str.isEmpty()) {
            return Component.empty();
        }

        String processedStr = str;
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String key = "%" + entry.getKey() + "%";
            Object value = entry.getValue();
            String replacement = value instanceof Component ? MiniMessage.miniMessage().serialize((Component) value) : value.toString();
            processedStr = processedStr.replace(key, replacement);
        }

        Component component;
        try {
            component = MiniMessage.miniMessage().deserialize(processedStr).decoration(TextDecoration.ITALIC, false);
        } catch (Exception e) {
            logInvalidValue(path, processedStr, "empty component");
            return Component.empty();
        }

        return component;
    }

    public Component getPlaceholdersComponent(String path, Map<String, Object> placeholders) {
        String str = getString(path);

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            Object value = entry.getValue();
            str = str.replace(placeholder, value != null ? value.toString() : "");
        }

        if (str.isEmpty()) {
            return Component.empty();
        }
        Component component;

        try {
            component = MiniMessage.miniMessage().deserialize(str).decoration(TextDecoration.ITALIC, false);
        } catch (Exception e) {
            logInvalidValue(path, str, "empty component");
            return Component.empty();
        }

        return component;
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
        } else {
            logInvalidValue(path, value, "empty list");
            return Collections.emptyList();
        }
    }

    public List<Component> getComponentList(String path) {
        List<String> stringList = getStringList(path);
        List<Component> componentList = new ArrayList<>();

        for (String line : stringList) {
            if (line == null || line.isEmpty()) {
                continue;
            }

            try {
                Component component = MiniMessage.miniMessage().deserialize(line).decoration(
                    TextDecoration.ITALIC,
                    false
                );

                List<Component> splitComponents = line.contains("\n") ? Arrays.stream(line.split("\n")).filter(part -> !part.isEmpty()).map(
                    part -> MiniMessage.miniMessage().deserialize(part).decoration(
                        TextDecoration.ITALIC,
                        false
                    )).toList() : List.of(component);

                componentList.addAll(splitComponents);
            } catch (Exception e) {
                logInvalidValue(path, line, "skipping invalid line");
            }
        }

        return componentList;
    }

    public List<Component> getComponentList(String path, Map<String, Object> placeholders) {
        List<String> stringList = getStringList(path);
        List<Component> componentList = new ArrayList<>();

        for (String rawLine : stringList) {
            if (rawLine == null || rawLine.isEmpty()) continue;

            String line = rawLine;

            String currentColorTag = "";
            int lastOpen = -1;
            int lastClose = -1;

            for (int i = line.lastIndexOf('<'); i >= 0; i = line.lastIndexOf('<', i - 1)) {
                int close = line.indexOf('>', i);
                if (close != -1) {
                    String possibleTag = line.substring(i + 1, close);
                    if (!possibleTag.startsWith("/") && !possibleTag.contains("<") && !possibleTag.contains(">")) {
                        lastOpen = i;
                        lastClose = close;
                        break;
                    }
                }
            }

            if (lastOpen != -1 && lastClose != -1) {
                currentColorTag = line.substring(lastOpen, lastClose + 1);
            }

            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key != null && value != null) {
                    line = line.replace("%" + key + "%", value.toString());
                }
            }

            line = line.replace("<newline>", "\n");
            for (String part : line.split("\n")) {
                if (part.isEmpty()) continue;

                String coloredPart = currentColorTag + part;
                try {
                    Component component = MiniMessage.miniMessage().deserialize(coloredPart).decoration(
                        TextDecoration.ITALIC,
                        false
                    );
                    componentList.add(component);
                } catch (Exception e) {
                    logInvalidValue(path, coloredPart, "skipping invalid part");
                }
            }
        }

        return componentList;
    }

    public Integer getInt(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            logInvalidValue(path, value, 0);
            return 0;
        }
    }

    public Long getLong(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            logInvalidValue(path, value, 0L);
            return 0L;
        }
    }

    public Double getDouble(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            logInvalidValue(path, value, 0.0);
            return 0.0;
        }
    }

    public Float getFloat(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return 0.0f;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {
            logInvalidValue(path, value, 0.0f);
            return 0.0f;
        }
    }

    public Villager.Profession getVillagerProfession(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Profession not found at path '" + path + "': value is empty");
        }
        String professionId = name.toLowerCase(Locale.ROOT).startsWith("minecraft:") ? name : "minecraft:" + name.toLowerCase(
            Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(professionId);
        if (key == null) {
            throw new IllegalArgumentException("Invalid profession name at path '" + path + "': " + name);
        }
        Villager.Profession profession = Registry.VILLAGER_PROFESSION.get(key);
        if (profession == null) {
            throw new IllegalArgumentException("No villager profession found at path '" + path + "' with name: " + name);
        }
        return profession;
    }

    public Action getAction(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return Action.RIGHT_CLICK_AIR;
        }
        try {
            return Action.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, Action.RIGHT_CLICK_AIR);
            return Action.RIGHT_CLICK_AIR;
        }
    }

    public List<Action> getActionList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        List<Action> result = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            try {
                result.add(Action.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "пропуск неверного действия блока");
            }
        }
        return result;
    }

    public InventoryAction getInventoryAction(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return InventoryAction.CLONE_STACK;
        }
        try {
            return InventoryAction.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, InventoryAction.CLONE_STACK);
            return InventoryAction.CLONE_STACK;
        }
    }

    public List<InventoryAction> getInventoryActionList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        List<InventoryAction> result = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            try {
                result.add(InventoryAction.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "пропуск неверного действия инвентаря");
            }
        }
        return result;
    }

    public ClickType getClickType(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return ClickType.LEFT;
        }
        try {
            return ClickType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, ClickType.LEFT);
            return ClickType.LEFT;
        }
    }

    public List<ClickType> getClickTypeList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClickType> result = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            try {
                result.add(ClickType.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "пропуск неверного ClickType");
            }
        }
        return result;
    }

    public Sound getSound(String path) {
        String name = getString(path);
        if (name == null || (name = name.trim()).isEmpty()) {
            return Sound.UI_TOAST_CHALLENGE_COMPLETE;
        }

        try {
            String soundId = name.toLowerCase(Locale.ROOT);
            if (!soundId.startsWith("minecraft:")) {
                soundId = "minecraft:" + soundId;
            }

            NamespacedKey key = NamespacedKey.fromString(soundId);
            if (key == null) {
                logInvalidValue(path, name, Sound.UI_TOAST_CHALLENGE_COMPLETE);
                return Sound.UI_TOAST_CHALLENGE_COMPLETE;
            }

            Sound sound = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.SOUND_EVENT)
                .get(key);

            if (sound == null) {
                logInvalidValue(path, name, Sound.UI_TOAST_CHALLENGE_COMPLETE);
                return Sound.UI_TOAST_CHALLENGE_COMPLETE;
            }

            return sound;
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, Sound.UI_TOAST_CHALLENGE_COMPLETE);
            return Sound.UI_TOAST_CHALLENGE_COMPLETE;
        }
    }

    public List<Sound> getSoundList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        return names.stream()
            .map(name -> {
                if (name == null || name.trim().isEmpty()) {
                    return null;
                }
                String trimmed = name.trim();
                try {
                    String soundId = trimmed.toLowerCase(Locale.ROOT);
                    if (!soundId.startsWith("minecraft:")) {
                        soundId = "minecraft:" + soundId;
                    }

                    NamespacedKey key = NamespacedKey.fromString(soundId);
                    if (key == null) {
                        logInvalidValue(path, trimmed, "skipping invalid sound");
                        return null;
                    }

                    Sound sound = RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.SOUND_EVENT)
                        .get(key);

                    if (sound == null) {
                        logInvalidValue(path, trimmed, "skipping invalid sound");
                        return null;
                    }

                    return sound;
                } catch (IllegalArgumentException e) {
                    logInvalidValue(path, trimmed, "skipping invalid sound");
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public SoundCategory getSoundCategory(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return SoundCategory.MASTER;
        }
        try {
            return SoundCategory.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, "MASTER (invalid SoundCategory)");
            return SoundCategory.MASTER;
        }
    }

    public List<SoundCategory> getSoundCategoryList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        List<SoundCategory> result = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            try {
                result.add(SoundCategory.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "skipping invalid SoundCategory");
            }
        }
        return result;
    }

    public PortalType getPortalType(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return null;
        }
        try {
            return PortalType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, "null (invalid PortalType)");
            return null;
        }
    }

    public List<PortalType> getPortalTypeList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        List<PortalType> result = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            try {
                result.add(PortalType.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "skipping invalid PortalType");
            }
        }
        return result;
    }

    public Biome getBiome(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return Biome.PLAINS;
        }
        try {
            String biomeId = name.toLowerCase().startsWith("minecraft:") ? name : "minecraft:" + name.toLowerCase();
            NamespacedKey key = NamespacedKey.fromString(biomeId);
            if (key == null) {
                logInvalidValue(path, name, Biome.PLAINS);
                return Biome.PLAINS;
            }
            return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key);
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, Biome.PLAINS);
            return Biome.PLAINS;
        }
    }

    public List<Biome> getBiomeList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        return names.stream().map(name -> {
            try {
                String biomeId = name.toLowerCase().startsWith("minecraft:") ? name : "minecraft:" + name.toLowerCase();
                NamespacedKey key = NamespacedKey.fromString(biomeId);
                if (key == null) return null;
                return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key);
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "skipping invalid biome");
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Particle getParticle(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return Particle.FIREWORK;
        }
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (Exception e) {
            logInvalidValue(path, name, Particle.FIREWORK);
            return Particle.FIREWORK;
        }
    }

    public List<Particle> getParticleList(String path) {
        List<String> names = getStringList(path);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }

        return names.stream().map(name -> {
            try {
                return Particle.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "skipping invalid particle");
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    public PotionEffectType getPotionEffectType(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            logMissingPath(path);
            return null;
        }

        String keyStr = name.toLowerCase(Locale.ROOT);
        if (!keyStr.contains(":")) {
            keyStr = "minecraft:" + keyStr;
        }

        NamespacedKey key = NamespacedKey.fromString(keyStr);
        if (key == null) {
            logInvalidValue(path, name, "null (invalid NamespacedKey)");
            return null;
        }

        PotionEffectType effect = RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(key);

        if (effect == null) {
            logInvalidValue(path, name, "null (effect not found)");
        }

        return effect;
    }

    public ConfigurationSection getSection(String path) {
        Object cached = cache.get(path);
        if (cached == null) {
            logMissingPath(path);
            return null;
        }
        if (cached instanceof ConfigurationSection) {
            return (ConfigurationSection) cached;
        } else {
            logInvalidValue(path, cached, "ожидается ConfigurationSection");
            return null;
        }
    }

    public boolean contains(String path) {
        if (cache.containsKey(path)) {
            return true;
        }
        if (config.isConfigurationSection(path)) {
            cache.put(path, config.getConfigurationSection(path));
            return true;
        }
        return config.contains(path);
    }

    public List<String> getSectionKeys(String path) {
        ConfigurationSection section = getSection(path);
        if (section == null) {
            return Collections.emptyList();
        }
        return List.copyOf(section.getKeys(false));
    }

    public Boolean getBoolean(String path) {
        Object value = cache.get(path);
        if (value == null) {
            logMissingPath(path);
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        try {
            return Boolean.parseBoolean(String.valueOf(value));
        } catch (Exception e) {
            logInvalidValue(path, value, false);
            return false;
        }
    }

    public BarColor getBarColor(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return BarColor.WHITE;
        }
        try {
            return BarColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, BarColor.WHITE);
            return BarColor.WHITE;
        }
    }

    public BarStyle getBarStyle(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return BarStyle.SOLID;
        }
        try {
            return BarStyle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, BarStyle.SOLID);
            return BarStyle.SOLID;
        }
    }

    public GameMode getGamemode(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            logMissingPath(path);
            return GameMode.SURVIVAL;
        }
        try {
            return GameMode.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            logInvalidValue(path, name, GameMode.SURVIVAL);
            return GameMode.SURVIVAL;
        }
    }

    public World getWorld(String path) {
        String worldName = getString(path);
        if (worldName.isEmpty()) {
            logMissingPath(path);
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            logInvalidValue(path, worldName, "null (world not found)");
        }
        return world;
    }

    public List<World> getWorldList(String path) {
        List<String> worldNames = getStringList(path);
        if (worldNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<World> worlds = new ArrayList<>();
        for (String worldName : worldNames) {
            if (worldName == null || worldName.isEmpty()) {
                continue;
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                logInvalidValue(path, worldName, "skipping invalid world");
                continue;
            }
            worlds.add(world);
        }

        return worlds;
    }

    public Material getMaterial(String path) {
        String name = getString(path);
        if (name.isEmpty()) {
            return Material.AIR;
        }
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (Exception e) {
            logInvalidValue(path, name, Material.AIR);
            return Material.AIR;
        }
    }

    public List<Material> getMaterialList(String path) {
        List<String> materialNames = getStringList(path);
        if (materialNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<Material> materialList = new ArrayList<>();
        for (String name : materialNames) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            try {
                Material material = Material.valueOf(name.toUpperCase(Locale.ROOT));
                materialList.add(material);
            } catch (IllegalArgumentException e) {
                logInvalidValue(path, name, "пропуск неверного материала");
            }
        }
        return materialList;
    }

    @Override
    public BaseModuleSystem getPlugin() {
        return plugin;
    }
}