
package net.jaams.shinerite.enchantment;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

import net.jaams.shinerite.util.ModTags;

import java.util.List;

public class LightguardEnchantment extends Enchantment {
	public LightguardEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.COMMON, EnchantmentCategory.ARMOR, slots);
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return List.of(Enchantments.THORNS).contains(ench);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.is(ModTags.ENCHANTABLE_LIGHTGUARD);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return stack.is(ModTags.ENCHANTABLE_LIGHTGUARD);
	}

	@Override
	public boolean isTradeable() {
		return false;
	}
}
