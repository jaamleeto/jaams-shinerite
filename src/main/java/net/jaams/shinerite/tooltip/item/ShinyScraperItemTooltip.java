package net.jaams.shinerite.tooltip.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModTooltips;
import net.jaams.shinerite.item.ShinyScraperItem;

import java.util.List;

public class ShinyScraperItemTooltip {
	public static void add(ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof ShinyScraperItem))
			return;
		ModTooltips.addLongDescription(stack, tooltip, "tooltip.jaams_shinerite.shiny_scraper.long_desc", ChatFormatting.GRAY);
	}
}
