package net.jaams.shinerite.mixins.compat.amendments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.jaams.shinerite.compat.AmendmentsCompat;

/**
 * Lets players imbue Shinerite items by interacting with Amendments' liquid
 * cauldron while it holds a potion.
 */
@Mixin(value = LiquidCauldronBlockTile.class, remap = false)
public abstract class LiquidCauldronBlockTileMixin extends BlockEntity {

	protected LiquidCauldronBlockTileMixin(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}

	@Inject(method = "handleInteraction", at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$imbueHeldItem(Player player, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
		if (AmendmentsCompat.attemptImbuement((LiquidCauldronBlockTile) (Object) this, player, hand)) {
			cir.setReturnValue(true);
		}
	}
}
