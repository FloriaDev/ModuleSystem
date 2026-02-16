package ru.feeland.modulesystem.service.impl.scheduler;


import org.bukkit.Location;
import ru.feeland.modulesystem.aware.AvoidInitializer;
import ru.feeland.modulesystem.module.Module;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

public class ModuleSchedulerService extends SchedulerService implements AvoidInitializer {
    protected Module module;

    public ModuleSchedulerService(Module module) {
        super(module.getPlugin());
        this.module = module;
    }

    public WrapperTask runRepeatingGlobal(Runnable runnable, long delayTicks, long periodTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runRepeatingGlobal(runnable, delayTicks, periodTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runLaterGlobal(Runnable runnable, long delayTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runLaterGlobal(runnable, delayTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runOnceGlobal(Runnable runnable) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runOnceGlobal(runnable);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runRepeatingRegion(Location loc, Runnable runnable, long delayTicks, long periodTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runRepeatingRegion(loc, runnable, delayTicks, periodTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runLaterRegion(Location loc, Runnable runnable, long delayTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runLaterRegion(loc, runnable, delayTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runOnceRegion(Location loc, Runnable runnable) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runOnceRegion(loc, runnable);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runRepeatingAsync(Runnable runnable, long delayTicks, long periodTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runRepeatingAsync(runnable, delayTicks, periodTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runLaterAsync(Runnable runnable, long delayTicks) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runLaterAsync(runnable, delayTicks);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }

    public WrapperTask runOnceAsync(Runnable runnable) {
        WrapperTask wrapperTask = getPlugin().getService(SchedulerService.class).runOnceAsync(runnable);
        module.addTask(wrapperTask, module);
        return wrapperTask;
    }
}