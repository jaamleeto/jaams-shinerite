package net.jaams.shinerite.block;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.component.block.BaseShineriteStairBlock;

public class CutShineriteStairsBlock extends BaseShineriteStairBlock {
	public CutShineriteStairsBlock() {
		super(() -> ModBlocks.CUT_SHINERITE.get().defaultBlockState(), BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops(), ModBlockEntities.CUT_SHINERITE_STAIRS);
	}

}
