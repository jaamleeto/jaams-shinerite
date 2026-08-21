package net.jaams.shinerite.tooltip.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.item.ShinyRootsItem;

import java.util.List;

public class ShinyRootsItemTooltip {
	public static void add(ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof ShinyRootsItem)) {
			return;
		}
		boolean hasColor = ModUtils.getCurrentColor(stack) != -1;
		boolean isImbued = ModUtils.isImbued(stack);
		tooltip.add(
				Component.translatable("tooltip.jaams_shinerite.effect.consume.glowing", Component.translatable("effect.minecraft.glowing").withStyle(ChatFormatting.BLUE), ModUtils.formatDuration(ShinyRootsItem.GLOWING_DURATION, ChatFormatting.BLUE))
						.withStyle(ChatFormatting.BLUE));
		if (hasColor && !isImbued) {
			tooltip.add(
					Component.translatable("tooltip.jaams_shinerite.effect.consume.poison", Component.translatable("effect.minecraft.poison").withStyle(ChatFormatting.RED), ModUtils.formatDuration(ShinyRootsItem.POISON_DURATION, ChatFormatting.RED))
							.withStyle(ChatFormatting.RED));
		}
	}
}
