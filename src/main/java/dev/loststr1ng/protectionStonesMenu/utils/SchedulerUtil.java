package dev.loststr1ng.protectionStonesMenu.utils;

import dev.loststr1ng.FoliaManager;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

}
