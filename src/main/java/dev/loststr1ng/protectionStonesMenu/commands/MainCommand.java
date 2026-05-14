package dev.loststr1ng.protectionStonesMenu.commands;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainCommand extends Command {

    private final ProtectionStonesMenu plugin;

    public MainCommand(@NotNull String name, ProtectionStonesMenu plugin) {
        super(name, plugin.getMainConfig().getCommandDescription(), "usage: /" + name, plugin.getMainConfig().getCommandAliases());
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player player){
            if(args.length >= 1){
                if(args[0].equalsIgnoreCase("reload")){
                    if(player.hasPermission(plugin.getMainConfig().getCommandPermission())){
                        plugin.getMainConfig().reloadConfig();
                        plugin.getMessageConfig().reloadConfig();
                        plugin.getUtils().sendMessage(player,
                                plugin.getMessageConfig().getReload(), true);
                    }else {
                        plugin.getUtils().sendMessage(player,
                                plugin.getMessageConfig().getNoPermissions(), true);
                    }
                    return true;
                }
                if(args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("settings")){
                    plugin.getInventoryManager().openPSMainMenu(player);
                    return true;
                }
            }else {
                plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getUsageMessage().replace("%command%", getName())
                        .replace("%arguments%", "menu/settings/reload"), true);
                return true;
            }
        }
        if(args.length >= 1){
            if(args[0].equalsIgnoreCase("reload")){
                sender.sendMessage(MessageUtils.getLegacy(
                        plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getReload()
                ));
                plugin.getMainConfig().reloadConfig();
                plugin.getMessageConfig().reloadConfig();
                return true;
            }
           sender.sendMessage( MessageUtils.getLegacy( plugin.getMessageConfig().getPrefix() + plugin.getMessageConfig().getUsageMessage().replace("%command%", getName())
                   .replace("%arguments%", "menu/settings/reload")));
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if(args.length == 1){
            return List.of("reload", "menu", "settings");
        }

        return List.of();
    }
}
