package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;

import net.jaams.shinerite.client.model.ArmorTrimModelGenerator;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.util.ModUtils;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nullable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	@Shadow
	public abstract void render(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model);

	@Shadow
	private ItemModelShaper itemModelShaper;

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void hideInvisibleImbuedShinerite(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
		if (ModUtils.isImbued(itemStack) && MechanicsConfig.IMBUED_INVISIBILITY_HIDE_ITEM.get()) {
			ListTag effectsTag = itemStack.getTag().getList("ImbuedEffects", Tag.TAG_COMPOUND);
			for (Tag inTag : effectsTag) {
				CompoundTag effectTag = (CompoundTag) inTag;
				MobEffectInstance effect = ModUtils.loadImbuedEffect(effectTag);
				if (effect != null && effect.getEffect() == MobEffects.INVISIBILITY) {
					ci.cancel();
					return;
				}
			}
		}
	}



	@Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
	private void shinerite$generateTrimIcon(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity entity, int seed,
			CallbackInfoReturnable<BakedModel> cir) {
		BakedModel resolved = cir.getReturnValue();
		BakedModel base = this.itemModelShaper.getItemModel(itemStack);
		if (base == null || base == this.itemModelShaper.getModelManager().getMissingModel()) {
			return;
		}

		BakedModel generated = ArmorTrimModelGenerator.generate(itemStack, base, resolved);
		if (generated != null) {
			cir.setReturnValue(generated);
		}
	}
}
