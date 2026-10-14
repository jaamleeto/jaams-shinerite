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

import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.configuration.common.WorldGenConfig;

import com.mojang.serialization.Codec;

public class NetherShineriteColoredOreFeature extends Feature<NoneFeatureConfiguration> {
	private static final int[] COLORS = {0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF, 0xFFFFFF};
	private final BlockState oreBlock;

	public NetherShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
		this.oreBlock = ModBlocks.NETHER_SHINERITE_ORE.get().defaultBlockState();
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		if (level == null) {
			return false;
		}
		BlockPos pos = context.origin();
		RandomSource random = context.random();
		if (!WorldGenConfig.NETHER_SHINERITE_COLORED_ORE.get()) {
			return false;
		}
		if (random.nextFloat() >= WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_CHANCE.get().floatValue()) {
			return false;
		}
		int selectedColor = COLORS[random.nextInt(COLORS.length)];
		boolean placed = false;
		int attempts = WorldGenConfig.rollInt(random, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MIN_SIZE, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MAX_SIZE);
		int spread = WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_SPREAD.get();
		for (int i = 0; i < attempts; i++) {
			BlockPos targetPos = pos.offset(
				randomOffset(random, spread),
				randomOffset(random, spread),
				randomOffset(random, spread)
			);
			if (isValidGround(level.getBlockState(targetPos))) {
				if (placeOreWithColor(level, targetPos, selectedColor)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private int randomOffset(RandomSource random, int spread) {
		return spread <= 0 ? 0 : random.nextInt(spread * 2 + 1) - spread;
	}

	private boolean isValidGround(BlockState state) {
		return state.is(ModBlocks.NETHER_SHINERITE_ORE.get()) || state.is(Blocks.NETHERRACK) || state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE);
	}

	private boolean placeOreWithColor(WorldGenLevel level, BlockPos pos, int color) {
		if (!level.setBlock(pos, oreBlock, 3)) {
			return false;
		}
		if (!level.getBlockState(pos).is(ModBlocks.NETHER_SHINERITE_ORE.get())) {
			return false;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			shineriteBlockEntity.setColor(color);
			shineriteBlockEntity.setChanged();
			if (!level.isClientSide()) {
				shineriteBlockEntity.sync();
			}
			return true;
		}
		return false;
	}
}
