package net.jaams.shinerite.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.biome.Biomes;

import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.configuration.common.WorldGenConfig;
import net.jaams.shinerite.util.ModUtils;

import com.mojang.serialization.Codec;



public class NetherShineriteFlowerPatchFeature extends Feature<NoneFeatureConfiguration> {
	private static final int[] CRIMSON_COLORS = {0x7A0C10, 0x9E1B22, 0xC1272D, 0xE0332B, 0xF0563C, 0xB22222};
	private static final int[] WARPED_COLORS = {0x0F3D3A, 0x16685C, 0x1E8A6E, 0x2FA98A, 0x48C9A6, 0x2B4F7C};

	private static final MobEffect[] CRIMSON_IMBUEMENT_EFFECTS = {MobEffects.DAMAGE_BOOST, MobEffects.FIRE_RESISTANCE, MobEffects.WITHER};
	private static final MobEffect[] WARPED_IMBUEMENT_EFFECTS = {MobEffects.POISON, MobEffects.JUMP, MobEffects.DIG_SPEED};

	private final BlockState flowerBlock;

	public NetherShineriteFlowerPatchFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
		this.flowerBlock = ModBlocks.SHINERITIA.get().defaultBlockState();
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		if (level == null || !WorldGenConfig.NETHER_SHINERITIA_FLOWER_PATCH.get()) {
			return false;
		}

		RandomSource random = context.random();
		BlockPos origin = context.origin();

		boolean crimson = isForest(level, origin, Biomes.CRIMSON_FOREST);
		boolean warped = !crimson && isForest(level, origin, Biomes.WARPED_FOREST);

		if (random.nextFloat() > WorldGenConfig.NETHER_SHINERITIA_FLOWER_PATCH_CHANCE.get()) {
			return false;
		}

		BlockPos ground = findGround(level, origin, WorldGenConfig.NETHER_SHINERITIA_FLOWER_SEARCH_DEPTH.get());
		if (ground == null) {
			return false;
		}

		if (!crimson && !warped) {
			crimson = isForest(level, ground, Biomes.CRIMSON_FOREST);
			warped = !crimson && isForest(level, ground, Biomes.WARPED_FOREST);
		}

		if (!crimson && !warped) {
			return false;
		}

		if (!placeFlower(level, ground.above(), random, crimson)) {
			return false;
		}

		int extra = WorldGenConfig.rollInt(random, WorldGenConfig.NETHER_SHINERITIA_FLOWER_MIN_EXTRA,
				WorldGenConfig.NETHER_SHINERITIA_FLOWER_MAX_EXTRA);
		int radius = WorldGenConfig.rollInt(random, WorldGenConfig.NETHER_SHINERITIA_FLOWER_MIN_RADIUS,
				WorldGenConfig.NETHER_SHINERITIA_FLOWER_MAX_RADIUS);

		for (int i = 0; i < extra; i++) {
			int x = random.nextInt(radius * 2 + 1) - radius;
			int z = random.nextInt(radius * 2 + 1) - radius;
			if (x == 0 && z == 0) {
				continue;
			}

			BlockPos candidate = ground.above().offset(x, 0, z);
			if (!isPlantableSpot(level, candidate)) {
				BlockPos neighbourGround = findGround(level, candidate, WorldGenConfig.NETHER_SHINERITIA_FLOWER_SEARCH_DEPTH.get());
				if (neighbourGround == null) {
					continue;
				}
				candidate = neighbourGround.above();
			}

			placeFlower(level, candidate, random, crimson);
		}

		return true;
	}

	private static boolean isForest(WorldGenLevel level, BlockPos pos, ResourceKey<Biome> biome) {
		return level.getBiome(pos).is(biome);
	}



	private BlockPos findGround(WorldGenLevel level, BlockPos pos, int searchDepth) {
		int top = Math.min(pos.getY() + searchDepth, level.getMaxBuildHeight() - 2);
		int bottom = Math.max(level.getMinBuildHeight(), pos.getY() - searchDepth * 2);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), top, pos.getZ());

		for (int y = top; y >= bottom; y--) {
			cursor.setY(y);
			if (isPlantableSpot(level, cursor.immutable())) {
				return cursor.below();
			}
		}

		return null;
	}

	private boolean isPlantableSpot(WorldGenLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || ShineriteFlowerGround.canReplaceGroundCover(level, pos);
	}

	private boolean placeFlower(WorldGenLevel level, BlockPos pos, RandomSource random, boolean crimson) {
		if (!isPlantableSpot(level, pos) || !ShineriteFlowerGround.isValidGround(level, pos.below()) || !flowerBlock.canSurvive(level, pos)) {
			return false;
		}

		if (!level.setBlock(pos, flowerBlock, 2)) {
			return false;
		}

		if (!level.getBlockState(pos).is(ModBlocks.SHINERITIA.get())) {
			return false;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity)) {
			return false;
		}

		double imbuedChance = crimson ? WorldGenConfig.NETHER_CRIMSON_FLOWER_IMBUED_CHANCE.get()
				: WorldGenConfig.NETHER_WARPED_FLOWER_IMBUED_CHANCE.get();

		if (ModUtils.applyNaturalImbuement(shineriteBlockEntity, random, crimson ? CRIMSON_IMBUEMENT_EFFECTS : WARPED_IMBUEMENT_EFFECTS, imbuedChance)) {
			shineriteBlockEntity.setChanged();
			return true;
		}

		int[] palette = crimson ? CRIMSON_COLORS : WARPED_COLORS;
		shineriteBlockEntity.setColor(palette[random.nextInt(palette.length)]);
		shineriteBlockEntity.setChanged();
		return true;
	}
}
