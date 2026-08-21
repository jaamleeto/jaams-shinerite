package net.jaams.shinerite.registry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.jaams.shinerite.component.block.BaseShineriteBlock;
import net.jaams.shinerite.init.ModBlocks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BlockColorRegister {
	@SubscribeEvent
	public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.SHINERITE_BLOCK);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.SHINERITE_ORE);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.DEEPSLATE_SHINERITE_ORE);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.NETHER_SHINERITE_ORE);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.RAW_SHINERITE_BLOCK);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.CUT_SHINERITE);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.CUT_SHINERITE_STAIRS);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.CUT_SHINERITE_SLAB);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.SHINERITE_TILES);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.SHINERITE_TILE_STAIRS);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.SHINERITE_TILE_SLAB);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.SHINERITIA);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.POTTED_SHINERITIA);
		BaseShineriteBlock.registerBlockColor(event, ModBlocks.SHINERITE_ROOTS);
	}

	@SubscribeEvent
	public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
		BaseShineriteBlock.registerItemColor(event, ModBlocks.SHINERITE_BLOCK);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.SHINERITE_ORE);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.DEEPSLATE_SHINERITE_ORE);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.NETHER_SHINERITE_ORE);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.RAW_SHINERITE_BLOCK);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.CUT_SHINERITE);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.CUT_SHINERITE_STAIRS);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.CUT_SHINERITE_SLAB);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.SHINERITE_TILES);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.SHINERITE_TILE_STAIRS);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.SHINERITE_TILE_SLAB);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.SHINERITIA);
		BaseShineriteBlock.registerItemColor(event, ModBlocks.SHINERITE_ROOTS);
	}
}