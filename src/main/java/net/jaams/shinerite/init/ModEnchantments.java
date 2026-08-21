
package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.enchantment.Enchantment;

import net.jaams.shinerite.enchantment.LightstealEnchantment;
import net.jaams.shinerite.enchantment.LightguardEnchantment;
import net.jaams.shinerite.enchantment.LightbringerEnchantment;
import net.jaams.shinerite.JaamsShineriteMod;

public class ModEnchantments {
	public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, JaamsShineriteMod.MODID);
	public static final RegistryObject<Enchantment> LIGHTBRINGER = REGISTRY.register("lightbringer", () -> new LightbringerEnchantment());
	public static final RegistryObject<Enchantment> LIGHTSTEAL = REGISTRY.register("lightsteal", () -> new LightstealEnchantment());
	public static final RegistryObject<Enchantment> LIGHTGUARD = REGISTRY.register("lightguard", () -> new LightguardEnchantment());
}
