package dev.loststr1ng.protectionStonesMenu;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.espi.protectionstones.PSCommand;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.loststr1ng.protectionStonesMenu.commands.MainCommand;
import dev.loststr1ng.protectionStonesMenu.commands.protectionstones.argBanCommand;
import dev.loststr1ng.protectionStonesMenu.commands.protectionstones.argBanListCommand;
import dev.loststr1ng.protectionStonesMenu.commands.protectionstones.argKickCommand;
import dev.loststr1ng.protectionStonesMenu.commands.protectionstones.argUnBanCommand;
import dev.loststr1ng.protectionStonesMenu.config.MainConfig;
import dev.loststr1ng.protectionStonesMenu.config.MessageConfig;
import dev.loststr1ng.protectionStonesMenu.events.PSPrompts;
import dev.loststr1ng.protectionStonesMenu.events.PsItemClick;
import dev.loststr1ng.protectionStonesMenu.events.PSJoin;
import dev.loststr1ng.protectionStonesMenu.managers.InventoryManager;
import dev.loststr1ng.protectionStonesMenu.utils.MessageUtils;
import dev.loststr1ng.protectionStonesMenu.utils.Metrics;
import dev.loststr1ng.protectionStonesMenu.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;


public final class ProtectionStonesMenu extends JavaPlugin {

    private MainConfig mainConfig;
    private MessageConfig messageConfig;
    public RegionContainer container;
    private Utils utils;
    private InventoryManager inventoryManager;
    public final Map<UUID, PSRegion> renamePrompts = new HashMap<>();
    public final Map<UUID, PSRegion> ownerPrompts = new HashMap<>();
    public final Map<UUID, PSRegion> memberPrompts = new HashMap<>();
    public final Map<UUID, PSRegion> banPrompts = new HashMap<>();
    public static StringFlag bannedPlayers;
    private ProtectionStones protectionStones;

    public String prefix = "&8[&aProtectionStones &8| &bMenu &8] &9";

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getPluginManager();
        this.messageConfig = new MessageConfig(this);
        this.utils = new Utils(this);
        this.mainConfig = new MainConfig(this);
        this.inventoryManager = new InventoryManager(this);

        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        Plugin ps = Bukkit.getPluginManager().getPlugin("ProtectionStones");

        if (wg == null || ps == null) {
            utils.log("WorldGuard or ProtectionStones wasn't found. Disabling plugin.");
            pm.disablePlugin(this);
            return;
        }
        protectionStones = ProtectionStones.getInstance();

        if (!WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(Entry.factory, null)) {
            utils.log("[ProtectionStonesMenu] Could not register the entry handler !");
            utils.log("[ProtectionStonesMenu] Please report this error. The plugin will now be disabled.");

            pm.disablePlugin(this);
            return;
        }

        registerEvents();
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        if(mainConfig.isBanModuleEnabled()){
            utils.log("&9Ban Module enabled, registering commands");
            protectionStones.addCommandArgument(new argBanCommand(this));
            protectionStones.addCommandArgument(new argUnBanCommand(this));
            protectionStones.addCommandArgument(new argBanListCommand(this));
            if(mainConfig.isKickModuleEnabled()){
                protectionStones.addCommandArgument(new argKickCommand(this));
            }
        }

        // register main command
        if(registerCommand()){
            utils.log("&aMain Command registered");
        }
        utils.log("&bPlugin loaded successfully");

        int pluginId = 28431;
        Metrics metrics = new Metrics(this, pluginId);

        String message = getUpdateMessage();
        if(message != null){
            Bukkit.getConsoleSender().sendMessage(MessageUtils.getLegacy(message));
        }
    }

    @Override
    public void onDisable() {
        mainConfig.clearCache();
        renamePrompts.clear();
        container = null;
    }

    public boolean registerCommand(){
        MainCommand command = new MainCommand(mainConfig.getCommandName(), this);
        CommandMap commandMap = null;
        try{
            Field f = getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(getServer().getPluginManager());

        }catch (Exception err){
            return false;
        }finally {
            if(commandMap != null){
                commandMap.register(command.getName(), command);
            }
        }
        return true;
    }

    @Override
    public void onLoad(){
        Logger log = Bukkit.getLogger();
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StringFlag stringFlag = new StringFlag("ps-player-banned");
            flagRegistry.register(stringFlag);
            bannedPlayers = stringFlag;
            log.info("[ProtectionStonesMenu] Custom flag (ps-player-banned) registered.");
        }catch (FlagConflictException exception){
            log.severe("[ProtectionStonesMenu] &cAn error occurred while updating the flag. Please try again later");
        }

    }

    public void registerEvents(){
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PSJoin(this), this);
        pm.registerEvents(new PsItemClick(this), this);
        pm.registerEvents(new PSPrompts(this), this);
    }

    public Utils getUtils(){ return utils; }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public MainConfig getMainConfig(){ return mainConfig; }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    @Nullable
    public String getUpdateMessage(){
        String version = getDescription().getVersion();
        String latestVersion;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=130907").openConnection();
            int timed_out = 1250;
            con.setConnectTimeout(timed_out);
            con.setReadTimeout(timed_out);
            latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (latestVersion.length() <= 10) {
                if(!version.equals(latestVersion)){
                    return "&8[&aProtectionStonesMenu&8] &cThere is a new version available. &e(&7"+latestVersion+"&e)\n&cYou can download it at: &fhttps://www.spigotmc.org/resources/130907/";
                }
            }
            return null;
        } catch (Exception ex) {
            return "&8[&aProtectionStonesMenu&8] &cError while checking update.";
        }
    }
}
