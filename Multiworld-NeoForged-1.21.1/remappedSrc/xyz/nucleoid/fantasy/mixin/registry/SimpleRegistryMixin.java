package xyz.nucleoid.fantasy.mixin.registry;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.fantasy.RemoveFromRegistry;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@Mixin(MappedRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RemoveFromRegistry<T> {

    @Shadow @Final private Map<T, Holder.Reference<T>> valueToEntry;
    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> idToEntry;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> keyToEntry;
    @Shadow @Final private Map<ResourceKey<T>, RegistrationInfo> keyToEntryInfo;
    @Shadow @Final private ObjectList<Holder.Reference<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;
    @Shadow @Final ResourceKey<? extends Registry<T>> key;
    @Shadow private boolean frozen;

    @Override
    public boolean fantasy$remove(T entry) {
        var registryEntry = this.valueToEntry.get(entry);
        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.rawIdToEntry.set(rawId, null);
            this.idToEntry.remove(registryEntry.key().location());
            this.keyToEntry.remove(registryEntry.key());
            this.keyToEntryInfo.remove(entry);
            this.valueToEntry.remove(entry);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean fantasy$remove(ResourceLocation key) {
        var entry = this.idToEntry.get(key);
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
