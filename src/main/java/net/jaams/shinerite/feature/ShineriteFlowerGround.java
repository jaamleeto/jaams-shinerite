package net.jaams.shinerite.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;



public final class ShineriteFlowerGround {
	private static final CollisionContext COLLISION_CONTEXT = CollisionContext.empty();

	private ShineriteFlowerGround() {
	}



	public static boolean isValidGround(BlockGetter level, BlockPos pos) {
		if (level == null || level.isOutsideBuildHeight(pos.getY())) {
			return false;
		}

		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !state.isSolidRender(level, pos)) {
			return false;
		}

		if (!state.isFaceSturdy(level, pos, Direction.UP)) {
			return false;
		}

		return state.getCollisionShape(level, pos, COLLISION_CONTEXT).equals(Shapes.block());
	}



	public static boolean canReplaceGroundCover(BlockGetter level, BlockPos pos) {
		if (level == null || level.isOutsideBuildHeight(pos.getY())) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !state.canBeReplaced()) {
			return false;
		}
		return state.getFluidState().isEmpty() && !state.hasBlockEntity();
	}
}
