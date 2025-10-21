package me.isaiah.multiworld.registry;

import com.mojang.serialization.Codec;
import me.isaiah.multiworld.MultiworldMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class DataAttachmentsRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MultiworldMod.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> PORTAL_DEBUG = ATTACHMENT_TYPES.register(
            "portal_debug", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );
}
