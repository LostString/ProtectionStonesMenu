package dev.loststr1ng.protectionStonesMenu.events;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.listeners.RegionJoinEvent;
import dev.loststr1ng.protectionStonesMenu.utils.MessageUtils;
import dev.loststr1ng.protectionStonesMenu.utils.PSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PSJoin implements Listener {

    protected final ProtectionStonesMenu plugin;
    public PSJoin(ProtectionStonesMenu plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegionJoin(RegionJoinEvent event){
        Player player = event.getPlayer();
        if(player == null) return;
        PSRegion region = PSRegion.fromWGRegion(player.getWorld(), event.getRegion());
        if(region == null) return;
        if(player.hasPermission("psmenu.admin")) return;
        if(PSUtils.isBanned(region, player.getUniqueId().toString())){
            event.setCancelled(true);
            player.setVelocity(player.getLocation().getDirection().setY(0).multiply(-4));
            plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getBanMessage(), true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        String message = event.getMessage();
        if(!message.startsWith("/")) return;
        if(!plugin.getMainConfig().isOpenPSCommands()) return;
        String[] args = message.split(" ");
        List<String> commands = ProtectionStones.getInstance().getConfigOptions().aliases;
        commands.add(ProtectionStones.getInstance().getConfigOptions().base_command);
        if(args.length < 1){
            return;
        }
        if(args.length == 1){
            plugin.getInventoryManager().openPSMainMenu(event.getPlayer());
            event.setCancelled(true);
            return;
        }
        if(args.length == 2){
            String subCommand = args[1];
            if(subCommand.equalsIgnoreCase("home")){
                plugin.getInventoryManager().openPSHomeMenu(event.getPlayer());
                event.setCancelled(true);
                return;
            }
            if(subCommand.equalsIgnoreCase("flag")){
                PSRegion region = PSRegion.fromLocation(event.getPlayer().getLocation());
                if(region == null) return;
                if(!PSUtils.canEdit(region, event.getPlayer())){
                    plugin.getInventoryManager().openPSMainMenu(event.getPlayer());
                    event.setCancelled(true);
                    return;
                }
                plugin.getInventoryManager().openPSEditFlagsMenu(event.getPlayer(), region);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(!plugin.getMainConfig().isOpenOnClickBlock()) return;
        PSRegion region = PSRegion.fromLocation(player.getLocation());
        if(region == null) return;
        if(event.hasBlock() && event.getClickedBlock() != null){
            if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
            if(event.getClickedBlock().getLocation().equals(region.getProtectBlock().getLocation())){
                plugin.getInventoryManager().openPSMainMenu(player);
                event.setCancelled(true);
            }
        }
    }

    // Update Message
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(event.getPlayer().hasPermission("psmenu.admin") || event.getPlayer().isOp()){
            String message = plugin.getUpdateMessage();
            if(message != null){
                plugin.getUtils().sendMessage(event.getPlayer(), message, false);
            }
        }
    }
}
