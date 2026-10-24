package net.jaams.shinerite.feature;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.jaams.shinerite.configuration.common.WorldGenConfig;
import net.jaams.shinerite.init.ModBlocks;

import com.mojang.serialization.Codec;



public class NetherShineriteColoredOreFeature extends AbstractShineriteColoredOreFeature {
	private static final int[] COLORS = {
			0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A,
			0xFFE06B, 0xFFE06B, 0xFFE06B, 0xFFE06B,
			0xF2C230, 0xF2C230, 0xF2C230, 0xF2C230,
			0xFFECB3, 0xFFECB3,
			0xE0A80D, 0xE0A80D
	};

	public NetherShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, ModBlocks.NETHER_SHINERITE_ORE.get().defaultBlockState(), COLORS, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_CHANCE::get, 0.5D,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_IMBUED_CHANCE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_COLORED_GENERATION::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MIN_SIZE::get, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MAX_SIZE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_SPREAD::get);
	}

	@Override
	protected boolean isValidGround(BlockState state) {
		return state.is(ModBlocks.NETHER_SHINERITE_ORE.get()) || state.is(Blocks.NETHERRACK);
	}
}
