package me.isaiah.multiworld.resource;

import me.isaiah.multiworld.gui.gif.GifAnimation;
import me.isaiah.multiworld.gui.gif.GifLoader;
import me.isaiah.multiworld.gui.gif.GifManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GifResource extends SimplePreparableReloadListener<Map<ResourceLocation, InputStream>> {
    public static final GifResource INSTANCE = new GifResource();

    @Override
    protected Map<ResourceLocation, InputStream> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, InputStream> map = new HashMap<>();
        FileToIdConverter filetoidconverter = new FileToIdConverter("gif", ".gif");

        for (Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            try {
                InputStream open = entry.getValue().open();
                map.put(resourcelocation1, open);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, InputStream> inputStreamMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        inputStreamMap.forEach((resourceLocation, imageInputStream) -> {
            try {
                GifAnimation gifAnimation = GifLoader.load(imageInputStream);
                GifManager.getInstance().addAnimation(resourceLocation, gifAnimation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }
}
