package net.jaams.shinerite.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.init.ModItems;



public final class ShineriteTrimSupport {



	public static final int WHITE_CHANNEL_MIN = 0xE0;

	private static Set<Item> tintableArmor;

	private ShineriteTrimSupport() {
	}

	public static synchronized Set<Item> tintableArmor() {
		if (tintableArmor == null) {
			Set<Item> set = new LinkedHashSet<>();
			set.add(ModItems.SHINERITE_ARMOR_HELMET.get());
			set.add(ModItems.SHINERITE_ARMOR_CHESTPLATE.get());
			set.add(ModItems.SHINERITE_ARMOR_LEGGINGS.get());
			set.add(ModItems.SHINERITE_ARMOR_BOOTS.get());
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



	public static boolean isTintable(ItemStack stack) {
		return stack != null && tintableArmor().contains(stack.getItem());
	}


	public static boolean isPlainColor(int color) {
		return (color >> 16 & 0xFF) >= WHITE_CHANNEL_MIN && (color >> 8 & 0xFF) >= WHITE_CHANNEL_MIN
				&& (color & 0xFF) >= WHITE_CHANNEL_MIN;
	}



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



	public static boolean shouldStoreColor(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof IDyeableItem dyeable)) {
			return false;
		}
		return dyeable.hasColor(stack);
	}
}
