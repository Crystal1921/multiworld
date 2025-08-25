package me.isaiah.multiworld.registry;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.block.PortalBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(MultiworldMod.MOD_ID);
    public static final Supplier<Block> PORTAL_BLOCK = BLOCKS.register("portal_block", PortalBlock::new);
}
