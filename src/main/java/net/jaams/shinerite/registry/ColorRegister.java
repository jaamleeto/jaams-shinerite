
package net.jaams.shinerite.registry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.init.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorRegister {
	@SubscribeEvent
	public static void onItemColorRegister(RegisterColorHandlersEvent.Item event) {
		event.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((IDyeableItem) stack.getItem()).getColor(stack), ModItems.SHINERITE_SWORD.get(), ModItems.SHINERITE_AXE.get(), ModItems.SHINERITE_PICKAXE.get(),
				ModItems.SHINERITE_SHOVEL.get(), ModItems.SHINERITE_HOE.get(), ModItems.SHINERANG.get(), ModItems.SHINERITE_STAR.get(), ModItems.SHINY_PAINT_BRUSH.get(), ModItems.SHINY_SCRAPER.get(),
				ModItems.SHINERITE_INGOT.get(), ModItems.RAW_SHINERITE.get(), ModItems.SHINERITE_NUGGET.get(), ModItems.GLOW_SALAD.get(), ModItems.MUSIC_DISC_SHINY.get(), ModItems.DISC_FRAGMENT_SHINY.get(),
				ModItems.SHINY_ROOTS.get());
	}
}
