package net.jaams.shinerite.tooltip.trait;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.client.TooltipsConfig;

import java.util.List;

public final class CoatableItemTooltip {
	public static void add(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
		if (!MechanicsConfig.COATING.get())
			return;
		if (!TooltipsConfig.TOOLTIP_COATABLE_TRAIT.get())
			return;
		if (!ModUtils.isCoatableItem(stack))
			return;
		boolean showTrait = TooltipsConfig.TOOLTIP_COATABLE_TRAIT.get() && (!TooltipsConfig.TOOLTIP_COATABLE_TRAIT_ADVANCED_ONLY.get() || flag.isAdvanced());
		if (showTrait) {
			tooltip.add(Component.translatable("tooltip.jaams_shinerite.trait.coatable").withStyle(ChatFormatting.AQUA));
			if (Screen.hasShiftDown()) {
				tooltip.add(Component.translatable("tooltip.jaams_shinerite.trait.coatable.desc").withStyle(ChatFormatting.GRAY));
			}
		}
	}
}
