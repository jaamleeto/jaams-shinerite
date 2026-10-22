package net.jaams.shinerite.feature;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.jaams.shinerite.configuration.common.WorldGenConfig;
import net.jaams.shinerite.init.ModBlocks;

import com.mojang.serialization.Codec;



public class ShineriteColoredOreFeature extends AbstractShineriteColoredOreFeature {
	private static final int[] COLORS = {
			0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,
			0x0D0D0D, 0x0D0D0D, 0x0D0D0D, 0x0D0D0D, 0x0D0D0D, 0x0D0D0D, 0x0D0D0D, 0x0D0D0D,
			0xC26B3A, 0xC26B3A, 0xC26B3A, 0xC26B3A, 0xC26B3A, 0xC26B3A,
			0xFFC42D, 0xFFC42D, 0xFFC42D, 0xFFC42D,
			0xD9D9D9, 0xD9D9D9, 0xD9D9D9, 0xD9D9D9,
			0xB4B4B4, 0xB4B4B4, 0xB4B4B4, 0xB4B4B4,
			0x8C8C8C, 0x8C8C8C, 0x8C8C8C,
			0x666666, 0x666666, 0x666666,
			0x3F3F3F, 0x3F3F3F,
			0x1C1C1C, 0x1C1C1C,
			0xD03A2A, 0xD03A2A, 0xD03A2A,
			0x3E9B4F, 0x3E9B4F,
			0x2F4FB5, 0x2F4FB5,
			0x46B7C9, 0x46B7C9,
			0xB44FC4
	};

	public ShineriteColoredOreFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, ModBlocks.SHINERITE_ORE.get().defaultBlockState(), COLORS, WorldGenConfig.SHINERITE_COLORED_ORE::get,
				WorldGenConfig.SHINERITE_COLORED_ORE_CHANCE::get, OVERWORLD_COLORED_CHANCE::get,
				WorldGenConfig.SHINERITE_COLORED_ORE_MIN_SIZE::get, WorldGenConfig.SHINERITE_COLORED_ORE_MAX_SIZE::get,
				WorldGenConfig.SHINERITE_COLORED_ORE_SPREAD::get);
	}


	private static final java.util.function.Supplier<Double> OVERWORLD_COLORED_CHANCE = () -> 0.3D;

	@Override
	protected boolean isValidGround(BlockState state) {
		return state.is(ModBlocks.SHINERITE_ORE.get()) || state.is(Blocks.STONE);
	}
}
