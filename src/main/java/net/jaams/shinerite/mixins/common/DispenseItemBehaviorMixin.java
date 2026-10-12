package net.jaams.shinerite.mixins.common;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;

import net.jaams.shinerite.dispenser.ProjectileDispenser;

@Mixin(DefaultDispenseItemBehavior.class)
public abstract class DispenseItemBehaviorMixin {
	@Inject(method = "execute", at = @At("HEAD"), cancellable = true)
	private void jaam$onExecuteDefaultDispense(BlockSource blockSource, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
		if (ProjectileDispenser.canDispense(stack)) {
			ItemStack result = ProjectileDispenser.dispense(blockSource, stack);
			cir.setReturnValue(result);
		}
	}
}
