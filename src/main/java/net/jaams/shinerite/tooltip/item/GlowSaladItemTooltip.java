package net.jaams.shinerite.tooltip.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.item.GlowSaladItem;

import java.util.List;

public class GlowSaladItemTooltip {
	public static void add(ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof GlowSaladItem))
			return;
		boolean hasColor = ModUtils.getCurrentColor(stack) != -1;
		boolean isImbued = ModUtils.isImbued(stack);
		tooltip.add(Component.translatable("tooltip.jaams_shinerite.effect.consume.night_vision", Component.translatable("effect.minecraft.night_vision").withStyle(ChatFormatting.BLUE),
				ModUtils.formatDuration(GlowSaladItem.NIGHT_VISION_DURATION, ChatFormatting.BLUE)).withStyle(ChatFormatting.BLUE));
		tooltip.add(
				Component.translatable("tooltip.jaams_shinerite.effect.consume.glowing", Component.translatable("effect.minecraft.glowing").withStyle(ChatFormatting.BLUE), ModUtils.formatDuration(GlowSaladItem.GLOWING_DURATION, ChatFormatting.BLUE))
						.withStyle(ChatFormatting.BLUE));
		if (hasColor && !isImbued) {
			tooltip.add(
					Component.translatable("tooltip.jaams_shinerite.effect.consume.poison", Component.translatable("effect.minecraft.poison").withStyle(ChatFormatting.RED), ModUtils.formatDuration(GlowSaladItem.POISON_DURATION, ChatFormatting.RED))
							.withStyle(ChatFormatting.RED));
		}
	}
}
