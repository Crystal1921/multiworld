package me.isaiah.multiworld.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.map.MapInstance;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@EventBusSubscriber
public class JoinServerEvent {
    public static final Gson GSON = new GsonBuilder().create();

    @SubscribeEvent
    public static void onPlayerJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft instance = Minecraft.getInstance();
        Path gameDir = instance.gameDirectory.toPath().resolve("maps");
        loadLocalMapConfigs(gameDir);
    }

    public static void loadLocalMapConfigs(Path mapsDir) {
        if (mapsDir == null) {
            MultiworldMod.LOGGER.warn("mapsDir is null, skip loading map configs.");
            return;
        }
        if (!Files.exists(mapsDir)) {
            MultiworldMod.LOGGER.info("No maps directory found at {}", mapsDir.toAbsolutePath());
            return;
        }
        try (Stream<Path> paths = Files.walk(mapsDir)) {
            MapInstance.INSTANCE.mapConfigs.clear();
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                            MapInstance.MapConfig mapConfig = GSON.fromJson(reader, MapInstance.MapConfig.class);
                            if (mapConfig != null && mapConfig.mapName() != null && mapConfig.worldID() != null) {
                                MapInstance.INSTANCE.mapConfigs.add(mapConfig);
                                MultiworldMod.LOGGER.info("Loaded map config from {}", path.toAbsolutePath());
                            } else {
                                MultiworldMod.LOGGER.warn("Map config file {} deserialized to null", path.toAbsolutePath());
                            }
                        } catch (IOException | com.google.gson.JsonSyntaxException e) {
                            MultiworldMod.LOGGER.error("Failed to load map config from {}", path.toAbsolutePath(), e);
                        }
                    });
        } catch (IOException e) {
            MultiworldMod.LOGGER.error("Failed to traverse maps directory {}", mapsDir.toAbsolutePath(), e);
        }
    }
}
