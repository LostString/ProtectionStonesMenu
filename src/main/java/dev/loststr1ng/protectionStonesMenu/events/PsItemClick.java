package dev.loststr1ng.protectionStonesMenu.events;

import dev.espi.protectionstones.PSRegion;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.listeners.PSItemClickEvent;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PsItemClick implements Listener {
    protected final ProtectionStonesMenu plugin;

    public PsItemClick(ProtectionStonesMenu plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemClicked(PSItemClickEvent event){
        Player player = event.getPlayer();
        Gui gui = event.getGui();
        String action = event.getAction();
        String args = event.getArgs();
        if(action == null) return;
        if(action.equalsIgnoreCase("NONE")) return;
        if(action.equalsIgnoreCase("CLOSE_MENU")){
            gui.close(player);
            return;
        }
        if(action.equalsIgnoreCase("PLAYER_COMMAND")){
            Bukkit.dispatchCommand(player, args.replaceAll("%player%", player.getName()));
            return;
        }
        if(action.equalsIgnoreCase("COMMAND")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args.replaceAll("%player%", player.getName()));
            return;
        }
        if(action.equalsIgnoreCase("OPEN_MENU")){
            PSRegion region = event.getRegion();
            plugin.getInventoryManager().openGui(player, args, region);
            return;
        }
    }
}
