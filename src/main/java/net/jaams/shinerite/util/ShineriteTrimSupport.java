package net.jaams.shinerite.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.init.ModItems;

/**
 * Central list of the armor pieces that can render a <b>tinted</b> Shinerite trim, both on the worn
 * model and on the inventory icon.
 *
 * <p>The smithing recipe freezes a colored ingot into the armor NBT for <b>every</b> trimmable armor, since the trim
 * icon is generated at runtime and the worn trim is rendered by the armor layer mixin, so the color also works on the
 * armor of other mods. This list is kept for informational purposes (tooltip, integrations) and only holds the pieces
 * that are tinted by their own color (Shinerite) or by dyeing (vanilla leather).
 */
public final class ShineriteTrimSupport {

	/**
	 * Minimum value (per channel) for a color to still be considered "plain white". Anything above
	 * this is treated as a real dye color and therefore only allowed on supported armor.
	 */
	public static final int WHITE_CHANNEL_MIN = 0xE0;

	private static Set<Item> tintableArmor;

	private ShineriteTrimSupport() {
	}

	public static synchronized Set<Item> tintableArmor() {
		if (tintableArmor == null) {
			Set<Item> set = new LinkedHashSet<>();
			// Shinerite armor.
			set.add(ModItems.SHINERITE_ARMOR_HELMET.get());
			set.add(ModItems.SHINERITE_ARMOR_CHESTPLATE.get());
			set.add(ModItems.SHINERITE_ARMOR_LEGGINGS.get());
			set.add(ModItems.SHINERITE_ARMOR_BOOTS.get());
			// Vanilla trimmable armor (we ship tinted trim models + ItemColor handlers for these).
			set.add(Items.LEATHER_HELMET);
			set.add(Items.LEATHER_CHESTPLATE);
			set.add(Items.LEATHER_LEGGINGS);
			set.add(Items.LEATHER_BOOTS);
			set.add(Items.CHAINMAIL_HELMET);
			set.add(Items.CHAINMAIL_CHESTPLATE);
			set.add(Items.CHAINMAIL_LEGGINGS);
			set.add(Items.CHAINMAIL_BOOTS);
			set.add(Items.IRON_HELMET);
			set.add(Items.IRON_CHESTPLATE);
			set.add(Items.IRON_LEGGINGS);
			set.add(Items.IRON_BOOTS);
			set.add(Items.GOLDEN_HELMET);
			set.add(Items.GOLDEN_CHESTPLATE);
			set.add(Items.GOLDEN_LEGGINGS);
			set.add(Items.GOLDEN_BOOTS);
			set.add(Items.DIAMOND_HELMET);
			set.add(Items.DIAMOND_CHESTPLATE);
			set.add(Items.DIAMOND_LEGGINGS);
			set.add(Items.DIAMOND_BOOTS);
			set.add(Items.NETHERITE_HELMET);
			set.add(Items.NETHERITE_CHESTPLATE);
			set.add(Items.NETHERITE_LEGGINGS);
			set.add(Items.NETHERITE_BOOTS);
			set.add(Items.TURTLE_HELMET);
			tintableArmor = Collections.unmodifiableSet(set);
		}
		return tintableArmor;
	}

	/** {@code true} when this armor piece has a colored base model of its own (Shinerite / dyeable leather).
	 */
	public static boolean isTintable(ItemStack stack) {
		return stack != null && tintableArmor().contains(stack.getItem());
	}

	/** {@code true} when the RGB value is (near enough) pure white. */
	public static boolean isPlainColor(int color) {
		return (color >> 16 & 0xFF) >= WHITE_CHANNEL_MIN && (color >> 8 & 0xFF) >= WHITE_CHANNEL_MIN
				&& (color & 0xFF) >= WHITE_CHANNEL_MIN;
	}

	/**
	 * {@code true} when the ingot carries no real dye color, i.e. it is undyed or white. Such an
	 * ingot is safe to use on any trimmable armor because it never introduces a tint.
	 */
	public static boolean isPlainIngot(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		if (!(stack.getItem() instanceof IDyeableItem dyeable)) {
			return true;
		}
		if (!dyeable.hasColor(stack)) {
			return true;
		}
		return isPlainColor(dyeable.getColor(stack));
	}

	/**
	 * {@code true} when the ingot carries a dye color that should be frozen into the armor NBT.
	 * White counts as a real color and is stored too, so a white ingot gives a white trim on
	 * supported armor. Only a completely undyed ingot leaves the trim on its natural palette color.
	 *
	 * <p>Callers must still check {@link #isTintable(ItemStack)} first: a color must never be
	 * stored on armor that is unable to render a tinted trim.
	 */
	public static boolean shouldStoreColor(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof IDyeableItem dyeable)) {
			return false;
		}
		return dyeable.hasColor(stack);
	}
}
