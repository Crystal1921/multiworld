package me.isaiah.multiworld.registry;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GroupRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MultiworldMod.MOD_ID);

    @SuppressWarnings("all")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MCGPROJECT_SPEC_MAIN = TABS.register("blocks", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .title(Component.translatable("itemGroup.mcgproject_spec"))
            .icon(() -> new ItemStack(ItemRegistry.PortalFinder.get()))
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.PortalFinder.get());
                output.accept(ItemRegistry.PortalBlock.get());
            }).build());
}
