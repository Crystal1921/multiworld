package me.isaiah.multiworld.dataGen;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.data.DataProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MultiworldMod.MOD_ID, value = Dist.CLIENT)
public class DataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var efh = event.getExistingFileHelper();
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var vanillaPack = generator.getVanillaPack(true);
        var existingFileHelper = event.getExistingFileHelper();
        var pack = generator.getPackOutput();
        var completableFuture = event.getLookupProvider();
        //ItemModel
        event.getGenerator().addProvider(
                event.includeClient(),
                (DataProvider.Factory<ModItem>) pOutput -> new ModItem(pOutput, MultiworldMod.MOD_ID, efh));

        //Language
        generator.addProvider(
                event.includeClient(), new ModLanguage(pack, "zh_cn"));
        generator.addProvider(
                event.includeClient(), new ModLanguage(pack, "en_us"));
    }
}
