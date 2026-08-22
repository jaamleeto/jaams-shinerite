package net.jaams.shinerite.mixins.mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.jaams.shinerite.configuration.common.AssortedCommonConfig;
import net.jaams.shinerite.item.ShineriteIngotItem;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @Shadow
    protected static void setAngerTarget(AbstractPiglin piglin, LivingEntity entity) {
        throw new AssertionError();
    }

    @Shadow
    protected static void broadcastAngerTarget(AbstractPiglin piglin, LivingEntity entity) {
        throw new AssertionError();
    }

    @Inject(method = "isLovedItem", at = @At("HEAD"), cancellable = true)
    private static void shinerite$isLovedItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ShineriteIngotItem.isGoldenTinted(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At("HEAD"), cancellable = true)
    private static void shinerite$betrayalBarter(Piglin piglin, boolean barter, CallbackInfo ci) {
        ItemStack stack = piglin.getItemInHand(InteractionHand.OFF_HAND);
        if (!barter || !piglin.isAdult() || !ShineriteIngotItem.isGoldenTinted(stack))
            return;
        double chance = AssortedCommonConfig.BARTERING_BETRAY_CHANCE.get();
        if (chance <= 0d || piglin.getRandom().nextDouble() >= chance)
            return;
        Player player = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
        if (player == null || player.isCreative() || player.isSpectator())
            return;
        ci.cancel();
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        Brain<Piglin> brain = piglin.getBrain();
        brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
        brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
        brain.eraseMemory(MemoryModuleType.DANCING);
        setAngerTarget(piglin, player);
        broadcastAngerTarget(piglin, player);
        piglin.playSound(SoundEvents.PIGLIN_ANGRY, 1.0F, piglin.getVoicePitch());
    }
}
