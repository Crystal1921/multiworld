package xyz.nucleoid.fantasy.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(FlatLevelGeneratorSettings.class)
public class FlatLevelGeneratorSettingsMixin {
    @Final
    @Shadow
    private List<BlockState> layers;

    /**
     * @author Crystal1921
     * @reason Filter out null layers to prevent crashes
     */
    @Overwrite
    public List<BlockState> getLayers() {
        List<BlockState> list = new ArrayList<>();
        for (BlockState layer : this.layers) {
            if (layer != null) {
                list.add(layer);
            } else {
                list.add(Blocks.AIR.defaultBlockState());
            }
        }

        return list;
    }
}
