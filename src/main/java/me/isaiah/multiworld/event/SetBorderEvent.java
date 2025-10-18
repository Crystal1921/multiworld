package me.isaiah.multiworld.event;

import me.isaiah.multiworld.command.commands.BorderCommand;
import me.isaiah.multiworld.network.WorldBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static me.isaiah.multiworld.command.commands.BorderCommand.save;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class SetBorderEvent {
    @SubscribeEvent
    public static void onPlayerTeleport(PlayerEvent.PlayerChangedDimensionEvent event) {
        ResourceLocation location = event.getTo().location();
        Player entity = event.getEntity();
        Level level = entity.level();
        setBorder(entity, location, level);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Level level = event.getEntity().level();
        ResourceLocation location = level.dimension().location();
        Player entity = event.getEntity();
        setBorder(entity, location, level);
    }

    private static void setBorder(Player entity, ResourceLocation location, Level level) {
        if (entity instanceof ServerPlayer serverPlayer) {
            BorderCommand.BorderData borderData = BorderCommand.BORDERS.get(location);
            if (borderData == null) {
                WorldBorder worldBorder = level.getWorldBorder();
                borderData = new BorderCommand.BorderData((int) worldBorder.getSize(), (int) worldBorder.getCenterX(), (int) worldBorder.getCenterZ());
                BorderCommand.BORDERS.put(location, borderData);
                // 保存到文件
                try {
                    save("config/multiworld/borders.yml");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            PacketDistributor.sendToPlayer(serverPlayer, new WorldBorderPacket(borderData.size(), borderData.x(), borderData.y()));
        }
    }

}
