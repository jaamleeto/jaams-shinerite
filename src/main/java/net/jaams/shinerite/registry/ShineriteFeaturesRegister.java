package net.jaams.shinerite.registry;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.core.registries.Registries;

import net.jaams.shinerite.feature.NetherShineriteColoredOreFeature;

import net.jaams.shinerite.feature.DeepslateShineriteColoredOreFeature;
import net.jaams.shinerite.feature.ShineriteColoredOreFeature;
import net.jaams.shinerite.feature.ShineriteFlowerPatchFeature;
import net.jaams.shinerite.feature.NetherShineriteFlowerPatchFeature;
import net.jaams.shinerite.feature.ShineriteRootsPatchFeature;
import net.jaams.shinerite.JaamsShineriteMod;

public class ShineriteFeaturesRegister {
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, JaamsShineriteMod.MODID);
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHINERITE_COLORED_ORE = FEATURES.register("shinerite_colored_ore", () -> new ShineriteColoredOreFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEEPSLATE_SHINERITE_COLORED_ORE = FEATURES.register("deepslate_shinerite_colored_ore", () -> new DeepslateShineriteColoredOreFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SHINERITE_COLORED_ORE = FEATURES.register("nether_shinerite_colored_ore", () -> new NetherShineriteColoredOreFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHINERITE_FLOWER_PATCH = FEATURES.register("shinerite_flower_patch", () -> new ShineriteFlowerPatchFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SHINERITE_FLOWER_PATCH = FEATURES.register("nether_shinerite_flower_patch", () -> new NetherShineriteFlowerPatchFeature(NoneFeatureConfiguration.CODEC));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHINERITE_ROOTS_PATCH = FEATURES.register("shinerite_roots_patch", () -> new ShineriteRootsPatchFeature(NoneFeatureConfiguration.CODEC, false));
	public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SHINERITE_ROOTS_PATCH = FEATURES.register("nether_shinerite_roots_patch", () -> new ShineriteRootsPatchFeature(NoneFeatureConfiguration.CODEC, true));

	public static void register(IEventBus eventBus) {
		FEATURES.register(eventBus);
	}
}
