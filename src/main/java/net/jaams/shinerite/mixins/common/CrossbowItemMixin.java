package net.jaams.shinerite.mixins.common;

import java.util.List;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.jaams.shinerite.tooltip.helper.TooltipHelper;

/** CrossbowItem.appendHoverText no llama a super, asi que los tooltips del Item mixin no llegan a las ballestas. */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
	@Inject(method = "appendHoverText", at = @At("TAIL"))
	private void jaam$appendCrossbowTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltip,
			TooltipFlag flag, CallbackInfo ci) {
		TooltipHelper.addAllTooltips(stack, tooltip);
	}
}
