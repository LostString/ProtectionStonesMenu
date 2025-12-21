package dev.loststr1ng.protectionStonesMenu.events;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.utils.PSUtils;
import dev.loststr1ng.protectionStonesMenu.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class PSPrompts implements Listener {

    private final ProtectionStonesMenu plugin;

    public PSPrompts(ProtectionStonesMenu plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        String message = event.getMessage();
        Utils utils = plugin.getUtils();
        if(plugin.renamePrompts.containsKey(player.getUniqueId())){
            event.setCancelled(true);
            if(message.equalsIgnoreCase("cancel")){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getInventoryManager().openPSEditMenu(player, plugin.renamePrompts.get(player.getUniqueId()));
                    plugin.renamePrompts.remove(player.getUniqueId());
                });

                utils.sendMessage(player, plugin.getMessageConfig().getEditRenameCancel(), true);
                return;
            }
            if(message.equalsIgnoreCase("none")){
                PSRegion region = plugin.renamePrompts.get(player.getUniqueId());
                region.setName(null);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getInventoryManager().openPSEditMenu(player, plugin.renamePrompts.get(player.getUniqueId()));
                    plugin.renamePrompts.remove(player.getUniqueId());
                });

                utils.sendMessage(player, plugin.getMessageConfig().getEditRenameSuccess()
                        .replaceAll("%name%", region.getId()), true);
                return;
            }
            PSRegion region = plugin.renamePrompts.get(player.getUniqueId());
            region.setName(message);
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getInventoryManager().openPSEditMenu(player, plugin.renamePrompts.get(player.getUniqueId()));
                plugin.renamePrompts.remove(player.getUniqueId());
            });
            utils.sendMessage(player, plugin.getMessageConfig().getEditRenameSuccess()
                    .replaceAll("%name%", region.getName()), true);
            return;
        } else if (plugin.ownerPrompts.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String playerToAdd = message.split(" ")[0];
            UUID uuid = UUIDCache.getUUIDFromName(playerToAdd);
            if(uuid == null){
                utils.sendMessage(player, plugin.getMessageConfig().getPlayerNotFound()
                        .replaceAll("%player%", playerToAdd), true);
            }else {
                String realName = UUIDCache.getNameFromUUID(uuid);
                PSRegion region = plugin.ownerPrompts.get(player.getUniqueId());
                if (region.isOwner(uuid)) {
                    utils.sendMessage(player, plugin.getMessageConfig().getEditOwnerAlready()
                            .replaceAll("%player%", realName), true);
                } else {
                    region.addOwner(uuid);
                    utils.sendMessage(player, plugin.getMessageConfig().getEditOwnerAddSuccess()
                            .replaceAll("%player%", realName), true);
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getInventoryManager().openPSOwnersMenu(player, plugin.ownerPrompts.get(player.getUniqueId()));
                plugin.ownerPrompts.remove(player.getUniqueId());
            });
        }else if (plugin.memberPrompts.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String playerToAdd = message.split(" ")[0];
            UUID uuid = UUIDCache.getUUIDFromName(playerToAdd);
            if(uuid == null){
                utils.sendMessage(player, plugin.getMessageConfig().getPlayerNotFound()
                        .replaceAll("%player%", playerToAdd), true);
            }else {
                String realName = UUIDCache.getNameFromUUID(uuid);
                PSRegion region = plugin.memberPrompts.get(player.getUniqueId());
                if (region.isMember(uuid)) {
                    utils.sendMessage(player, plugin.getMessageConfig().getEditMemberAlready()
                            .replaceAll("%player%", realName), true);
                } else {
                    region.addMember(uuid);
                    utils.sendMessage(player, plugin.getMessageConfig().getEditMemberAddSuccess()
                            .replaceAll("%player%", realName), true);
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getInventoryManager().openPSMembersMenu(player, plugin.memberPrompts.get(player.getUniqueId()));
                plugin.memberPrompts.remove(player.getUniqueId());
            });
        }else if (plugin.banPrompts.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String playerToAdd = message.split(" ")[0];
            UUID uuid = UUIDCache.getUUIDFromName(playerToAdd);
            if(uuid == null){
                utils.sendMessage(player, plugin.getMessageConfig().getPlayerNotFound()
                        .replaceAll("%player%", playerToAdd), true);
            }else {
                String realName = UUIDCache.getNameFromUUID(uuid);
                PSRegion region = plugin.banPrompts.get(player.getUniqueId());
                if(player.getUniqueId().equals(uuid)){
                    utils.sendMessage(player, plugin.getMessageConfig().getBanSelf(), true);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getInventoryManager().openPSBansMenu(player, plugin.banPrompts.get(player.getUniqueId()));
                        plugin.banPrompts.remove(player.getUniqueId());
                    });
                    return;
                }
                if (PSUtils.isBanned(region, uuid.toString())) {
                    utils.sendMessage(player, plugin.getMessageConfig().getBanAlready()
                            .replaceAll("%player%", realName), true);
                } else {
                    PSUtils.banPlayer(region, uuid.toString());
                    utils.sendMessage(player, plugin.getMessageConfig().getBanAddSuccess()
                            .replaceAll("%player%", realName), true);
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getInventoryManager().openPSBansMenu(player, plugin.banPrompts.get(player.getUniqueId()));
                plugin.banPrompts.remove(player.getUniqueId());
            });
        }
    }
}
