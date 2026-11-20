package net.jaams.shinerite.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import net.jaams.shinerite.component.block.BaseShineriteVerticalSlabBlock;
import net.jaams.shinerite.init.ModBlockEntities;

public class CutShineriteVerticalSlabBlock extends BaseShineriteVerticalSlabBlock {
	public CutShineriteVerticalSlabBlock() {
		super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops(), ModBlockEntities.CUT_SHINERITE_VERTICAL_SLAB);
	}
}
