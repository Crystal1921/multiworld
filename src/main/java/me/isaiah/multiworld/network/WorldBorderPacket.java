package me.isaiah.multiworld.network;

import io.netty.buffer.ByteBuf;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record WorldBorderPacket(double size, double x, double y) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorldBorderPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MultiworldMod.MOD_ID, "world_border"));
    public static final StreamCodec<ByteBuf, WorldBorderPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            WorldBorderPacket::size,
            ByteBufCodecs.DOUBLE,
            WorldBorderPacket::x,
            ByteBufCodecs.DOUBLE,
            WorldBorderPacket::y,
            WorldBorderPacket::new
    );

    public static void handleOnClient(final WorldBorderPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.getWorldBorder().setSize(payload.size);
                Minecraft.getInstance().level.getWorldBorder().setCenter(payload.x(), payload.y());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
