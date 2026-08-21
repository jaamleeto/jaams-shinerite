package net.jaams.shinerite.handler.jukebox;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.HashMap;

public class JukeboxDiscTrackerHandler {
	private static final Map<BlockPos, ItemStack> lastProcessedDiscs = new HashMap<>();

	public static void putDisc(BlockPos pos, ItemStack disc) {
		lastProcessedDiscs.put(pos, disc.copy());
	}

	public static ItemStack getDisc(BlockPos pos) {
		return lastProcessedDiscs.getOrDefault(pos, ItemStack.EMPTY);
	}

	public static void removeDisc(BlockPos pos) {
		lastProcessedDiscs.remove(pos);
	}
}
