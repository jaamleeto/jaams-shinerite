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

import net.jaams.shinerite.block.PottedShineritiaBlock;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.dyeable.IDyeableItem;

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
			if (stack.hasTag() && stack.getTag().contains("Waxed", 1)) {
				shineriteBlockEntity.setWaxed(stack.getTag().getBoolean("Waxed"));
			}
			shineriteBlockEntity.setChanged();
			level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
		}
	}
}