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
		int selectedColor = COLORS[random.nextInt(COLORS.length)];
		boolean placed = false;
		int attempts = 6 + random.nextInt(5);
		for (int i = 0; i < attempts; i++) {
			BlockPos targetPos = pos.offset(
				random.nextInt(3) - 1 + random.nextInt(3) - 1,
				random.nextInt(3) - 1 + random.nextInt(3) - 1,
				random.nextInt(3) - 1 + random.nextInt(3) - 1
			);
			if (isValidGround(level.getBlockState(targetPos))) {
				if (placeOreWithColor(level, targetPos, selectedColor)) {
					placed = true;
				}
			}
		}
		return placed;
	}

	private boolean isValidGround(BlockState state) {
		return state.is(ModBlocks.NETHER_SHINERITE_ORE.get()) || state.is(Blocks.NETHERRACK) || state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE);
	}

	private boolean placeOreWithColor(WorldGenLevel level, BlockPos pos, int color) {
		if (!level.setBlock(pos, oreBlock, 2 | 16)) {
			return false;
		}
		if (!level.getBlockState(pos).is(ModBlocks.NETHER_SHINERITE_ORE.get())) {
			return false;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			shineriteBlockEntity.setColor(color);
			return true;
		}
		return false;
	}
}
