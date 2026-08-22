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

public class ShineriteRootsFeature extends Feature<NoneFeatureConfiguration> {
	private static final int[] WHITE_GRAY_COLORS = {0xFFFFFF, 0xD3D3D3, 0xA9A9A9, 0x808080};
	private final BlockState rootBlock;

	public ShineriteRootsFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
		this.rootBlock = ModBlocks.SHINERITE_ROOTS.get().defaultBlockState();
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		if (level == null) {
			return false;
		}
		BlockPos pos = context.origin();
		RandomSource random = context.random();
		if (!WorldGenConfig.SHINERITE_ROOTS.get()) {
			return false;
		}
		BlockState currentState = level.getBlockState(pos);
		if (!isValidCeiling(currentState)) {
			return false;
		}
		BlockPos belowPos = pos.below();
		BlockState belowState = level.getBlockState(belowPos);
		if (!belowState.isAir()) {
			return false;
		}
		int selectedColor = WHITE_GRAY_COLORS[random.nextInt(WHITE_GRAY_COLORS.length)];
		if (!placeRootWithColor(level, belowPos, selectedColor)) {
			return false;
		}
		int maxLength = 3 + random.nextInt(5);
		BlockPos currentPos = belowPos.below();
		for (int i = 0; i < maxLength - 1; i++) {
			if (!level.getBlockState(currentPos).isAir()) {
				break;
			}
			if (!placeRootWithColor(level, currentPos, selectedColor)) {
				break;
			}
			currentPos = currentPos.below();
		}
		return true;
	}

	private boolean placeRootWithColor(WorldGenLevel level, BlockPos pos, int color) {
		boolean placed = level.setBlock(pos, rootBlock, 2 | 16);
		if (!placed) {
			return false;
		}
		if (!level.getBlockState(pos).is(ModBlocks.SHINERITE_ROOTS.get())) {
			return false;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			shineriteBlockEntity.setColor(color);
			return true;
		} else {
			return false;
		}
	}

	private boolean isValidCeiling(BlockState state) {
		return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(ModBlocks.SHINERITE_ORE.get()) || state.is(ModBlocks.DEEPSLATE_SHINERITE_ORE.get());
	}
}
