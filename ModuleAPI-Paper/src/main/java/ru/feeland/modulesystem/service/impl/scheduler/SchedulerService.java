package ru.feeland.modulesystem.service.impl.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.service.BaseService;
import ru.feeland.modulesystem.service.impl.scheduler.task.WrapperTask;

import java.util.concurrent.TimeUnit;

public class SchedulerService extends BaseService {
    private static final long TICKS_TO_MILLIS = 50L;
    private final boolean isFolia;

    public SchedulerService(BaseModuleSystem plugin) {
        super(plugin);
        this.isFolia = detectFolia();
    }

    private boolean detectFolia() {
        return classExists("io.papermc.paper.threadedregions.RegionizedServer")
                || classExists("io.canvasmc.canvas.server.ThreadedServer");
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public WrapperTask runRepeatingGlobal(Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                    getPlugin(),
                    scheduledTask -> runnable.run(),
                    delayTicks,
                    periodTicks
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                    getPlugin(),
                    runnable,
                    delayTicks,
                    periodTicks
            );
            return WrapperTask.forBukkit(task, true);
        }
    }

    public WrapperTask runLaterGlobal(Runnable runnable, long delayTicks) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getGlobalRegionScheduler().runDelayed(
                    getPlugin(),
                    scheduledTask -> runnable.run(),
                    delayTicks
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(
                    getPlugin(),
                    runnable,
                    delayTicks
            );
            return WrapperTask.forBukkit(task, false);
        }
    }

    public WrapperTask runOnceGlobal(Runnable runnable) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getGlobalRegionScheduler().run(
                    getPlugin(),
                    scheduledTask -> runnable.run()
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTask(getPlugin(), runnable);
            return WrapperTask.forBukkit(task, false);
        }
    }

    public WrapperTask runRepeatingRegion(Location loc, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getRegionScheduler().runAtFixedRate(
                    getPlugin(),
                    loc,
                    scheduledTask -> runnable.run(),
                    delayTicks,
                    periodTicks
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                    getPlugin(),
                    runnable,
                    delayTicks,
                    periodTicks
            );
            return WrapperTask.forBukkit(task, true);
        }
    }

    public WrapperTask runLaterRegion(Location loc, Runnable runnable, long delayTicks) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getRegionScheduler().runDelayed(
                    getPlugin(),
                    loc,
                    scheduledTask -> runnable.run(),
                    delayTicks
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(
                    getPlugin(),
                    runnable,
                    delayTicks
            );
            return WrapperTask.forBukkit(task, false);
        }
    }

    public WrapperTask runOnceRegion(Location loc, Runnable runnable) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getRegionScheduler().run(
                    getPlugin(),
                    loc,
                    scheduledTask -> runnable.run()
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTask(getPlugin(), runnable);
            return WrapperTask.forBukkit(task, false);
        }
    }

    public WrapperTask runRepeatingAsync(Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getAsyncScheduler().runAtFixedRate(
                    getPlugin(),
                    scheduledTask -> runnable.run(),
                    delayTicks * TICKS_TO_MILLIS,
                    periodTicks * TICKS_TO_MILLIS,
                    TimeUnit.MILLISECONDS
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    getPlugin(),
                    runnable,
                    delayTicks,
                    periodTicks
            );
            return WrapperTask.forBukkit(task, true);
        }
    }

    public WrapperTask runLaterAsync(Runnable runnable, long delayTicks) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getAsyncScheduler().runDelayed(
                    getPlugin(),
                    scheduledTask -> runnable.run(),
                    delayTicks * TICKS_TO_MILLIS,
                    TimeUnit.MILLISECONDS
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(
                    getPlugin(),
                    runnable,
                    delayTicks
            );
            return WrapperTask.forBukkit(task, false);
        }
    }

    public WrapperTask runOnceAsync(Runnable runnable) {
        if (isFolia) {
            ScheduledTask task = Bukkit.getAsyncScheduler().runNow(
                    getPlugin(),
                    scheduledTask -> runnable.run()
            );
            return WrapperTask.forFolia(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), runnable);
            return WrapperTask.forBukkit(task, false);
        }
    }
}