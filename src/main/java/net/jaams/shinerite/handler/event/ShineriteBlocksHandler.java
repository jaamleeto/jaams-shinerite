package net.jaams.shinerite.handler.event;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.level.BlockEvent;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

@Mod.EventBusSubscriber
public class ShineriteBlocksHandler {
	@SubscribeEvent
	public static void onShineriteBlockBreak(BlockEvent.BreakEvent event) {
		//handleShineriteEvent(event.getState().getBlock(), event.getLevel(), event.getPos(), SoundEvents.AMETHYST_BLOCK_BREAK);
	}

	@SubscribeEvent
	public static void onShineriteBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		//handleShineriteEvent(event.getPlacedBlock().getBlock(), event.getLevel(), event.getPos(), SoundEvents.AMETHYST_BLOCK_PLACE);
	}

	private static void handleShineriteEvent(Block block, LevelAccessor levelAccessor, BlockPos pos, SoundEvent sound) {
		ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
		if (blockId == null || !blockId.toString().toLowerCase().contains("shinerite"))
			return;
		Level level = levelAccessor instanceof Level ? (Level) levelAccessor : null;
		if (level == null || level.isClientSide())
			return;
		level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
	}
}
