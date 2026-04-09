package dev.loststr1ng.protectionStonesMenu.events;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.enums.PromptType;
import dev.loststr1ng.protectionStonesMenu.models.PromptModel;
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
        if(!plugin.promptModelMap.containsKey(playerUUID)) return;
        PromptModel promptModel = plugin.promptModelMap.get(playerUUID);
        if (promptModel.getType().equals(PromptType.RENAME)) {
            event.setCancelled(true);
            handleRename(player, message, utils);
            return;
        }

        if (promptModel.getType().equals(PromptType.ADD_OWNER)) {
            event.setCancelled(true);
            handleOwner(player, message, utils);
            return;
        }

        if (promptModel.getType().equals(PromptType.ADD_MEMBER)) {
            event.setCancelled(true);
            handleMember(player, message, utils);
            return;
        }

        if (promptModel.getType().equals(PromptType.BAN)) {
            event.setCancelled(true);
            handleBan(player, message, utils);
        }

        if(promptModel.getType().equals(PromptType.EDIT_FLAG)){
            event.setCancelled(true);
            handleFlag(player, event.getMessage(), utils);
        }
    }

    private void handleFlag(Player player, String message, Utils utils){
        UUID uuid = player.getUniqueId();
        PromptModel promptModel = plugin.promptModelMap.get(uuid);
        PSRegion region = promptModel.getRegion();
        if(message.equalsIgnoreCase("cancel")){
            reopenFlagsMenu(player, region);
            return;
        }
        if(message.equalsIgnoreCase("none")){
            utils.updateFlag(region, promptModel.getArgs(), null, utils.getFlagGroup(region, promptModel.getArgs()));
            String m = utils.getFlag(plugin.getMessageConfig().getEditFlagUpdated(), region, promptModel.getArgs());
            utils.sendMessage(player, m, true);
            reopenFlagsMenu(player, region);
            return;
        }
        utils.updateFlag(region, promptModel.getArgs(), message, utils.getFlagGroup(region, promptModel.getArgs()));
        String m = utils.getFlag(plugin.getMessageConfig().getEditFlagUpdated(), region, promptModel.getArgs());
        utils.sendMessage(player, m, true);
        reopenFlagsMenu(player, region);
    }

    private void reopenFlagsMenu(Player player, PSRegion region){
        plugin.getScheduler().runTask(player, () ->
        {
            plugin.getInventoryManager().openPSEditFlagsMenu(player, region);
            plugin.promptModelMap.remove(player.getUniqueId());
        });
    }


    private void handleRename(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.promptModelMap.get(uuid).getRegion();

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
            plugin.promptModelMap.remove(player.getUniqueId());
        });
    }


    private void handleOwner(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.promptModelMap.get(uuid).getRegion();

        UUID targetUUID = getUUIDFromMessage(player, message, utils);
        if (targetUUID == null) {
            plugin.getScheduler().runTask(player, () -> {
                plugin.getInventoryManager().openPSOwnersMenu(player, region);
                plugin.promptModelMap.remove(uuid);
            });
            return;
        }

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
            plugin.promptModelMap.remove(uuid);
        });
    }


    private void handleMember(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.promptModelMap.get(uuid).getRegion();

        UUID targetUUID = getUUIDFromMessage(player, message, utils);
        if (targetUUID == null) {
            plugin.getScheduler().runTask(player, () -> {
                plugin.getInventoryManager().openPSMembersMenu(player, region);
                plugin.promptModelMap.remove(uuid);
            });
            return;
        }

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
            plugin.promptModelMap.remove(uuid);
        });
    }


    private void handleBan(Player player, String message, Utils utils) {
        UUID uuid = player.getUniqueId();
        PSRegion region = plugin.promptModelMap.get(uuid).getRegion();

        UUID targetUUID = getUUIDFromMessage(player, message, utils);
        if (targetUUID == null) {
            reopenBanMenu(player, region);
            return;
        }

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
            plugin.promptModelMap.remove(player.getUniqueId());
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
