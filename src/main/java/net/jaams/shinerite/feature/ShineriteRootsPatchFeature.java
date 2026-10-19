package net.jaams.shinerite.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;

import net.jaams.shinerite.block.ShineriteRootsBlock;
import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.configuration.common.WorldGenConfig;
import net.jaams.shinerite.util.ModUtils;

import com.mojang.serialization.Codec;

/**
 * Grows patches of Shinerite Roots from the underside of solid blocks, the same way lush cave vines
 * cling to ceilings.
 *
 * <p>The datapack placement follows the vanilla {@code cave_vines} recipe: a uniformly random height
 * across the whole column, followed by {@code environment_scan} walking up through air until it hits
 * a ceiling. That means the origin this feature receives is always the ceiling block, so the roots go
 * on {@code origin.below()} and grow sideways/downwards from there.</p>
 *
 * <p>One implementation serves both dimensions; the {@code nether} flag only picks the colour palette,
 * the natural imbuement pool and which config section is read.</p>
 */
public class ShineriteRootsPatchFeature extends Feature<NoneFeatureConfiguration> {
	private static final int[] NETHER_RED_COLORS = {0x7A0C10, 0x9E1B22, 0xC1272D, 0xE0332B, 0xB22222, 0x8E2C2C};
	private static final int[] NETHER_YELLOW_COLORS = {0xB8860B, 0xD4A017, 0xE8C547, 0xF0C419, 0xFFD54F, 0xFFE082};
	/** Warped forests get the cool end of the spectrum instead: cyans and purples. */
	private static final int[] NETHER_WARPED_CYAN_COLORS = {0x0F3D3A, 0x16685C, 0x1E8A6E, 0x2FA98A, 0x48C9A6, 0x27C9D6, 0x4FD9E6};
	private static final int[] NETHER_WARPED_PURPLE_COLORS = {0x2B1B4D, 0x3E2A6B, 0x54348C, 0x6B44AD, 0x8059C9, 0x9A72E0};
	private static final int[] OVERWORLD_WHITE_COLORS = {0xFAFAFA, 0xEFEFEF, 0xE0E0E0, 0xD0D0D0, 0xF5F5F5};
	private static final int[] OVERWORLD_GREY_COLORS = {0xB0B0B0, 0x9E9E9E, 0x8C8C8C, 0x757575, 0x616161};
	private static final MobEffect[] NETHER_IMBUEMENT_EFFECTS = {MobEffects.WITHER, MobEffects.FIRE_RESISTANCE, MobEffects.DAMAGE_BOOST};
	private static final MobEffect[] OVERWORLD_IMBUEMENT_EFFECTS = {MobEffects.POISON, MobEffects.REGENERATION, MobEffects.NIGHT_VISION, MobEffects.DIG_SPEED};
	/** How far the patch may wander from the anchor looking for one more valid hanging spot. */
	private static final int NEIGHBOUR_REACH = 2;

	private final boolean nether;
	private final Block rootsBlock;

	public ShineriteRootsPatchFeature(Codec<NoneFeatureConfiguration> codec, boolean nether) {
		super(codec);
		this.nether = nether;
		this.rootsBlock = ModBlocks.SHINERITE_ROOTS.get();
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		if (level == null || !isEnabled()) {
			return false;
		}

		RandomSource random = context.random();
		if (random.nextFloat() > getChance()) {
			return false;
		}

		BlockPos ceiling = context.origin();
		if (!isCeiling(level, ceiling)) {
			return false;
		}

		BlockPos anchor = ceiling.below();
		if (!canPlace(level, anchor)) {
			return false;
		}

		int size = WorldGenConfig.rollInt(random, getMinPatchSize(), getMaxPatchSize());
		int radius = WorldGenConfig.rollInt(random, getMinRadius(), getMaxRadius());

		int placed = 0;
		BlockPos current = anchor;
		if (placeRoots(level, current, random)) {
			placed++;
		}

		// Grow the patch outwards from the last successful spot: sideways (and up or down) to find other
		// nearby ceilings to hang from.
		int attempts = Math.max(8, size * NEIGHBOUR_REACH * 4);
		for (int i = 0; i < attempts && placed < size; i++) {
			BlockPos candidate = current.offset(spread(random, radius), random.nextInt(3) - 1, spread(random, radius));
			BlockPos target = resolveHangingSpot(level, candidate);
			if (target == null || target.equals(current)) {
				continue;
			}
			if (placeRoots(level, target, random)) {
				placed++;
			}
			current = target;
		}

		return placed > 0;
	}

	/**
	 * Turns a wanted position into one the roots can actually occupy: either it already hangs under a
	 * ceiling, or one of its immediate neighbours does.
	 */
	private BlockPos resolveHangingSpot(WorldGenLevel level, BlockPos wanted) {
		if (canPlace(level, wanted) && isCeiling(level, wanted.above())) {
			return wanted;
		}

		for (int dy = -NEIGHBOUR_REACH; dy <= NEIGHBOUR_REACH; dy++) {
			for (int dx = -NEIGHBOUR_REACH; dx <= NEIGHBOUR_REACH; dx++) {
				for (int dz = -NEIGHBOUR_REACH; dz <= NEIGHBOUR_REACH; dz++) {
					BlockPos spot = wanted.offset(dx, dy, dz);
					if (canPlace(level, spot) && isCeiling(level, spot.above())) {
						return spot;
					}
				}
			}
		}

		return null;
	}

