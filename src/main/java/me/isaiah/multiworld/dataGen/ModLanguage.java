package me.isaiah.multiworld.dataGen;

import com.google.gson.JsonObject;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.registry.ItemRegistry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class ModLanguage implements DataProvider {
    private final Map<String, String> enData = new TreeMap<>();
    private final Map<String, String> cnData = new TreeMap<>();
    private final PackOutput output;
    private final String locale;

    public ModLanguage(PackOutput output, String locale) {
        this.output = output;
        this.locale = locale;
    }

    private void addTranslations() {
        this.add(MultiworldMod.MOD_ID, "Multiworld", "Multiworld");
        this.add("itemGroup.multiworld", "Multiworld", "多世界");

        this.add(ItemRegistry.PortalBlock.asItem(), "Portal Block", "传送门方块");
        this.add(ItemRegistry.PortalFinder.asItem(), "Portal Finder", "传送门探测器");
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        this.addTranslations();
        Path path = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(MultiworldMod.MOD_ID).resolve("lang");
        if (this.locale.equals("en_us") && !this.enData.isEmpty()) {
            return this.save(this.enData, cache, path.resolve("en_us.json"));
        }

        if (this.locale.equals("zh_cn") && !this.cnData.isEmpty()) {
            return this.save(this.cnData, cache, path.resolve("zh_cn.json"));
        }

        return CompletableFuture.allOf();
    }

    private CompletableFuture<?> save(Map<String, String> data, CachedOutput cache, Path target) {
        JsonObject json = new JsonObject();
        data.forEach(json::addProperty);
        return DataProvider.saveStable(cache, json, target);
    }

    public void add(Block key, String en, String cn) {
        this.add(key.getDescriptionId(), en, cn);
    }

    public void add(Item key, String en, String cn) {
        this.add(key.getDescriptionId(), en, cn);
    }

    private void add(String key, String en, String cn) {
        if (this.locale.equals("en_us") && !this.enData.containsKey(key)) {
            this.enData.put(key, en);
        } else if (this.locale.equals("zh_cn") && !this.cnData.containsKey(key)) {
            this.cnData.put(key, cn);
        }
    }

    @Override
    public @NotNull String getName() {
        return "language:" + this.locale;
    }
}
