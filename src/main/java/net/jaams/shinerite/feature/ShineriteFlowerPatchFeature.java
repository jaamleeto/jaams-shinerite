package net.jaams.shinerite.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.configuration.common.WorldGenConfig;
import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.util.ModUtils;

import com.mojang.serialization.Codec;



public class ShineriteFlowerPatchFeature extends Feature<NoneFeatureConfiguration> {

	private static final int[] COLD_COLORS = {0x8FA6BC, 0xBFD3E6, 0xF2F6FA, 0x9AA5B1, 0x5B6B7C, 0xD6E4F0};
	private static final int[] CHERRY_COLORS = {0xFFB7D5, 0xFFCCE0, 0xF7CAD0, 0xFFE4EC, 0xE58BB1, 0xFDD7E4};
	private static final int[] FLORAL_COLORS = {0xFF4444, 0xFF77C8, 0xFFD700, 0xFF9933, 0xFF55FF, 0xFFB347};
	private static final int[] DEFAULT_COLORS = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0xFF77C8, 0x99FF99, 0xFFB366};

	private static final int[] BIRCH_COLORS = {0xFFF3B0, 0xFFE282, 0xE8D08C, 0xDCC184, 0xF7E9C4, 0xCDB377};

	private static final int[] DARK_FOREST_COLORS = {0x38598C, 0x4A6FA5, 0x2C4066, 0x6B4B3A, 0x8C6239, 0x5C4432};

	private static final int[] JUNGLE_COLORS = {0x4CC94C, 0x2FA84F, 0x7CD97C, 0x9C6BD1, 0x6E44B5, 0x39C97A};


	private static final MobEffect[] COLD_IMBUEMENT_EFFECTS = {MobEffects.DAMAGE_RESISTANCE, MobEffects.MOVEMENT_SLOWDOWN};
	private static final MobEffect[] CHERRY_IMBUEMENT_EFFECTS = {MobEffects.REGENERATION, MobEffects.HEAL};
	private static final MobEffect[] FLORAL_IMBUEMENT_EFFECTS = {MobEffects.LUCK, MobEffects.REGENERATION, MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED};
	private static final MobEffect[] DEFAULT_IMBUEMENT_EFFECTS = {MobEffects.REGENERATION, MobEffects.POISON, MobEffects.DIG_SPEED, MobEffects.LUCK, MobEffects.LEVITATION};

	private static final MobEffect[] JUNGLE_IMBUEMENT_EFFECTS = {MobEffects.POISON};
	private static final double JUNGLE_IMBUEMENT_MULTIPLIER = 5.0D;


	private final BlockState flowerBlock;

	public ShineriteFlowerPatchFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
		this.flowerBlock = ModBlocks.SHINERITIA.get().defaultBlockState();
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		if (level == null) {
			return false;
		}
		BlockPos pos = context.origin();
		RandomSource random = context.random();
		if (!WorldGenConfig.SHINERITIA_FLOWER_PATCH.get()) {
			return false;
		}
		if (random.nextFloat() > WorldGenConfig.SHINERITIA_FLOWER_PATCH_CHANCE.get().floatValue()) {
			return false;
		}
		FlowerPalette palette = resolvePalette(level, pos);
		if (palette == null) {
			return false;
		}
		BlockPos groundPos = findValidGround(level, pos, WorldGenConfig.SHINERITIA_FLOWER_SEARCH_DEPTH.get());
		if (groundPos == null) {
			return false;
		}
		BlockPos basePos = groundPos.above();
		if (!placeFlower(level, basePos, random, palette)) {
			return false;
		}
		int flowerCount = WorldGenConfig.rollInt(random, WorldGenConfig.SHINERITIA_FLOWER_MIN_EXTRA, WorldGenConfig.SHINERITIA_FLOWER_MAX_EXTRA);
		int radius = WorldGenConfig.rollInt(random, WorldGenConfig.SHINERITIA_FLOWER_MIN_RADIUS, WorldGenConfig.SHINERITIA_FLOWER_MAX_RADIUS);
		for (int i = 0; i < flowerCount; i++) {
			int x = random.nextInt(radius * 2 + 1) - radius;
			int z = random.nextInt(radius * 2 + 1) - radius;
			if (x == 0 && z == 0) {
				continue;
			}
			BlockPos checkPos = basePos.offset(x, 0, z);
			if (!isPlantableSpot(level, checkPos) || !ShineriteFlowerGround.isValidGround(level, checkPos.below()) || !flowerBlock.canSurvive(level, checkPos)) {
				continue;
			}
			placeFlower(level, checkPos, random, palette);
		}
		return true;
	}


	private FlowerPalette resolvePalette(WorldGenLevel level, BlockPos pos) {
		ResourceKey<Biome> key = level.getBiome(pos).unwrapKey().orElse(null);
		if (key == null) {
			return null;
		}
		String name = key.location().getPath();
		switch (name) {
			case "cherry_grove":
				return new FlowerPalette(CHERRY_COLORS, CHERRY_IMBUEMENT_EFFECTS, 1.0D);
			case "flower_forest":
				return new FlowerPalette(FLORAL_COLORS, FLORAL_IMBUEMENT_EFFECTS, 1.0D);
			case "taiga":
				return new FlowerPalette(COLD_COLORS, COLD_IMBUEMENT_EFFECTS, 1.0D);
			case "plains":
			case "forest":
			case "meadow":
				return new FlowerPalette(DEFAULT_COLORS, DEFAULT_IMBUEMENT_EFFECTS, 1.0D);
			case "birch_forest":
			case "old_growth_birch_forest":
				return new FlowerPalette(BIRCH_COLORS, DEFAULT_IMBUEMENT_EFFECTS, 1.0D);
			case "dark_forest":
				return new FlowerPalette(DARK_FOREST_COLORS, DEFAULT_IMBUEMENT_EFFECTS, 1.0D);
			case "jungle":
			case "bamboo_jungle":
				return new FlowerPalette(JUNGLE_COLORS, JUNGLE_IMBUEMENT_EFFECTS, JUNGLE_IMBUEMENT_MULTIPLIER);
			default:
				return null;
		}
	}

	private BlockPos findValidGround(WorldGenLevel level, BlockPos pos, int searchDepth) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
		for (int i = 0; i < searchDepth; i++) {
			BlockPos flowerPos = mutable.above();
			if (isPlantableSpot(level, flowerPos) && ShineriteFlowerGround.isValidGround(level, mutable)) {
				return mutable.immutable();
			}
			mutable.move(Direction.DOWN);
		}
		return null;
	}

	private boolean isPlantableSpot(WorldGenLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || ShineriteFlowerGround.canReplaceGroundCover(level, pos);
	}

	private boolean placeFlower(WorldGenLevel level, BlockPos pos, RandomSource random, FlowerPalette palette) {
		if (!level.setBlock(pos, flowerBlock, 2)) {
			return false;
		}
		if (!level.getBlockState(pos).is(ModBlocks.SHINERITIA.get())) {
			return false;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			double rate = WorldGenConfig.SHINERITIA_FLOWER_IMBUED_CHANCE.get() * palette.rate;
			if (ModUtils.applyNaturalImbuement(shineriteBlockEntity, random, palette.effects, rate)) {
				shineriteBlockEntity.setChanged();
				return true;
			}
			shineriteBlockEntity.setColor(palette.colors[random.nextInt(palette.colors.length)]);
			shineriteBlockEntity.setChanged();
			return true;
		}
		return false;
	}


	private record FlowerPalette(int[] colors, MobEffect[] effects, double rate) {
	}
}
