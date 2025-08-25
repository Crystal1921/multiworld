package me.isaiah.multiworld.registry;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.argument.SpaceBreakStringArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCommandArgumentRegistry {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MultiworldMod.MOD_ID);

    static {
        REGISTRY.register("space_break_string", () -> ArgumentTypeInfos.registerByClass(SpaceBreakStringArgumentType.class, new SpaceBreakStringArgumentType.Serializer()));
    }
}
