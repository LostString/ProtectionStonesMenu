package dev.loststr1ng.protectionStonesMenu.utils;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.loststr1ng.protectionStonesMenu.ProtectionStonesMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PSUtils {


    public static List<String> getBannedPlayers(PSRegion region){
        ProtectedRegion protectedRegion = region.getWGRegion();
        String value = protectedRegion.getFlag(ProtectionStonesMenu.bannedPlayers);
        if(value == null) return new ArrayList<>();
        if(value.contains("None")) return new ArrayList<>();
        return List.of(value.split(";"));
    }


    public static boolean banPlayer(PSRegion region, String uuid){
        ProtectedRegion protectedRegion = region.getWGRegion();
        String value = protectedRegion.getFlag(ProtectionStonesMenu.bannedPlayers);
        if(value == null) value = "";
        value = String.join(value, uuid+";");
        if(isBanned(region, uuid)) return false;
        protectedRegion.setFlag(ProtectionStonesMenu.bannedPlayers, value);
        region.removeOwner(UUID.fromString(uuid));
        region.removeMember(UUID.fromString(uuid));
        return true;
    }

    public static boolean unBanPlayer(PSRegion region, String UUID){
        ProtectedRegion protectedRegion = region.getWGRegion();
        String value = protectedRegion.getFlag(ProtectionStonesMenu.bannedPlayers);
        if(value == null) return false;
        if(value.contains(UUID)){
            value = value.replaceAll(UUID+";", "");
            if(value.isEmpty()){
                protectedRegion.setFlag(ProtectionStonesMenu.bannedPlayers, null);
                return true;
            }
            protectedRegion.setFlag(ProtectionStonesMenu.bannedPlayers, value);
            return true;
        }
        return false;
    }

    public static boolean isBanned(PSRegion region, String UUID){
        ProtectedRegion protectedRegion = region.getWGRegion();
        String value = protectedRegion.getFlag(ProtectionStonesMenu.bannedPlayers);
        if(value == null) return false;
        return value.contains(UUID);
    }

    public static boolean canEdit(PSRegion region, Player player){
        return region.isOwner(player.getUniqueId()) || player.hasPermission("psmenu.admin");
    }

    public static void kickPlayer(PSRegion region, Player player){
        ProtectedRegion protectedRegion = region.getWGRegion();
        Location location = player.getLocation();
        BlockVector3 min = protectedRegion.getMinimumPoint();
        BlockVector3 max = protectedRegion.getMaximumPoint();
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();


        // Calculate distance
        double distMinX = Math.abs(x - min.getX());
        double distMaxX = Math.abs(max.getX() + 1 - x);
        double distMinZ = Math.abs(z - min.getZ());
        double distMaxZ = Math.abs(max.getZ() + 1 - z);

        // Find the border
        double minDist = Math.min(Math.min(distMinX, distMaxX), Math.min(distMinZ, distMaxZ));
        if (minDist == distMinX) {
            x = min.getX() - 1;
        } else if (minDist == distMaxX) {
            x = max.getX() + 1;
        } else if (minDist == distMinZ) {
            z = min.getZ() - 1;
        } else {
            z = max.getZ() + 1;
        }

        Location safe = findSafeLocation(region.getWorld(), x,y,z);
        if(safe != null){
            safe.setYaw(location.getYaw());
            safe.setPitch(location.getPitch());
            player.teleport(safe);
        }else {
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }

    private static Location findSafeLocation(World world, double x, double y, double z) {
        int maxHeight = world.getMaxHeight();
        int minHeight = world.getMinHeight();

        // Find from "Y" to top
        for (int checkY = (int) y; checkY < maxHeight - 2; checkY++) {
            Block block = world.getBlockAt((int) x, checkY, (int) z);
            Block above = block.getRelative(0, 1, 0);
            Block above2 = block.getRelative(0, 2, 0);

            if (block.getType().isSolid() &&
                    above.getType() == Material.AIR &&
                    above2.getType() == Material.AIR) {
                return new Location(world, x + 0.5, checkY + 1, z + 0.5);
            }
        }

        // Find down "Y" player
        for (int checkY = (int) y; checkY > minHeight; checkY--) {
            Block block = world.getBlockAt((int) x, checkY, (int) z);
            Block above = block.getRelative(0, 1, 0);
            Block above2 = block.getRelative(0, 2, 0);

            if (block.getType().isSolid() &&
                    above.getType() == Material.AIR &&
                    above2.getType() == Material.AIR) {
                return new Location(world, x + 0.5, checkY + 1, z + 0.5);
            }
        }

        return null; // No safe location found
    }
}
