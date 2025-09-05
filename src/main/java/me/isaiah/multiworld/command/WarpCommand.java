package me.isaiah.multiworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.core.registries.Registries.DIMENSION;

public class WarpCommand {
    public static HashMap<String, WarpData> WARPS = new HashMap<>();

    // On command register
    public static void register_commands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("warp")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests(new MultiworldCommand.WarpSuggestionProvider())
                        .executes(context -> {
                            String destination = StringArgumentType.getString(context, "name");
                            return warpPoint(context, destination);
                        })));

        dispatcher.register(literal("setWarp")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player != null) {
                                BlockPos pos = player.blockPosition();
                                String name = StringArgumentType.getString(context, "name");
                                if (name.equals("spawn")) {
                                    context.getSource().sendFailure(Component.literal("Warp name 'spawn' is reserved."));
                                    return 0;
                                }
                                return setWarpPoint(context, name, pos);
                            }
                            context.getSource().sendFailure(Component.literal("Invalid Operation"));
                            return 0;
                        })
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {
                                    var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                                    String name = StringArgumentType.getString(context, "name");
                                    if (name.equals("spawn")) {
                                        context.getSource().sendFailure(Component.literal("Warp name 'spawn' is reserved."));
                                        return 0;
                                    }
                                    return setWarpPoint(context, name, pos);
                                }))));

        dispatcher.register(literal("removeWarp")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests(new MultiworldCommand.WarpSuggestionProvider())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (WARPS.containsKey(name)) {
                                WARPS.remove(name);
                                // 保存到文件
                                try {
                                    save("config\\multiworld\\warps.yml");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                context.getSource().sendSuccess(() ->Component.literal("Warp point '" + name + "' has been removed."), true);
                                return 1;
                            } else {
                                context.getSource().sendFailure(Component.literal("Warp point '" + name + "' does not exist."));
                                return 0;
                            }
                        })));

        dispatcher.register(literal("setSpawn")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player != null) {
                        BlockPos pos = player.blockPosition();
                        String name = "spawn";
                        return setWarpPoint(context, name, pos);
                    }
                    context.getSource().sendFailure(Component.literal("Invalid Operation"));
                    return 0;
                })
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> {
                            var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                            String name = "spawn";
                            return setWarpPoint(context, name, pos);
                        })));

        dispatcher.register(literal("spawn")
                .executes(context -> warpPoint(context, "spawn")));
    }

    private static int warpPoint(CommandContext<CommandSourceStack> context, String destination) {
        ServerPlayer player = context.getSource().getPlayer();
        WarpData warpData = WARPS.get(destination);
        if (player != null && warpData != null) {
            ResourceLocation levelID = ResourceLocation.parse(warpData.worldId);
            if (player.level().dimension().location().equals(levelID)) {
                player.teleportTo(warpData.x, warpData.y, warpData.z);
                player.sendSystemMessage(Component.literal("Teleported to position " + warpData.x + ", " + warpData.y + ", " + warpData.z + " in current dimension."));
                return 1;
            } else {
                ServerLevel level = player.server.getLevel(ResourceKey.create(DIMENSION, levelID));
                if (level != null) {
                    DimensionTransition target = new DimensionTransition(level, new Vec3(warpData.x, warpData.y, warpData.z), new Vec3(0, 0, 0), 0f, 0f, DimensionTransition.DO_NOTHING);
                    player.changeDimension(target);
                    player.sendSystemMessage(Component.literal("Teleported to dimension " + warpData.worldId + " at position " + warpData.x + ", " + warpData.y + ", " + warpData.z + "."));
                    return 1;
                }
            }
        }
        context.getSource().sendFailure(Component.literal("Invalid destination"));
        return 0;
    }

    private static int setWarpPoint(CommandContext<CommandSourceStack> context, String name, BlockPos pos) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ResourceLocation location = player.level().dimension().location();
            WARPS.put(name, new WarpData(location.toString(), name, pos.getX(), pos.getY(), pos.getZ()));
            player.sendSystemMessage(Component.literal("Set warp point '" + name + "' at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " in dimension " + location.toString()));
            // 保存到文件
            try {
                save("config\\multiworld\\warps.yml");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return 1;
        }
        return 0;
    }

    // 保存
    public static void save(String path) throws Exception {
        Map<String, Map<String, Object>> saveMap = new HashMap<>();
        for (Map.Entry<String, WarpData> e : WARPS.entrySet()) {
            WarpData wd = e.getValue();
            Map<String, Object> data = new HashMap<>();
            data.put("worldId", wd.worldId());
            data.put("name", wd.name());
            data.put("x", wd.x());
            data.put("y", wd.y());
            data.put("z", wd.z());
            saveMap.put(e.getKey().toString(), data);
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(path)) {
            yaml.dump(Map.of("warps", saveMap), writer);
        }
    }

    // 读取
    public static void load(String path) throws Exception {
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(path)) {
            Map<String, Object> data = yaml.load(reader);
            if (data != null && data.containsKey("warps")) {
                Map<String, Map<String, Object>> loaded = (Map<String, Map<String, Object>>) data.get("warps");
                WARPS.clear();
                for (Map.Entry<String, Map<String, Object>> e : loaded.entrySet()) {
                    Map<String, Object> map = e.getValue();
                    String worldId = (String) map.get("worldId");
                    String name = (String) map.get("name");
                    double x = ((Number) map.get("x")).doubleValue();
                    double y = ((Number) map.get("y")).doubleValue();
                    double z = ((Number) map.get("z")).doubleValue();
                    WARPS.put(e.getKey(), new WarpData(worldId, name, x, y, z));
                }
            }
        }
    }

    public static void initWarp(MinecraftServer mc) {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File wc = new File(configDir, "warps.yml");

        try {
            if (!wc.exists()) {
                wc.createNewFile();
            } else {
                load("config\\multiworld\\warps.yml");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record WarpData(String worldId, String name, double x, double y, double z) {
    }
}
