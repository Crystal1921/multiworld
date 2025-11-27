package me.isaiah.multiworld.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static ModConfigSpec.BooleanValue ENABLE_LITTLE_MAP;

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLE_LITTLE_MAP = builder
                .comment("启用小地图")
                .define("enableLittleMap", true);

        return builder.build();
    }
}
