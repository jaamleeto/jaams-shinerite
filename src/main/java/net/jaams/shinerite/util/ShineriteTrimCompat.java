package net.jaams.shinerite.util;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;



public final class ShineriteTrimCompat {

	private static final Set<String> EXCLUDED_NAMESPACES = new LinkedHashSet<>();

	private ShineriteTrimCompat() {
	}



	public static void excludeNamespace(String namespace) {
		EXCLUDED_NAMESPACES.add(namespace);
	}

	public static boolean isExcluded(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
		return id != null && EXCLUDED_NAMESPACES.contains(id.getNamespace());
	}
}
