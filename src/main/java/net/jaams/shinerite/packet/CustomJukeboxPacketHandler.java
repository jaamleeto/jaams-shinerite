package net.jaams.shinerite.packet;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.Minecraft;

import net.jaams.shinerite.handler.jukebox.SyncedJukeboxItemHandler;

@OnlyIn(Dist.CLIENT)
public class CustomJukeboxPacketHandler {
	public static void handleJukeboxSync(BlockPos pos, ItemStack item, long sequenceId) {
		Level level = Minecraft.getInstance().level;
		if (level != null) {
			LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
			if (levelRenderer instanceof SyncedJukeboxItemHandler handler) {
				synchronized (handler.getLock()) {
					if (handler.isLatestSequence(pos, sequenceId)) {
						ItemStack currentItem = handler.getSyncedItem(pos);
						boolean itemChanged = currentItem.isEmpty() != item.isEmpty() || !ItemStack.matches(currentItem, item);
						handler.updateSyncedJukeboxItem(pos, item.copy(), sequenceId);
						SoundInstance currentSound = handler.getActiveSound(pos);
						if (itemChanged && !item.isEmpty() && item.getItem() instanceof RecordItem recordItem && currentSound == null) {
							levelRenderer.playStreamingMusic(recordItem.getSound(), pos, recordItem);
						} else if (itemChanged && item.isEmpty() && currentSound != null) {
							levelRenderer.playStreamingMusic(null, pos, null);
						}
					}
				}
			}
		}
	}
}
