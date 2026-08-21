package net.jaams.shinerite.mixins.mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;

import net.jaams.shinerite.item.ShineriteIngotItem;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @Inject(method = "isLovedItem", at = @At("HEAD"), cancellable = true)
    private static void shinerite$isLovedItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ShineriteIngotItem.isGoldenTinted(stack)) {
            cir.setReturnValue(true);
        }
    }
}
