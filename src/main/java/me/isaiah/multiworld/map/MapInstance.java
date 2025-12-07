package me.isaiah.multiworld.map;

import me.isaiah.multiworld.MultiworldMod;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MapInstance {
    INSTANCE;

    public int mapSize;
    public int mapPosX;
    public int mapPosY;
    public double mapScale;
    public final List<MapConfig> mapConfigs = new ArrayList<>();

    public static void initMapConfig() {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File wc = new File(configDir, "map.yml");

        try {
            if (!wc.exists()) {
                MultiworldMod.LOGGER.warn("MapInstance config file does not exist!");
                // 设置默认值
                setDefaultValue();
            } else {
                load("config/multiworld/map.yml");
            }
        } catch (Exception e) {
            MultiworldMod.LOGGER.warn(e.getMessage());
        }
    }

    // 保存
    public static void save(String path) throws Exception {
        // 写入 Map 到 YAML 文件
        Map<String, Object> data = new HashMap<>();
        data.put("size", MapInstance.INSTANCE.mapSize);
        data.put("posX", MapInstance.INSTANCE.mapPosX);
        data.put("posY", MapInstance.INSTANCE.mapPosY);
        data.put("scale", MapInstance.INSTANCE.mapScale);

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(path)) {
            yaml.dump(data, writer);
        }
    }

    // 读取
    public static void load(String path) throws Exception {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        // 读取回 Map（load 返回 Object，需要强转）
        try (FileReader reader = new FileReader(path)) {

            Map<String, Object> loaded = yaml.load(reader);
            if (loaded == null) {
                // 设置默认值
                setDefaultValue();
                return;
            }
            System.out.println("loaded = " + loaded);
            MapInstance.INSTANCE.mapSize = (int) loaded.getOrDefault("size", 100);
            MapInstance.INSTANCE.mapPosX = (int) loaded.getOrDefault("posX", 10);
            MapInstance.INSTANCE.mapPosY = (int) loaded.getOrDefault("posY", 10);
            MapInstance.INSTANCE.mapScale = (double) loaded.getOrDefault("scale", 2F);
        }
    }

    private static void setDefaultValue() throws Exception {
        MapInstance.INSTANCE.mapSize = 100;
        MapInstance.INSTANCE.mapPosX = 10;
        MapInstance.INSTANCE.mapPosY = 10;
        MapInstance.INSTANCE.mapScale = 2F;
        save("config/multiworld/map.yml");
    }

    public static List<String> getWorldIdList() {
        List<String> names = new ArrayList<>();
        for (MapConfig config : MapInstance.INSTANCE.mapConfigs) {
            names.add(config.worldID);
        }
        return names;
    }

    public record MapConfig(String worldID, String mapName, int minX, int minZ, int maxX, int maxZ) {
    }
}
