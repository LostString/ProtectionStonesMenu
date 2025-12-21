package dev.loststr1ng.protectionStonesMenu.managers;

import com.sk89q.worldguard.protection.flags.Flag;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.config.MainConfig;
import dev.loststr1ng.protectionStonesMenu.config.MessageConfig;
import dev.loststr1ng.protectionStonesMenu.listeners.PSItemClickEvent;
import dev.loststr1ng.protectionStonesMenu.utils.MessageUtils;
import dev.loststr1ng.protectionStonesMenu.utils.PSUtils;
import dev.loststr1ng.protectionStonesMenu.utils.Utils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class InventoryManager {

    protected final ProtectionStonesMenu plugin;

    public InventoryManager(ProtectionStonesMenu plugin){
        this.plugin = plugin;
    }

    public void openGui(Player player, String gui, PSRegion region){
        if(gui.equalsIgnoreCase("main")){
            openPSMainMenu(player);
            return;
        }
        if(gui.equalsIgnoreCase("homes")){
            openPSHomeMenu(player);
            return;
        }
        if(gui.equalsIgnoreCase("edit") && region != null){
            openPSEditMenu(player, region);
            return;
        }

    }

    public void openPSMainMenu(Player player){
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        PSRegion region = PSRegion.fromLocation(player.getLocation());
        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(mainConfig.getMainGuiTitle()))
                .rows(mainConfig.getMainGuiSize())
                .create();
        for(Utils.InventoryItem inventoryItem: mainConfig.getMainGuiItems()){
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem);
                guiItem.setAction( inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.equalsIgnoreCase("ps-homes")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    openPSHomeMenu(player);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(region != null && inventoryItem.item.equalsIgnoreCase("ps-info")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    // Open Edit Gui
                    openPSEditMenu(player, region);

                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(region == null && inventoryItem.item.equalsIgnoreCase("ps-info2")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }

        }
        gui.open(player);
        gui.setDefaultClickAction( event -> { event.setCancelled(true); });

    }

    public void openPSHomeMenu(Player player){
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(mainConfig.getHomesGuiTitle()))
                .rows(mainConfig.getHomesGuiSize())
                .create();
        for(Utils.InventoryItem inventoryItem: mainConfig.getHomesGuiItems()){
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.equalsIgnoreCase("ps-item")){
                PSPlayer psPlayer = PSPlayer.fromPlayer(player);
                List<Integer> slots = new ArrayList<>(inventoryItem.slots);
                for(PSRegion psRegion: psPlayer.getPSRegions(player.getWorld(), true)){
                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, psRegion);
                    guiItem.setAction(inventoryClickEvent -> {
                        inventoryClickEvent.setCancelled(true);

                        // TP LOGIC
                        if(inventoryClickEvent.isLeftClick()){
                            player.teleport(psRegion.getHome());
                            return;
                        }
                        // EDIT LOGIC
                        if(inventoryClickEvent.isRightClick()){
                            openPSEditMenu(player, psRegion);

                        }
                    });

                    for(Integer slot: inventoryItem.slots){
                        if(slots.contains(slot)){
                            gui.setItem(slot, guiItem);
                            slots.remove(slot);
                            break;
                        }
                    }
                }
            }
        }

        gui.open(player);
        gui.setDefaultClickAction(inventoryClickEvent -> { inventoryClickEvent.setCancelled(true); });
    }

    public void openPSEditMenu(Player player, PSRegion region){
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        if( !(PSUtils.canEdit(region, player)) ){
            utils.sendMessage(player, plugin.getMessageConfig().getNoPermissions(), true);
            return;
        }
        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(utils.parsePSVar(mainConfig.getEditGuiTitle(), region)))
                .rows(mainConfig.getEditGuiSize())
                .create();
        for(Utils.InventoryItem inventoryItem: mainConfig.getEditGuiItems()){
            if(inventoryItem.item.equalsIgnoreCase("ps-rename")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    if(!region.isOwner(player.getUniqueId())){
                        utils.sendMessage(player, plugin.getMessageConfig().getNoPermissions(), true);
                        return;
                    }
                    // set name logic
                    gui.close(player);
                    plugin.renamePrompts.put(player.getUniqueId(), region);
                    utils.sendMessage(player, plugin.getMessageConfig().getEditRename(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.equalsIgnoreCase("ps-flags")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    // set flags logic
                    openPSEditFlagsMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.equalsIgnoreCase("ps-owners")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    // set owners logic
                    openPSOwnersMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.equalsIgnoreCase("ps-members")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    // set members logic
                    openPSMembersMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }

            if(inventoryItem.item.equalsIgnoreCase("ps-hide-on") && !region.isHidden()){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    // Un hide protection block
                    inventoryClickEvent.setCancelled(true);
                    region.hide();
                    // refresh menu
                    openPSEditMenu(player, region);
                    utils.sendMessage(player, plugin.getMessageConfig().getEditVisibilityOn(), true);

                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.equalsIgnoreCase("ps-hide-off") && region.isHidden()){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    // Hide protection block
                    inventoryClickEvent.setCancelled(true);
                    region.unhide();
                    // refresh menu
                    openPSEditMenu(player, region);
                    utils.sendMessage(player, plugin.getMessageConfig().getEditVisibilityOff(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.equalsIgnoreCase("ps-ban")){
                if(!mainConfig.isBanModuleEnabled()){
                    continue;
                }
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    // set ban logic
                    openPSBansMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
        }

        gui.open(player);
        gui.setDefaultClickAction(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
        });
    }

    public void openPSEditFlagsMenu(Player player, PSRegion region){
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();
        if( !(PSUtils.canEdit(region, player)) ){
            utils.sendMessage(player, messageConfig.getNoPermissions(), true);
            return;
        }
        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(mainConfig.getEditFlagsGuiTitle()))
                .rows(mainConfig.getEditFlagsGuiSize())
                .create();

        for(Utils.InventoryItem inventoryItem : mainConfig.getEditFlagsGuiItems()){
            if(inventoryItem.item.startsWith("flags:")){
                String flag = inventoryItem.item.replaceAll("flags:", "");
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, flag);
                guiItem.setAction( event -> {
                    event.setCancelled(true);
                    Boolean value = utils.getFlagValue(region, flag);
                    String group = utils.getFlagGroup(region, flag);
                    if(event.isLeftClick()){
                        // Alternate value Allow/Deny/None
                        if(value == Boolean.TRUE){
                            // set to deny
                            utils.updateFlag(region, flag, false, group);
                        } else if (value == Boolean.FALSE) {
                            // set to none
                            utils.updateFlag(region, flag, null, group);

                        }else {
                            // set to allow
                            utils.updateFlag(region, flag, true, group);
                        }
                        String updated = utils.getFlag(messageConfig.getEditFlagUpdated(), region, flag);
                        utils.sendMessage(player, MessageUtils.getLegacy(utils.parsePSVar(updated, region)), true);
                        openPSEditFlagsMenu(player, region);
                    }else
                    if(event.isRightClick()){
                        // Alternate Groups All/Owners/Members/NonOwners/NonMembers
                        switch (group){
                            case "all" -> utils.updateFlag(region, flag, value, "owners");
                            case "owners" -> utils.updateFlag(region, flag, value, "members");
                            case "members" -> utils.updateFlag(region, flag, value, "nonmembers");
                            case "nonmembers" -> utils.updateFlag(region, flag, value, "nonowners");
                            case "nonowners" -> utils.updateFlag(region, flag, value, "all");
                        }
                        String updated = utils.getFlag(messageConfig.getEditFlagUpdated(), region, flag);
                        utils.sendMessage(player, MessageUtils.getLegacy(utils.parsePSVar(updated, region)), true);
                        openPSEditFlagsMenu(player, region);
                    }
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction, region);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
        }

        gui.open(player);
        gui.setDefaultClickAction( inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
        });
    }

    public void openPSOwnersMenu(Player player, PSRegion region){
        Utils utils = plugin.getUtils();
        MessageConfig messageConfig = plugin.getMessageConfig();
        MainConfig mainConfig = plugin.getMainConfig();
        if( !(PSUtils.canEdit(region, player)) ){
            utils.sendMessage(player, messageConfig.getNoPermissions(), true);
            return;
        }
        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(mainConfig.getEditOwnersTitle()))
                .rows(mainConfig.getEditOwnersSize())
                .create();

        for(Utils.InventoryItem inventoryItem: mainConfig.getEditOwnersGuiItems()){
            if(inventoryItem.item.equalsIgnoreCase("owners")){
                List<Integer> slots = new ArrayList<>(inventoryItem.slots);
                Iterator<Integer> integerIterator = slots.iterator();
                for(UUID ownerUUID: region.getOwners()){
                    if(!integerIterator.hasNext()) break;

                    int slot = integerIterator.next();
                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, ownerUUID);
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        openPSPlayerMenu(player, region, ownerUUID);
                    });
                    gui.setItem(slot, guiItem);
                }
            }
            if(inventoryItem.item.equalsIgnoreCase("add-owner")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    plugin.ownerPrompts.put(player.getUniqueId(), region);
                    gui.close(player);
                    utils.sendMessage(player, messageConfig.getEditOwnerPrompt(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction, region);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }

        }

        gui.open(player);
        gui.setDefaultClickAction(inventoryClickEvent -> { inventoryClickEvent.setCancelled(true); });
    }

    public void openPSMembersMenu(Player player, PSRegion region){
        Utils utils = plugin.getUtils();
        MessageConfig messageConfig = plugin.getMessageConfig();
        MainConfig mainConfig = plugin.getMainConfig();
        if( !(PSUtils.canEdit(region, player)) ){
            utils.sendMessage(player, messageConfig.getNoPermissions(), true);
            return;
        }
        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(mainConfig.getEditMembersTitle()))
                .rows(mainConfig.getEditMembersSize())
                .create();

        for(Utils.InventoryItem inventoryItem: mainConfig.getEditMembersGuiItems()){
            if(inventoryItem.item.equalsIgnoreCase("members")){
                List<Integer> slots = new ArrayList<>(inventoryItem.slots);
                Iterator<Integer> integerIterator = slots.iterator();
                for(UUID memberUUID: region.getMembers()){
                    if(!integerIterator.hasNext()) break;
                    int slot = integerIterator.next();
                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, memberUUID);
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        openPSPlayerMenu(player, region, memberUUID);
                    });
                    gui.setItem(slot, guiItem);
                }
            }
            if(inventoryItem.item.equalsIgnoreCase("add-member")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    plugin.memberPrompts.put(player.getUniqueId(), region);
                    gui.close(player);
                    utils.sendMessage(player, messageConfig.getEditMemberPrompt(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction, region);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
        }
        gui.open(player);
        gui.setDefaultClickAction(inventoryClickEvent -> { inventoryClickEvent.setCancelled(true); });
    }

    public void openPSBansMenu(Player player, PSRegion region){
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();
        if( !(PSUtils.canEdit(region, player)) ){
            utils.sendMessage(player, messageConfig.getNoPermissions(), true);
            return;
        }
        if(!mainConfig.isBanModuleEnabled()){
            utils.sendMessage(player, messageConfig.getBanDisabled(), true);
        }
        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(utils.parsePSVar(mainConfig.getEditBansTitle(), region)))
                .rows(mainConfig.getEditBansSize())
                .create();

        for(Utils.InventoryItem inventoryItem: mainConfig.getEditBansGuiItems()){
            if(inventoryItem.item.equalsIgnoreCase("banned-players")){
                List<Integer> slots = new ArrayList<>(inventoryItem.slots);
                for(String bannedUUID: PSUtils.getBannedPlayers(region)){
                    UUID uuid = UUID.fromString(bannedUUID);
                    String name = UUIDCache.getNameFromUUID(uuid);
                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, uuid);
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        // Unban Player
                        if(event.isLeftClick()){
                            if(PSUtils.unBanPlayer(region, bannedUUID)){
                                utils.sendMessage(player, messageConfig.getBanRemoveSuccess()
                                        .replaceAll("%player%", name), true);
                                openPSBansMenu(player, region);
                            }else {
                                utils.sendMessage(player, messageConfig.getBanNot(), true);
                            }
                        }
                    });
                    for(int slot: inventoryItem.slots){
                        if(slots.contains(slot)){
                            slots.remove(slot);
                            gui.setItem(slot, guiItem);
                            break;
                        }
                    }
                }
            }
            if(inventoryItem.item.equalsIgnoreCase("add-ban")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    plugin.banPrompts.put(player.getUniqueId(), region);
                    gui.close(player);
                    utils.sendMessage(player, messageConfig.getBanPrompt(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction, region);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
        }

        gui.open(player);
        gui.setDefaultClickAction(inventoryClickEvent -> { inventoryClickEvent.setCancelled(true);});
    }

    public void openPSPlayerMenu(Player player, PSRegion region, UUID target){
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();
        String name = UUIDCache.getNameFromUUID(target);
        if( !(PSUtils.canEdit(region, player)) ){
            utils.sendMessage(player, messageConfig.getNoPermissions(), true);
            return;
        }

        if(player.getUniqueId().equals(target)){
            utils.sendMessage(player, messageConfig.getEditPlayerSelf(), true);
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

        Gui gui = Gui.gui()
                .title(MessageUtils.getColoredMessage(utils.parsePSVar(mainConfig.getEditPlayerTitle().replaceAll("%player%", offlinePlayer.getName()), region)))
                .rows(mainConfig.getEditPlayerSize())
                .create();

        for(Utils.InventoryItem inventoryItem: mainConfig.getEditPlayerGuiItems()){
            // Ban Item
            if(inventoryItem.item.equalsIgnoreCase("ban")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction( event ->{
                    event.setCancelled(true);
                    if(!mainConfig.isBanModuleEnabled()){
                        utils.sendMessage(player, messageConfig.getBanDisabled(), true);
                    }
                    if(player.getUniqueId().equals(target)){
                        utils.sendMessage(player, messageConfig.getBanSelf(), true);
                    }
                    if(PSUtils.banPlayer(region, target.toString())){
                        utils.sendMessage(player, messageConfig.getBanAddSuccess().replaceAll("%player%", name), true);
                    }else {
                        utils.sendMessage(player, messageConfig.getBanAlready().replaceAll("%player%", name), true);
                        return;
                    }
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            // Kick Item
            if(inventoryItem.item.equalsIgnoreCase("kick")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction( event -> {
                    event.setCancelled(true);
                    Player targetPlayer = Bukkit.getPlayer(target);
                    if(targetPlayer == null){
                        utils.sendMessage(player, messageConfig.getPlayerNotFound()
                                .replaceAll("%player%", name), true);
                        return;
                    }
                    if(region.isOwner(player.getUniqueId())){
                        PSUtils.kickPlayer(region, targetPlayer);
                        utils.sendMessage(player, plugin.getMessageConfig().getKickSuccess()
                                .replaceAll("%player%", name), true);
                        utils.sendMessage(targetPlayer, plugin.getMessageConfig().getKickMessage()
                                .replaceAll("%player%", player.getName()), true);

                    }else {
                        utils.sendMessage(player, plugin.getMessageConfig().getNoPermissions(), true);
                    }
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            // Remove Item
            if(inventoryItem.item.equalsIgnoreCase("remove")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    utils.sendMessage(player, messageConfig.getEditMemberRemoveSuccess().replaceAll("%player%", name), true);
                    region.removeMember(target);
                    region.removeOwner(target);
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            // Promote Item
            if(inventoryItem.item.equalsIgnoreCase("owner") && !region.isOwner(target)){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction( event -> {
                    event.setCancelled(true);
                    utils.sendMessage(player, messageConfig.getEditPlayerPromote().replaceAll("%player%", name), true);
                    region.removeMember(target);
                    region.addOwner(target);
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            // Demote Item
            if(inventoryItem.item.equalsIgnoreCase("member") && region.isOwner(target)){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction( event -> {
                    event.setCancelled(true);
                    utils.sendMessage(player, messageConfig.getEditPlayerDemote().replaceAll("%player%", name), true);
                    region.removeOwner(target);
                    region.addMember(target);
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
            // Custom item
            if(inventoryItem.item.startsWith("custom:")){
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    PSItemClickEvent event = new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction, region);
                    Bukkit.getPluginManager().callEvent(event);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
        }

        gui.open(player);
        gui.setDefaultClickAction( inventoryClickEvent -> { inventoryClickEvent.setCancelled(true);});
    }

}
