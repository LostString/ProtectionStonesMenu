package dev.loststr1ng.protectionStonesMenu.models;


import dev.espi.protectionstones.PSRegion;
import dev.loststr1ng.protectionStonesMenu.enums.PromptType;
import org.bukkit.entity.Player;

public class PromptModel {

    private final Player player;

    private final PSRegion region;

    private final PromptType type;

    private String args;

    public PromptModel(Player player, PSRegion region, PromptType type) {
        this.player = player;
        this.region = region;
        this.type = type;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getArgs() {
        return args;
    }

    public Player getPlayer() {
        return player;
    }

    public PSRegion getRegion() {
        return region;
    }

    public PromptType getType() {
        return type;
    }
}
