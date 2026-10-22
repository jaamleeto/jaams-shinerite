package net.jaams.shinerite.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.jaams.shinerite.client.model.ArmorTrimModelGenerator;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModModels {


	@SubscribeEvent
	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		ArmorTrimModelGenerator.clearCache();
	}
}
