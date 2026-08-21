
package net.jaams.shinerite.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.inventory.MenuType;

import net.jaams.shinerite.JaamsShineriteMod;

public class ModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, JaamsShineriteMod.MODID);
}
