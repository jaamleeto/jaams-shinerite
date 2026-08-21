package net.jaams.shinerite.tooltip.trait;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTooltips;
import net.jaams.shinerite.util.ModTraits;
import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.configuration.client.TooltipsConfig;

import java.util.List;

public class FrailglowItemTooltip {
	public static void add(ItemStack stack, List<Component> tooltip) {
		if (!TraitsConfig.FRAILGLOW.get()) {
			return;
		}
		if (!ModTraits.isFrailglowItem(stack)) {
			return;
		}
		boolean glowingOnBreak = ModUtils.getConfigOrNbtBoolean(stack, "FrailglowGlowingOnBreak", TraitsConfig.FRAILGLOW_GLOWING_ON_BREAK::get);
		String descKey = glowingOnBreak ? "tooltip.jaams_shinerite.trait.frailglow.desc" : "tooltip.jaams_shinerite.trait.frailglow.alt.desc";
		ModTooltips.addNegativeTrait(stack, tooltip, "tooltip.jaams_shinerite.trait.frailglow", descKey);
		if (!TooltipsConfig.TOOLTIP_FRAILGLOW_PROPERTIES.get()) {
			return;
		}
		ModTooltips.addExtraInfo(stack, tooltip, "tooltip.jaams_shinerite.properties.frailglow", ChatFormatting.RED);
		addBreakChanceLine(stack, tooltip);
		addGlowEffectLineIfEnabled(stack, tooltip, glowingOnBreak);
	}

	private static void addBreakChanceLine(ItemStack stack, List<Component> tooltip) {
		double breakChance = ModUtils.getConfigOrNbtDouble(stack, "FrailglowBreakChance", TraitsConfig.FRAILGLOW_BREAK_CHANCE::get);
		double percent = breakChance * 100.0;
		String chanceValue = String.format("%.0f", percent);
		tooltip.add(Component.translatable("tooltip.jaams_shinerite.properties.frailglow.break_chance", chanceValue).withStyle(ChatFormatting.GRAY));
	}

	private static void addGlowEffectLineIfEnabled(ItemStack stack, List<Component> tooltip, boolean enabled) {
		if (!enabled) {
			return;
		}
		int durationTicks = ModUtils.getConfigOrNbtInt(stack, "FrailglowGlowDuration", TraitsConfig.FRAILGLOW_GLOW_DURATION::get);
		int amplifier = ModUtils.getConfigOrNbtInt(stack, "FrailglowGlowAmplifier", TraitsConfig.FRAILGLOW_GLOW_AMPLIFIER::get);
		float seconds = durationTicks / 20.0f;
		String timeStr = String.format("%02d:%02d", (int) (seconds / 60), (int) seconds % 60);
		int level = amplifier + 1;
		String romanLevel = ModUtils.toRoman(level);
		Component prefix = Component.translatable("tooltip.jaams_shinerite.properties.frailglow.effect_prefix").withStyle(ChatFormatting.GRAY);
		Component effectPart = Component.translatable("tooltip.jaams_shinerite.properties.frailglow.glow_effect", Component.translatable("effect.minecraft.glowing"), romanLevel, timeStr).withStyle(ChatFormatting.RED);
		tooltip.add(Component.empty().append(prefix).append(effectPart));
	}
}
