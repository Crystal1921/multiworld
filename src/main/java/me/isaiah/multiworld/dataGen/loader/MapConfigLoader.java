package me.isaiah.multiworld.dataGen.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class MapConfigLoader extends SimplePreparableReloadListener<Map<ResourceLocation, Resource>> {
    public static final Gson GSON = new GsonBuilder().create();

    @Override
    protected Map<ResourceLocation, Resource> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return resourceManager.listResources("maps", resource -> resource.getPath().endsWith(".json")).entrySet().stream()
                .filter(entry -> entry.getKey().getNamespace().equals(MultiworldMod.MOD_ID))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    protected void apply(Map<ResourceLocation, Resource> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        MapInstance.INSTANCE.mapConfigs.clear();
        resourceLocationJsonElementMap.forEach((resourceLocation, resource) -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                MapInstance.MapConfig mapConfig = GSON.fromJson(reader, MapInstance.MapConfig.class);
                MapInstance.INSTANCE.mapConfigs.add(mapConfig);
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                MultiworldMod.LOGGER.error("Failed to load map config: {}", resourceLocation, e);
            }
        });
    }
}
