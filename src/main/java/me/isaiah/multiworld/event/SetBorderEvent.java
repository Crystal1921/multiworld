package me.isaiah.multiworld.event;

import me.isaiah.multiworld.command.BorderCommand;
import me.isaiah.multiworld.network.WorldBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class SetBorderEvent {
    @SubscribeEvent
    public static void  onPlayerTeleport(PlayerEvent.PlayerChangedDimensionEvent event) {
        ResourceLocation location = event.getTo().location();
        Player entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            Integer i = BorderCommand.BORDERS.get(location);
            PacketDistributor.sendToPlayer(serverPlayer, new WorldBorderPacket(i));
        }
    }

    @SubscribeEvent
    public static void  onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ResourceLocation location = event.getEntity().level().dimension().location();
        Player entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            Integer i = BorderCommand.BORDERS.get(location);
            PacketDistributor.sendToPlayer(serverPlayer, new WorldBorderPacket(i));
        }
    }
}
