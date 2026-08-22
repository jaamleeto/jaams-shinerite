package net.jaams.shinerite.mixins.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import net.jaams.shinerite.block.PottedShineritiaBlock;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.particle.CustomEffectParticleData;

@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockMixin {
	@Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 0, shift = At.Shift.AFTER))
	private void jaams$transferShineriteNbt(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (level.getBlockState(pos).getBlock() instanceof PottedShineritiaBlock && level.getBlockEntity(pos) instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.getItem() instanceof IDyeableItem dyeableItem && dyeableItem.hasColor(stack)) {
				shineriteBlockEntity.setColor(dyeableItem.getColor(stack));
			} else if (stack.hasTag() && stack.getTag().contains("color", 3)) {
				shineriteBlockEntity.setColor(stack.getTag().getInt("color"));
			}
			if (stack.hasTag() && stack.getTag().contains("Coated", 1)) {
				shineriteBlockEntity.setCoated(stack.getTag().getBoolean("Coated"));
			}
			if (stack.hasTag() && stack.getTag().contains("ImbuedEffects", Tag.TAG_LIST)) {
				ListTag effects = stack.getTag().getList("ImbuedEffects", Tag.TAG_COMPOUND);
				int uses = stack.getTag().getInt("ImbuedUses");
				int maxUses = stack.getTag().getInt("MaxImbuedUses");
				int imbuedColor = stack.getTag().getInt("ImbuedColor");
				shineriteBlockEntity.setImbuedState(effects, uses, maxUses, imbuedColor);
				if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
					serverLevel.sendParticles(new CustomEffectParticleData(
						((imbuedColor >> 16) & 255) / 255.0F,
						((imbuedColor >> 8) & 255) / 255.0F,
						(imbuedColor & 255) / 255.0F, 0.3F),
						pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.12);
				}
			}
			shineriteBlockEntity.setChanged();
			level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
		}
	}
}