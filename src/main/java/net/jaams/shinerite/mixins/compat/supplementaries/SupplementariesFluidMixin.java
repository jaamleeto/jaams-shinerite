package net.jaams.shinerite.mixins.compat.supplementaries;

import org.spongepowered.asm.mixin.Mixin;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidConsumer;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.jaams.shinerite.block.PottedShineritiaBlock;
import net.jaams.shinerite.compat.SupplementariesCompat;
import net.jaams.shinerite.component.block.BaseShineriteBlock;
import net.jaams.shinerite.component.block.BaseShineriteFlowerBlock;
import net.jaams.shinerite.component.block.BaseShineriteSlabBlock;
import net.jaams.shinerite.component.block.BaseShineriteStairBlock;

/**
 * Lets Supplementaries faucets treat every Shinerite block with a block entity as
 * a Soft Fluid consumer, so water washes and potions imbue it.
 */
@Mixin(value = {
		BaseShineriteBlock.class,
		BaseShineriteSlabBlock.class,
		BaseShineriteStairBlock.class,
		BaseShineriteFlowerBlock.class,
		PottedShineritiaBlock.class
})
public abstract class SupplementariesFluidMixin implements ISoftFluidConsumer {

	@Override
	public boolean tryAcceptingFluid(Level level, BlockState state, BlockPos pos, SoftFluidStack fluid) {
		return SupplementariesCompat.tryAcceptSoftFluid(level, pos, state, fluid);
	}
}
