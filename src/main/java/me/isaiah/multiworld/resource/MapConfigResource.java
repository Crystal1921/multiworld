package me.isaiah.multiworld.resource;

import com.google.gson.*;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class MapConfigResource extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    public static final MapConfigResource INSTANCE = new MapConfigResource();
    Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> map = new HashMap<>();
        FileToIdConverter filetoidconverter = FileToIdConverter.json("maps");

        for (Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement jsonelement = GSON.fromJson(reader, JsonElement.class);
                JsonElement jsonelement1 = map.put(resourcelocation1, jsonelement);
                if (jsonelement1 != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + resourcelocation1);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                MultiworldMod.LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, jsonparseexception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonElementMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        MapInstance.INSTANCE.mapConfigs.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet()) {
            ResourceLocation key = entry.getKey();
            JsonElement entryValue = entry.getValue();
            try {
                MapInstance.MapConfig mapConfig = getMapConfig(entryValue);
                if (mapConfig.mapName() != null && mapConfig.worldID() != null) {
                    MapInstance.INSTANCE.mapConfigs.add(mapConfig);
                } else {
                    MultiworldMod.LOGGER.warn("MapConfig with ID {} has no world ID", mapConfig.mapName());
                }
            } catch (Exception e) {
                MultiworldMod.LOGGER.error("Couldn't parse data file {} from {}", key, entryValue, e);
            }
        }

    }

    private static MapInstance.@NotNull MapConfig getMapConfig(JsonElement entryValue) {
        JsonObject obj = entryValue.getAsJsonObject();
        String mapBgName =
                obj.has("mapBgName") && !obj.get("mapBgName").isJsonNull()
                        ? obj.get("mapBgName").getAsString()
                        : null;
        return new MapInstance.MapConfig(
                obj.get("worldID").getAsString(),
                obj.get("mapName").getAsString(),
                mapBgName,
                obj.get("minX").getAsInt(),
                obj.get("minZ").getAsInt(),
                obj.get("maxX").getAsInt(),
                obj.get("maxZ").getAsInt()
        );
    }
}
