package net.jaams.shinerite.compat;

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Compatibility with Waxed &amp; Shiny.
 *
 * <p>The mod adds {@code waxed:wax}, an item that behaves like a vanilla
 * honeycomb. It is recognised by registry id instead of a direct class
 * reference, so Shinerite still loads when the mod is absent.</p>
 */
public final class WaxedCompat {

	public static final String MOD_ID = "waxed";
	public static final ResourceLocation WAX_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "wax");

	private static Boolean loaded;

	private WaxedCompat() {
	}

	/** Cheap presence check that never touches an optional class. */
	public static boolean isLoaded() {
		if (loaded == null) {
			try {
				loaded = ModList.get() != null && ModList.get().isLoaded(MOD_ID);
			} catch (Throwable t) {
				loaded = false;
			}
		}
		return loaded;
	}

	/** True for the Waxed &amp; Shiny wax item. */
	public static boolean isWax(ItemStack stack) {
		return !stack.isEmpty()
				&& BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(WAX_ID);
	}

	/**
	 * True for any item that can apply a Shinerite coating: the vanilla
	 * honeycomb or the wax from Waxed &amp; Shiny.
	 */
	public static boolean isCoatingAgent(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(Items.HONEYCOMB) || isWax(stack));
	}

	/** The wax item, or {@code null} when Waxed &amp; Shiny is not installed. */
	@Nullable
	public static Item getWaxItem() {
		return isLoaded() ? ForgeRegistries.ITEMS.getValue(WAX_ID) : null;
	}
}
