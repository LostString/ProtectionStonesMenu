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
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String message = event.getMessage();
        Utils utils = plugin.getUtils();

        if (plugin.renamePrompts.containsKey(playerUUID)) {
            event.setCancelled(true);
            handleRename(player, message, utils);
            return;
        }

        if (plugin.ownerPrompts.containsKey(playerUUID)) {
            event.setCancelled(true);
            handleOwner(player, message, utils);
            return;
        }

        if (plugin.memberPrompts.containsKey(playerUUID)) {
            event.setCancelled(true);
            handleMember(player, message, utils);
            return;
        }

        if (plugin.banPrompts.containsKey(playerUUID)) {
            event.setCancelled(true);
            handleBan(player, message, utils);
        }
    }


    private void handleRename(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.renamePrompts.get(uuid);

        if (message.equalsIgnoreCase("cancel")) {
            reopenRenameMenu(player, region);
            utils.sendMessage(player, plugin.getMessageConfig().getEditRenameCancel(), true);
            return;
        }

        if (message.equalsIgnoreCase("none")) {
            region.setName(null);
            reopenRenameMenu(player, region);
            utils.sendMessage(
                    player,
                    plugin.getMessageConfig().getEditRenameSuccess()
                            .replace("%name%", region.getId()),
                    true
            );
            return;
        }

        region.setName(message);
        reopenRenameMenu(player, region);
        utils.sendMessage(
                player,
                plugin.getMessageConfig().getEditRenameSuccess()
                        .replace("%name%", region.getName()),
                true
        );
    }

    private void reopenRenameMenu(Player player, PSRegion region) {
        plugin.getScheduler().runTask(player, () -> {
            plugin.getInventoryManager().openPSEditMenu(player, region);
            plugin.renamePrompts.remove(player.getUniqueId());
        });
    }


    private void handleOwner(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.ownerPrompts.get(uuid);

        UUID targetUUID = getUUIDFromMessage(player, message, utils);
        if (targetUUID == null) return;

        String name = UUIDCache.getNameFromUUID(targetUUID);

        if (region.isOwner(targetUUID)) {
            utils.sendMessage(player,
                    plugin.getMessageConfig().getEditOwnerAlready().replace("%player%", name),
                    true
            );
        } else {
            region.addOwner(targetUUID);
            utils.sendMessage(player,
                    plugin.getMessageConfig().getEditOwnerAddSuccess().replace("%player%", name),
                    true
            );
        }

        plugin.getScheduler().runTask(player, () -> {
            plugin.getInventoryManager().openPSOwnersMenu(player, region);
            plugin.ownerPrompts.remove(uuid);
        });
    }


    private void handleMember(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.memberPrompts.get(uuid);

        UUID targetUUID = getUUIDFromMessage(player, message, utils);
        if (targetUUID == null) return;

        String name = UUIDCache.getNameFromUUID(targetUUID);

        if (region.isMember(targetUUID)) {
            utils.sendMessage(player,
                    plugin.getMessageConfig().getEditMemberAlready().replace("%player%", name),
                    true
            );
        } else {
            region.addMember(targetUUID);
            utils.sendMessage(player,
                    plugin.getMessageConfig().getEditMemberAddSuccess().replace("%player%", name),
                    true
            );
        }

        plugin.getScheduler().runTask(player, () -> {
            plugin.getInventoryManager().openPSMembersMenu(player, region);
            plugin.memberPrompts.remove(uuid);
        });
    }


    private void handleBan(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.banPrompts.get(uuid);

        UUID targetUUID = getUUIDFromMessage(player, message, utils);
        if (targetUUID == null) return;

        if (uuid.equals(targetUUID)) {
            utils.sendMessage(player, plugin.getMessageConfig().getBanSelf(), true);
            reopenBanMenu(player, region);
            return;
        }

        String name = UUIDCache.getNameFromUUID(targetUUID);

        if (PSUtils.isBanned(region, targetUUID.toString())) {
            utils.sendMessage(player,
                    plugin.getMessageConfig().getBanAlready().replace("%player%", name),
                    true
            );
        } else {
            PSUtils.banPlayer(region, targetUUID.toString());
            utils.sendMessage(player,
                    plugin.getMessageConfig().getBanAddSuccess().replace("%player%", name),
                    true
            );
        }

        reopenBanMenu(player, region);
    }

    private void reopenBanMenu(Player player, PSRegion region) {
        plugin.getScheduler().runTask(player, () -> {
            plugin.getInventoryManager().openPSBansMenu(player, region);
            plugin.banPrompts.remove(player.getUniqueId());
        });
    }


    private UUID getUUIDFromMessage(Player player, String message, Utils utils) {
        String name = message.split(" ")[0];
        UUID uuid = UUIDCache.getUUIDFromName(name);

        if (uuid == null) {
            utils.sendMessage(player,
                    plugin.getMessageConfig().getPlayerNotFound().replace("%player%", name),
                    true
            );
        }

        return uuid;
    }
}
