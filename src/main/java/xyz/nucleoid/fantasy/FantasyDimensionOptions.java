package xyz.nucleoid.fantasy;

import java.util.function.Predicate;
import net.minecraft.world.level.dimension.LevelStem;

public interface FantasyDimensionOptions {
    Predicate<LevelStem> SAVE_PREDICATE = (e) -> ((FantasyDimensionOptions) (Object) e).fantasy$getSave();

    void fantasy$setSave(boolean value);
    boolean fantasy$getSave();
}
