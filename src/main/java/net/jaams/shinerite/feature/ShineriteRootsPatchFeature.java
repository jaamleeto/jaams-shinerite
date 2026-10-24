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



public class ShineriteRootsPatchFeature extends Feature<NoneFeatureConfiguration> {
	private static final int[] NETHER_RED_COLORS = {0x7A0C10, 0x9E1B22, 0xC1272D, 0xE0332B, 0xB22222, 0x8E2C2C};

	private static final int[] NETHER_CRIMSON_BURGUNDY_COLORS = {0x5C0A0E, 0x741016, 0x8E1418, 0xA81C1C, 0x6B1218, 0x992226};

	private static final int[] NETHER_WARPED_CYAN_COLORS = {0x0F3D3A, 0x16685C, 0x1E8A6E, 0x2FA98A, 0x48C9A6, 0x27C9D6, 0x4FD9E6};
	private static final int[] NETHER_WARPED_PURPLE_COLORS = {0x2B1B4D, 0x3E2A6B, 0x54348C, 0x6B44AD, 0x8059C9, 0x9A72E0};

	private static final int[] NETHER_ASH_GREY_COLORS = {0x6E6E6E, 0x828282, 0x9A9A9A, 0x565656, 0x757575, 0xB0B0B0};
	private static final int[] NETHER_ASH_DARK_COLORS = {0x3A3A3A, 0x4B4B4B, 0x2E2E2E, 0x616161, 0x444444};

	private static final int[] NETHER_CRIMSON_RED_COLORS = {0x8B0E14, 0xA31A21, 0xBC2A2A, 0x7A0C10, 0x9E1B22, 0xB22222};
	private static final int[] NETHER_CRIMSON_PINK_COLORS = {0xE0537A, 0xD97FA6, 0xC94F6B, 0xE86C96, 0xB54360, 0xF07FA8};

	private static final int[] NETHER_SPECTRAL_CYAN_COLORS = {0x3FC9C9, 0x62E0E0, 0x8FF2F2, 0x4FB3D9, 0x6FD6F0, 0xA6F0F0};
	private static final int[] NETHER_SPECTRAL_BLUE_COLORS = {0x2C5ED9, 0x4A7BE0, 0x7AA1F0, 0x1F4099, 0x5C8CD9, 0x9FC0F2};
	private static final int[] OVERWORLD_WHITE_COLORS = {0xFAFAFA, 0xEFEFEF, 0xE0E0E0, 0xD0D0D0, 0xF5F5F5};
	private static final int[] OVERWORLD_GREY_COLORS = {0xB0B0B0, 0x9E9E9E, 0x8C8C8C, 0x757575, 0x616161};
	private static final MobEffect[] NETHER_IMBUEMENT_EFFECTS = {MobEffects.FIRE_RESISTANCE, MobEffects.DAMAGE_BOOST, MobEffects.GLOWING, MobEffects.WITHER};
	private static final MobEffect[] OVERWORLD_IMBUEMENT_EFFECTS = {MobEffects.POISON, MobEffects.REGENERATION, MobEffects.NIGHT_VISION, MobEffects.DIG_SPEED};

	private static final int NEIGHBOUR_REACH = 2;


	private static final int ORE_SEARCH_RADIUS = 4;
	private static final int ORE_SEARCH_DEPTH = 6;

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
		BlockPos oreCeiling = findShineriteOreCeiling(level, context.origin());
		BlockPos ceiling = oreCeiling != null ? oreCeiling : context.origin();

		boolean anchoredToOre = oreCeiling != null;
		if (!anchoredToOre && random.nextFloat() > getChance()) {
			return false;
		}

		if (!isCeiling(level, ceiling)) {
			return false;
		}

		BlockPos anchor = ceiling.below();
		if (!canPlace(level, anchor)) {
			return anchoredToOre;
		}

		int patchColor = anchoredToOre ? readOreColor(level, oreCeiling) : -1;

		int size = WorldGenConfig.rollInt(random, getMinPatchSize(), getMaxPatchSize());
		int radius = WorldGenConfig.rollInt(random, getMinRadius(), getMaxRadius());

		int placed = 0;
		BlockPos current = anchor;
		if (placeRoots(level, current, random, ceilingColor(level, current.above(), patchColor))) {
			placed++;
		}

		int attempts = Math.max(8, size * NEIGHBOUR_REACH * 4);
		for (int i = 0; i < attempts && placed < size; i++) {
			BlockPos candidate = current.offset(spread(random, radius), random.nextInt(3) - 1, spread(random, radius));
			BlockPos target = resolveHangingSpot(level, candidate);
			if (target == null || target.equals(current)) {
				continue;
			}
			if (placeRoots(level, target, random, ceilingColor(level, target.above(), patchColor))) {
				placed++;
			}
			current = target;
		}

