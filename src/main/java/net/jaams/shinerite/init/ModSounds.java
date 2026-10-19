
package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.jaams.shinerite.JaamsShineriteMod;

public class ModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JaamsShineriteMod.MODID);
	public static final RegistryObject<SoundEvent> EATS_GLOW_ROOTS = REGISTRY.register("eats_glow_roots", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "eats_glow_roots")));
	public static final RegistryObject<SoundEvent> EATS_GLOW_SALAD = REGISTRY.register("eats_glow_salad", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "eats_glow_salad")));
	public static final RegistryObject<SoundEvent> SHINERITE_TOOL_HIT = REGISTRY.register("shinerite_tool_hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerite_tool_hit")));
	public static final RegistryObject<SoundEvent> SHINERANG_SHOOT = REGISTRY.register("shinerang_shoot", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerang_shoot")));
	public static final RegistryObject<SoundEvent> SHINERANG_SHOOT_RESONATES = REGISTRY.register("shinerang_shoot_resonates", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerang_shoot_resonates")));
	public static final RegistryObject<SoundEvent> SHINERANG_HIT = REGISTRY.register("shinerang_hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerang_hit")));
	public static final RegistryObject<SoundEvent> SHINERANG_GROUND = REGISTRY.register("shinerang_ground", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerang_ground")));
	public static final RegistryObject<SoundEvent> SHINERITE_STAR_SHOOT = REGISTRY.register("shinerite_star_shoot", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerite_star_shoot")));
	public static final RegistryObject<SoundEvent> SHINERITE_STAR_SHOOT_RESONATES = REGISTRY.register("shinerite_star_shoot_resonates",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerite_star_shoot_resonates")));
	public static final RegistryObject<SoundEvent> SHINERITE_STAR_HIT = REGISTRY.register("shinerite_star_hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerite_star_hit")));
	public static final RegistryObject<SoundEvent> SHINERITE_STAR_GROUND = REGISTRY.register("shinerite_star_ground", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerite_star_ground")));
	public static final RegistryObject<SoundEvent> EMPTY_SOUND = REGISTRY.register("empty_sound", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "empty_sound")));
	public static final RegistryObject<SoundEvent> LIGHTSTEAL_HIT = REGISTRY.register("lightsteal_hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "lightsteal_hit")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_BLADE = REGISTRY.register("music_disc_blade", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_blade")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_BLOODY = REGISTRY.register("music_disc_bloody", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_bloody")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_CALIFORNIA = REGISTRY.register("music_disc_california", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_california")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_FAMILY = REGISTRY.register("music_disc_family", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_family")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_GURENGE = REGISTRY.register("music_disc_gurenge", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_gurenge")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_HOLY = REGISTRY.register("music_disc_holy", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_holy")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_HUNTER = REGISTRY.register("music_disc_hunter", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_hunter")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_LITHIUM = REGISTRY.register("music_disc_lithium", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_lithium")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_NEVER = REGISTRY.register("music_disc_never", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_never")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_REVENGE = REGISTRY.register("music_disc_revenge", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_revenge")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_SADNESS = REGISTRY.register("music_disc_sadness", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_sadness")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_SAKE = REGISTRY.register("music_disc_sake", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_sake")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_SHINY = REGISTRY.register("music_disc_shiny", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_shiny")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_SPECIAL = REGISTRY.register("music_disc_special", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_special")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_SPY = REGISTRY.register("music_disc_spy", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_spy")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_TOXICITY = REGISTRY.register("music_disc_toxicity", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_toxicity")));
	public static final RegistryObject<SoundEvent> MUSIC_DISC_WAR = REGISTRY.register("music_disc_war", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "music_disc_war")));
	public static final RegistryObject<SoundEvent> PROJECTILE_GROUND = REGISTRY.register("projectile_ground", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "projectile_ground")));
	public static final RegistryObject<SoundEvent> PROJECTILE_RETURN = REGISTRY.register("projectile_return", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "projectile_return")));
	public static final RegistryObject<SoundEvent> PROJECTILE_HIT = REGISTRY.register("projectile_hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "projectile_hit")));
	public static final RegistryObject<SoundEvent> PROJECTILE_THROW = REGISTRY.register("projectile_throw", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "projectile_throw")));
	public static final RegistryObject<SoundEvent> SWOOSH_AIR = REGISTRY.register("swoosh_air", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "swoosh_air")));
}
