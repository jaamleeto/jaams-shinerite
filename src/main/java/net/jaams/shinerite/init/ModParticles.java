package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraft.core.particles.ParticleType;

import net.jaams.shinerite.particle.CustomHeartParticleData;
import net.jaams.shinerite.particle.CustomFireworkParticleData;
import net.jaams.shinerite.particle.CustomEffectParticleData;
import net.jaams.shinerite.particle.CustomCritParticleData;

import com.mojang.serialization.Codec;

public class ModParticles {
	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "jaams_shinerite");
	public static final RegistryObject<ParticleType<CustomEffectParticleData>> CUSTOM_EFFECT_PARTICLE = PARTICLES.register("custom_effect", () -> new ParticleType<CustomEffectParticleData>(false, CustomEffectParticleData.DESERIALIZER) {
		@Override
		public Codec<CustomEffectParticleData> codec() {
			return CustomEffectParticleData.CODEC;
		}
	});
	public static final RegistryObject<ParticleType<CustomFireworkParticleData>> CUSTOM_FIREWORK_PARTICLE = PARTICLES.register("custom_firework", () -> new ParticleType<CustomFireworkParticleData>(false, CustomFireworkParticleData.DESERIALIZER) {
		@Override
		public Codec<CustomFireworkParticleData> codec() {
			return CustomFireworkParticleData.CODEC;
		}
	});
	public static final RegistryObject<ParticleType<CustomCritParticleData>> CUSTOM_CRIT_PARTICLE = PARTICLES.register("custom_crit", () -> new ParticleType<CustomCritParticleData>(false, CustomCritParticleData.DESERIALIZER) {
		@Override
		public Codec<CustomCritParticleData> codec() {
			return CustomCritParticleData.CODEC;
		}
	});
	public static final RegistryObject<ParticleType<CustomHeartParticleData>> CUSTOM_HEART_PARTICLE = PARTICLES.register("custom_heart", () -> new ParticleType<CustomHeartParticleData>(false, CustomHeartParticleData.DESERIALIZER) {
		@Override
		public Codec<CustomHeartParticleData> codec() {
			return CustomHeartParticleData.CODEC;
		}
	});

	public static void register(IEventBus eventBus) {
		PARTICLES.register(eventBus);
	}
}
