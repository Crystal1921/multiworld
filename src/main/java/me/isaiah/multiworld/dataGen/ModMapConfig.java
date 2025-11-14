package me.isaiah.multiworld.dataGen;

import me.isaiah.multiworld.dataGen.provider.MapConfigProvider;
import net.minecraft.data.PackOutput;

public class ModMapConfig extends MapConfigProvider {
    public ModMapConfig(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerMaps() {
        MapBuilder.map()
                .add("minecraft:overworld", "overworld", -3200,-3200,3200,3200)
                .add("myid:myvalue", "lily_white", -200,-200,200,200)
                .build();
    }
}