	/**
	 * A ceiling is only a block whose lower face is solid. Roots are never a ceiling: like vanilla
	 * roots, Shinerite roots cannot hang from other roots, so strands never stack downwards.
	 */
	private boolean isCeiling(WorldGenLevel level, BlockPos pos) {
		if (level.isOutsideBuildHeight(pos.getY())) {
			return false;
		}
		return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.DOWN);
	}

	private boolean canPlace(WorldGenLevel level, BlockPos pos) {
		if (level.isOutsideBuildHeight(pos.getY())) {
			return false;
		}

		BlockState here = level.getBlockState(pos);
		if (!here.isAir() && !here.canBeReplaced()) {
			return false;
		}

		// Never hang inside lava, the patch would burn away the moment it is lit.
		FluidState fluid = here.getFluidState();
		if (fluid.is(Fluids.LAVA) || fluid.is(Fluids.FLOWING_LAVA)) {
			return false;
		}

		// The roots survive only while a solid block holds them from above.
		return isCeiling(level, pos.above());
	}

	private boolean placeRoots(WorldGenLevel level, BlockPos pos, RandomSource random) {
		BlockState placed = level.getFluidState(pos).getType() == Fluids.WATER
				? rootsBlock.defaultBlockState().setValue(ShineriteRootsBlock.WATERLOGGED, true)
				: rootsBlock.defaultBlockState();

		// Flag 2 only: no neighbour or client updates. This runs against a WorldGenLevel proto-chunk,
		// and BaseShineriteBlockEntity#sync is a no-op there since there is no ServerLevel to broadcast to.
		if (!level.setBlock(pos, placed, 2)) {
			return false;
		}

		if (!level.getBlockState(pos).is(rootsBlock)) {
			return false;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity)) {
			return false;
		}

		applyGenerationState(level, pos, shineriteBlockEntity, random);
		return true;
	}

	private void applyGenerationState(WorldGenLevel level, BlockPos pos, BaseShineriteBlockEntity blockEntity, RandomSource random) {
		MobEffect[] pool = nether ? NETHER_IMBUEMENT_EFFECTS : OVERWORLD_IMBUEMENT_EFFECTS;
		if (ModUtils.applyNaturalImbuement(blockEntity, random, pool, getImbuedChance())) {
			blockEntity.setChanged();
			return;
		}

		int[] primary = nether ? NETHER_RED_COLORS : OVERWORLD_WHITE_COLORS;
		int[] secondary = nether ? NETHER_YELLOW_COLORS : OVERWORLD_GREY_COLORS;

		if (nether && isWarpedForest(level, pos)) {
			primary = NETHER_WARPED_CYAN_COLORS;
			secondary = NETHER_WARPED_PURPLE_COLORS;
		}

		int[] palette = random.nextBoolean() ? primary : secondary;
		blockEntity.setColor(palette[random.nextInt(palette.length)]);
		blockEntity.setChanged();
	}

	/**
	 * Roots hanging inside a warped forest (or one of its basalt deltas) take the cool cyan/purple
	 * palette instead of the crimson red/gold one.
	 */
	private boolean isWarpedForest(WorldGenLevel level, BlockPos pos) {
		return level.getBiome(pos).is(Biomes.WARPED_FOREST);
	}

	private static int spread(RandomSource random, int radius) {
		return radius <= 0 ? 0 : random.nextInt(radius * 2 + 1) - radius;
	}

	private boolean isEnabled() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_PATCH.get() : WorldGenConfig.SHINERITE_ROOTS_PATCH.get();
	}

	private double getChance() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_PATCH_CHANCE.get() : WorldGenConfig.SHINERITE_ROOTS_PATCH_CHANCE.get();
	}

	private int getMinPatchSize() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_MIN_PATCH_SIZE.get() : WorldGenConfig.SHINERITE_ROOTS_MIN_PATCH_SIZE.get();
	}

	private int getMaxPatchSize() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_MAX_PATCH_SIZE.get() : WorldGenConfig.SHINERITE_ROOTS_MAX_PATCH_SIZE.get();
	}

	private int getMinRadius() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_MIN_RADIUS.get() : WorldGenConfig.SHINERITE_ROOTS_MIN_RADIUS.get();
	}

	private int getMaxRadius() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_MAX_RADIUS.get() : WorldGenConfig.SHINERITE_ROOTS_MAX_RADIUS.get();
	}

	private double getImbuedChance() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_IMBUED_CHANCE.get() : WorldGenConfig.SHINERITE_ROOTS_IMBUED_CHANCE.get();
	}
}