package net.jaams.shinerite.mixins.common;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;

import net.jaams.shinerite.tooltip.trait.CoatableItemTooltip;
import net.jaams.shinerite.tooltip.item.ShinyScraperItemTooltip;
import net.jaams.shinerite.tooltip.item.ShinyRootsItemTooltip;
import net.jaams.shinerite.tooltip.item.ShinyPaintBrushItemTooltip;
import net.jaams.shinerite.tooltip.throwable.ThrowableShineriteStarTooltip;
import net.jaams.shinerite.tooltip.throwable.ThrowableShinemerangTooltip;
import net.jaams.shinerite.tooltip.throwable.ThrowableItemTooltip;
import net.jaams.shinerite.tooltip.trait.ImbuableItemTooltip;
import net.jaams.shinerite.tooltip.item.GlowSaladItemTooltip;
import net.jaams.shinerite.tooltip.trait.FrailglowItemTooltip;

import javax.annotation.Nullable;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemTooltipMixin {
	@Inject(method = "appendHoverText", at = @At("HEAD"))
	private void addCustomTooltipsHead(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
		ImbuableItemTooltip.add(stack, tooltip, flag);
		CoatableItemTooltip.add(stack, tooltip, flag);
		FrailglowItemTooltip.add(stack, tooltip);
		GlowSaladItemTooltip.add(stack, tooltip);
		ShinyRootsItemTooltip.add(stack, tooltip);
		ThrowableItemTooltip.add(stack, tooltip);
		ThrowableShinemerangTooltip.add(stack, tooltip);
		ThrowableShineriteStarTooltip.add(stack, tooltip);
		ShinyPaintBrushItemTooltip.add(stack, tooltip);
		ShinyScraperItemTooltip.add(stack, tooltip);
	}
}
