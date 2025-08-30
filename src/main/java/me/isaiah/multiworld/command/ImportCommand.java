package me.isaiah.multiworld.command;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.chunk.ChunkGenerator;

import static me.isaiah.multiworld.NeoForgeWorldCreator.getStaticVoidGen;
import static me.isaiah.multiworld.command.CreateCommand.get_dim_id;
import static me.isaiah.multiworld.command.CreateCommand.make_config;

public class ImportCommand {
    public static int run(MinecraftServer mc, CommandSourceStack source, ResourceLocation worldName) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        String processedWorldId = worldName.toString();
        String environment = "NORMAL";
        String customGen = "VOID";
        ResourceLocation dim = get_dim_id(environment);
        ChunkGenerator gen = getStaticVoidGen(mc);
        long seed = 0;

        ServerLevel world = MultiworldMod.create_world(processedWorldId, dim, gen, Difficulty.PEACEFUL, seed);
        make_config(world, environment, seed, customGen);

        initGamerule(mc, player, world);

        mc.sendSystemMessage(Component.literal("Imported world " + processedWorldId));

        return 1;
    }

    private static void initGamerule(MinecraftServer mc, ServerPlayer player, ServerLevel world) {
        GameruleCommand.setGamerule(mc, player, "doMobSpawning", "false", world, false);
        GameruleCommand.setGamerule(mc, player, "keepInventory", "true", world, false);
        GameruleCommand.setGamerule(mc, player, "naturalRegeneration", "false", world, false);
    }
}