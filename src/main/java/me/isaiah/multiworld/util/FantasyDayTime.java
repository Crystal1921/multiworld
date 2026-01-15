package me.isaiah.multiworld.util;

import lombok.Getter;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class FantasyDayTime {
    public static final FantasyDayTime INSTANCE = new FantasyDayTime();
    private final Map<ResourceLocation, TimeData> dataMap = new HashMap<>();

    public static TimeData getTimeData(WorldData worldData, ServerLevelData wrapped) {
        return new TimeData(0, 0, 0);
    }

    public static void initDayTimeConfig() {
        File configDir = new File("config/multiworld");
        configDir.mkdirs();

        File wc = new File(configDir, "daytime.yml");

        try {
            if (!wc.exists()) {
                MultiworldMod.LOGGER.warn("FantasyDayTime config file does not exist!");
                // 创建空配置文件
                save("config/multiworld/daytime.yml");
            } else {
                load("config/multiworld/daytime.yml");
            }
        } catch (Exception e) {
            MultiworldMod.LOGGER.warn(e.getMessage());
        }
    }

    // 保存
    public static void save(String path) throws Exception {
        // 将 ResourceLocation 转换为字符串存储
        Map<String, Map<String, Object>> data = new HashMap<>();
        for (Map.Entry<ResourceLocation, TimeData> entry : FantasyDayTime.INSTANCE.dataMap.entrySet()) {
            Map<String, Object> timeData = new HashMap<>();
            timeData.put("dayTime", entry.getValue().datTime());
            timeData.put("timeFraction", entry.getValue().timeFraction());
            timeData.put("timePerTick", entry.getValue().timePerTick());
            data.put(entry.getKey().toString(), timeData);
        }

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
        try (FileReader reader = new FileReader(path)) {
            Map<String, Map<String, Object>> loaded = yaml.load(reader);
            if (loaded == null) {
                return;
            }
            MultiworldMod.LOGGER.info("Loading FantasyDayTime data: " + loaded);

            for (Map.Entry<String, Map<String, Object>> entry : loaded.entrySet()) {
                ResourceLocation key = ResourceLocation.tryParse(entry.getKey());
                if (key == null) {
                    MultiworldMod.LOGGER.warn("Invalid ResourceLocation: " + entry.getKey());
                    continue;
                }

                Map<String, Object> timeDataMap = entry.getValue();
                long datTime = ((Number) timeDataMap.getOrDefault("dayTime", 0L)).longValue();
                float timeFraction = ((Number) timeDataMap.getOrDefault("timeFraction", 0.0F)).floatValue();
                float timePerTick = ((Number) timeDataMap.getOrDefault("timePerTick", 0.0F)).floatValue();

                FantasyDayTime.INSTANCE.dataMap.put(key, new TimeData(datTime, timeFraction, timePerTick));
            }
        }
    }

    public record TimeData(long datTime, float timeFraction, float timePerTick) {

    }
}
