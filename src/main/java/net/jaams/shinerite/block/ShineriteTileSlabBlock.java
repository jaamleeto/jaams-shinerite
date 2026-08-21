package net.jaams.shinerite.block;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.component.block.BaseShineriteSlabBlock;

public class ShineriteTileSlabBlock extends BaseShineriteSlabBlock {
	public ShineriteTileSlabBlock() {
		super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops(), ModBlockEntities.SHINERITE_TILE_SLAB);
	}

}
