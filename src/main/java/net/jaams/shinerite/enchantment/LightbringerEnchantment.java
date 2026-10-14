
package net.jaams.shinerite.enchantment;

import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.init.ModEnchantments;
import net.jaams.shinerite.configuration.common.EnchantmentsConfig;

import java.util.List;

public class LightbringerEnchantment extends Enchantment {
	public LightbringerEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.WEAPON, slots);
	}

	@Override
	public int getMaxLevel() {
		return EnchantmentsConfig.LIGHTBRINGER_MAX_LEVEL.get();
	}

	@Override
	public int getMinCost(int level) {
		return EnchantmentsConfig.LIGHTBRINGER_MIN_COST.get() + (level - 1) * EnchantmentsConfig.LIGHTBRINGER_COST_PER_LEVEL.get();
	}

	@Override
	public int getMaxCost(int level) {
		return getMinCost(level) + EnchantmentsConfig.LIGHTBRINGER_COST_RANGE.get();
	}

	@Override
	protected boolean checkCompatibility(Enchantment ench) {
		return this != ench && !List.of(ModEnchantments.LIGHTSTEAL.get()).contains(ench);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return EnchantmentsConfig.LIGHTBRINGER.get() && stack.is(ModTags.ENCHANTABLE_LIGHTBRINGER);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return EnchantmentsConfig.LIGHTBRINGER.get() && stack.is(ModTags.ENCHANTABLE_LIGHTBRINGER);
	}

	@Override
	public boolean isAllowedOnBooks() {
		return EnchantmentsConfig.LIGHTBRINGER.get();
	}

	@Override
	public boolean isTradeable() {
		return false;
	}

	@Override
	public boolean isTreasureOnly() {
		return EnchantmentsConfig.LIGHTBRINGER_IS_TREASURE.get();
	}
}
