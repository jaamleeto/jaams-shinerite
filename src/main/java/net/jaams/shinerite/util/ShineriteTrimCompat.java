package net.jaams.shinerite.util;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;

/**
 * Pieces of the mods that render their armor completely on their own, where {@code HumanoidArmorLayerMixin}
 * must not take over the piece.
 *
 * <p>Those mods build the armor and its trim themselves, with their own model and their own trim draw, so
 * replacing that render is not needed: they keep drawing their armor and their trim, and the trim only gets
 * the Shinerite glow and color through the {@code renderTrim} of Forge, which every one of those renderers
 * ends up calling.
 */
public final class ShineriteTrimCompat {

	private static final Set<String> EXCLUDED_NAMESPACES = new LinkedHashSet<>();

	private ShineriteTrimCompat() {
	}

	/**
	 * Excludes every item of a mod namespace. Only needed for mods that own their whole armor render, like
	 * the electrum armor of Oreganized, whose model does not share the UV layout of the trim patterns.
	 */
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