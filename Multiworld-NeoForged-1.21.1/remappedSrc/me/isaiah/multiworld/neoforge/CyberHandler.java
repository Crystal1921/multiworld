package me.isaiah.multiworld.neoforge;

import cyber.permissions.v1.CyberPermissions;
import cyber.permissions.v1.Permission;
import cyber.permissions.v1.PermissionDefaults;
import net.minecraft.server.level.ServerPlayer;

public class CyberHandler {
    
    public static boolean hasPermission(ServerPlayer plr, String perm) {
        Permission p = new Permission(perm, "A permission for Multiworld", PermissionDefaults.OPERATOR);
        return plr.hasPermissions(2) || CyberPermissions.getPlayerPermissible(plr).hasPermission(p);
    }

}