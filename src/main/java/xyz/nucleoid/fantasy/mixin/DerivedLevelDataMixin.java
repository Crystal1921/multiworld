package xyz.nucleoid.fantasy.mixin;

import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DerivedLevelData.class)
public class DerivedLevelDataMixin {
    @Unique
    private long fantasy$dayTime;
    @Unique
    private float fantasy$dayTimeFraction;
    @Unique
    private float fantasy$dayTimePerTick;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fantasy$initDayTime(WorldData worldData, ServerLevelData wrapped, CallbackInfo ci) {
        this.fantasy$dayTime = wrapped.getDayTime();
        this.fantasy$dayTimeFraction = wrapped.getDayTimeFraction();
        this.fantasy$dayTimePerTick = wrapped.getDayTimePerTick();
    }

    /**
     * @author Crystal1921
     * @reason 独立存储当前世界时间
     */
    @Overwrite
    public long getDayTime() {
        return this.fantasy$dayTime;
    }

    /**
     * @author Crystal1921
     * @reason 独立存储当前世界时间
     */
    @Overwrite
    public void setDayTime(long time) {
        this.fantasy$dayTime = time;
    }

    /**
     * @author Crystal1921
     * @reason 独立存储日夜进度
     */
    @Overwrite
    public float getDayTimeFraction() {
        return this.fantasy$dayTimeFraction;
    }

    /**
     * @author Crystal1921
     * @reason 独立存储每刻日夜流逝比例
     */
    @Overwrite
    public float getDayTimePerTick() {
        return this.fantasy$dayTimePerTick;
    }

    /**
     * @author Crystal1921
     * @reason 独立存储日夜进度
     */
    @Overwrite
    public void setDayTimeFraction(float dayTimeFraction) {
        this.fantasy$dayTimeFraction = dayTimeFraction;
    }

    /**
     * @author Crystal1921
     * @reason 独立存储每刻日夜流逝比例
     */
    @Overwrite
    public void setDayTimePerTick(float dayTimePerTick) {
        this.fantasy$dayTimePerTick = dayTimePerTick;
    }
}
