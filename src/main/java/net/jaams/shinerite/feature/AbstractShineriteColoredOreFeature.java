package net.jaams.shinerite.feature;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;

import com.mojang.serialization.Codec;

/**
 * Shared implementation of the recoloured Shinerite ore blobs. Each dimension / depth variant only
 * supplies its ore block, its ground predicate and a weighted colour palette; the palette is a flat
 * array where a repeated entry simply raises that colour's weight.
 */
public abstract class AbstractShineriteColoredOreFeature extends Feature<NoneFeatureConfiguration> {
	private static final int NEIGHBOUR_SEARCH = 2;

	private final BlockState oreBlock;
	private final int[] palette;
	private final Supplier<Boolean> enabled;
	private final Supplier<Double> chance;
	private final IntSupplier minSize;
	private final IntSupplier maxSize;
	private final IntSupplier spread;

	protected AbstractShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec, BlockState oreBlock, int[] palette, Supplier<Boolean> enabled,
			Supplier<Double> chance, IntSupplier minSize, IntSupplier maxSize, IntSupplier spread) {
		super(codec);
		this.oreBlock = oreBlock;
		this.palette = palette;
		this.enabled = enabled;
		this.chance = chance;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.spread = spread;
	}

	/** Blocks this ore may replace. */
	protected abstract boolean isValidGround(BlockState state);

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		if (level == null || !enabled.get()) {
			return false;
		}
		RandomSource random = context.random();
		if (random.nextFloat() >= chance.get()) {
			return false;
		}
		BlockPos origin = context.origin();
		int attempts = rollInt(random, minSize.getAsInt(), maxSize.getAsInt());
		int spread = this.spread.getAsInt();
		int color = palette[random.nextInt(palette.length)];
		boolean placed = false;
		BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
		for (int i = 0; i < attempts; i++) {
			target.set(
					origin.getX() + randomOffset(random, spread),
					origin.getY() + randomOffset(random, spread),
					origin.getZ() + randomOffset(random, spread)
			);
			// Spread can nudge the sample out of the stone it started in, so nudge back in before
			// giving up on the block instead of letting the blob silently shrink.
			if (findGround(level, target, spread)) {
				placed |= placeOreWithColor(level, target, color);
			}
		}
		return placed;
	}

	private boolean findGround(WorldGenLevel level, BlockPos.MutableBlockPos target, int spread) {
		if (isValidGround(level.getBlockState(target))) {
			return true;
		}
		int reach = Math.max(1, Math.min(spread, NEIGHBOUR_SEARCH));
		for (int dy = 0; dy <= reach; dy++) {
			for (int dx = -reach; dx <= reach; dx++) {
				for (int dz = -reach; dz <= reach; dz++) {
					target.move(dx, dy, dz);
					boolean valid = isValidGround(level.getBlockState(target));
					target.move(-dx, -dy, -dz);
					if (valid) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static int randomOffset(RandomSource random, int spread) {
		return spread <= 0 ? 0 : random.nextInt(spread * 2 + 1) - spread;
	}

	private boolean placeOreWithColor(WorldGenLevel level, BlockPos pos, int color) {
		// A Shinerite ore blob must never land on top of an existing block entity. LevelChunk reuses
		// whatever block entity already sits at a position instead of creating the right one, so the
		// chunk would end up holding (for example) a deepslate ore block entity under a stone ore block
		// and vanilla would log "invalid for ticking" forever while never ticking it.
		if (level.getBlockState(pos).hasBlockEntity()) {
			return false;
		}

		// Flag 2 only: no client notification. This runs against a WorldGenLevel proto-chunk, and
		// BaseShineriteBlockEntity#sync is a no-op there anyway since there is no ServerLevel to broadcast to.
		if (!level.setBlock(pos, oreBlock, 2)) {
			return false;
		}

		BlockState placedState = level.getBlockState(pos);
		if (!placedState.is(oreBlock.getBlock())) {
			return false;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return false;
		}

		if (!blockEntity.getType().isValid(placedState)) {
			// Defensive: never let a stale block entity of another type survive next to our ore.
			if (level instanceof Level realLevel) {
				realLevel.removeBlockEntity(pos);
			}
			return false;
		}

		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			shineriteBlockEntity.setColor(color);
			shineriteBlockEntity.setChanged();
			return true;
		}
		return false;
	}

	private static int rollInt(RandomSource random, int min, int max) {
		int low = Math.min(min, max);
		int high = Math.max(min, max);
		return high <= low ? low : random.nextInt(high - low + 1) + low;
	}
}