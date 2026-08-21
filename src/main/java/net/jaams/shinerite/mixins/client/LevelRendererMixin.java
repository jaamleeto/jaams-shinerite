package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.Minecraft;

import net.jaams.shinerite.item.MusicDiscShinyItem;
import net.jaams.shinerite.init.ModSounds;
import net.jaams.shinerite.handler.jukebox.SyncedJukeboxItemHandler;

import javax.annotation.Nullable;

import java.util.Map;
import java.util.HashMap;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements SyncedJukeboxItemHandler {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	private Map<BlockPos, SoundInstance> playingRecords;

	@Shadow
	private void notifyNearbyEntities(Level level, BlockPos pos, boolean playing) {
	}

	private final Map<BlockPos, ItemStack> syncedJukeboxItems = new HashMap<>();
	private final Map<BlockPos, Long> jukeboxSequenceIds = new HashMap<>();
	private final Object lock = new Object();

	@Override
	public Object getLock() {
		return lock;
	}

	@Override
	public void updateSyncedJukeboxItem(BlockPos pos, ItemStack item, long sequenceId) {
		synchronized (lock) {
			if (item.isEmpty()) {
				this.syncedJukeboxItems.remove(pos);
				this.jukeboxSequenceIds.remove(pos);
				SoundInstance soundInstance = this.playingRecords.get(pos);
				if (soundInstance != null) {
					this.minecraft.getSoundManager().stop(soundInstance);
					this.playingRecords.remove(pos);
				}
			} else {
				this.syncedJukeboxItems.put(pos, item.copy());
				this.jukeboxSequenceIds.put(pos, sequenceId);
			}
		}
	}

	@Override
	public SoundInstance getActiveSound(BlockPos pos) {
		synchronized (lock) {
			return this.playingRecords.get(pos);
		}
	}

	@Override
	public boolean isLatestSequence(BlockPos pos, long sequenceId) {
		synchronized (lock) {
			Long currentSequenceId = this.jukeboxSequenceIds.getOrDefault(pos, -1L);
			return sequenceId >= currentSequenceId;
		}
	}

	@Override
	public ItemStack getSyncedItem(BlockPos pos) {
		synchronized (lock) {
			return this.syncedJukeboxItems.getOrDefault(pos, ItemStack.EMPTY);
		}
	}

	@Inject(method = "playStreamingMusic(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/RecordItem;)V", at = @At("HEAD"), remap = false, cancellable = true)
	private void injectPlayStreamingMusic(@Nullable SoundEvent soundEvent, BlockPos pos, @Nullable RecordItem musicDiscItem, CallbackInfo ci) {
		if (musicDiscItem instanceof MusicDiscShinyItem shinyDisc && this.minecraft.level != null) {
			ItemStack stack;
			synchronized (lock) {
				stack = this.syncedJukeboxItems.getOrDefault(pos, ItemStack.EMPTY);
			}
			if (!stack.isEmpty() && stack.getItem() instanceof MusicDiscShinyItem actualShinyDisc) {
				int color = actualShinyDisc.getColor(stack);
				String colorCategory = getColorCategory(color);
				SoundEvent selectedSound = switch (colorCategory) {
					case "WHITE" -> ModSounds.MUSIC_DISC_SHINY.get();
					case "ORANGE" -> ModSounds.MUSIC_DISC_CALIFORNIA.get();
					case "MAGENTA" -> ModSounds.MUSIC_DISC_LITHIUM.get();
					case "LIGHT_BLUE" -> ModSounds.MUSIC_DISC_SADNESS.get();
					case "YELLOW" -> ModSounds.MUSIC_DISC_SPECIAL.get();
					case "LIME" -> ModSounds.MUSIC_DISC_SPY.get();
					case "PINK" -> ModSounds.MUSIC_DISC_FAMILY.get();
					case "GRAY" -> ModSounds.MUSIC_DISC_BLADE.get();
					case "LIGHT_GRAY" -> ModSounds.MUSIC_DISC_HUNTER.get();
					case "CYAN" -> ModSounds.MUSIC_DISC_HOLY.get();
					case "PURPLE" -> ModSounds.MUSIC_DISC_GURENGE.get();
					case "BLUE" -> ModSounds.MUSIC_DISC_SADNESS.get();
					case "BROWN" -> ModSounds.MUSIC_DISC_WAR.get();
					case "GREEN" -> ModSounds.MUSIC_DISC_TOXICITY.get();
					case "RED" -> ModSounds.MUSIC_DISC_BLOODY.get();
					case "BLACK" -> ModSounds.MUSIC_DISC_REVENGE.get();
					default -> ModSounds.MUSIC_DISC_NEVER.get();
				};
				synchronized (lock) {
					SoundInstance soundInstance = this.playingRecords.get(pos);
					if (soundInstance != null && this.minecraft.getSoundManager().isActive(soundInstance)) {
						ci.cancel();
						return;
					}
					if (soundInstance != null) {
						this.minecraft.getSoundManager().stop(soundInstance);
						this.playingRecords.remove(pos);
					}
					if (selectedSound != null) {
						Component displayName = Component.literal(getDisplayNameForColor(colorCategory));
						this.minecraft.gui.setNowPlaying(displayName);
						SoundInstance simpleSoundInstance = SimpleSoundInstance.forRecord(selectedSound, Vec3.atCenterOf(pos));
						this.playingRecords.put(pos, simpleSoundInstance);
						this.minecraft.getSoundManager().play(simpleSoundInstance);
					}
				}
				this.notifyNearbyEntities(this.minecraft.level, pos, selectedSound != null);
				ci.cancel();
			} else {
				ci.cancel();
			}
		} else if (musicDiscItem == null) {
			synchronized (lock) {
				SoundInstance soundInstance = this.playingRecords.get(pos);
				if (soundInstance != null) {
					this.minecraft.getSoundManager().stop(soundInstance);
					this.playingRecords.remove(pos);
					this.syncedJukeboxItems.remove(pos);
					this.jukeboxSequenceIds.remove(pos);
				}
			}
			this.notifyNearbyEntities(this.minecraft.level, pos, false);
			ci.cancel();
		}
	}

	private String getColorCategory(int color) {
		Object[][] preciseColors = {{0xF9FFFE, "WHITE"}, {0xF9801D, "ORANGE"}, {0xC74EBD, "MAGENTA"}, {0x3AB3DA, "LIGHT_BLUE"}, {0xFED83D, "YELLOW"}, {0x80C71F, "LIME"}, {0xF38BAA, "PINK"}, {0x474F52, "GRAY"}, {0x9D9D97, "LIGHT_GRAY"},
				{0x169C9C, "CYAN"}, {0x8932B8, "PURPLE"}, {0x3C44AA, "BLUE"}, {0x835432, "BROWN"}, {0x5E7C16, "GREEN"}, {0xB02E26, "RED"}, {0x1D1D21, "BLACK"}};
		for (Object[] entry : preciseColors) {
			int target = (Integer) entry[0];
			if (colorMatches(color, target, 25)) {
				return (String) entry[1];
			}
		}
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		if (r > 220 && g > 220 && b > 220)
			return "WHITE";
		if (r > g + 50 && r > b + 50 && r > 120)
			return "RED";
		if (g > r + 50 && g > b + 50 && g > 120)
			return "LIME";
		if (b > r + 50 && b > g + 50 && b > 120)
			return "BLUE";
		if (r < 100 && g < 100 && b < 100)
			return "BLACK";
		return "NEVER";
	}

	private boolean colorMatches(int color1, int color2, int tolerance) {
		int r1 = (color1 >> 16) & 0xFF;
		int g1 = (color1 >> 8) & 0xFF;
		int b1 = color1 & 0xFF;
		int r2 = (color2 >> 16) & 0xFF;
		int g2 = (color2 >> 8) & 0xFF;
		int b2 = color2 & 0xFF;
		return Math.abs(r1 - r2) <= tolerance && Math.abs(g1 - g2) <= tolerance && Math.abs(b1 - b2) <= tolerance;
	}

	private String getDisplayNameForColor(String colorCategory) {
		return switch (colorCategory) {
			case "WHITE" -> "jaamleeto - Shiny";
			case "ORANGE" -> "jaamleeto - California";
			case "MAGENTA" -> "jaamleeto - Lithium";
			case "LIGHT_BLUE" -> "jaamleeto - Sadness";
			case "YELLOW" -> "jaamleeto - Special";
			case "LIME" -> "jaamleeto - Spy";
			case "PINK" -> "jaamleeto - Family";
			case "GRAY" -> "jaamleeto - Blade";
			case "LIGHT_GRAY" -> "jaamleeto - Hunter";
			case "CYAN" -> "jaamleeto - Holy";
			case "PURPLE" -> "jaamleeto - Gurenge";
			case "BLUE" -> "jaamleeto - Sadness";
			case "BROWN" -> "jaamleeto - War";
			case "GREEN" -> "jaamleeto - Toxicity";
			case "RED" -> "jaamleeto - Bloody";
			case "BLACK" -> "jaamleeto - Revenge";
			default -> "jaamleeto - Never Gonna Give You Up";
		};
	}
}
