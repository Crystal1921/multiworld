package cyber.permissions.v1;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CyberPermissions {

    public static Permissible getPermissible(Object obj) {
        if (!(obj instanceof Permissible)) {
            if (obj instanceof CommandSourceStack cs) {
                if (null == cs.getEntity()) {
                    return (Permissible) cs.getServer();
                } else {
                    return cs.getEntity() instanceof Permissible ? (Permissible) cs.getEntity() : null;
                }
            }
            return null;
        }
        return (Permissible) obj;
    }

    public static Permissible getServerPermissible(MinecraftServer server) {
        return (Permissible) server;
    }

    public static Permissible getPlayerPermissible(ServerPlayer plr) {
        return (Permissible) plr;
    }

}