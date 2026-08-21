package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	@Shadow
	public abstract void render(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model);

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void hideInvisibleImbuedShinerite(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
		if (ModUtils.isImbued(itemStack) && MechanicsConfig.IMBUED_INVISIBILITY_HIDE_ITEM.get()) {
			ListTag effectsTag = itemStack.getTag().getList("ImbuedEffects", Tag.TAG_COMPOUND);
			for (Tag inTag : effectsTag) {
				CompoundTag effectTag = (CompoundTag) inTag;
				MobEffectInstance effect = MobEffectInstance.load(effectTag);
				if (effect != null && effect.getEffect() == MobEffects.INVISIBILITY) {
					ci.cancel();
					return;
				}
			}
		}
	}
}
