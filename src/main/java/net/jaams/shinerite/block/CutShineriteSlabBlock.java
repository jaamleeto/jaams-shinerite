package net.jaams.shinerite.block;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.component.block.BaseShineriteSlabBlock;

public class CutShineriteSlabBlock extends BaseShineriteSlabBlock {
	public CutShineriteSlabBlock() {
		super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops(), ModBlockEntities.CUT_SHINERITE_SLAB);
	}

}
