package net.jaams.shinerite.block;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.component.block.BaseShineriteBlock;

public class RawShineriteOreBlock extends BaseShineriteBlock {
	public RawShineriteOreBlock() {
		super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops(), ModBlockEntities.RAW_SHINERITE_BLOCK);
	}

}
