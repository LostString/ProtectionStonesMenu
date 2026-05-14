package dev.loststr1ng.protectionStonesMenu.utils;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.config.MessageConfig;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

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
        public String permission;
        public String action;
        public String argAction;
        public boolean enabled = true;
        public boolean lock = false;

        public InventoryItem(FileConfiguration configuration, String path, String displayName){
            ConfigurationSection configurationSection = configuration.getConfigurationSection(path);
            this.item = displayName;
            if(configurationSection != null){
                this.id = configurationSection.getString("id", "STONE");
                this.urlSkull = configurationSection.getString("url");
                this.enabled = configurationSection.getBoolean("enabled", true);
                this.lock = configurationSection.getBoolean("lock");
                this.name = configurationSection.getString("name", "Default name");
                this.permission = configurationSection.getString("permission");
                this.lore = configurationSection.getStringList("lore");
                List<String> slotList = configurationSection.getStringList("slots");
                this.slots = slotList.isEmpty()
                        ? List.of(configurationSection.getInt("slot", 1))
                        : parseSlotsRange(slotList);
                this.action = configurationSection.getString("action");
                if(action == null) return;
                this.argAction = switch (action) {
                    case "OPEN_MENU" -> configurationSection.getString("menu", "main");
                    case "COMMAND", "PLAYER_COMMAND" -> configurationSection.getString("command", "say error");
                    default -> "";
                };
            }
        }

        public boolean isValid(){
            return id != null && name != null && !slots.isEmpty();
        }

    }

    public InventoryItem createItem(FileConfiguration configuration, String path, String s){
        return new InventoryItem(configuration, path, s);
    }

    public List<PSRegion> getRegions(Player player){
        List<PSRegion> regions = new ArrayList<>();
        PSPlayer psPlayer = PSPlayer.fromPlayer(player);
        for(World world: Bukkit.getWorlds()){
            regions.addAll(psPlayer.getPSRegions(world, true));
        }
        return regions;
    }


    public List<String> parsePSVar(List<String> s, PSRegion region){
        List<String> parsed = new ArrayList<>(s.size());
        for(String line: s){
            parsed.add(parsePSVar(line, region));
        }
        return parsed;
    }

    public String parsePSVar(String s, PSRegion region){
        String parsed = getLocationParsed(s, region);

        parsed = parsed.replace("%members%", joinNames(region.getMembers()));
        parsed = parsed.replace("%owners%", joinNames(region.getOwners()));

        
        List<String> bannedUUIDs = PSUtils.getBannedPlayers(region);
        StringJoiner bannedJoiner = new StringJoiner(", ");
        for (String uuid : bannedUUIDs) {
            bannedJoiner.add(UUIDCache.getNameFromUUID(UUID.fromString(uuid)));
        }
        parsed = parsed.replace("%banned%", bannedJoiner.toString());
        return parsed;
    }

    private static @NotNull String getLocationParsed(String s, PSRegion region) {
        Location location = region.getHome().getBlock().getLocation();
        String name = region.getName() != null ? region.getName() : region.getId();

        // Use replace() instead of replaceAll() — these are literal strings, not regex patterns
        return s.replace("%x%", String.valueOf(location.getBlockX()))
                .replace("%y%", String.valueOf(location.getBlockY()))
                .replace("%z%", String.valueOf(location.getBlockZ()))
                .replace("%name%", name)
                .replace("%world%", region.getWorld().getName());
    }


    private String joinNames(List<UUID> uuids) {
        StringJoiner joiner = new StringJoiner(", ");
        for (UUID uuid : uuids) {
            joiner.add(UUIDCache.getNameFromUUID(uuid));
        }
        return joiner.toString();
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
                } catch (NumberFormatException ignored) {
                    
                }
            } else {
                try {
                    slots.add(Integer.parseInt(slotConfig));
                } catch (NumberFormatException ignored) {
                    
                }
            }
        }
        return slots;
    }


    public GuiItem createItemBuilder(InventoryItem inventoryItem){
        return createItemBuilder(inventoryItem, (Player) null);
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, @Nullable Player viewer){
        return buildGuiItem(inventoryItem, inventoryItem.name, inventoryItem.lore,
                resolveMaterial(inventoryItem, null), null, viewer, false);
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion){
        return createItemBuilder(inventoryItem, psRegion, (Player) null);
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion, @Nullable Player viewer){
        if(psRegion == null) return createItemBuilder(inventoryItem, viewer);
        String name = parsePSVar(inventoryItem.name, psRegion);
        List<String> lore = parsePSVar(inventoryItem.lore, psRegion);
        return buildGuiItem(inventoryItem, name, lore,
                resolveMaterial(inventoryItem, psRegion), null, viewer, false);
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion, UUID player){
        return createItemBuilder(inventoryItem, psRegion, player, null);
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion, UUID player, @Nullable Player viewer){

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        String playerName = String.valueOf(offlinePlayer.getName());
        String name = parsePSVar(inventoryItem.name, psRegion).replace("%player%", playerName);
        List<String> lore = new ArrayList<>();
        for (String line : parsePSVar(inventoryItem.lore, psRegion)) {
            lore.add(line.replace("%player%", playerName));
        }
        return buildGuiItem(inventoryItem, name, lore,
                resolveMaterial(inventoryItem, psRegion), offlinePlayer, viewer, true);
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion, String flag){
        return createItemBuilder(inventoryItem, psRegion, flag, null);
    }

    public GuiItem createItemBuilder(InventoryItem inventoryItem, PSRegion psRegion, String flag, @Nullable Player viewer){
        String name = getFlag(parsePSVar(inventoryItem.name, psRegion), psRegion, flag);
        List<String> lore = new ArrayList<>();
        for (String line : parsePSVar(inventoryItem.lore, psRegion)) {
            lore.add(getFlag(line, psRegion, flag));
        }
        return buildGuiItem(inventoryItem, name, lore,
                resolveMaterial(inventoryItem, psRegion), null, viewer, true);
    }


    private Material resolveMaterial(InventoryItem inventoryItem, @Nullable PSRegion psRegion) {
        if (psRegion != null && inventoryItem.id != null && inventoryItem.id.equalsIgnoreCase("PS_BLOCK")) {
            return psRegion.getProtectBlock().getType();
        }
        if(inventoryItem.id == null){
            return Material.BARRIER;
        }
        Material material = Material.getMaterial(inventoryItem.id);
        return material != null ? material : Material.BARRIER;
    }

    /**
     * item builder
     *
     * @param inventoryItem  the item config
     * @param name           already-parsed display name
     * @param lore           already-parsed lore lines
     * @param material       resolved material
     * @param skullOwner     if non-null and urlSkull == "player", sets the skull owner
     * @param addItemFlags   whether to add ItemFlag.values()
     */
    @SuppressWarnings("deprecation")
    private GuiItem buildGuiItem(InventoryItem inventoryItem, String name, List<String> lore,
                                 Material material, @Nullable OfflinePlayer skullOwner,
                                 @Nullable Player viewer, boolean addItemFlags) {
        GuiItem guiItem;
        if (material.name().equalsIgnoreCase("PLAYER_HEAD") && inventoryItem.urlSkull != null) {
            SkullBuilder builder = ItemBuilder.skull()
                    .setName(MessageUtils.getLegacyFallback(viewer, name))
                    .setLore(MessageUtils.getColoredList(viewer, lore));

            if (skullOwner != null && inventoryItem.urlSkull.equalsIgnoreCase("player")) {
                builder.owner(skullOwner);
            } else {
                builder.texture(inventoryItem.urlSkull);
            }

            if (addItemFlags) builder.flags(ItemFlag.values());
            guiItem = builder.asGuiItem();
        } else {
            var builder = ItemBuilder.from(material)
                    .setName(MessageUtils.getLegacyFallback(viewer, name))
                    .setLore(MessageUtils.getColoredList(viewer, lore));

            if (addItemFlags) builder.flags(ItemFlag.values());
            guiItem = builder.asGuiItem();
        }

        applyAdventureDisplay(guiItem, name, lore, viewer);
        return guiItem;
    }

    private void applyAdventureDisplay(GuiItem guiItem, String name, List<String> lore, @Nullable Player viewer) {
        try {
            Method getter = GuiItem.class.getMethod("getItemStack");
            Object result = getter.invoke(guiItem);
            if (!(result instanceof ItemStack itemStack)) return;

            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) return;

            Method displayName = meta.getClass().getMethod("displayName", net.kyori.adventure.text.Component.class);
            displayName.invoke(meta, MessageUtils.getColoredMessage(viewer, name));

            Method metaLore = meta.getClass().getMethod("lore", List.class);
            metaLore.invoke(meta, MessageUtils.components(viewer, lore));

            itemStack.setItemMeta(meta);
            guiItem.setItemStack(itemStack);
        } catch (ReflectiveOperationException ignored) {
            // Spigot-only item meta does not expose Adventure components; legacy text is already applied.
        }
    }


    private Flag<?> lookupFlag(String flagName) {
        return Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
    }

    public String getFlag(String s, PSRegion region, String flagName){
        MessageConfig messages = plugin.getMessageConfig();
        Flag<?> flag = lookupFlag(flagName);
        if (flag == null) return s;

        String group = getFlagGroup(region, flagName);

        if (flag instanceof BooleanFlag || flag instanceof StateFlag) {
            Boolean value = getFlagValue(region, flagName);
            if (value == Boolean.TRUE) {
                s = s.replace("%value%", messages.getEditFlagAllow());
            } else if (value == Boolean.FALSE) {
                s = s.replace("%value%", messages.getEditFlagDeny());
            } else {
                s = s.replace("%value%", messages.getEditFlagNone());
            }
        } else if (flag instanceof StringFlag) {
            String stringFlag = getStringFlag(region, flagName);
            s = s.replace("%value%", stringFlag != null ? stringFlag : messages.getEditFlagNone());
        }

        s = s.replace("%group%", switch (group) {
            case "owners"     -> messages.getEditFlagGroupOwners();
            case "members"    -> messages.getEditFlagGroupMembers();
            case "nonmembers" -> messages.getEditFlagGroupNonMembers();
            case "nonowners"  -> messages.getEditFlagGroupNonOwners();
            case "all"        -> messages.getEditFlagGroupAll();
            default           -> "Null";
        });

        s = s.replace("%flag%", flagName);
        return s;
    }

    @Nullable
    public Boolean getFlagValue(PSRegion region, String flagName){
        ProtectedRegion protectedRegion = region.getWGRegion();
        Flag<?> flag = lookupFlag(flagName);
        if (flag == null) return null;

        Object value = protectedRegion.getFlag(flag);
        if (value == null) return null;

        if (flag instanceof BooleanFlag) {
            return (Boolean) value;
        }
        if (flag instanceof StateFlag) {
            return value == StateFlag.State.ALLOW ? Boolean.TRUE
                 : value == StateFlag.State.DENY  ? Boolean.FALSE
                 : null;
        }
        return null;
    }

    public String getStringFlag(PSRegion region, String flagName){
        ProtectedRegion rg = region.getWGRegion();
        Flag<?> flag = lookupFlag(flagName);
        if (flag instanceof StringFlag stringFlag) {
            Object value = rg.getFlag(stringFlag);
            return value != null ? value.toString() : null;
        }
        return null;
    }

    public boolean isStringFlag(String flagName){
        return lookupFlag(flagName) instanceof StringFlag;
    }

    public String getFlagGroup(PSRegion region, String flagName){
        ProtectedRegion protectedRegion = region.getWGRegion();
        Flag<?> flag = lookupFlag(flagName);
        if (flag == null) return "null";

        RegionGroup regionGroup = protectedRegion.getFlag(flag.getRegionGroupFlag());
        if (flag.getRegionGroupFlag() != null && regionGroup != null) {
            return regionGroup.toString().toLowerCase().replace("_", "");
        }
        return "all";
    }

    public void updateFlag(PSRegion region, String flagName, Object value, String group){
        ProtectedRegion protectedRegion = region.getWGRegion();
        Flag<?> flag = lookupFlag(flagName);
        if (flag == null) return;

        RegionGroup regionGroup = switch (group) {
            case "owners"     -> RegionGroup.OWNERS;
            case "members"    -> RegionGroup.MEMBERS;
            case "nonowners"  -> RegionGroup.NON_OWNERS;
            case "nonmembers" -> RegionGroup.NON_MEMBERS;
            default           -> RegionGroup.ALL;
        };

        protectedRegion.setFlag(flag.getRegionGroupFlag(), regionGroup);

        if (value == null) {
            protectedRegion.setFlag(flag, null);
            return;
        }

        if (flag instanceof BooleanFlag booleanFlag) {
            protectedRegion.setFlag(booleanFlag, (Boolean) value);
        } else if (flag instanceof StateFlag stateFlag) {
            protectedRegion.setFlag(stateFlag,
                    (Boolean) value ? StateFlag.State.ALLOW : StateFlag.State.DENY);
        } else if (flag instanceof StringFlag stringFlag) {
            protectedRegion.setFlag(stringFlag, (String) value);
        }
    }


    public void sendMessage(Player player, String message, boolean prefixed){
        String parsed = prefixed ? plugin.getMessageConfig().getPrefix() + message : message;
        sendFormatted(player, parsed);
    }

    private void sendFormatted(Player player, String message) {
        try {
            Method sendMessage = player.getClass().getMethod("sendMessage", net.kyori.adventure.text.Component.class);
            sendMessage.invoke(player, MessageUtils.getColoredMessage(player, message));
            return;
        } catch (ReflectiveOperationException ignored) {
            player.sendMessage(MessageUtils.getLegacy(player, message));
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

    public void teleportPlayer(Player player, Location location, int delay){
        if(delay <= 0){
            teleportPlayer(player, location);
            return;
        }else {
            Location actualLoc = player.getLocation();
            if(plugin.isFolia()){
                AtomicInteger finalD = new AtomicInteger(delay);

                plugin.getScheduler().runTaskTimer(player, scheduledTask -> {
                    if(finalD.get() == 0){
                        plugin.getScheduler().cancelTask(scheduledTask);
                        plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getTeleported(), true);
                        teleportPlayer(player, location);
                        return;
                    }
                    if(!equalsLocCords(actualLoc, player.getLocation())){
                        plugin.getScheduler().cancelTask(scheduledTask);
                        plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getTeleportCancelled(), true);
                        return;
                    }
                    plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getTeleporting()
                            .replace("%time%", String.valueOf(finalD.get())), true);
                    finalD.set(finalD.get() - 1);
                    }, 0L, 20L);
                return;
            }
            new BukkitRunnable() {
                private int delayed = delay;
                /**
                 * Runs this operation.
                 */
                @Override
                public void run() {
                    if(delayed == 0){
                        cancel();
                        plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getTeleported(), true);
                        teleportPlayer(player, location);
                        return;
                    }
                    if(!equalsLocCords(actualLoc, player.getLocation())){
                        cancel();
                        plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getTeleportCancelled(), true);
                        return;
                    }
                    plugin.getUtils().sendMessage(player, plugin.getMessageConfig().getTeleporting()
                            .replace("%time%", String.valueOf(delayed)), true);
                    delayed--;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    public boolean equalsLocCords(Location location1, Location location2){
        return location1.getY() == location2.getY() && location1.getX() == location2.getX() && location1.getZ() == location2.getZ();
    }

    public void teleportPlayer(Player player, Location location){
        if(plugin.isFolia()){
            plugin.getScheduler().teleport(player, location);
            return;
        }
        player.teleport(location);
    }
}
