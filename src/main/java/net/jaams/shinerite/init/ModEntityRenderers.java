
package net.jaams.shinerite.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.jaams.shinerite.client.renderer.ShineriteStarProjectileRenderer;
import net.jaams.shinerite.client.renderer.ShinerangProjectileRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.SHINERANG_PROJECTILE.get(), ShinerangProjectileRenderer::new);
		event.registerEntityRenderer(ModEntities.SHINERITE_STAR_PROJECTILE.get(), ShineriteStarProjectileRenderer::new);
	}
}
