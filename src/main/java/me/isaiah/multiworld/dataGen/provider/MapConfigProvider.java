package me.isaiah.multiworld.dataGen.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public abstract class MapConfigProvider implements DataProvider {
    protected final PackOutput output;
    protected static final ArrayList<MapInstance.MapConfig> mapList = new ArrayList<>();

    public MapConfigProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        this.clearMaps();
        this.registerMaps();
        return this.generateAllDialogs(cachedOutput);
    }

    protected CompletableFuture<?> generateAllDialogs(CachedOutput cache) {
        CompletableFuture<?>[] futures = new CompletableFuture[mapList.size()];
        int i = 0;

        MapInstance.MapConfig dialog;
        Path target;
        for(Iterator<MapInstance.MapConfig> iterator = mapList.iterator();
            iterator.hasNext();
            futures[i++] = DataProvider.saveStable(cache, serializeMap(dialog), target)) {

            dialog = iterator.next();
            target = this.getPath(dialog);
        }

        return CompletableFuture.allOf(futures);
    }

    protected Path getPath(MapInstance.MapConfig dialogSequence) {
        return this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(MultiworldMod.MOD_ID).resolve("maps").resolve(dialogSequence.mapName() + ".json");
    }

    private JsonElement serializeMap(MapInstance.MapConfig mapConfig) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJsonTree(mapConfig);
    }

    protected abstract void registerMaps();

    private void clearMaps() {
    }

    @Override
    public @NotNull String getName() {
        return "Map Configs";
    }

    protected static class MapBuilder {
        public static MapBuilder map() {
            return new MapBuilder();
        }

        private final ArrayList<MapInstance.MapConfig> entries = new ArrayList<>();

        public MapBuilder add(String worldID, String mapName, int minX, int minZ, int maxX, int maxZ) {
            this.entries.add(new MapInstance.MapConfig(worldID, mapName, minX, minZ, maxX, maxZ));
            return this;
        }

        public void build() {
            mapList.addAll(this.entries);
        }
    }
}
