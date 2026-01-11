package me.isaiah.multiworld.gui.gif;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GifManager {
    private final Map<ResourceLocation, GifAnimation> animations = new HashMap<>();
    private static final GifManager INSTANCE = new GifManager();
    private GifManager() {

    }
    public static GifManager getInstance() {
        return INSTANCE;
    }

    public GifAnimation getAnimation(ResourceLocation location) {
        return animations.get(location);
    }

    public void addAnimation(ResourceLocation location, GifAnimation animation) {
        animations.put(location, animation);
    }
}
