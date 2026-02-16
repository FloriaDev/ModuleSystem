package ru.feeland.modulesystem.service.impl.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import ru.feeland.modulesystem.BaseModuleSystemVelocity;
import ru.feeland.modulesystem.service.BaseService;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

import java.util.concurrent.TimeUnit;

public class SchedulerService extends BaseService {

    public SchedulerService(BaseModuleSystemVelocity plugin) {
        super(plugin);
    }

    public WrapperTask runRepeating(Runnable runnable, long delayMillis, long periodMillis) {
        ScheduledTask task = getPlugin().getServer().getScheduler()
            .buildTask(getPlugin(), runnable)
            .delay(delayMillis, TimeUnit.MILLISECONDS)
            .repeat(periodMillis, TimeUnit.MILLISECONDS)
            .schedule();
        return WrapperTask.forVelocity(task, true);
    }

    public WrapperTask runLater(Runnable runnable, long delayMillis) {
        ScheduledTask task = getPlugin().getServer().getScheduler()
            .buildTask(getPlugin(), runnable)
            .delay(delayMillis, TimeUnit.MILLISECONDS)
            .schedule();
        return WrapperTask.forVelocity(task, false);
    }

    public WrapperTask runAsync(Runnable runnable) {
        ScheduledTask task = getPlugin().getServer().getScheduler()
            .buildTask(getPlugin(), runnable)
            .schedule();
        return WrapperTask.forVelocity(task, false);
    }
}