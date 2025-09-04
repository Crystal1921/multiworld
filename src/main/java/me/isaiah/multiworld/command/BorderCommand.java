package me.isaiah.multiworld.command;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class BorderCommand {
    public static HashMap<ResourceLocation, Integer> BORDERS = new HashMap<>();

    public static int run(MinecraftServer mc, ServerPlayer plr, ResourceLocation worldId, int size) {
        mc.levelKeys().forEach(r -> {
            if (r.location().equals(worldId)) {
                ServerLevel level = mc.getLevel(r);
                if (level != null) {
                    WorldBorder worldBorder = level.getWorldBorder();
                    worldBorder.setSize(size);
                    BORDERS.put(worldId, size);
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
        Map<String, Integer> saveMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, Integer> e : BORDERS.entrySet()) {
            saveMap.put(e.getKey().toString(), e.getValue());
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
                Map<String, Integer> loaded = (Map<String, Integer>) data.get("borders");
                BORDERS.clear();
                for (Map.Entry<String, Integer> e : loaded.entrySet()) {
                    BORDERS.put(ResourceLocation.parse(e.getKey()), e.getValue());
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
                    BORDERS.put(level.dimension().location(), (int) border.getSize());
                });
                load(wc.getPath());
                mc.getAllLevels().forEach(level -> {
                    WorldBorder border = level.getWorldBorder();
                    Integer size = BORDERS.get(level.dimension().location());
                    if (size != null) {
                        border.setSize(size);
                    }
                });
                return;
            }

            load(wc.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
