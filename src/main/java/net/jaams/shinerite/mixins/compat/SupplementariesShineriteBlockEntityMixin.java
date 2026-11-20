package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.jaams.shinerite.compat.SupplementariesCompat;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;

/**
 * Makes every Shinerite block entity implement Moonlight's {@link IWashable},
 * which is the hook Supplementaries soap uses when cleaning blocks.
 */
@Mixin(BaseShineriteBlockEntity.class)
public abstract class SupplementariesShineriteBlockEntityMixin extends BlockEntity implements IWashable {

	protected SupplementariesShineriteBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	@SuppressWarnings("removal")
	public boolean tryWash(Level level, BlockPos pos, BlockState state) {
		return SupplementariesCompat.tryWashBlock(level, pos, state);
	}

	@Override
	public boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {
		return SupplementariesCompat.tryWashBlock(level, pos, state);
	}
}
