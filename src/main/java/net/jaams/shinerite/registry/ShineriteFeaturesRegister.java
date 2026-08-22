package net.jaams.shinerite.registry;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.core.registries.Registries;

import net.jaams.shinerite.feature.NetherShineriteColoredOreFeature;
import net.jaams.shinerite.feature.NetherShineriteFlowerPatchFeature;
import net.jaams.shinerite.feature.NetherShineriteRootsFeature;

import net.jaams.shinerite.feature.ShineriteColoredOreFeature;
import net.jaams.shinerite.feature.ShineriteFlowerPatchFeature;
import net.jaams.shinerite.feature.ShineriteRootsFeature;
import net.jaams.shinerite.JaamsShineriteMod;

public class ShineriteFeaturesRegister {
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, JaamsShineriteMod.MODID);
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHINERITE_ROOTS = FEATURES.register("shinerite_roots", () -> new ShineriteRootsFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SHINERITE_ROOTS = FEATURES.register("nether_shinerite_roots", () -> new NetherShineriteRootsFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHINERITE_COLORED_ORE = FEATURES.register("shinerite_colored_ore", () -> new ShineriteColoredOreFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SHINERITE_COLORED_ORE = FEATURES.register("nether_shinerite_colored_ore", () -> new NetherShineriteColoredOreFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHINERITE_FLOWER_PATCH = FEATURES.register("shinerite_flower_patch", () -> new ShineriteFlowerPatchFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SHINERITE_FLOWER_PATCH = FEATURES.register("nether_shinerite_flower_patch", () -> new NetherShineriteFlowerPatchFeature(NoneFeatureConfiguration.CODEC));

	public static void register(IEventBus eventBus) {
		FEATURES.register(eventBus);
	}
}
