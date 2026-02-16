package ru.feeland.modulesystem.service.impl.scheduler;


import ru.feeland.modulesystem.aware.AvoidInitializer;
import ru.feeland.modulesystem.module.Module;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

/**
 * Планировщик задач для конкретного модуля
 * Автоматически управляет отменой задач при отключении модуля
 */
public class ModuleSchedulerService extends SchedulerService implements AvoidInitializer {
    protected Module module;

    public ModuleSchedulerService(Module module) {
        super(module.getPlugin());
        this.module = module;
    }

    public WrapperTask runRepeating(Runnable runnable, long delayTicks, long periodTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runRepeating(runnable, delayTicks, periodTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runLater(Runnable runnable, long delayTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runLater(runnable, delayTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runAsync(Runnable runnable) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runAsync(runnable);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }
}