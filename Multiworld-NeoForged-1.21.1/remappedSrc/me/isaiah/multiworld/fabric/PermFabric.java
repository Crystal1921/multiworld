package me.isaiah.multiworld.fabric;

import me.isaiah.multiworld.perm.Perm;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class PermFabric extends Perm {
    
    public static void init() {
        Perm.setPerm(new PermFabric());
        
        // TODO: move this
        ICommonHooks.register();
        FabricEvents.register();
    }

    @Override
    public boolean has_impl(ServerPlayer plr, String perm) {
        boolean cyber = FabricLoader.getInstance().getModContainer("cyber-permissions").isPresent();
        boolean luck =  FabricLoader.getInstance().getModContainer("fabric-permissions-api-v0").isPresent();
        
        boolean res = plr.hasPermissions(2);

        if (cyber) {
            if (CyberHandler.hasPermission(plr, perm)) res = true;
        }

        if (luck) {
            if (LuckHandler.hasPermission(plr, perm)) res = true;
        }

        return res;
    }

}