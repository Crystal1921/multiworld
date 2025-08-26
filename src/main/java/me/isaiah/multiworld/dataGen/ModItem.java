package me.isaiah.multiworld.dataGen;

import me.isaiah.multiworld.registry.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class ModItem extends ItemModelProvider {
    public ModItem(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.basicItem(ItemRegistry.PortalFinder.get());
        this.basicItem(ItemRegistry.PortalBlock.get());
    }

    public ItemModelBuilder localItem(Item item, String filePath) {
        return localItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)), filePath);
    }

    public ItemModelBuilder localItem(ResourceLocation item, String filePath) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + filePath + item.getPath()));
    }
}
