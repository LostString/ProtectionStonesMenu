package dev.loststr1ng.protectionStonesMenu.utils;

import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class SchedulerUtil {

    private final ProtectionStonesMenu plugin;

    public SchedulerUtil(ProtectionStonesMenu plugin) {
        this.plugin = plugin;
    }

    public void runTask(Player player, Runnable task){
        if(plugin.isFolia()){
            plugin.getFoliaManager().runTask(player, task);
        }else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runTaskTimer(Player player, Runnable task, long d, long t){
        if(plugin.isFolia()){
            plugin.getFoliaManager().runTaskTimer(player, scheduledTask -> {
                task.run();
            }, d,t);
        }
        else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, d,t);
        }

    }

}
