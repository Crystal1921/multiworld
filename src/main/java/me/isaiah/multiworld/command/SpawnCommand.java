package me.isaiah.multiworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static me.isaiah.multiworld.command.WarpCommand.warpTo;
import static net.minecraft.commands.Commands.literal;

public class SpawnCommand {
    public static SpawnData SPAWN;

    public static void register_commands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("setspawn")
                .requires(source -> source.hasPermission(4))
                .executes(SpawnCommand::setSpawn));

        dispatcher.register(literal("spawn")
                .requires(source -> source.hasPermission(4))
                .executes(context -> runSpawn(context, context.getSource().getPlayer()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> runSpawn(context, EntityArgument.getPlayer(context, "player")))));
    }

    private static int setSpawn(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Invalid Operation"));
            return 0;
        }
        BlockPos pos = player.blockPosition();
        String location = player.level().dimension().location().toString();
        SPAWN = new SpawnData(location, pos.getX(), pos.getY(), pos.getZ());
        context.getSource().sendSuccess(() -> Component.literal("Set spawn point at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " in dimension " + location), true);
        // 保存到文件
        try {
            saveSpawn("config/multiworld/spawn.yml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static int runSpawn(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Player to be spawned not found"));
            return 0;
        }
        if (SPAWN == null) {
            context.getSource().sendFailure(Component.literal("Spawn point not set"));
            return 0;
        }
        int ret = warpTo(player, new WarpCommand.WarpData(SPAWN.worldId(), "spawn", SPAWN.x(), SPAWN.y(), SPAWN.z()));
        if (ret == 1) {
            return 1;
        }
        context.getSource().sendFailure(Component.literal("Failed to warp to spawn point"));
        return 0;
    }

    public static void saveSpawn(String path) throws Exception {
        if (SPAWN == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("worldId", SPAWN.worldId());
        data.put("x", SPAWN.x());
        data.put("y", SPAWN.y());
        data.put("z", SPAWN.z());

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(path)) {
            yaml.dump(Map.of("spawn", data), writer);
        }
    }

    // 读取spawn
    private static void loadSpawn(String path) throws Exception {
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(path)) {
            Map<String, Object> data = yaml.load(reader);
            if (data != null && data.containsKey("spawn")) {
                Map<String, Object> map = (Map<String, Object>) data.get("spawn");
                String worldId = (String) map.get("worldId");
                double x = ((Number) map.get("x")).doubleValue();
                double y = ((Number) map.get("y")).doubleValue();
                double z = ((Number) map.get("z")).doubleValue();
                SPAWN = new SpawnData(worldId, x, y, z);
            }
        }
    }

    public static void initSpawn(MinecraftServer mc) {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File sc = new File(configDir, "spawn.yml");

        try {
            if (!sc.exists()) {
                sc.createNewFile();
            } else {
                loadSpawn("config/multiworld/spawn.yml");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record SpawnData(String worldId, double x, double y, double z) {
    }
}
