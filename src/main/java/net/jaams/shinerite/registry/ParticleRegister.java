package net.jaams.shinerite.registry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.jaams.shinerite.particle.CustomHeartParticle;
import net.jaams.shinerite.particle.CustomFireworkParticle;
import net.jaams.shinerite.particle.CustomEffectParticle;
import net.jaams.shinerite.particle.CustomCritParticle;
import net.jaams.shinerite.init.ModParticles;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleRegister {
	@SubscribeEvent
	public static void registerCustomParticleFactories(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ModParticles.CUSTOM_EFFECT_PARTICLE.get(), CustomEffectParticle.CustomEffectParticleProvider::new);
		event.registerSpriteSet(ModParticles.CUSTOM_FIREWORK_PARTICLE.get(), CustomFireworkParticle.CustomFireworkParticleProvider::new);
		event.registerSpriteSet(ModParticles.CUSTOM_CRIT_PARTICLE.get(), CustomCritParticle.CustomCritParticleProvider::new);
		event.registerSpriteSet(ModParticles.CUSTOM_HEART_PARTICLE.get(), CustomHeartParticle.CustomHeartParticleProvider::new);
	}
}
