package net.jaams.shinerite.tooltip.helper;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.jaams.shinerite.tooltip.item.GlowSaladItemTooltip;
import net.jaams.shinerite.tooltip.item.ShinyPaintBrushItemTooltip;
import net.jaams.shinerite.tooltip.item.ShinyRootsItemTooltip;
import net.jaams.shinerite.tooltip.item.ShinyScraperItemTooltip;
import net.jaams.shinerite.tooltip.throwable.ThrowableItemTooltip;
import net.jaams.shinerite.tooltip.throwable.ThrowableShinerangTooltip;
import net.jaams.shinerite.tooltip.throwable.ThrowableShineriteStarTooltip;
import net.jaams.shinerite.tooltip.trait.FrailglowItemTooltip;

public final class TooltipHelper {
	private TooltipHelper() {
	}

	/** Tooltips del mod anyadidos via appendHoverText. CrossbowItem no llama a super, asi que necesita su propia inyeccion. */
	public static void addAllTooltips(ItemStack stack, List<Component> tooltip) {
		FrailglowItemTooltip.add(stack, tooltip);
		GlowSaladItemTooltip.add(stack, tooltip);
		ShinyRootsItemTooltip.add(stack, tooltip);
		ThrowableItemTooltip.add(stack, tooltip);
		ThrowableShinerangTooltip.add(stack, tooltip);
		ThrowableShineriteStarTooltip.add(stack, tooltip);
		ShinyPaintBrushItemTooltip.add(stack, tooltip);
		ShinyScraperItemTooltip.add(stack, tooltip);
	}
}
