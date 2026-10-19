package net.jaams.shinerite.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.jaams.shinerite.client.model.ArmorTrimModelGenerator;
import net.jaams.shinerite.client.model.Modelplaceholder_model;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Modelplaceholder_model.LAYER_LOCATION, Modelplaceholder_model::createBodyLayer);
	}

	/**
	 * The armor trim icons are baked at runtime and cached per model, so the cache has to be dropped whenever the
	 * models are re-baked (resource pack load, "reload models", joining a world with another datapack...).
	 */
	@SubscribeEvent
	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		ArmorTrimModelGenerator.clearCache();
	}
}