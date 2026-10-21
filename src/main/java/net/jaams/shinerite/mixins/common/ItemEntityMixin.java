package net.jaams.shinerite.mixins.common;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
	private void makeImbuedShineriteFireImmune(CallbackInfoReturnable<Boolean> cir) {
		ItemEntity itemEntity = (ItemEntity) (Object) this;
		ItemStack stack = itemEntity.getItem();
		if (ModUtils.isImbued(stack) && MechanicsConfig.IMBUED_FIRE_RESISTANCE_FIREPROOF.get()) {
			ListTag effectsTag = stack.getTag().getList("ImbuedEffects", Tag.TAG_COMPOUND);
			for (Tag tag : effectsTag) {
				CompoundTag effectTag = (CompoundTag) tag;
				MobEffectInstance effect = ModUtils.loadImbuedEffect(effectTag);
				if (effect != null && effect.getEffect() == MobEffects.FIRE_RESISTANCE) {
					cir.setReturnValue(true);
					return;
				}
			}
		}
	}
}
