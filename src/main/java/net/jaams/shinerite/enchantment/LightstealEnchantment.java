
package net.jaams.shinerite.enchantment;

import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.init.ModEnchantments;

import java.util.List;

public class LightstealEnchantment extends Enchantment {
	public LightstealEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, slots);
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return this != ench && !List.of(ModEnchantments.LIGHTBRINGER.get()).contains(ench);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.is(ModTags.ENCHANTABLE_LIGHTSTEAL);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.is(ModTags.ENCHANTABLE_LIGHTSTEAL);
	}

	@Override
	public boolean isTradeable() {
		return false;
	}
}
