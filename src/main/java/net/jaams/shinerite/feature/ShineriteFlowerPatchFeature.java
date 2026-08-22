package net.jaams.shinerite.feature;

import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.configuration.common.WorldGenConfig;

import com.mojang.serialization.Codec;

public class ShineriteFlowerPatchFeature extends Feature<NoneFeatureConfiguration> {
	private static final int[] COLORS = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF, 0xFF77C8, 0x99FF99, 0xFFB366};
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
		BlockPos groundPos = findValidGround(level, pos);
		if (groundPos == null) {
			return false;
		}
		BlockPos basePos = groundPos.above();
		if (!placeFlowerWithRandomColor(level, basePos, random)) {
			return false;
		}
		int flowerCount = 1 + random.nextInt(4);
		int radius = 1 + random.nextInt(2);
		for (int i = 0; i < flowerCount; i++) {
			int x = random.nextInt(radius * 2 + 1) - radius;
			int z = random.nextInt(radius * 2 + 1) - radius;
			if (x == 0 && z == 0) {
				continue;
			}
			BlockPos checkPos = basePos.offset(x, 0, z);
			BlockState below = level.getBlockState(checkPos.below());
			if (!isValidGround(below) || !level.getBlockState(checkPos).isAir()) {
				continue;
			}
			placeFlowerWithRandomColor(level, checkPos, random);
		}
		return true;
	}

	private BlockPos findValidGround(WorldGenLevel level, BlockPos pos) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
		for (int i = 0; i < 8; i++) {
			BlockState state = level.getBlockState(mutable);
			if (isValidGround(state) && level.getBlockState(mutable.above()).isAir()) {
				return mutable.immutable();
			}
			mutable.move(Direction.DOWN);
		}
		return null;
	}

	private boolean isValidGround(BlockState state) {
		return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL);
	}

	private boolean placeFlowerWithRandomColor(WorldGenLevel level, BlockPos pos, RandomSource random) {
		if (!level.setBlock(pos, flowerBlock, 2 | 16)) {
			return false;
		}
		if (!level.getBlockState(pos).is(ModBlocks.SHINERITIA.get())) {
			return false;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			shineriteBlockEntity.setColor(COLORS[random.nextInt(COLORS.length)]);
			return true;
		}
		return false;
	}
}
