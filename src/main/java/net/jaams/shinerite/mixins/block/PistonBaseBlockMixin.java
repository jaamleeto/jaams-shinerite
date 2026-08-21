package net.jaams.shinerite.mixins.block;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.component.block.BaseShineriteFlowerBlock;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.block.ShineriteRootsBlock;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {
	@Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
	private static void isPushable(BlockState state, Level level, BlockPos pos, Direction direction, boolean destroy, Direction pistonDirection, CallbackInfoReturnable<Boolean> cir) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity != null) {
			if (blockEntity instanceof BaseShineriteBlockEntity && !(state.getBlock() instanceof BaseShineriteFlowerBlock) && !(state.getBlock() instanceof ShineriteRootsBlock)) {
				cir.setReturnValue(true);
			}
		}
	}
}
