package me.isaiah.multiworld.event;

import me.isaiah.multiworld.command.commands.BorderCommand;
import me.isaiah.multiworld.network.WorldBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class SetBorderEvent {
    @SubscribeEvent
    public static void  onPlayerTeleport(PlayerEvent.PlayerChangedDimensionEvent event) {
        ResourceLocation location = event.getTo().location();
        Player entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            BorderCommand.BorderData borderData = BorderCommand.BORDERS.get(location);
            PacketDistributor.sendToPlayer(serverPlayer, new WorldBorderPacket(borderData.size(), borderData.x(), borderData.y()));
        }
    }

    @SubscribeEvent
    public static void  onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ResourceLocation location = event.getEntity().level().dimension().location();
        Player entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            BorderCommand.BorderData borderData = BorderCommand.BORDERS.get(location);
            PacketDistributor.sendToPlayer(serverPlayer, new WorldBorderPacket(borderData.size(), borderData.x(), borderData.y()));
        }
    }
}
