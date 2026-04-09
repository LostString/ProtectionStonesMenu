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

public class argBanCommand implements PSCommandArg {

    private final ProtectionStonesMenu plugin;

    public argBanCommand(ProtectionStonesMenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> getNames() {
        return List.of("ban");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return List.of("psmenu.ban");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender commandSender, String[] args, HashMap<String, String> hashMap) {
        Utils utils = plugin.getUtils();
        Player player = (Player) commandSender;
        if(args.length == 1){
            utils.sendMessage(player, plugin.getMessageConfig().getUsageMessage().replace("%command%", "ps")
                    .replace("%arguments%", "ban <player>"), true);
            return true;
        }
        String playerName = args[1];
        UUID uuid = UUIDCache.getUUIDFromName(playerName);
        String name = UUIDCache.getNameFromUUID(uuid);
        Player target = Bukkit.getPlayer(playerName);
        PSRegion region = PSRegion.fromLocation(player.getLocation());
        if(region == null){
            utils.sendMessage(player, plugin.getMessageConfig().getNoRegionFound(), true);
            return false;
        }
        if(uuid == null || name == null || target == null){
            utils.sendMessage(player, plugin.getMessageConfig().getPlayerNotFound()
                    .replaceAll("%player%", playerName), true);
            return false;
        }
        if(!PSUtils.canEdit(region, player) && !target.hasPermission("psmenu.admin")){
            utils.sendMessage(player, plugin.getMessageConfig().getNoPermissions(), true);
            return false;
        }

        if(player.getUniqueId().equals(uuid)){
            utils.sendMessage(player, plugin.getMessageConfig().getBanSelf(), true);
            return false;
        }
        if(!PSUtils.isBanned(region, uuid.toString())){
            PSUtils.banPlayer(region, uuid.toString());
            utils.sendMessage(player, plugin.getMessageConfig()
                    .getBanAddSuccess().replaceAll("%player%", name), true);
        }else {
            utils.sendMessage(player, plugin.getMessageConfig().getBanAlready()
                    .replaceAll("%player%", name), true);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String s, String[] strings) {
        List<String> ret = new ArrayList<>();
        Player player = (Player) commandSender;
        if(s.length() == 2){
            for(Player target : Bukkit.getOnlinePlayers()) {
                if(player.canSee(target)){
                    ret.add(target.getName());
                }
            }
        }
        return ret;
    }
}
