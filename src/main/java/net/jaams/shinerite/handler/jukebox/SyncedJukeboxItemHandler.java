package net.jaams.shinerite.handler.jukebox;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.sounds.SoundInstance;

public interface SyncedJukeboxItemHandler {
	void updateSyncedJukeboxItem(BlockPos pos, ItemStack item, long sequenceId);

	SoundInstance getActiveSound(BlockPos pos);

	boolean isLatestSequence(BlockPos pos, long sequenceId);

	ItemStack getSyncedItem(BlockPos pos);

	Object getLock();
}
