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
			// bright gold, the default nether mimic
			0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A, 0xFFD24A,
			// lighter golden tones
			0xFFE06B, 0xFFE06B, 0xFFE06B, 0xFFE06B,
			// deeper yolks
			0xF2C230, 0xF2C230, 0xF2C230, 0xF2C230,
			// pale champagne and dark old gold as accents
			0xFFECB3, 0xFFECB3,
			0xE0A80D, 0xE0A80D
	};

	public NetherShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, ModBlocks.NETHER_SHINERITE_ORE.get().defaultBlockState(), COLORS, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_CHANCE::get, COLORED_CHANCE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MIN_SIZE::get, WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_MAX_SIZE::get,
				WorldGenConfig.NETHER_SHINERITE_COLORED_ORE_SPREAD::get);
	}

	/** Even split: half the nether veins stay uncoloured, half come out in yellow tones. */
	private static final java.util.function.Supplier<Double> COLORED_CHANCE = () -> 0.5D;

	@Override
	protected boolean isValidGround(BlockState state) {
		// Only ever grow inside netherrack: replacing basalt / blackstone makes blobs surface on the
		// ceilings of biomes like basalt deltas and clash with the surrounding terrain.
		return state.is(ModBlocks.NETHER_SHINERITE_ORE.get()) || state.is(Blocks.NETHERRACK);
	}
}