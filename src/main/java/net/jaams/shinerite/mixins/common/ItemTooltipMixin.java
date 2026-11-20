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

import net.jaams.shinerite.tooltip.helper.TooltipHelper;

import javax.annotation.Nullable;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemTooltipMixin {
	@Inject(method = "appendHoverText", at = @At("HEAD"))
	private void addCustomTooltipsHead(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
		TooltipHelper.addAllTooltips(stack, tooltip);
	}
}
