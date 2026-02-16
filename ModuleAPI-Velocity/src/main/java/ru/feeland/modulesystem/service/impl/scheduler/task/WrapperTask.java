package ru.feeland.modulesystem.service.impl.scheduler.task;

import com.velocitypowered.api.scheduler.ScheduledTask;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.impl.scheduler.task.impl.VelocityTask;

public interface WrapperTask {

    void cancel();

    boolean isCancelled();

    boolean isRepeatingTask();

    boolean isCurrentlyRunning();

    static WrapperTask forVelocity(ScheduledTask task, boolean isRepeating) {
        if (task == null) {
            Logger.error().log("Velocity task null");
            throw new IllegalArgumentException();
        }
        return new VelocityTask(task, isRepeating);
    }
}