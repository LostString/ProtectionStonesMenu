package dev.loststr1ng.protectionStonesMenu.config;

import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.models.ConfigModel;
import dev.loststr1ng.protectionStonesMenu.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MainConfig {

    private final ProtectionStonesMenu plugin;
    private final ConfigModel configModel;

    // General configs
    private boolean banModuleEnabled;
    private boolean kickModuleEnabled;
    private boolean openPSCommands;
    private boolean openOnClickBlock;
    private boolean usePermissions;
    private int teleportDelay;

    // comand
    private String CommandName;
    private String CommandDescription;
    private String CommandPermission;
    private List<String> CommandAliases;

    // Main Gui
    private String mainGuiTitle;
    private int mainGuiSize;
    private final List<Utils.InventoryItem> mainGuiItems = new ArrayList<>();

    // Homes Gui
    private String homesGuiTitle;
    private int homesGuiSize;
    private final List<Utils.InventoryItem> homesGuiItems = new ArrayList<>();

    // Edit Gui
    private String editGuiTitle;
    private int editGuiSize;
    private final List<Utils.InventoryItem> editGuiItems = new ArrayList<>();

    // Edit Flags Gui
    private String editFlagsGuiTitle;
    private int editFlagsGuiSize;
    private final List<Utils.InventoryItem> editFlagsGuiItems = new ArrayList<>();

    // Edit Owners Gui
    private String editOwnersTitle;
    private int editOwnersSize;
    private final List<Utils.InventoryItem> editOwnersGuiItems = new ArrayList<>();

    // Edit Members Gui
    private String editMembersTitle;
    private int editMembersSize;
    private final List<Utils.InventoryItem> editMembersGuiItems = new ArrayList<>();

    // Edit Ban Gui
    private String editBansTitle;
    private int editBansSize;
    private final List<Utils.InventoryItem> editBansGuiItems = new ArrayList<>();

    // Edit Player Gui
    private String editPlayerTitle;
    private int editPlayerSize;
    private final List<Utils.InventoryItem> editPlayerGuiItems = new ArrayList<>();

    public MainConfig(ProtectionStonesMenu plugin){
        this.plugin = plugin;
        this.configModel = new ConfigModel("config.yml", plugin, null, false);
        this.configModel.registerConfig();
        loadConfig();

    }

    public void loadConfig(){
        FileConfiguration configuration = configModel.getConfig();
        // General Configs
        this.banModuleEnabled = configuration.getBoolean("config.banModule");
        this.kickModuleEnabled = configuration.getBoolean("config.kickModule");
        this.openPSCommands = configuration.getBoolean("config.openPSCommands");
        this.openOnClickBlock = configuration.getBoolean("config.openOnClickBlock");
        this.usePermissions = configuration.getBoolean("config.usePermissions");
        this.teleportDelay = configuration.getInt("config.teleportDelay", 0);
        // command config
        this.CommandName = configuration.getString("command.name", "psmenu");
        this.CommandDescription = configuration.getString("command.description", "Main plugin command");
        this.CommandPermission = configuration.getString("command.permission", "psmenu.admin");
        this.CommandAliases = configuration.getStringList("command.aliases");
        // Main gui
        ConfigurationSection mainGui = configuration.getConfigurationSection("guis.main");
        if(mainGui != null){
            this.mainGuiTitle = mainGui.getString("title");
            this.mainGuiSize = mainGui.getInt("rows");
            // ps-item
            Utils.InventoryItem psItem = plugin.getUtils().createItem(configuration, "guis.main.ps-homes","ps-homes");
            mainGuiItems.add(psItem);
            // ps-info
            Utils.InventoryItem psInfoItem = plugin.getUtils().createItem(configuration, "guis.main.ps-info","ps-info");
            mainGuiItems.add(psInfoItem);
            // ps-info2
            Utils.InventoryItem psInfoItem2 = plugin.getUtils().createItem(configuration, "guis.main.ps-info2","ps-info2");
            mainGuiItems.add(psInfoItem2);
            // fillers item
            ConfigurationSection fillers = mainGui.getConfigurationSection("custom");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.main.custom."+key, "custom:"+key);
                    mainGuiItems.add(fillerItem);
                }
            }
        }

        // Homes gui
        ConfigurationSection homesGui = configuration.getConfigurationSection("guis.homes");
        if(homesGui != null){
            this.homesGuiTitle = homesGui.getString("title");
            this.homesGuiSize = homesGui.getInt("rows");
            // ps-item
            Utils.InventoryItem psItem = plugin.getUtils().createItem(configuration, "guis.homes.ps-item","ps-item");
            homesGuiItems.add(psItem);
            // fillers item
            ConfigurationSection fillers = homesGui.getConfigurationSection("custom");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.homes.custom."+key, "custom:"+key);
                    homesGuiItems.add(fillerItem);
                }
            }
        }

        // Edit Homes GUI

        ConfigurationSection editGui = configuration.getConfigurationSection("guis.edit");
        if(editGui != null){
            this.editGuiTitle = editGui.getString("title");
            this.editGuiSize = editGui.getInt("rows");
            // ps-rename
            Utils.InventoryItem psRename = plugin.getUtils().createItem(configuration, "guis.edit.ps-rename","ps-rename");
            editGuiItems.add(psRename);
            // ps-flags
            Utils.InventoryItem psFlags = plugin.getUtils().createItem(configuration, "guis.edit.ps-flags","ps-flags");
            editGuiItems.add(psFlags);
            // ps-owners
            Utils.InventoryItem psOwners = plugin.getUtils().createItem(configuration, "guis.edit.ps-owners","ps-owners");
            editGuiItems.add(psOwners);
            // ps-members
            Utils.InventoryItem psMembers = plugin.getUtils().createItem(configuration, "guis.edit.ps-members","ps-members");
            editGuiItems.add(psMembers);
            // ps-hide-on & off
            Utils.InventoryItem psHideOn = plugin.getUtils().createItem(configuration, "guis.edit.ps-hide-on","ps-hide-on");
            Utils.InventoryItem psHideOff = plugin.getUtils().createItem(configuration, "guis.edit.ps-hide-off","ps-hide-off");
            editGuiItems.add(psHideOn);
            editGuiItems.add(psHideOff);
            // ps-ban
            Utils.InventoryItem psBan = plugin.getUtils().createItem(configuration, "guis.edit.ps-ban","ps-ban");
            editGuiItems.add(psBan);
            // fillers item
            ConfigurationSection fillers = editGui.getConfigurationSection("custom");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.edit.custom."+key, "custom:"+key);
                    editGuiItems.add(fillerItem);
                }
            }
        }

        // Flags edit gui
        ConfigurationSection editFlagsSections = configuration.getConfigurationSection("guis.edit-flags");
        if(editFlagsSections != null){
            this.editFlagsGuiTitle = editFlagsSections.getString("title");
            this.editFlagsGuiSize = editFlagsSections.getInt("rows");
            // fillers item
            ConfigurationSection fillers = editFlagsSections.getConfigurationSection("custom");
            ConfigurationSection flags = editFlagsSections.getConfigurationSection("flags");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.edit-flags.custom."+key, "custom:"+key);
                    editFlagsGuiItems.add(fillerItem);
                }
            }
            if(flags != null){
                for( String key : flags.getKeys(false)){
                    String flag = key.replaceAll("flag-", "");
                    Utils.InventoryItem flagItem = plugin.getUtils().createItem(configuration, "guis.edit-flags.flags."+key, "flags:"+flag);
                    editFlagsGuiItems.add(flagItem);
                }
            }
        }

        // Edit Owners gui
        ConfigurationSection ownersGui = configuration.getConfigurationSection("guis.edit-owners");
        if(ownersGui != null){
            this.editOwnersTitle = ownersGui.getString("title");
            this.editOwnersSize = ownersGui.getInt("rows");
            // ownerItem
            Utils.InventoryItem ownerItem = plugin.getUtils().createItem(configuration, "guis.edit-owners.owners","owners");
            editOwnersGuiItems.add(ownerItem);
            // addOwnerItem
            Utils.InventoryItem addOwnerItem = plugin.getUtils().createItem(configuration, "guis.edit-owners.add-owner","add-owner");
            editOwnersGuiItems.add(addOwnerItem);
            // fillers item
            ConfigurationSection fillers = ownersGui.getConfigurationSection("custom");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.edit-owners.custom."+key, "custom:"+key);
                    editOwnersGuiItems.add(fillerItem);
                }
            }
        }

        // Edit Members gui
        ConfigurationSection membersGui = configuration.getConfigurationSection("guis.edit-members");
        if(membersGui != null){
            this.editMembersTitle = membersGui.getString("title");
            this.editMembersSize = membersGui.getInt("rows");
            // memberItem
            Utils.InventoryItem memberItem = plugin.getUtils().createItem(configuration, "guis.edit-members.members","members");
            editMembersGuiItems.add(memberItem);
            // addMemberItem
            Utils.InventoryItem addMemberItem = plugin.getUtils().createItem(configuration, "guis.edit-members.add-member","add-member");
            editMembersGuiItems.add(addMemberItem);
            // fillers item
            ConfigurationSection fillers = membersGui.getConfigurationSection("custom");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.edit-members.custom."+key, "custom:"+key);
                    editMembersGuiItems.add(fillerItem);
                }
            }
        }

        // Edit Bans gui
        ConfigurationSection bansGui = configuration.getConfigurationSection("guis.ban");
        if(bansGui != null){
            this.editBansTitle = bansGui.getString("title");
            this.editBansSize = bansGui.getInt("rows");
            // banItem
            Utils.InventoryItem banItem = plugin.getUtils().createItem(configuration, "guis.ban.banned-players","banned-players");
            editBansGuiItems.add(banItem);
            // addBanItem
            Utils.InventoryItem addBanItem = plugin.getUtils().createItem(configuration, "guis.ban.add-ban","add-ban");
            editBansGuiItems.add(addBanItem);
            // fillers item
            ConfigurationSection fillers = bansGui.getConfigurationSection("custom");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.ban.custom."+key, "custom:"+key);
                    editBansGuiItems.add(fillerItem);
                }
            }
        }

        // Edit Player gui
        ConfigurationSection playerGui = configuration.getConfigurationSection("guis.edit-player");
        if(bansGui != null){
            this.editPlayerTitle = playerGui.getString("title");
            this.editPlayerSize = playerGui.getInt("rows");
            // banItem
            Utils.InventoryItem banItem = plugin.getUtils().createItem(configuration, "guis.edit-player.ban","ban");
            editPlayerGuiItems.add(banItem);
            // kickItem
            Utils.InventoryItem kickItem = plugin.getUtils().createItem(configuration, "guis.edit-player.kick","kick");
            editPlayerGuiItems.add(kickItem);
            // removeItem
            Utils.InventoryItem removeItem = plugin.getUtils().createItem(configuration, "guis.edit-player.remove","remove");
            editPlayerGuiItems.add(removeItem);
            // ownerItem
            Utils.InventoryItem ownerItem = plugin.getUtils().createItem(configuration, "guis.edit-player.owner","owner");
            editPlayerGuiItems.add(ownerItem);
            // memberItem
            Utils.InventoryItem memberItem = plugin.getUtils().createItem(configuration, "guis.edit-player.member","member");
            editPlayerGuiItems.add(memberItem);
            // fillers item
            ConfigurationSection fillers = playerGui.getConfigurationSection("custom");
            if(fillers != null){
                for( String key : fillers.getKeys(false)){
                    Utils.InventoryItem fillerItem = plugin.getUtils().createItem(configuration, "guis.edit-player.custom."+key, "custom:"+key);
                    editPlayerGuiItems.add(fillerItem);
                }
            }
        }
    }

    public boolean reloadConfig(){
        if(!configModel.reloadConfig()){
            return false;
        }
        this.homesGuiItems.clear();
        this.mainGuiItems.clear();
        this.editGuiItems.clear();
        this.editFlagsGuiItems.clear();
        this.editOwnersGuiItems.clear();
        this.editMembersGuiItems.clear();
        loadConfig();
        return true;
    }

    public void clearCache(){
        this.homesGuiItems.clear();
        this.mainGuiItems.clear();
        this.editGuiItems.clear();
        this.editFlagsGuiItems.clear();
        this.editOwnersGuiItems.clear();
        this.editMembersGuiItems.clear();
    }

    public String getHomesGuiTitle() {
        return homesGuiTitle;
    }

    public int getHomesGuiSize() {
        return homesGuiSize;
    }

    public List<Utils.InventoryItem> getHomesGuiItems() {
        return homesGuiItems;
    }

    public String getMainGuiTitle() {
        return mainGuiTitle;
    }

    public int getMainGuiSize() {
        return mainGuiSize;
    }

    public List<Utils.InventoryItem> getMainGuiItems() {
        return mainGuiItems;
    }

    public String getEditGuiTitle() {
        return editGuiTitle;
    }

    public int getEditGuiSize() {
        return editGuiSize;
    }

    public List<Utils.InventoryItem> getEditGuiItems() {
        return editGuiItems;
    }

    public String getEditFlagsGuiTitle() {
        return editFlagsGuiTitle;
    }

    public int getEditFlagsGuiSize() {
        return editFlagsGuiSize;
    }

    public List<Utils.InventoryItem> getEditFlagsGuiItems() {
        return editFlagsGuiItems;
    }

    public String getEditOwnersTitle() {
        return editOwnersTitle;
    }

    public int getEditOwnersSize() {
        return editOwnersSize;
    }

    public List<Utils.InventoryItem> getEditOwnersGuiItems() {
        return editOwnersGuiItems;
    }

    public String getEditMembersTitle() {
        return editMembersTitle;
    }

    public int getEditMembersSize() {
        return editMembersSize;
    }

    public List<Utils.InventoryItem> getEditMembersGuiItems() {
        return editMembersGuiItems;
    }

    public boolean isBanModuleEnabled() {
        return banModuleEnabled;
    }

    public String getEditBansTitle() {
        return editBansTitle;
    }

    public int getEditBansSize() {
        return editBansSize;
    }

    public List<Utils.InventoryItem> getEditBansGuiItems() {
        return editBansGuiItems;
    }

    public String getEditPlayerTitle() {
        return editPlayerTitle;
    }

    public int getEditPlayerSize() {
        return editPlayerSize;
    }

    public List<Utils.InventoryItem> getEditPlayerGuiItems() {
        return editPlayerGuiItems;
    }

    public boolean isKickModuleEnabled() {
        return kickModuleEnabled;
    }

    public boolean isOpenOnClickBlock() {
        return openOnClickBlock;
    }

    public boolean isOpenPSCommands() {
        return openPSCommands;
    }

    public String getCommandName() {
        return CommandName;
    }

    public String getCommandDescription() {
        return CommandDescription;
    }

    public String getCommandPermission() {
        return CommandPermission;
    }

    public List<String> getCommandAliases() {
        return CommandAliases;
    }

    public ConfigModel getConfiguration(){
        return configModel;
    }

    public boolean isUsePermissions() {
        return usePermissions;
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }
}
