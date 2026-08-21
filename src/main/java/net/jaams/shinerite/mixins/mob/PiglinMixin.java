package net.jaams.shinerite.mixins.mob;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import net.jaams.shinerite.loader.EquipmentModifierLoader;

@Mixin(Piglin.class)
public abstract class PiglinMixin {
    @Inject(method = "createSpawnWeapon", at = @At("HEAD"), cancellable = true)
    private void modifyCreateSpawnWeapon(CallbackInfoReturnable<ItemStack> cir) {
        Piglin self = (Piglin) (Object) this;
        RandomSource random = self.getRandom();
        if (EquipmentModifierLoader.INSTANCE.applySpawnEquipment(self, random)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
