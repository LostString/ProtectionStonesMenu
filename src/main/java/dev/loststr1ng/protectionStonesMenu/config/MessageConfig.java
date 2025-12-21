package dev.loststr1ng.protectionStonesMenu.config;

import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import dev.loststr1ng.protectionStonesMenu.models.ConfigModel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageConfig {

    protected final ProtectionStonesMenu plugin;
    private final ConfigModel configModel;

    // General Messages
    private String prefix;
    private String reload;
    private String noPermissions;
    private String playerOnly;
    private String playerNotFound;
    private String noRegionFound;
    private String kickSuccess;
    private String kickSelf;
    private String kickMessage;

    // Edit Messages
    private String editRenameSuccess;
    private String editRenameCancel;
    private String editRename;
    private String editFlagUpdated;
    private String editFlagAllow;
    private String editFlagDeny;
    private String editFlagNone;
    private String editFlagGroupAll;
    private String editFlagGroupMembers;
    private String editFlagGroupOwners;
    private String editFlagGroupNonMembers;
    private String editFlagGroupNonOwners;
    private String editOwnerAddSuccess;
    private String editOwnerRemoveSuccess;
    private String editOwnerAlready;
    private String editOwnerNot;
    private String editOwnerPrompt;
    private String editMemberAddSuccess;
    private String editMemberRemoveSuccess;
    private String editMemberAlready;
    private String editMemberNot;
    private String editMemberPrompt;
    private String editVisibilityOn;
    private String editVisibilityOff;


    // Ban Messages
    private String banAddSuccess;
    private String banRemoveSuccess;
    private String banAlready;
    private String banNot;
    private String banSelf;
    private String banPrompt;
    private String banMessage;
    private String banDisabled;
    private String banListHeader;
    private String banListEntry;
    private String banListEmpty;

    // Player edit messages
    private String editPlayerSelf;
    private String editPlayerPromote;
    private String editPlayerDemote;


    public MessageConfig(ProtectionStonesMenu plugin){
        this.plugin = plugin;
        this.configModel = new ConfigModel("messages.yml", plugin, null, false);
        this.configModel.registerConfig();
        loadConfig();
    }

    public void loadConfig(){
        FileConfiguration fileConfiguration = configModel.getConfig();
        // General Messages
        prefix = fileConfiguration.getString("messages.prefix");
        reload = fileConfiguration.getString("messages.reload");
        noPermissions = fileConfiguration.getString("messages.no-permission");
        playerOnly = fileConfiguration.getString("messages.player-only");
        playerNotFound = fileConfiguration.getString("messages.player-not-found");
        noRegionFound = fileConfiguration.getString("messages.not-in-region");
        kickSuccess = fileConfiguration.getString("messages.kicked-success");
        kickSelf = fileConfiguration.getString("messages.kick-self");
        kickMessage = fileConfiguration.getString("messages.kick-message");

        // Edit Menu Messages
        ConfigurationSection editSection = fileConfiguration.getConfigurationSection("messages.edit-menu");
        if(editSection != null){
            this.editRenameSuccess = editSection.getString("rename-success");
            this.editRenameCancel = editSection.getString("rename-cancel");
            this.editRename = editSection.getString("rename-prompt");
            this.editFlagUpdated = editSection.getString("flag-updated");
            this.editFlagAllow = editSection.getString("flag-allow");
            this.editFlagDeny = editSection.getString("flag-deny");
            this.editFlagNone = editSection.getString("flag-none");
            this.editFlagGroupAll = editSection.getString("flag-groups.all");
            this.editFlagGroupOwners = editSection.getString("flag-groups.owners");
            this.editFlagGroupMembers = editSection.getString("flag-groups.members");
            this.editFlagGroupNonMembers = editSection.getString("flag-groups.nonmembers");
            this.editFlagGroupNonOwners = editSection.getString("flag-groups.nonowners");
            this.editOwnerAddSuccess = editSection.getString("owner-add-success");
            this.editOwnerRemoveSuccess = editSection.getString("owner-remove-success");
            this.editOwnerAlready = editSection.getString("owner-already");
            this.editOwnerNot = editSection.getString("owner-not");
            this.editOwnerPrompt = editSection.getString("owner-prompt");
            this.editMemberAddSuccess = editSection.getString("member-add-success");
            this.editMemberRemoveSuccess = editSection.getString("member-remove-success");
            this.editMemberAlready = editSection.getString("member-already");
            this.editMemberNot = editSection.getString("member-not");
            this.editMemberPrompt = editSection.getString("member-prompt");
            this.editVisibilityOn = editSection.getString("visibility-on");
            this.editVisibilityOff = editSection.getString("visibility-off");
            this.banAddSuccess = editSection.getString("ban-add-success");
            this.banRemoveSuccess = editSection.getString("ban-remove-success");
            this.banAlready = editSection.getString("ban-already");
            this.banNot = editSection.getString("ban-not");
            this.banPrompt = editSection.getString("ban-prompt");
            this.banMessage = editSection.getString("banned-message");
            this.banDisabled = editSection.getString("ban-module-disabled");
            this.banSelf = editSection.getString("ban-self");
            this.banListHeader = editSection.getString("ban-list-header");
            this.banListEmpty = editSection.getString("ban-list-empty");
            this.banListEntry = editSection.getString("ban-list-entry");
            this.editPlayerSelf = editSection.getString("player-self");
            this.editPlayerDemote = editSection.getString("player-demoted-success");
            this.editPlayerPromote = editSection.getString("player-promoted-success");
        }
    }

    public boolean reloadConfig(){
        if(configModel.reloadConfig()) {
            loadConfig();
            return true;
        }
        return false;
    }

    public String getPrefix() {
        return prefix;
    }
    public String getNoPermissions() {
        return noPermissions;
    }
    public String getPlayerOnly() {
        return playerOnly;
    }
    public String getPlayerNotFound() {
        return playerNotFound;
    }
    public String getNoRegionFound() {
        return noRegionFound;
    }
    public String getKickSuccess() {
        return kickSuccess;
    }
    public String getKickSelf() {
        return kickSelf;
    }
    public String getKickMessage() {
        return kickMessage;
    }

    // Getters (Edit Messages)
    public String getEditRenameSuccess() {
        return editRenameSuccess;
    }
    public String getEditRenameCancel() {
        return editRenameCancel;
    }
    public String getEditRename() {
        return editRename;
    }
    public String getEditFlagUpdated() {
        return editFlagUpdated;
    }
    public String getEditFlagAllow() {
        return editFlagAllow;
    }
    public String getEditFlagDeny() {
        return editFlagDeny;
    }
    public String getEditFlagNone() {
        return editFlagNone;
    }
    public String getEditFlagGroupAll() {
        return editFlagGroupAll;
    }
    public String getEditFlagGroupMembers() {
        return editFlagGroupMembers;
    }
    public String getEditFlagGroupOwners() {
        return editFlagGroupOwners;
    }
    public String getEditFlagGroupNonMembers() {
        return editFlagGroupNonMembers;
    }
    public String getEditFlagGroupNonOwners() {
        return editFlagGroupNonOwners;
    }
    public String getEditOwnerAddSuccess() {
        return editOwnerAddSuccess;
    }
    public String getEditOwnerRemoveSuccess() {
        return editOwnerRemoveSuccess;
    }
    public String getEditOwnerAlready() {
        return editOwnerAlready;
    }
    public String getEditOwnerNot() {
        return editOwnerNot;
    }
    public String getEditOwnerPrompt() {
        return editOwnerPrompt;
    }
    public String getEditMemberAddSuccess() {
        return editMemberAddSuccess;
    }
    public String getEditMemberRemoveSuccess() {
        return editMemberRemoveSuccess;
    }
    public String getEditMemberAlready() {
        return editMemberAlready;
    }
    public String getEditMemberNot() {
        return editMemberNot;
    }
    public String getEditMemberPrompt() {
        return editMemberPrompt;
    }
    public String getEditVisibilityOn() {
        return editVisibilityOn;
    }
    public String getEditVisibilityOff() {
        return editVisibilityOff;
    }
    public String getBanAddSuccess() {
        return banAddSuccess;
    }
    public String getBanRemoveSuccess() {
        return banRemoveSuccess;
    }
    public String getBanAlready() {
        return banAlready;
    }
    public String getBanNot() {
        return banNot;
    }
    public String getBanPrompt() {
        return banPrompt;
    }
    public String getBanMessage() {
        return banMessage;
    }
    public String getBanDisabled() {
        return banDisabled;
    }
    public String getBanListHeader() {
        return banListHeader;
    }
    public String getBanListEntry() {
        return banListEntry;
    }
    public String getBanListEmpty() {
        return banListEmpty;
    }
    public String getEditPlayerSelf() {
        return editPlayerSelf;
    }
    public String getEditPlayerPromote() {
        return editPlayerPromote;
    }
    public String getEditPlayerDemote() {
        return editPlayerDemote;
    }
    public String getBanSelf() {
        return banSelf;
    }
    public String getReload() {
        return reload;
    }
}
