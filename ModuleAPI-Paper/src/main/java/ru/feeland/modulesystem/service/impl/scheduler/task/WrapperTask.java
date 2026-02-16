package ru.feeland.modulesystem.service.impl.scheduler.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.service.impl.scheduler.task.impl.BukkitTask;
import ru.feeland.modulesystem.service.impl.scheduler.task.impl.FoliaTask;

public interface WrapperTask {

    void cancel();

    boolean isCancelled();

    boolean isRepeatingTask();

    boolean isCurrentlyRunning();

    static WrapperTask forFolia(ScheduledTask task) {
        if (task == null) {
            Logger.error().log("Folia task null");
            throw new IllegalArgumentException();
        }
        return new FoliaTask(task);
    }

    static WrapperTask forBukkit(org.bukkit.scheduler.BukkitTask task, boolean isRepeating) {
        if (task == null) {
            Logger.error().log("Bukkit task null");
            throw new IllegalArgumentException();
        }
        return new BukkitTask(task, isRepeating);
    }
}