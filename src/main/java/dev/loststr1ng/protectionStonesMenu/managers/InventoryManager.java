package dev.loststr1ng.protectionStonesMenu.managers;

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.config.MainConfig;
import dev.loststr1ng.protectionStonesMenu.config.MessageConfig;
import dev.loststr1ng.protectionStonesMenu.enums.PromptType;
import dev.loststr1ng.protectionStonesMenu.listeners.PSItemClickEvent;
import dev.loststr1ng.protectionStonesMenu.models.PromptModel;
import dev.loststr1ng.protectionStonesMenu.utils.MessageUtils;
import dev.loststr1ng.protectionStonesMenu.utils.PSUtils;
import dev.loststr1ng.protectionStonesMenu.utils.Utils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class InventoryManager {

    protected final ProtectionStonesMenu plugin;

    public InventoryManager(ProtectionStonesMenu plugin) {
        this.plugin = plugin;
    }


    private boolean hasPermission(Player player, String permission) {
        return !plugin.getMainConfig().isUsePermissions() || player.hasPermission(permission);
    }


    private boolean checkCanEdit(Player player, PSRegion region) {
        if (PSUtils.canEdit(region, player)) {
            return true;
        }
        plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getNoPermissions(), true);
        return false;
    }

    private Gui createAndOpenGui(Player player, String title, int rows) {

        return Gui.gui()
                .title(MessageUtils.getColoredMessage(title))
                .rows(rows)
                .create();
    }

    private void openGui(Gui gui, Player player) {
        gui.open(player);
        gui.setDefaultClickAction(event -> event.setCancelled(true));
    }

    /**
     * Cycles to the next flag group in the rotation:
     * all → owners → members → nonmembers → nonowners → all
     */
    private static String nextGroup(String current) {
        return switch (current) {
            case "all"        -> "owners";
            case "owners"     -> "members";
            case "members"    -> "nonmembers";
            case "nonmembers" -> "nonowners";
            default           -> "all";
        };
    }


    public void openGui(Player player, String gui, PSRegion region) {
        switch (gui.toLowerCase()) {
            case "main"  -> openPSMainMenu(player);
            case "homes" -> openPSHomeMenu(player);
            case "edit"  -> { if (region != null) openPSEditMenu(player, region); }
        }
    }


    public void openPSMainMenu(Player player) {
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        PSRegion region = PSRegion.fromLocation(player.getLocation());

        Gui gui = createAndOpenGui(player, mainConfig.getMainGuiTitle(), mainConfig.getMainGuiSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getMainGuiItems()) {
            String type = inventoryItem.item;

            if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, region, player);
            } else if (type.equalsIgnoreCase("ps-homes")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    openPSHomeMenu(player);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (region != null && type.equalsIgnoreCase("ps-info")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (region == null && type.equalsIgnoreCase("ps-info2")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem);
                guiItem.setAction(e -> e.setCancelled(true));
                gui.setItem(inventoryItem.slots, guiItem);
            }
        }

        openGui(gui, player);
    }


    public void openPSHomeMenu(Player player) {
        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();

        Gui gui = createAndOpenGui(player, mainConfig.getHomesGuiTitle(), mainConfig.getHomesGuiSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getHomesGuiItems()) {
            String type = inventoryItem.item;

            if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, null, player);
            } else if (type.equalsIgnoreCase("ps-item")) {
                Iterator<Integer> slotIterator = inventoryItem.slots.iterator();

                for (PSRegion psRegion : utils.getRegions(player)) {
                    if (!slotIterator.hasNext()) break;
                    int slot = slotIterator.next();

                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, psRegion);
                    guiItem.setAction(e -> {
                        e.setCancelled(true);
                        if (e.isLeftClick()) {
                            if (hasPermission(player, "protectionstones.home")) {
                                utils.teleportPlayer(player, psRegion.getHome(), plugin.getMainConfig().getTeleportDelay());
                                gui.close(player);
                            } else {
                                openPSEditMenu(player, psRegion);
                            }
                        } else if (e.isRightClick()) {
                            openPSEditMenu(player, psRegion);
                        }
                    });
                    gui.setItem(slot, guiItem);
                }
            }
        }

        openGui(gui, player);
    }


    public void openPSEditMenu(Player player, PSRegion region) {
        if (!checkCanEdit(player, region)) return;

        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();

        Gui gui = createAndOpenGui(player,
                utils.parsePSVar(mainConfig.getEditGuiTitle(), region),
                mainConfig.getEditGuiSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getEditGuiItems()) {
            if (!inventoryItem.isValid() || !inventoryItem.enabled) continue;

            String type = inventoryItem.item;

            if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, region, player);
            } else if (type.equalsIgnoreCase("ps-rename")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    if (!region.isOwner(player.getUniqueId()) && !hasPermission(player, "protectionstones.name")) {
                        utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                        return;
                    }
                    gui.close(player);
                    plugin.promptModelMap.put(player.getUniqueId(),
                            new PromptModel(player, region, PromptType.RENAME));
                    utils.sendMessage(player, messageConfig.getEditRename(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("ps-flags")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    openPSEditFlagsMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("ps-owners")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    openPSOwnersMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("ps-members")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    openPSMembersMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("ps-hide-on") && !region.isHidden()) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    if (!hasPermission(player, "protectionstones.hide")) {
                        utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                        return;
                    }
                    region.hide();
                    openPSEditMenu(player, region);
                    utils.sendMessage(player, messageConfig.getEditVisibilityOff(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("ps-hide-off") && region.isHidden()) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    if (!hasPermission(player, "protectionstones.hide")) {
                        utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                        return;
                    }
                    region.unhide();
                    openPSEditMenu(player, region);
                    utils.sendMessage(player, messageConfig.getEditVisibilityOn(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("ps-ban") && mainConfig.isBanModuleEnabled()) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(e -> {
                    e.setCancelled(true);
                    openPSBansMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            }
        }

        openGui(gui, player);
    }


    public void openPSEditFlagsMenu(Player player, PSRegion region) {
        if (!checkCanEdit(player, region)) return;

        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();

        Gui gui = createAndOpenGui(player,
                mainConfig.getEditFlagsGuiTitle(),
                mainConfig.getEditFlagsGuiSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getEditFlagsGuiItems()) {
            String type = inventoryItem.item;

            if (type.startsWith("flags:")) {
                String flag = type.substring(6); // faster than replaceAll("flags:", "")
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, flag);

                if (utils.isStringFlag(flag)) {
                    String flagString = utils.getStringFlag(region, flag);
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        if (!hasPermission(player, "protectionstones.flags.edit." + flag)) {
                            utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                            return;
                        }
                        if (event.isLeftClick()) {
                            gui.close(player);
                            PromptModel promptModel = new PromptModel(player, region, PromptType.EDIT_FLAG);
                            promptModel.setArgs(flag);
                            plugin.promptModelMap.put(player.getUniqueId(), promptModel);
                            utils.sendMessage(player, messageConfig.getValuePrompt()
                                    .replace("%flag%", flag), true);
                        } else if (event.isRightClick() && !inventoryItem.lock) {
                            String group = utils.getFlagGroup(region, flag);
                            utils.updateFlag(region, flag, flagString, nextGroup(group));
                            sendFlagUpdatedMessage(utils, messageConfig, player, region, flag);
                            openPSEditFlagsMenu(player, region);
                        }
                    });
                } else {
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        if (!hasPermission(player, "protectionstones.flags.edit." + flag)) {
                            utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                            return;
                        }
                        Boolean value = utils.getFlagValue(region, flag);
                        String group = utils.getFlagGroup(region, flag);

                        if (event.isLeftClick()) {
                            // Cycle: true → false → null → true
                            if (value == Boolean.TRUE) {
                                utils.updateFlag(region, flag, false, group);
                            } else if (value == Boolean.FALSE) {
                                utils.updateFlag(region, flag, null, group);
                            } else {
                                utils.updateFlag(region, flag, true, group);
                            }
                            sendFlagUpdatedMessage(utils, messageConfig, player, region, flag);
                            openPSEditFlagsMenu(player, region);
                        } else if (event.isRightClick() && !inventoryItem.lock) {
                            utils.updateFlag(region, flag, value, nextGroup(group));
                            sendFlagUpdatedMessage(utils, messageConfig, player, region, flag);
                            openPSEditFlagsMenu(player, region);
                        }
                    });
                }
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, region, player);
            }
        }

        openGui(gui, player);
    }

    private void sendFlagUpdatedMessage(Utils utils, MessageConfig messageConfig,
                                        Player player, PSRegion region, String flag) {
        String updated = utils.getFlag(messageConfig.getEditFlagUpdated(), region, flag);
        utils.sendMessage(player, MessageUtils.getLegacy(utils.parsePSVar(updated, region)), true);
    }


    public void openPSOwnersMenu(Player player, PSRegion region) {
        if (!checkCanEdit(player, region)) return;

        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();

        Gui gui = createAndOpenGui(player,
                mainConfig.getEditOwnersTitle(),
                mainConfig.getEditOwnersSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getEditOwnersGuiItems()) {
            String type = inventoryItem.item;

            if (type.equalsIgnoreCase("owners")) {
                Iterator<Integer> slotIterator = inventoryItem.slots.iterator();
                for (UUID ownerUUID : region.getOwners()) {
                    if (!slotIterator.hasNext()) break;
                    int slot = slotIterator.next();
                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, ownerUUID);
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        openPSPlayerMenu(player, region, ownerUUID);
                    });
                    gui.setItem(slot, guiItem);
                }
            } else if (type.equalsIgnoreCase("add-owner")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    if (!hasPermission(player, "protectionstones.owners")) {
                        utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                        return;
                    }
                    plugin.promptModelMap.put(player.getUniqueId(),
                            new PromptModel(player, region, PromptType.ADD_OWNER));
                    gui.close(player);
                    utils.sendMessage(player, messageConfig.getEditOwnerPrompt(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, region, player);
            }
        }

        openGui(gui, player);
    }


    public void openPSMembersMenu(Player player, PSRegion region) {
        if (!checkCanEdit(player, region)) return;

        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();

        Gui gui = createAndOpenGui(player,
                mainConfig.getEditMembersTitle(),
                mainConfig.getEditMembersSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getEditMembersGuiItems()) {
            String type = inventoryItem.item;

            if (type.equalsIgnoreCase("members")) {
                Iterator<Integer> slotIterator = inventoryItem.slots.iterator();
                for (UUID memberUUID : region.getMembers()) {
                    if (!slotIterator.hasNext()) break;
                    int slot = slotIterator.next();
                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, memberUUID);
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        openPSPlayerMenu(player, region, memberUUID);
                    });
                    gui.setItem(slot, guiItem);
                }
            } else if (type.equalsIgnoreCase("add-member")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    if (!hasPermission(player, "protectionstones.members")) {
                        utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                        return;
                    }
                    plugin.promptModelMap.put(player.getUniqueId(),
                            new PromptModel(player, region, PromptType.ADD_MEMBER));
                    gui.close(player);
                    utils.sendMessage(player, messageConfig.getEditMemberPrompt(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, region, player);
            }
        }

        openGui(gui, player);
    }


    public void openPSBansMenu(Player player, PSRegion region) {
        if (!checkCanEdit(player, region)) return;

        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();

        if (!mainConfig.isBanModuleEnabled()) {
            utils.sendMessage(player, messageConfig.getBanDisabled(), true);
            return; // original code was missing this return
        }

        Gui gui = createAndOpenGui(player,
                utils.parsePSVar(mainConfig.getEditBansTitle(), region),
                mainConfig.getEditBansSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getEditBansGuiItems()) {
            String type = inventoryItem.item;

            if (type.equalsIgnoreCase("banned-players")) {
                Iterator<Integer> slotIterator = inventoryItem.slots.iterator();
                for (String bannedUUID : PSUtils.getBannedPlayers(region)) {
                    if (!slotIterator.hasNext()) break;
                    int slot = slotIterator.next();
                    UUID uuid = UUID.fromString(bannedUUID);
                    String name = UUIDCache.getNameFromUUID(uuid);
                    GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, uuid);
                    guiItem.setAction(event -> {
                        event.setCancelled(true);
                        if (event.isLeftClick()) {
                            if (PSUtils.unBanPlayer(region, bannedUUID)) {
                                utils.sendMessage(player, messageConfig.getBanRemoveSuccess()
                                        .replace("%player%", name), true);
                                openPSBansMenu(player, region);
                            } else {
                                utils.sendMessage(player, messageConfig.getBanNot(), true);
                            }
                        }
                    });
                    gui.setItem(slot, guiItem);
                }
            } else if (type.equalsIgnoreCase("add-ban")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    plugin.promptModelMap.put(player.getUniqueId(),
                            new PromptModel(player, region, PromptType.BAN));
                    gui.close(player);
                    utils.sendMessage(player, messageConfig.getBanPrompt(), true);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, region, player);
            }
        }

        openGui(gui, player);
    }


    public void openPSPlayerMenu(Player player, PSRegion region, UUID target) {
        if (!checkCanEdit(player, region)) return;

        Utils utils = plugin.getUtils();
        MainConfig mainConfig = plugin.getMainConfig();
        MessageConfig messageConfig = plugin.getMessageConfig();
        String name = UUIDCache.getNameFromUUID(target);

        if (player.getUniqueId().equals(target)) {
            utils.sendMessage(player, messageConfig.getEditPlayerSelf(), true);
            return;
        }

        Gui gui = createAndOpenGui(player,
                utils.parsePSVar(mainConfig.getEditPlayerTitle().replace("%player%", name), region),
                mainConfig.getEditPlayerSize());

        for (Utils.InventoryItem inventoryItem : mainConfig.getEditPlayerGuiItems()) {
            String type = inventoryItem.item;

            if (type.equalsIgnoreCase("ban")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    if (!mainConfig.isBanModuleEnabled()) {
                        utils.sendMessage(player, messageConfig.getBanDisabled(), true);
                        return; // FIX: was missing return
                    }
                    if (player.getUniqueId().equals(target)) {
                        utils.sendMessage(player, messageConfig.getBanSelf(), true);
                        return; // FIX: was missing return
                    }
                    if (PSUtils.banPlayer(region, target.toString())) {
                        utils.sendMessage(player, messageConfig.getBanAddSuccess()
                                .replace("%player%", name), true);
                        openPSEditMenu(player, region);
                    } else {
                        utils.sendMessage(player, messageConfig.getBanAlready()
                                .replace("%player%", name), true);
                    }
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("kick")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    Player targetPlayer = Bukkit.getPlayer(target);
                    if (targetPlayer == null) {
                        utils.sendMessage(player, messageConfig.getPlayerNotFound()
                                .replace("%player%", name), true);
                        return;
                    }
                    if (region.isOwner(player.getUniqueId())) {
                        PSUtils.kickPlayer(region, targetPlayer);
                        utils.sendMessage(player, messageConfig.getKickSuccess()
                                .replace("%player%", name), true);
                        utils.sendMessage(targetPlayer, messageConfig.getKickMessage()
                                .replace("%player%", player.getName()), true);
                    } else {
                        utils.sendMessage(player, messageConfig.getNoPermissions(), true);
                    }
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("remove")) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    utils.sendMessage(player, messageConfig.getEditMemberRemoveSuccess()
                            .replace("%player%", name), true);
                    region.removeMember(target);
                    region.removeOwner(target);
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("owner") && !region.isOwner(target)) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    utils.sendMessage(player, messageConfig.getEditPlayerPromote()
                            .replace("%player%", name), true);
                    region.removeMember(target);
                    region.addOwner(target);
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.equalsIgnoreCase("member") && region.isOwner(target)) {
                GuiItem guiItem = utils.createItemBuilder(inventoryItem, region, target);
                guiItem.setAction(event -> {
                    event.setCancelled(true);
                    utils.sendMessage(player, messageConfig.getEditPlayerDemote()
                            .replace("%player%", name), true);
                    region.removeOwner(target);
                    region.addMember(target);
                    openPSEditMenu(player, region);
                });
                gui.setItem(inventoryItem.slots, guiItem);
            } else if (type.startsWith("custom:")) {
                addCustomItem(gui, inventoryItem, region, player);
            }
        }

        openGui(gui, player);
    }


    private void addCustomItem(Gui gui, Utils.InventoryItem inventoryItem, PSRegion region, Player player) {
        GuiItem guiItem = plugin.getUtils().createItemBuilder(inventoryItem, region);
        guiItem.setAction(event -> {
            event.setCancelled(true);
            boolean allowed = !plugin.getMainConfig().isUsePermissions()
                    || inventoryItem.permission == null
                    || player.hasPermission(inventoryItem.permission);

            if (allowed) {
                Bukkit.getPluginManager().callEvent(
                        new PSItemClickEvent(player, gui, inventoryItem.action, inventoryItem.argAction, region));
            } else {
                plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getNoPermissions(), true);
            }
        });
        gui.setItem(inventoryItem.slots, guiItem);
    }
}
