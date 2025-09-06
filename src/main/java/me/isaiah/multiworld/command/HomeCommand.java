package me.isaiah.multiworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.isaiah.multiworld.command.WarpCommand.warpTo;
import static net.minecraft.commands.Commands.literal;

public class HomeCommand {
    public static Map<UUID, HomeData> HOMES = new HashMap<>();

    public static void register_commands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("sethome")
                .requires(source -> source.hasPermission(4))
                .executes(HomeCommand::setHome));

        dispatcher.register(literal("home")
                .requires(source -> source.hasPermission(4))
                .executes(context -> warpToHome(context, context.getSource().getPlayer()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> warpToHome(context, EntityArgument.getPlayer(context, "player")))));
    }

    private static int setHome(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Invalid Operation"));
            return 0;
        }
        Vec3 pos = player.position();
        String location = player.level().dimension().location().toString();
        HOMES.put(player.getUUID(), new HomeData(location, pos.x, pos.y, pos.z));
        player.sendSystemMessage(Component.literal("Set home point at " + pos.x + ", " + pos.y + ", " + pos.z + " in dimension " + location));
        // 保存到文件
        try {
            saveHome("config/multiworld/homes.yml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static int warpToHome(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Player not found"));
            return 0;
        }
        HomeData homeData = HOMES.get(player.getUUID());
        if (homeData == null) {
            context.getSource().sendFailure(Component.literal("Home point not set"));
            return 0;
        }
        int ret = warpTo(player, new WarpCommand.WarpData(homeData.worldId(), "home", homeData.x(), homeData.y(), homeData.z()));
        if (ret == 1) {
            return 1;
        }
        context.getSource().sendFailure(Component.literal("Failed to warp to home point"));
        return 0;
    }

    public static void saveHome(String path) throws Exception {
        Map<String, Map<String, Object>> saveMap = new HashMap<>();
        for (Map.Entry<UUID, HomeData> e : HOMES.entrySet()) {
            HomeData hd = e.getValue();
            Map<String, Object> data = new HashMap<>();
            data.put("worldId", hd.worldId());
            data.put("x", hd.x());
            data.put("y", hd.y());
            data.put("z", hd.z());
            saveMap.put(e.getKey().toString(), data);
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(path)) {
            yaml.dump(Map.of("homes", saveMap), writer);
        }
    }

    // 读取homes
    private static void loadHome(String path) throws Exception {
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(path)) {
            Map<String, Object> data = yaml.load(reader);
            if (data != null && data.containsKey("homes")) {
                Map<String, Map<String, Object>> loaded = (Map<String, Map<String, Object>>) data.get("homes");
                HOMES.clear();
                for (Map.Entry<String, Map<String, Object>> e : loaded.entrySet()) {
                    UUID name = UUID.fromString(e.getKey());
                    Map<String, Object> map = e.getValue();
                    String worldId = (String) map.get("worldId");
                    double x = ((Number) map.get("x")).doubleValue();
                    double y = ((Number) map.get("y")).doubleValue();
                    double z = ((Number) map.get("z")).doubleValue();
                    HOMES.put(name, new HomeData(worldId, x, y, z));
                }
            }
        }
    }

    public static void initHome(MinecraftServer mc) {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File sc = new File(configDir, "homes.yml");

        try {
            if (!sc.exists()) {
                sc.createNewFile();
            } else {
                loadHome("config/multiworld/homes.yml");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record HomeData(String worldId, double x, double y, double z) {
    }
}
