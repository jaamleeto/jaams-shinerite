package net.jaams.shinerite.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * Shared ground check for the Shineritia flower features.
 *
 * <p>{@link net.minecraft.world.level.block.BushBlock#canSurvive} happily accepts anything whose top
 * face is sturdy, which in the nether includes plenty of vegetation (warped/crimson fungi, nylium,
 * roots). Planting a flower on top of that foliage looks broken, so only genuinely solid, full
 * shaped blocks are accepted here.</p>
 */
public final class ShineriteFlowerGround {
	private static final CollisionContext COLLISION_CONTEXT = CollisionContext.empty();

	private ShineriteFlowerGround() {
	}

	/**
	 * @return {@code true} when {@code pos} is a full, solid, opaque-enough block that a Shineritia
	 *         flower can root on without floating over vegetation.
	 */
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

		// Only full blocks qualify: slabs, stairs, fences, plants and other partial shapes are out.
		return state.getCollisionShape(level, pos, COLLISION_CONTEXT).equals(Shapes.block());
	}

	/**
	 * @return {@code true} when the flower may be planted directly on top of {@code pos}'s ground even
	 *         though the spot is not air: vanilla patches it replaces on the spot, like pink petals in
	 *         cherry groves or grass and fern tufts in every meadow-like biome. Only soft, replaceable,
	 *         block-entity-free plants qualify, so generation never eats into hard terrain.
	 */
	public static boolean canReplaceGroundCover(BlockGetter level, BlockPos pos) {
		if (level == null || level.isOutsideBuildHeight(pos.getY())) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !state.canBeReplaced()) {
			return false;
		}
		// Never overwrite liquids and never trample anything that owns a block entity.
		return state.getFluidState().isEmpty() && !state.hasBlockEntity();
	}
}