		return placed > 0;
	}



	private BlockPos findShineriteOreCeiling(WorldGenLevel level, BlockPos origin) {
		for (int dy = -ORE_SEARCH_DEPTH; dy <= ORE_SEARCH_DEPTH; dy++) {
			for (int dx = -ORE_SEARCH_RADIUS; dx <= ORE_SEARCH_RADIUS; dx++) {
				for (int dz = -ORE_SEARCH_RADIUS; dz <= ORE_SEARCH_RADIUS; dz++) {
					BlockPos ceiling = origin.offset(dx, dy, dz);
					if (!isShineriteOre(level.getBlockState(ceiling))) {
						continue;
					}
					BlockPos below = ceiling.below();
					if (!level.isOutsideBuildHeight(below.getY()) && canPlace(level, below)) {
						return ceiling;
					}
				}
			}
		}
		return null;
	}

	private static boolean isShineriteOre(BlockState state) {
		return state.is(ModBlocks.SHINERITE_ORE.get()) || state.is(ModBlocks.DEEPSLATE_SHINERITE_ORE.get())
				|| state.is(ModBlocks.NETHER_SHINERITE_ORE.get());
	}



	private int ceilingColor(WorldGenLevel level, BlockPos ceilingPos, int fallback) {
		int oreColor = readOreColor(level, ceilingPos);
		return oreColor != -1 ? oreColor : fallback;
	}

	private int readOreColor(WorldGenLevel level, BlockPos ceilingPos) {
		if (!isShineriteOre(level.getBlockState(ceilingPos))) {
			return -1;
		}
		BlockEntity blockEntity = level.getBlockEntity(ceilingPos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			return shineriteBlockEntity.getColor();
		}
		return -1;
	}



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

		FluidState fluid = here.getFluidState();
		if (fluid.is(Fluids.LAVA) || fluid.is(Fluids.FLOWING_LAVA)) {
			return false;
		}

		return isCeiling(level, pos.above());
	}

	private boolean placeRoots(WorldGenLevel level, BlockPos pos, RandomSource random, int color) {
		BlockState placed = level.getFluidState(pos).getType() == Fluids.WATER
				? rootsBlock.defaultBlockState().setValue(ShineriteRootsBlock.WATERLOGGED, true)
				: rootsBlock.defaultBlockState();

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

		applyGenerationState(level, pos, shineriteBlockEntity, random, color);
		return true;
	}

	private void applyGenerationState(WorldGenLevel level, BlockPos pos, BaseShineriteBlockEntity blockEntity, RandomSource random, int inheritedColor) {
		MobEffect[] pool = nether ? NETHER_IMBUEMENT_EFFECTS : OVERWORLD_IMBUEMENT_EFFECTS;
		if (ModUtils.applyNaturalImbuement(blockEntity, random, pool, getImbuedChance())) {
			blockEntity.setChanged();
			return;
		}

		if (isColoredGeneration()) {
			if (inheritedColor != -1 && !isBasaltDeltas(level, pos)) {
				blockEntity.setColor(inheritedColor);
				blockEntity.setChanged();
				return;
			}

			int[] primary = nether ? NETHER_RED_COLORS : OVERWORLD_WHITE_COLORS;
			int[] secondary = nether ? NETHER_CRIMSON_BURGUNDY_COLORS : OVERWORLD_GREY_COLORS;

			if (nether && isWarpedForest(level, pos)) {
				primary = NETHER_WARPED_CYAN_COLORS;
				secondary = NETHER_WARPED_PURPLE_COLORS;
			}

			if (nether && isBasaltDeltas(level, pos)) {
				primary = NETHER_ASH_GREY_COLORS;
				secondary = NETHER_ASH_DARK_COLORS;
			}

			if (nether && isCrimsonForest(level, pos)) {
				primary = NETHER_CRIMSON_RED_COLORS;
				secondary = NETHER_CRIMSON_PINK_COLORS;
			}

			if (nether && isSoulSandValley(level, pos)) {
				primary = NETHER_SPECTRAL_CYAN_COLORS;
				secondary = NETHER_SPECTRAL_BLUE_COLORS;
			}

			int[] palette = random.nextBoolean() ? primary : secondary;
			blockEntity.setColor(palette[random.nextInt(palette.length)]);
			blockEntity.setChanged();
		}
	}


	private boolean isWarpedForest(WorldGenLevel level, BlockPos pos) {
		return level.getBiome(pos).is(Biomes.WARPED_FOREST);
	}

	private boolean isBasaltDeltas(WorldGenLevel level, BlockPos pos) {
		return level.getBiome(pos).is(Biomes.BASALT_DELTAS);
	}

	private boolean isCrimsonForest(WorldGenLevel level, BlockPos pos) {
		return level.getBiome(pos).is(Biomes.CRIMSON_FOREST);
	}

	private boolean isSoulSandValley(WorldGenLevel level, BlockPos pos) {
		return level.getBiome(pos).is(Biomes.SOUL_SAND_VALLEY);
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

	private boolean isColoredGeneration() {
		return nether ? WorldGenConfig.NETHER_SHINERITE_ROOTS_COLORED_GENERATION.get() : WorldGenConfig.SHINERITE_ROOTS_COLORED_GENERATION.get();
	}
}
