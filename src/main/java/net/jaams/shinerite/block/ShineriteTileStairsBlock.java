package net.jaams.shinerite.block;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.component.block.BaseShineriteStairBlock;

public class ShineriteTileStairsBlock extends BaseShineriteStairBlock {
	public ShineriteTileStairsBlock() {
		super(() -> ModBlocks.SHINERITE_TILES.get().defaultBlockState(), BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops(), ModBlockEntities.SHINERITE_TILE_STAIRS);
	}

}
