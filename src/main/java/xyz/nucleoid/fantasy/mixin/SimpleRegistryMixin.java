package xyz.nucleoid.fantasy.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.fantasy.RemoveFromRegistry;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@Mixin(MappedRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RemoveFromRegistry<T> {

    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<ResourceKey<T>, RegistrationInfo> registrationInfos;
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow @Final ResourceKey<? extends Registry<T>> key;
    @Shadow private boolean frozen;

    @Override
    public boolean fantasy$remove(T entry) {
        var registryEntry = this.byValue.get(entry);
        int rawId = this.toId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.byId.set(rawId, null);
            this.byLocation.remove(registryEntry.key().location());
            this.byKey.remove(registryEntry.key());
            this.registrationInfos.remove(entry);
            this.byValue.remove(entry);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean fantasy$remove(ResourceLocation key) {
        var entry = this.byLocation.get(key);
        return entry != null && entry.isBound() && this.fantasy$remove(entry.value());
    }

    @Override
    public void fantasy$setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean fantasy$isFrozen() {
        return this.frozen;
    }
}
