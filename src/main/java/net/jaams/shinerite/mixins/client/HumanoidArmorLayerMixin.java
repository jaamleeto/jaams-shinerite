package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.A;

import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.HumanoidModel;

import net.jaams.shinerite.item.ShineriteArmorItem;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
	private static final Logger LOGGER = LoggerFactory.getLogger(HumanoidArmorLayerMixin.class);
	@Mutable
	@Shadow
	@Final
	private A innerModel;
	@Mutable
	@Shadow
	@Final
	private A outerModel;
	@Mutable
	@Shadow
	@Final
	private TextureAtlas armorTrimAtlas;

	public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(RenderLayerParent<T, M> context, A pInnerModel, A pOuterModel, ModelManager pModelManager, CallbackInfo ci) {
		this.innerModel = pInnerModel;
		this.outerModel = pOuterModel;
		this.armorTrimAtlas = pModelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
	}

	@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
	private void renderArmorPiece(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci) {
		ItemStack armorStack = entity.getItemBySlot(slot);
		if (!(armorStack.getItem() instanceof ShineriteArmorItem shineriteItem) || shineriteItem.getEquipmentSlot() != slot) {
			return;
		}
		this.getParentModel().copyPropertiesTo(model);
		this.setPartVisibility(model, slot);
		A finalModel = (A) getArmorModelHook(entity, armorStack, slot, model);
		renderColoredModel(matrices, vertexConsumers, finalModel, shineriteItem, armorStack);
		renderTrims(matrices, vertexConsumers, entity, armorStack, finalModel, shineriteItem, light); // Pasa el parámetro light
		renderGlintIfNeeded(matrices, vertexConsumers, armorStack, finalModel);
		ci.cancel();
	}

	@Unique
	private void renderColoredModel(PoseStack matrices, MultiBufferSource vertexConsumers, A model, ShineriteArmorItem shineriteItem, ItemStack armorStack) {
		int color = shineriteItem.getColor(armorStack);
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		ResourceLocation armorResource = ((HumanoidArmorLayer<T, M, A>) (Object) this).getArmorResource(null, armorStack, null, null);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(armorResource));
		model.renderToBuffer(matrices, vertexConsumer, FULL_BRIGHT_LIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
	}

	@Unique
	private void renderTrims(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, ItemStack armorStack, A model, ShineriteArmorItem shineriteItem, int light) {
		if (matrices == null || vertexConsumers == null || entity == null || armorStack == null || model == null || shineriteItem == null) {
			return;
		}
		ArmorTrim.getTrim(entity.level().registryAccess(), armorStack).ifPresent(trim -> {
			boolean usesInnerModel = this.usesInnerModel(shineriteItem.getEquipmentSlot());
			ResourceLocation texture = usesInnerModel ? trim.innerTexture(shineriteItem.getMaterial()) : trim.outerTexture(shineriteItem.getMaterial());
			if (texture != null) {
				TextureAtlasSprite sprite = armorTrimAtlas.getSprite(texture);
				VertexConsumer vertexConsumer = sprite.wrap(vertexConsumers.getBuffer(Sheets.armorTrimsSheet()));
				model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F); // Usa light en lugar de FULL_BRIGHT_LIGHT
			}
		});
	}

	@Unique
	private void renderGlintIfNeeded(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack armorStack, A model) {
		if (!armorStack.hasFoil())
			return;
		try {
			renderGlint(matrices, vertexConsumers, FULL_BRIGHT_LIGHT, model);
		} catch (Exception e) {
			LOGGER.error("Error rendering glint: {}", e.getMessage(), e);
		}
	}

	@Shadow
	protected abstract void setPartVisibility(A model, EquipmentSlot slot);

	@Shadow
	protected abstract boolean usesInnerModel(EquipmentSlot slot);

	@Unique
	private void renderGlint(PoseStack matrices, MultiBufferSource vertexConsumers, int light, A armorModel) {
		armorModel.renderToBuffer(matrices, vertexConsumers.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Unique
	protected net.minecraft.client.model.Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model) {
		return model;
	}
}
