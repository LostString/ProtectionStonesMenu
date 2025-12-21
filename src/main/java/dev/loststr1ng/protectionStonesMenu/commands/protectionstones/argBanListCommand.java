package dev.loststr1ng.protectionStonesMenu.commands.protectionstones;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.commands.PSCommandArg;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.utils.PSUtils;
import dev.loststr1ng.protectionStonesMenu.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class argBanListCommand implements PSCommandArg {

    private final ProtectionStonesMenu plugin;

    public argBanListCommand(ProtectionStonesMenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> getNames() {
        return List.of("banlist");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return List.of("psmenu.banlist");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender commandSender, String[] args, HashMap<String, String> hashMap) {
        Utils utils = plugin.getUtils();
        Player player = (Player) commandSender;
        if(args.length > 1){
            utils.sendMessage(player, "&cUsage &n/ps banlist", true);
            return true;
        }
        PSRegion region = PSRegion.fromLocation(player.getLocation());
        if(region == null){
            utils.sendMessage(player, plugin.getMessageConfig().getNoRegionFound(), true);
            return false;
        }
        List<String> bannedPlayers = PSUtils.getBannedPlayers(region);
        utils.sendMessage(player, utils.parsePSVar(plugin.getMessageConfig().getBanListHeader(), region), false);
        if(bannedPlayers.isEmpty()){
            utils.sendMessage(player, plugin.getMessageConfig().getBanListEmpty(), false);
            return false;
        }
        for(String bannedUUID : bannedPlayers){
            UUID uuid = UUID.fromString(bannedUUID);
            String name = UUIDCache.getNameFromUUID(uuid);
            String message = utils.parsePSVar(plugin.getMessageConfig().getBanListEntry()
                    .replaceAll("%player%", name), region);
            utils.sendMessage(player, message, false);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] strings) {
        return List.of();
    }
}
