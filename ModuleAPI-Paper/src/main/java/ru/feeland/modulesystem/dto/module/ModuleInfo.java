package ru.feeland.modulesystem.dto.module;

public final class ModuleInfo {
    private final String moduleName;
    private final boolean loaded;
    private final long createTimeMillis;
    private boolean hasUpdate;

    public ModuleInfo(
        String moduleName,
        boolean loaded,
        long createTimeMillis
    ) {
        this.moduleName = moduleName;
        this.loaded = loaded;
        this.createTimeMillis = createTimeMillis;
    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public long getCreateTimeMillis() {
        return createTimeMillis;
    }

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }
}
