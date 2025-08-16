package xyz.nucleoid.fantasy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public interface IMC {

	public void add_world(ResourceKey<Level> key, ServerLevel value);
	
}
