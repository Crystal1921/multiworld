package me.isaiah.multiworld.network;

import me.isaiah.multiworld.MultiworldMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


@EventBusSubscriber(modid = MultiworldMod.MOD_ID)
public class MultiworldNetwork {
    public static final String VERSION = "1.0";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(WorldBorderPacket.TYPE, WorldBorderPacket.STREAM_CODEC, WorldBorderPacket::handleOnClient);
    }
}
