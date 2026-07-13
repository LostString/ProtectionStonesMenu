package dev.loststr1ng.protectionStonesMenu.utils;

import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class SchedulerUtil {

    private final ProtectionStonesMenu plugin;

    public SchedulerUtil(ProtectionStonesMenu plugin) {
        this.plugin = plugin;
    }

    public void runTask(Player player, Runnable task){
        if(plugin.isFolia()){
            runFoliaTask(player, ignored -> task.run(), null, 0L, 0L);
        }else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runTaskTimer(Player player, Runnable task, long d, long t){
        runTaskTimer(player, ignored -> task.run(), d, t);
    }

    public void runTaskTimer(Player player, Consumer<Object> task, long d, long t){
        if(plugin.isFolia()){
            runFoliaTask(player, task, null, d, t);
        }
        else {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> task.accept(null), d,t);
        }

    }

    public void cancelTask(Object task) {
        if (task == null) return;
        try {
            task.getClass().getMethod("cancel").invoke(task);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public void teleport(Player player, Location location) {
        if (!plugin.isFolia()) {
            player.teleport(location);
            return;
        }

        try {
            Method teleportAsync = player.getClass().getMethod("teleportAsync", Location.class);
            teleportAsync.invoke(player, location);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private void runFoliaTask(Player player, Consumer<Object> task, Runnable retired,
                              long delay, long period) {
        try {
            Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
            Method method = period > 0
                    ? scheduler.getClass().getMethod("runAtFixedRate",
                    org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class, long.class, long.class)
                    : scheduler.getClass().getMethod("run",
                    org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class);

            Consumer<Object> consumer = task::accept;
            if (period > 0) {
                method.invoke(scheduler, plugin, consumer, retired, Math.max(delay, 1L), period);
            } else {
                method.invoke(scheduler, plugin, consumer, retired);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

}
