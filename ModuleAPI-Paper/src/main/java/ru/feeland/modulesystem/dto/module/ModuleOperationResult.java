package ru.feeland.modulesystem.dto.module;

import ru.feeland.modulesystem.enums.ModuleOperationResultType;
import ru.feeland.modulesystem.module.Module;

public class ModuleOperationResult {
    protected final String moduleName;
    protected final Module module;
    protected final ModuleOperationResultType resultType;

    public ModuleOperationResult(String moduleName, Module module) {
        this.moduleName = moduleName;
        this.module = module;
        this.resultType = ModuleOperationResultType.OK;
    }

    public ModuleOperationResult(String moduleName, ModuleOperationResultType resultType) {
        this.moduleName = moduleName;
        this.resultType = resultType;
        this.module = null;
    }

    public String getModuleName() {
        if (moduleName == null) return null;

        String replace = moduleName.replace(".jar", "");
        return replace.substring(replace.lastIndexOf(".") + 1);
    }

    public ModuleOperationResultType getResultType() {
        return resultType;
    }

    public Module getModule() {
        return module;
    }
}
