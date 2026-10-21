package net.jaams.shinerite.feature;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.jaams.shinerite.configuration.common.WorldGenConfig;
import net.jaams.shinerite.init.ModBlocks;

import com.mojang.serialization.Codec;

/**
 * Deepslate depth palette: dominated by diamond and emerald mimics (cyan, green and blue) with
 * gold as the secondary mimic, so a deepslate vein reads as a more valuable seam than the stone one.
 */
public class DeepslateShineriteColoredOreFeature extends AbstractShineriteColoredOreFeature {
	private static final int[] COLORS = {
			0x5BE3E8, 0x5BE3E8, 0x5BE3E8, 0x5BE3E8, 0x5BE3E8, 0x5BE3E8, 0x5BE3E8, 0x5BE3E8, 0x5BE3E8,
			0x36C46B, 0x36C46B, 0x36C46B, 0x36C46B, 0x36C46B, 0x36C46B, 0x36C46B, 0x36C46B, 0x36C46B,
			0x2E7FD6, 0x2E7FD6, 0x2E7FD6, 0x2E7FD6, 0x2E7FD6, 0x2E7FD6, 0x2E7FD6,
			0xFED65A, 0xFED65A, 0xFED65A, 0xFED65A, 0xFED65A, 0xFED65A,
			0x1F7FA8, 0x1F7FA8, 0x1F7FA8,
			0xFFFFFF, 0xFFFFFF, 0xFFFFFF,
			0x6E6E6E, 0x6E6E6E,
			0x151515, 0x151515
	};

	public DeepslateShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, ModBlocks.DEEPSLATE_SHINERITE_ORE.get().defaultBlockState(), COLORS, WorldGenConfig.SHINERITE_COLORED_ORE::get,
				WorldGenConfig.SHINERITE_COLORED_ORE_CHANCE::get, OVERWORLD_COLORED_CHANCE::get,
				WorldGenConfig.SHINERITE_COLORED_ORE_MIN_SIZE::get, WorldGenConfig.SHINERITE_COLORED_ORE_MAX_SIZE::get,
				WorldGenConfig.SHINERITE_COLORED_ORE_SPREAD::get);
	}

	/** Same balance as the stone variant: 70% plain, 30% mimics. */
	private static final java.util.function.Supplier<Double> OVERWORLD_COLORED_CHANCE = () -> 0.3D;

	@Override
	protected boolean isValidGround(BlockState state) {
		return state.is(ModBlocks.DEEPSLATE_SHINERITE_ORE.get()) || state.is(Blocks.DEEPSLATE);
	}
}