package dev.loststr1ng.protectionStonesMenu.utils;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.config.MessageConfig;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Utils {

    private final ProtectionStonesMenu plugin;

    public Utils(ProtectionStonesMenu plugin){
        this.plugin = plugin;
    }


    public class InventoryItem {

        public String item;
        public String id;
        public String urlSkull;
        public String name;
        public List<String> lore;
        public List<Integer> slots;
        public String action;
        public String argAction;
        public boolean enabled = true;

        public InventoryItem(FileConfiguration configuration, String path, String displayName){
            ConfigurationSection configurationSection = configuration.getConfigurationSection(path);
            this.item = displayName;
            if(configurationSection != null){
                this.id = configurationSection.getString("id");
                this.urlSkull = configurationSection.getString("url");
                this.enabled = configurationSection.getBoolean("enabled");
                this.name = configurationSection.getString("name");
                this.lore = configurationSection.getStringList("lore");
                if(configurationSection.getStringList("slots").isEmpty()){
                    this.slots = List.of(configurationSection.getInt("slot"));
                }else {
                    this.slots = parseSlotsRange(configurationSection.getStringList("slots"));
                }
                this.action = configurationSection.getString("action");
                if(action == null) return;
                switch (action){
                    case "OPEN_MENU" -> this.argAction = configurationSection.getString("menu");
                    case "COMMAND", "PLAYER_COMMAND" -> this.argAction = configuration.getString("command");
                    default -> this.argAction = "";
                }

            }
        }



    }

    public InventoryItem createItem(FileConfiguration configuration, String path, String s){
        return new InventoryItem(configuration, path, s);
    }

    public List<String> parsePSVar(List<String> s, PSRegion region){
        List<String> parsed = new ArrayList<>();
        for(String line: s){
            parsed.add(parsePSVar(line, region));
        }
        return parsed;
    }

    public String parsePSVar(String s, PSRegion region){
        Location location = region.getHome().getBlock().getLocation();
        String name = region.getName() != null ? region.getName() : region.getId();
        String parsed = s.replaceAll("%x%", String.valueOf(location.getBlockX()))
                .replaceAll("%y%", String.valueOf(location.getBlockY()))
                .replaceAll("%z%", String.valueOf(location.getBlockZ()))
                .replaceAll("%name%", name)
                .replaceAll("%world%", region.getWorld().getName());
        String members = "";
        for(UUID uuid: region.getMembers()){
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
            if(region.getMembers().indexOf(uuid) == (region.getMembers().size() - 1) ){
                members = String.join(members, member.getName());
            }else {
                members = String.join(members, " "+member.getName()+",");
            }
        }
        parsed = parsed.replaceAll("%members%", members);
        String owners = "";
        for(UUID uuid: region.getOwners()){
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
            if(region.getMembers().indexOf(uuid) == (region.getMembers().size() - 1) ){
                owners = String.join(owners, member.getName());
            }else {
                owners = String.join(owners, " "+member.getName()+",");
            }
        }
        parsed = parsed.replaceAll("%owners%", owners);
        String banned = "";
        for(String uuid: PSUtils.getBannedPlayers(region)){
            UUID bannedUUID = UUID.fromString(uuid);
            OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(bannedUUID);
            if(PSUtils.getBannedPlayers(region).indexOf(uuid) == (PSUtils.getBannedPlayers(region).size() - 1) ){
                banned = String.join(banned, bannedPlayer.getName());
            }else {
                banned = String.join(banned, " "+bannedPlayer.getName()+",");
            }
        }
        parsed = parsed.replaceAll("%banned%", banned);
        return parsed;
    }



    public List<Integer> parseSlotsRange(List<String> ranges){
        List<Integer> slots = new ArrayList<>();
        for (String slotConfig : ranges) {
            String[] range = slotConfig.split("-");
            if (range.length == 2) {
                try {
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);

                    for (int i = start; i <= end; i++) {
                        slots.add(i);
                    }
                } catch (NumberFormatException e) {
                    // MessageUtils.sendMessage(plugin.getConsole(), "<dark_red>Error: the config of slots '" + slotConfig + "' is invalid.");
                }
            } else {
                slots.add(Integer.parseInt(slotConfig));
                // MessageUtils.sendMessage(plugin.getConsole(),"<dark_red>Error: <red>the format range of slots '" + slotConfig + "' is invalid.");
            }
        }
        return slots;
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem){
        GuiItem guiItem;
        Material material = Material.getMaterial(inventoryItem.id);
        if(material == null) material = Material.BARRIER;
        if(material.name().equalsIgnoreCase("PLAYER_HEAD")){
            guiItem = ItemBuilder.skull()
                    .name(MessageUtils.getColoredMessage(inventoryItem.name))
                    .lore(MessageUtils.components(inventoryItem.lore))
                    .texture(inventoryItem.urlSkull)
                    .asGuiItem();
        }else {
            guiItem = ItemBuilder.from(material)
                    .name(MessageUtils.getColoredMessage(inventoryItem.name))
                    .lore(MessageUtils.components(inventoryItem.lore))
                    .asGuiItem();
        }
        return guiItem;
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion){
        GuiItem guiItem;
        Material material = Material.getMaterial(inventoryItem.id);
        if(material == null) material = Material.BARRIER;
        if(material.name().equalsIgnoreCase("PLAYER_HEAD") && inventoryItem.urlSkull != null){
            guiItem = ItemBuilder.skull()
                    .name(MessageUtils.getColoredMessage(parsePSVar(inventoryItem.name, psRegion)))
                    .lore(MessageUtils.components(parsePSVar(inventoryItem.lore, psRegion)))
                    .texture(inventoryItem.urlSkull)
                    .asGuiItem();
        }else {
            guiItem = ItemBuilder.from(material)
                    .name(MessageUtils.getColoredMessage(parsePSVar(inventoryItem.name, psRegion)))
                    .lore(MessageUtils.components(parsePSVar(inventoryItem.lore, psRegion)))
                    .asGuiItem();
        }
        return guiItem;
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion, UUID player){

        GuiItem guiItem;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        String playerName = offlinePlayer.getName();
        String name = parsePSVar(inventoryItem.name, psRegion).replaceAll("%player%", String.valueOf(playerName));
        List<String> lore = parsePSVar(inventoryItem.lore, psRegion);
        List<String> newLore = new ArrayList<>();
        lore.forEach( line -> {
            newLore.add(line.replaceAll("%player%", String.valueOf(playerName)));
        });
        Material material = Material.getMaterial(inventoryItem.id);
        if(material == null) material = Material.BARRIER;
        if(material.name().equalsIgnoreCase("PLAYER_HEAD") && inventoryItem.urlSkull != null){
            if(inventoryItem.urlSkull.equalsIgnoreCase("player")){
                guiItem = ItemBuilder.skull()
                        .name(MessageUtils.getColoredMessage(name))
                        .lore(MessageUtils.components(newLore))
                        .owner(offlinePlayer)
                        .flags(ItemFlag.values())
                        .asGuiItem();
            }else {
                guiItem = ItemBuilder.skull()
                        .name(MessageUtils.getColoredMessage(name))
                        .lore(MessageUtils.components(newLore))
                        .texture(inventoryItem.urlSkull)
                        .flags(ItemFlag.values())
                        .asGuiItem();
            }
        }else {
            guiItem = ItemBuilder.from(material)
                    .name(MessageUtils.getColoredMessage(name))
                    .lore(MessageUtils.components(newLore))
                    .flags(ItemFlag.values())
                    .asGuiItem();
        }
        return guiItem;
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion, String flag){

        GuiItem guiItem;
        String name = parsePSVar(inventoryItem.name, psRegion);
        List<String> lore = parsePSVar(inventoryItem.lore, psRegion);
        List<String> newLore = new ArrayList<>();
        lore.forEach( line -> {
            newLore.add(getFlag(line, psRegion, flag));
        });
        Material material = Material.getMaterial(inventoryItem.id);
        if(material == null) material = Material.BARRIER;
        if(material.name().equalsIgnoreCase("PLAYER_HEAD") && inventoryItem.urlSkull != null){
            guiItem = ItemBuilder.skull()
                    .name(MessageUtils.getColoredMessage(getFlag(name, psRegion, flag)))
                    .lore(MessageUtils.components(newLore))
                    .texture(inventoryItem.urlSkull)
                    .flags(ItemFlag.values())
                    .asGuiItem();
        }else {
            guiItem = ItemBuilder.from(material)
                    .name(MessageUtils.getColoredMessage(getFlag(name, psRegion, flag)))
                    .lore(MessageUtils.components(newLore))
                    .flags(ItemFlag.values())
                    .asGuiItem();
        }
        return guiItem;
    }

    public String getFlag(String s, PSRegion region, String flagName){
        MessageConfig messages = plugin.getMessageConfig();
        ProtectedRegion protectedRegion = region.getWGRegion();
        Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
        if( flag == null) return s;
        Boolean value = getFlagValue(region, flagName);
        String group = getFlagGroup(region, flagName);
        if(value == Boolean.TRUE){
            s = s.replaceAll("%value%", messages.getEditFlagAllow());
        }else if(value == Boolean.FALSE){
            s = s.replaceAll("%value%", messages.getEditFlagDeny());
        }else {
            s = s.replaceAll("%value%", messages.getEditFlagNone());
        }
        switch (group) {
            case "owners" -> s = s.replaceAll("%group%", messages.getEditFlagGroupOwners());
            case "members" -> s = s.replaceAll("%group%", messages.getEditFlagGroupMembers());
            case "nonmembers" -> s = s.replaceAll("%group%", messages.getEditFlagGroupNonMembers());
            case "nonowners" -> s = s.replaceAll("%group%", messages.getEditFlagGroupNonOwners());
            case "all" -> s = s.replaceAll("%group%", messages.getEditFlagGroupAll());
            default -> s = s.replaceAll("%group%", "Null");
        }
        s = s.replaceAll("%flag%", flagName);
        return s;
    }

    @Nullable
    public Boolean getFlagValue(PSRegion region, String flagName){
        ProtectedRegion protectedRegion = region.getWGRegion();
        Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
        if( flag == null) return null;
        Object value = protectedRegion.getFlag(flag);
        if(value == null) return null;
        if(flag instanceof BooleanFlag){
            return (Boolean) value;
        }
        if(flag instanceof StateFlag){
            if(value == StateFlag.State.ALLOW){
                return true;
            }else if (value == StateFlag.State.DENY){
                return false;
            }
        }
        return null;
    }


    public String getFlagGroup(PSRegion region, String flagName){
        ProtectedRegion protectedRegion = region.getWGRegion();
        Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
        if( flag == null) return "null";
        RegionGroup regionGroup = protectedRegion.getFlag(flag.getRegionGroupFlag());
        String group = "all";
        if(flag.getRegionGroupFlag() != null && regionGroup != null){
            group = regionGroup.toString().toLowerCase().replaceAll("_", "");
        }
        return group;
    }

    public void updateFlag(PSRegion region, String flagName, Boolean value, String group){
        ProtectedRegion protectedRegion = region.getWGRegion();
        Flag flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
        if( flag == null) return;
        RegionGroup regionGroup = RegionGroup.ALL;
        switch (group){
            case "owners" -> regionGroup = RegionGroup.OWNERS;
            case "members" -> regionGroup = RegionGroup.MEMBERS;
            case "nonowners" -> regionGroup = RegionGroup.NON_OWNERS;
            case "nonmembers" -> regionGroup = RegionGroup.NON_MEMBERS;
        }
        if(flag instanceof BooleanFlag){
            if(value == null){
                protectedRegion.setFlag(flag.getRegionGroupFlag(), regionGroup);
                protectedRegion.setFlag(flag, null);
                return;
            }
            if(value){
                protectedRegion.setFlag(flag.getRegionGroupFlag(), regionGroup);
                protectedRegion.setFlag(flag, true);
                return;
            }else {
                protectedRegion.setFlag(flag.getRegionGroupFlag(), regionGroup);
                protectedRegion.setFlag(flag, false);
                return;
            }
        } else if (flag instanceof StateFlag) {
            if(value == null){
                protectedRegion.setFlag(flag.getRegionGroupFlag(), regionGroup);
                protectedRegion.setFlag(flag, null);
                return ;
            }
            if(value){
                protectedRegion.setFlag(flag.getRegionGroupFlag(), regionGroup);
                protectedRegion.setFlag(flag, StateFlag.State.ALLOW);
                return ;
            }else {
                protectedRegion.setFlag(flag.getRegionGroupFlag(), regionGroup);
                protectedRegion.setFlag(flag, StateFlag.State.DENY);
                return ;
            }
        }
    }



    public void sendMessage(Player player, String message, boolean prefixed){
        if(prefixed){
            player.sendMessage(MessageUtils.getLegacy(plugin.getMessageConfig().getPrefix() + message));
        }else {
            player.sendMessage(MessageUtils.getLegacy(message));
        }
    }

    public void sendMessages(Player player, List<String> message){
        for(String line: message){
            sendMessage(player, line, false);
        }
    }

    public void log(String message){
        Bukkit.getConsoleSender().sendMessage(MessageUtils.getLegacy(plugin.prefix + message));
    }
}
