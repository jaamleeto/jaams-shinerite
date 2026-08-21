package net.jaams.shinerite.registry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.init.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ArmorColorRegister {
	@SubscribeEvent
	public static void onColorArmorRegister(RegisterColorHandlersEvent.Item event) {
		event.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((IDyeableItem) stack.getItem()).getColor(stack), ModItems.SHINERITE_ARMOR_HELMET.get(), ModItems.SHINERITE_ARMOR_CHESTPLATE.get(),
				ModItems.SHINERITE_ARMOR_LEGGINGS.get(), ModItems.SHINERITE_ARMOR_BOOTS.get());
	}
}
