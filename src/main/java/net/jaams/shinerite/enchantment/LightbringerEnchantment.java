
package net.jaams.shinerite.enchantment;

import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.init.ModEnchantments;

import java.util.List;

public class LightbringerEnchantment extends Enchantment {
	public LightbringerEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.WEAPON, slots);
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return this != ench && !List.of(ModEnchantments.LIGHTSTEAL.get()).contains(ench);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.is(ModTags.ENCHANTABLE_LIGHTBRINGER);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.is(ModTags.ENCHANTABLE_LIGHTBRINGER);
	}

	@Override
	public boolean isTradeable() {
		return false;
	}
}
