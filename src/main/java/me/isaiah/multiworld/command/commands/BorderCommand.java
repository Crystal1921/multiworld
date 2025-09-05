package me.isaiah.multiworld.command.commands;

import me.isaiah.multiworld.network.WorldBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class BorderCommand {
    public static HashMap<ResourceLocation, BorderData> BORDERS = new HashMap<>();

    public static int runSize(MinecraftServer mc, ServerPlayer plr, ResourceLocation worldId, int size) {
        mc.levelKeys().forEach(r -> {
            if (r.location().equals(worldId)) {
                ServerLevel level = mc.getLevel(r);
                if (level != null) {
                    WorldBorder worldBorder = level.getWorldBorder();
                    worldBorder.setSize(size);
                    PacketDistributor.sendToPlayersInDimension(level, new WorldBorderPacket(size, worldBorder.getCenterX(), worldBorder.getCenterZ()));
                    BORDERS.put(worldId, new BorderData(size, worldBorder.getCenterX(), worldBorder.getCenterZ()));
                    // 保存到文件
                    try {
                        save("config\\multiworld\\borders.yml");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
        return 1;
    }

    public static int runCenter(MinecraftServer server, ServerPlayer player, ResourceLocation worldName, int x, int y) {
        server.levelKeys().forEach(r -> {
            if (r.location().equals(worldName)) {
                ServerLevel level = server.getLevel(r);
                if (level != null) {
                    WorldBorder worldBorder = level.getWorldBorder();
                    worldBorder.setCenter(x, y);
                    PacketDistributor.sendToPlayersInDimension(level, new WorldBorderPacket(worldBorder.getSize(), x, y));
                    BORDERS.put(worldName, new BorderData(worldBorder.getSize(), x, y));
                    // 保存到文件
                    try {
                        save("config\\multiworld\\borders.yml");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
        return 1;
    }

    // 保存
    public static void save(String path) throws Exception {
        Map<String, Map<String, Object>> saveMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, BorderData> e : BORDERS.entrySet()) {
            BorderData bd = e.getValue();
            Map<String, Object> data = new HashMap<>();
            data.put("size", bd.size());
            data.put("x", bd.x());
            data.put("y", bd.y());
            saveMap.put(e.getKey().toString(), data);
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(path)) {
            yaml.dump(Map.of("borders", saveMap), writer);
        }
    }

    // 读取
    public static void load(String path) throws Exception {
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(path)) {
            Map<String, Object> data = yaml.load(reader);
            if (data != null && data.containsKey("borders")) {
                Map<String, Map<String, Object>> loaded = (Map<String, Map<String, Object>>) data.get("borders");
                BORDERS.clear();
                for (Map.Entry<String, Map<String, Object>> e : loaded.entrySet()) {
                    Map<String, Object> map = e.getValue();
                    double size = (double) map.get("size");
                    double x = (double) map.get("x");
                    double y = (double) map.get("y");
                    BORDERS.put(ResourceLocation.parse(e.getKey()), new BorderData(size, x, y));
                }
            }
        }
    }

    public static void initWorldBorder(MinecraftServer mc) {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File wc = new File(configDir, "borders.yml");

        try {
            if (!wc.exists()) {
                wc.createNewFile();
                mc.getAllLevels().forEach(level -> {
                    WorldBorder border = level.getWorldBorder();
                    BORDERS.put(level.dimension().location(), new BorderData(border.getSize(), border.getCenterX(), border.getCenterZ()));
                    try {
                        save("config\\multiworld\\borders.yml");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                load(wc.getPath());
                mc.getAllLevels().forEach(level -> {
                    WorldBorder border = level.getWorldBorder();
                    BorderData borderData = BORDERS.get(level.dimension().location());
                    if (borderData != null) {
                        border.setCenter(borderData.x(), borderData.y());
                        border.setSize(borderData.size());
                    }
                });
                return;
            }

            load(wc.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record BorderData(double size, double x, double y) {}
}
