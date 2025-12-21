package dev.loststr1ng.protectionStonesMenu.listeners;

import dev.espi.protectionstones.PSRegion;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PSItemClickEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled = false;
    private final Player player;
    private final Gui gui;
    private final String action;
    private final String args;
    private final PSRegion region;

    /**
     * This event is called when a PSitem is Clicked
     * from a custom gui
     * @param player player who clicked
     * @param gui gui clicked
     */
    public PSItemClickEvent(Player player, Gui gui, String action, String args){
        this.player = player;
        this.gui = gui;
        this.action = action;
        this.args = args;
        this.region = null;
    }

    public PSItemClickEvent(Player player, Gui gui, String action, String args, PSRegion region){
        this.player = player;
        this.gui = gui;
        this.action = action;
        this.args = args;
        this.region = region;
    }

    public Player getPlayer() {
        return player;
    }

    public Gui getGui() {
        return gui;
    }

    public String getAction() {
        return action;
    }

    public String getArgs() {
        return args;
    }

    public PSRegion getRegion() {
        return region;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
