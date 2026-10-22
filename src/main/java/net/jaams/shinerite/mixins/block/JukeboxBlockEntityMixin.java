package net.jaams.shinerite.mixins.block;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;


import net.minecraftforge.network.PacketDistributor;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.packet.CustomJukeboxPacket;
import net.jaams.shinerite.item.MusicDiscShinyItem;
import net.jaams.shinerite.JaamsShineriteMod;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity {
	private ItemStack syncedItem = ItemStack.EMPTY;
	private long sequenceId = 0;
	private long lastChangeTime = 0;
	private static final long DEBOUNCE_TIME = 100;
	@Shadow
	private long tickCount;
	@Shadow
	private long recordStartedTick;
	@Shadow
	private NonNullList<ItemStack> items;

	protected JukeboxBlockEntityMixin(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}

	public ItemStack getSyncedItem() {
		return this.syncedItem;
	}

	public void setSyncedItem(ItemStack item) {
		this.syncedItem = item;
	}

	@Inject(method = "shouldRecordStopPlaying", at = @At("HEAD"), cancellable = true)
	private void customShinyDiscDuration(RecordItem recordItem, CallbackInfoReturnable<Boolean> cir) {
		ItemStack stack = this.items.get(0);
		if (!stack.isEmpty() && stack.getItem() instanceof MusicDiscShinyItem shinyDisc) {
			int color = shinyDisc.getColor(stack);
			String colorCategory = getColorCategory(color);
			int customLengthTicks = getLengthForCategory(colorCategory);
			boolean shouldStop = this.tickCount >= this.recordStartedTick + customLengthTicks + 20L;
			cir.setReturnValue(shouldStop);
		}
	}

	private static int getLengthForCategory(String colorCategory) {
		return switch (colorCategory) {
			case "WHITE" -> (1 * 60 + 34) * 20;
			case "ORANGE" -> (0 * 60 + 53) * 20;
			case "MAGENTA" -> (1 * 60 + 17) * 20;
			case "LIGHT_BLUE" -> (1 * 60 + 40) * 20;
			case "YELLOW" -> (1 * 60 + 29) * 20;
			case "LIME" -> (0 * 60 + 58) * 20;
			case "PINK" -> (1 * 60 + 7) * 20;
			case "GRAY" -> (1 * 60 + 36) * 20;
			case "LIGHT_GRAY" -> (1 * 60 + 48) * 20;
			case "CYAN" -> (1 * 60 + 35) * 20;
			case "PURPLE" -> (1 * 60 + 42) * 20;
			case "BLUE" -> (1 * 60 + 40) * 20;
			case "BROWN" -> (1 * 60 + 29) * 20;
			case "GREEN" -> (1 * 60 + 23) * 20;
			case "RED" -> (1 * 60 + 23) * 20;
			case "BLACK" -> (1 * 60 + 26) * 20;
			default -> (1 * 60 + 1) * 20;
		};
	}

	private static String getColorCategory(int color) {
		int[][] preciseColors = {{0xF9FFFE, 0}, {0xF9801D, 1}, {0xC74EBD, 2}, {0x3AB3DA, 3}, {0xFED83D, 4}, {0x80C71F, 5}, {0xF38BAA, 6}, {0x474F52, 7}, {0x9D9D97, 8}, {0x169C9C, 9}, {0x8932B8, 10}, {0x3C44AA, 11}, {0x835432, 12}, {0x5E7C16, 13},
				{0xB02E26, 14}, {0x1D1D21, 15}};
		String[] names = {"WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"};
		for (int i = 0; i < preciseColors.length; i++) {
			int target = preciseColors[i][0];
			if (colorMatches(color, target, 25)) {
				return names[i];
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

	private static boolean colorMatches(int color1, int color2, int tolerance) {
		int r1 = (color1 >> 16) & 0xFF;
		int g1 = (color1 >> 8) & 0xFF;
		int b1 = color1 & 0xFF;
		int r2 = (color2 >> 16) & 0xFF;
		int g2 = (color2 >> 8) & 0xFF;
		int b2 = color2 & 0xFF;
		return Math.abs(r1 - r2) <= tolerance && Math.abs(g1 - g2) <= tolerance && Math.abs(b1 - b2) <= tolerance;
	}

	@Inject(method = "setItem(ILnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
	private void onSetItem(int index, ItemStack stack, CallbackInfo ci) {
		if (index == 0 && this.level != null && !this.level.isClientSide) {
			this.syncedItem = stack.copy();
			this.sequenceId++;
			this.setChanged();
			sendSyncPacket();
		}
	}

	@Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"))
	private void onRemoveItem(int index, int count, CallbackInfoReturnable<ItemStack> cir) {
		if (index == 0 && this.level != null && !this.level.isClientSide) {
			this.syncedItem = ItemStack.EMPTY;
			this.sequenceId++;
			this.setChanged();
			sendSyncPacket();
		}
	}

	@Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void onLoad(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("RecordItem", 10)) {
			this.syncedItem = ItemStack.of(tag.getCompound("RecordItem"));
		} else {
			this.syncedItem = ItemStack.EMPTY;
		}
		if (tag.contains("SequenceId", 4)) {
			this.sequenceId = tag.getLong("SequenceId");
		}
		if (this.level != null && !this.level.isClientSide) {
			sendSyncPacket();
		}
	}

	@Inject(method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void onSaveAdditional(CompoundTag tag, CallbackInfo ci) {
		tag.putLong("SequenceId", this.sequenceId);
	}

	@Inject(method = "popOutRecord", at = @At("HEAD"), cancellable = true)
	private void onPopOutRecord(CallbackInfo ci) {
		if (this.level != null && !this.level.isClientSide) {
			BlockPos blockpos = this.getBlockPos();
			JukeboxBlockEntity jukebox = (JukeboxBlockEntity) (Object) this;
			ItemStack itemstack = jukebox.getItem(0);
			if (!itemstack.isEmpty()) {
				ItemStack removedItem = jukebox.removeItem(0, 1);
				Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5D, 1.01D, 0.5D).offsetRandom(this.level.random, 0.7F);
				ItemStack itemstack1 = removedItem.copy();
				ItemEntity itementity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
				itementity.setDefaultPickUpDelay();
				this.level.addFreshEntity(itementity);
			}
			ci.cancel();
		}
	}

	private void sendSyncPacket() {
		if (this.level != null && !this.level.isClientSide) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastChangeTime < DEBOUNCE_TIME) {
				this.level.getServer().execute(() -> {
					try {
						Thread.sleep(DEBOUNCE_TIME - (currentTime - lastChangeTime));
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					sendSyncPacketNow();
				});
			} else {
				sendSyncPacketNow();
			}
		}
	}

	private void sendSyncPacketNow() {
		if (this.level != null && !this.level.isClientSide) {
			ItemStack currentItem = this.syncedItem.copy();
			if (!ItemStack.matches(currentItem, this.getSyncedItem())) {
				this.setSyncedItem(currentItem);
			}
			this.lastChangeTime = System.currentTimeMillis();
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(this.getBlockPos())), new CustomJukeboxPacket(this.getBlockPos(), currentItem, this.sequenceId));
		}
	}
}
