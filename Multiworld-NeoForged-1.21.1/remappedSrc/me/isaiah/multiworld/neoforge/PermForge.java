package me.isaiah.multiworld.neoforge;

import me.isaiah.multiworld.perm.Perm;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public class PermForge extends Perm {

    public static void init() {
        Perm.setPerm(new PermForge());
    }

    @Override
    public boolean has_impl(ServerPlayer plr, String perm) {
        boolean cyber = ModList.get().getModContainerById("cyberpermissions").isPresent();
        
        boolean res = plr.hasPermissions(2);

        if (cyber) {
            if (CyberHandler.hasPermission(plr, perm)) res = true;
        }

        return res;
    }

}