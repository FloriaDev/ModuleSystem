package ru.feeland.modulesystem.service.impl.scheduler.task.impl;

import com.velocitypowered.api.scheduler.ScheduledTask;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

public class VelocityTask implements WrapperTask {
    private final ScheduledTask task;
    private final boolean repeating;

    public VelocityTask(ScheduledTask task, boolean repeating) {
        this.task = task;
        this.repeating = repeating;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isRepeatingTask() {
        return repeating;
    }

    @Override
    public boolean isCurrentlyRunning() {
        return false;
    }
}