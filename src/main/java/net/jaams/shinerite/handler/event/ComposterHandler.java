package net.jaams.shinerite.handler.event;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.block.ComposterBlock;

import net.jaams.shinerite.init.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ComposterHandler {
	@SubscribeEvent
	public static void onComposterRegister(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			ComposterBlock.COMPOSTABLES.put(ModItems.SHINY_ROOTS.get().asItem(), 0.25f);
		});
	}
}
