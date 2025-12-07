package me.isaiah.multiworld.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static ModConfigSpec.BooleanValue ENABLE_LITTLE_MAP;
    public static ModConfigSpec.BooleanValue ENABLE_PORTALS;
    public static ModConfigSpec.BooleanValue ENABLE_WAYPOINTS;
    public static ModConfigSpec.DoubleValue MAP_SCALE;

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLE_LITTLE_MAP = builder
                .comment("启用小地图")
                .define("enableLittleMap", true);
        ENABLE_PORTALS = builder
                .comment("在小地图上显示传送门位置")
                .define("enablePortals", true);
        ENABLE_WAYPOINTS = builder
                .comment("在小地图上显示路径点位置")
                .define("enableWaypoints", true);
        MAP_SCALE = builder
                .comment("小地图缩放比例")
                .defineInRange("mapScale", 1.0, 0.1, 10.0);
        return builder.build();
    }
}
