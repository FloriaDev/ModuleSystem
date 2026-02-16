package ru.feeland.modulesystem.service.impl;

import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.constants.BaseConstants;
import ru.feeland.modulesystem.dto.module.ModuleInfo;
import ru.feeland.modulesystem.dto.module.ModuleOperationResult;
import ru.feeland.modulesystem.enums.ModuleOperationResultType;
import ru.feeland.modulesystem.initializer.ModuleInitializer;
import ru.feeland.modulesystem.module.Module;
import ru.feeland.modulesystem.service.BaseService;
import ru.feeland.modulesystem.logger.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleService extends BaseService {
    public ModuleService(BaseModuleSystem plugin) {
        super(plugin);
    }

    public ModuleOperationResult load(String moduleName) {
        BaseMainConfig baseMainConfig = getPlugin().getConfig(BaseMainConfig.class);
        silentCreateDirectories();

        final String preparedModuleName = formatModuleName(moduleName);
        final Optional<Path> optionalPath = getJars().stream()
            .filter(jar -> jar.getFileName().toString().toLowerCase().equals(preparedModuleName))
            .findFirst();

        if (optionalPath.isEmpty()) {
            return new ModuleOperationResult(moduleName, ModuleOperationResultType.NO_MODULE);
        }

        final ModuleOperationResult result = findModuleFromJar(optionalPath.get());
        if (result.getModule() != null) {
            baseMainConfig.set("modules." + result.getModule().getConfigName() + ".enabled", true);
            if (result.getModule().isEnabled()) {
                result.getModule().init();
                getPlugin().getInitializer(ModuleInitializer.class).addComponent(result.getModule());
                Logger.info().log("Модуль {} загружен", moduleName);
            }
        }
        return result;
    }

    public ModuleOperationResult unload(String moduleName) {
        BaseMainConfig baseMainConfig = getPlugin().getConfig(BaseMainConfig.class);
        final boolean unloaded = getPlugin().getInitializer(ModuleInitializer.class).removeComponent(moduleName);
        if (unloaded) {
            baseMainConfig.set(
                "modules."
                + moduleName
                .substring(moduleName.lastIndexOf(".") + 1)
                .toLowerCase()
                .replace("module", "")
                + ".enabled", false
            );
            Logger.info().log("Модуль {} отгружен", moduleName);
        }
        return new ModuleOperationResult(
            moduleName,
            unloaded ? ModuleOperationResultType.OK : ModuleOperationResultType.NOT_LOADED
        );
    }

    public ModuleOperationResult reload(String moduleName) {
        final ModuleOperationResult unloadResult = unload(moduleName);
        if (unloadResult.getResultType() != ModuleOperationResultType.OK) {
            return unloadResult;
        }
        return load(moduleName);
    }

    public void loadAll() {
        BaseMainConfig baseMainConfig = getPlugin().getOptionalConfig(BaseMainConfig.class).orElseThrow(() -> {
            Logger.error().log("Ошибка конфигурации в классе {}", getClass().getSimpleName());
            return new IllegalStateException();
        });
        final List<Path> jars = getJars();
        for (Path jar : jars) {
            if (baseMainConfig.getBoolean("modules." + getClassSimpleNameByJarFile(jar) + ".enabled")) {
                final ModuleOperationResult result = findModuleFromJar(jar);
                if (result.getModule() != null) {
                    result.getModule().init();
                    getPlugin().getInitializer(ModuleInitializer.class).addComponent(result.getModule());
                }
            }
        }
    }

    protected ModuleOperationResult findModuleFromJar(Path jar) {
        try {
            final Class classToLoad = getClassByJar(jar);
            final Object instance = classToLoad.getDeclaredConstructor(BaseModuleSystem.class).newInstance(getPlugin());

            if (!(instance instanceof Module module)) {
                Logger.info().log("loaded jar is not module: {}", classToLoad.getSimpleName());
                return new ModuleOperationResult(jar.getFileName().toString(), ModuleOperationResultType.NOT_A_MODULE);
            }

            module.setCreateTimeMillis(getFileCreateTimeMillis(jar));
            return new ModuleOperationResult(jar.getFileName().toString(), module);
        } catch (Exception e) {
            Logger.error().log("caught exception", e);
            return new ModuleOperationResult(jar.getFileName().toString(), ModuleOperationResultType.ERROR_ON_LOAD);
        }
    }

    public List<ModuleInfo> getModuleInfoList() {
        final List<ModuleInfo> loadedModules = getPlugin().getInitializer(ModuleInitializer.class).getComponents()
            .stream()
            .map(module -> new ModuleInfo(
                module.getModuleName(),
                true,
                module.getCreateTimeMillis()
            )).toList();
        final Set<String> loadedNames = loadedModules.stream().map(ModuleInfo::getModuleName).collect(Collectors.toSet());

        final List<ModuleInfo> unloadedModules = new ArrayList<>();
        for (Path path : getJars()) {
            final String fileName = path.getFileName().toString();
            final String moduleName = fileName.substring(0, fileName.indexOf(".jar"));

            if (loadedNames.contains(moduleName)) {
                final long millis = getFileCreateTimeMillis(path);

                loadedModules.stream()
                    .filter(info -> info.getModuleName().equals(moduleName))
                    .forEach(info -> info.setHasUpdate(info.getCreateTimeMillis() == millis));
                continue;
            }

            unloadedModules.add(new ModuleInfo(moduleName, false, 0));
        }

        final List<ModuleInfo> moduleInfos = new ArrayList<>();
        moduleInfos.addAll(loadedModules);
        moduleInfos.addAll(unloadedModules);

        return moduleInfos;
    }

    protected Path getModulePath() {
        return getPlugin().getDataPath().resolve("modules");
    }

    protected List<Path> getJars() {
        final Path path = getModulePath();

        silentCreateDirectories();
        final List<Path> jars = new ArrayList<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            for (Path objectPath : paths) {
                if (!Files.isRegularFile(objectPath)) {
                    continue;
                }

                if (objectPath.getFileName().toString().endsWith(".jar")) {
                    jars.add(objectPath);
                }
            }
        } catch (IOException e) {
            Logger.error().log("caught exception", e);
        }
        return jars;
    }

    protected void silentCreateDirectories() {
        final Path path = getModulePath();
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                Logger.info().log("[ModuleService] create directories for path: {}", path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected String formatModuleName(String moduleName) {
        String preparedModuleName = moduleName.toLowerCase()
            .replace("modulemodule", "module")
            .replace(".jar", "");

        if (preparedModuleName.indexOf(".") != preparedModuleName.lastIndexOf(".")) {
            preparedModuleName = preparedModuleName.substring(preparedModuleName.lastIndexOf(".") + 1);
        }

        if (!preparedModuleName.endsWith("module")) {
            preparedModuleName = preparedModuleName + "module";
        }

        preparedModuleName = preparedModuleName + ".jar";

        return preparedModuleName;
    }

    protected String getClassNameByJarFile(Path jar) {
        final String fileName = jar.getFileName().toString();
        return BaseConstants.BASE_CLASS_NAME + fileName.substring(0, fileName.indexOf(".jar"));
    }

    protected String getClassSimpleNameByJarFile(Path jar) {
        final String fileName = jar.getFileName().toString().toLowerCase();

        final String suffix = "module.jar";
        if (!fileName.endsWith(suffix)) {
            Logger.error().log("Invalid module jar name: {}", fileName);
            return fileName.replace(".jar", "");
        }

        return fileName.substring(0, fileName.length() - suffix.length());
    }


    protected long getFileCreateTimeMillis(Path path) {
        final BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
            return attributes.creationTime().toMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    protected Class getClassByJar(Path jar) throws MalformedURLException, ClassNotFoundException {
        final URLClassLoader child = new URLClassLoader(
            new URL[]{jar.toUri().toURL()}, ModuleService.class.getClassLoader()
        );
        return Class.forName(getClassNameByJarFile(jar), true, child);
    }
}
