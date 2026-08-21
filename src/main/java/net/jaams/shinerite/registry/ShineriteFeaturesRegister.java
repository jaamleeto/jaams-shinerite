package net.jaams.shinerite.registry;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.core.registries.Registries;

import net.jaams.shinerite.world.NetherShineriteRootsFeature;
import net.jaams.shinerite.world.ShineriteRootsFeature;
import net.jaams.shinerite.JaamsShineriteMod;

public class ShineriteFeaturesRegister {
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, JaamsShineriteMod.MODID);
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHINERITE_ROOTS = FEATURES.register("shinerite_roots", () -> new ShineriteRootsFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SHINERITE_ROOTS = FEATURES.register("nether_shinerite_roots", () -> new NetherShineriteRootsFeature(NoneFeatureConfiguration.CODEC));

	public static void register(IEventBus eventBus) {
		FEATURES.register(eventBus);
	}
}
