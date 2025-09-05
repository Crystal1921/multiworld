package me.isaiah.multiworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.isaiah.multiworld.command.commands.BorderCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.border.WorldBorder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.commands.Commands.literal;

public class WarpCommand {
    public static HashMap<ResourceLocation, WarpData> WARPS = new HashMap<>();

    // On command register
    public static void register_commands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("warp")
                .then(Commands.argument("destination", StringArgumentType.string())
                        .executes(context -> {
                            return 1;
                        })));

        dispatcher.register(literal("setWarp")
                .then(Commands.argument("destination", StringArgumentType.string())
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> {

                                    return 1;
                                }))));
    }

    // 保存
    public static void save(String path) throws Exception {
        Map<String, Map<String, Object>> saveMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, WarpData> e : WARPS.entrySet()) {
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
                    WARPS.put(ResourceLocation.parse(e.getKey()), new WarpData(worldId, name, x, y, z));
                }
            }
        }
    }

    public static void initWorldBorder(MinecraftServer mc) {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File wc = new File(configDir, "warps.yml");

        try {
            if (!wc.exists()) {

                return;
            }

            load(wc.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record WarpData(String worldId, String name, double x, double y, double z) {
    }
}
