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

public class argKickCommand implements PSCommandArg {

    private final ProtectionStonesMenu plugin;

    public argKickCommand(ProtectionStonesMenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> getNames() {
        return List.of("kick");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return List.of("psmenu.kick");
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
            utils.sendMessage(player, "&cUsage &n/ps kick <player>", true);
            return true;
        }
        String playerName = args[1];
        UUID uuid = UUIDCache.getUUIDFromName(playerName);
        String name = UUIDCache.getNameFromUUID(uuid);
        Player target = Bukkit.getPlayer(uuid);
        if(target == null || name == null){
            utils.sendMessage(player, plugin.getMessageConfig().getPlayerNotFound()
                    .replaceAll("%player%", playerName), true);
            return false;
        }
        if(player.getUniqueId().equals(uuid)){
            utils.sendMessage(player, plugin.getMessageConfig().getKickSelf(), true);
            return false;
        }
        PSRegion region = PSRegion.fromLocation(target.getLocation());
        if(region == null){
            utils.sendMessage(player, plugin.getMessageConfig().getNoRegionFound(), true);
            return false;
        }
        if(PSUtils.canEdit(region, player)){
            PSUtils.kickPlayer(region, target);
            utils.sendMessage(player, plugin.getMessageConfig().getKickSuccess()
                    .replaceAll("%player%", name), true);
            utils.sendMessage(target, plugin.getMessageConfig().getKickMessage()
                    .replaceAll("%player%", player.getName()), true);

        }else {
            utils.sendMessage(player, plugin.getMessageConfig().getNoPermissions(), true);
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
