package me.isaiah.multiworld.gui.gif;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GifManager {
    private final Map<ResourceLocation, GifAnimation> animations = new HashMap<>();
    private final List<GifPlayer> players = new ArrayList<>();
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

    /**
     * 注册一个 GifPlayer，用于资源重载时重建纹理
     */
    public void registerPlayer(GifPlayer player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * 取消注册 GifPlayer
     */
    public void unregisterPlayer(GifPlayer player) {
        players.remove(player);
    }

    /**
     * 重建所有 GifPlayer 的纹理（窗口大小改变或资源重载时调用）
     */
    public void recreateAllTextures() {
        // 使用副本进行遍历，避免并发修改
        List<GifPlayer> copy = new ArrayList<>(players);
        for (GifPlayer player : copy) {
            player.recreateTextures();
        }
    }

    /**
     * 清理所有 GifPlayer（资源卸载时调用）
     */
    public void clearPlayers() {
        players.clear();
    }
}
