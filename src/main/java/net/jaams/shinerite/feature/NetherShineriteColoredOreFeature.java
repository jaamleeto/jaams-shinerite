package net.jaams.shinerite.feature;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.jaams.shinerite.configuration.common.WorldGenConfig;
import net.jaams.shinerite.init.ModBlocks;

import com.mojang.serialization.Codec;

/**
 * Nether palette: dominated by the hot end of the spectrum, yellow / orange / red, with the
 * uncolored white variant reduced to a rarity.
 */
public class NetherShineriteColoredOreFeature extends AbstractShineriteColoredOreFeature {
	private static final int[] COLORS = {
			// yellow
			0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A,
			// orange
			0xFF9B2F, 0xFF9B2F, 0xFF9B2F, 0xFF9B2F, 0xFF9B2F, 0xFF9B2F, 0xFF9B2F, 0xFF9B2F, 0xFF9B2F, 0xFF9B2F,
			// red
			0xE0432A, 0xE0432A, 0xE0432A, 0xE0432A, 0xE0432A, 0xE0432A, 0xE0432A, 0xE0432A, 0xE0432A, 0xE0432A,
			// deep orange
			0xFF6A00, 0xFF6A00, 0xFF6A00, 0xFF6A00, 0xFF6A00, 0xFF6A00,
			// uncolored, now a rarity
			0xFFFFFF
	};

	public NetherShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, ModBlocks.NETHER_SHINERITE_ORE.get().defaultBlockState(), COLORS, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_CHANCE::get, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MIN_SIZE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MAX_SIZE::get, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_SPREAD::get);
	}

	@Override
	protected boolean isValidGround(BlockState state) {
		return state.is(ModBlocks.NETHER_SHINERITE_ORE.get()) || state.is(Blocks.NETHERRACK) || state.is(Blocks.BASALT) || state.is(Blocks.BLACKSTONE);
	}
}