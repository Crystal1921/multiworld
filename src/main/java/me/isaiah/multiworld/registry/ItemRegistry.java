package me.isaiah.multiworld.registry;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MultiworldMod.MOD_ID);

    public static final DeferredItem<Item> PortalFinder = ITEMS.register("portal_finder", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PortalBlock = ITEMS.register("portal_block", () -> new BlockItem(BlockRegistry.PORTAL_BLOCK.get(), new Item.Properties()));
}
