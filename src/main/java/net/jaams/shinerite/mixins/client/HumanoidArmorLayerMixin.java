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

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.jaams.shinerite.recipe.ShineriteTrimSmithingRecipe;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
	private static final String SHINERITE_TRIM = "jaams_shinerite:shinerite";
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
		ShineriteArmorItem shineriteItem = armorStack.getItem() instanceof ShineriteArmorItem si ? si : null;
		boolean isShineriteArmor = shineriteItem != null && shineriteItem.getEquipmentSlot() == slot;
		boolean isShineriteTrim = hasShineriteTrim(armorStack);
		if (!isShineriteArmor && !isShineriteTrim) {
			return;
		}
		this.getParentModel().copyPropertiesTo(model);
		this.setPartVisibility(model, slot);
		A finalModel = (A) getArmorModelHook(entity, armorStack, slot, model);
		renderColoredModel(matrices, vertexConsumers, finalModel, shineriteItem, armorStack, slot, light);
		renderTrims(matrices, vertexConsumers, entity, armorStack, finalModel, slot, light); // Pasa el parámetro light
		renderGlintIfNeeded(matrices, vertexConsumers, armorStack, finalModel);
		ci.cancel();
	}

	@Unique
	private void renderColoredModel(PoseStack matrices, MultiBufferSource vertexConsumers, A model, ShineriteArmorItem shineriteItem, ItemStack armorStack, EquipmentSlot slot, int light) {
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		int renderLight = light;
		if (shineriteItem != null) {
			int color = shineriteItem.getColor(armorStack);
			r = (color >> 16 & 255) / 255.0F;
			g = (color >> 8 & 255) / 255.0F;
			b = (color & 255) / 255.0F;
			renderLight = FULL_BRIGHT_LIGHT;
		} else if (armorStack.getItem() instanceof DyeableLeatherItem leather) {
			int color = leather.getColor(armorStack);
			r = (color >> 16 & 255) / 255.0F;
			g = (color >> 8 & 255) / 255.0F;
			b = (color & 255) / 255.0F;
		}
		ResourceLocation armorResource = ((HumanoidArmorLayer<T, M, A>) (Object) this).getArmorResource(null, armorStack, slot, null);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(armorResource));
		model.renderToBuffer(matrices, vertexConsumer, renderLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);

		// Leather armor has a second (overlay) pass, exactly like vanilla does. Without it the
		// leather overlay texture is lost whenever we take over the rendering.
		if (armorStack.getItem() instanceof DyeableLeatherItem) {
			ResourceLocation overlayResource = ((HumanoidArmorLayer<T, M, A>) (Object) this).getArmorResource(null, armorStack, slot, "overlay");
			VertexConsumer overlayConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(overlayResource));
			model.renderToBuffer(matrices, overlayConsumer, renderLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	@Unique
	private static String lastDiag = "";

	@Unique
	private static void logDiag(String message) {
		if (!message.equals(lastDiag)) {
			lastDiag = message;
			LOGGER.info("[ShineriteTrim] {}", message);
		}
	}

	@Unique
	private void renderTrims(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, ItemStack armorStack, A model, EquipmentSlot slot, int light) {
		if (matrices == null || vertexConsumers == null || armorStack == null || model == null || armorTrimAtlas == null) {
			return;
		}
		CompoundTag tag = armorStack.getTag();
		if (tag == null || !tag.contains("Trim", Tag.TAG_COMPOUND)) {
			return;
		}
		CompoundTag trimTag = tag.getCompound("Trim");
		String materialId = trimTag.getString("material");
		String patternId = trimTag.getString("pattern");
		if (materialId.isEmpty() || patternId.isEmpty()) {
			return;
		}
		boolean isShinerite = SHINERITE_TRIM.equals(materialId);
		String materialName = idPath(materialId);
		String patternName = idPath(patternId);
		boolean inner = this.usesInnerModel(slot);
		ResourceLocation spriteId = ResourceLocation.fromNamespaceAndPath("minecraft",
				"trims/models/armor/" + patternName + (inner ? "_leggings" : "") + "_" + materialName);
		TextureAtlasSprite sprite = armorTrimAtlas.getSprite(spriteId);
		if (sprite == null) {
			logDiag("sprite MISSING for " + spriteId);
			return;
		}
		int renderLight = light;
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		if (isShinerite) {
			renderLight = FULL_BRIGHT_LIGHT;
			if (tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR, Tag.TAG_INT)) {
				int color = tag.getInt(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR);
				r = (color >> 16 & 255) / 255.0F;
				g = (color >> 8 & 255) / 255.0F;
				b = (color & 255) / 255.0F;
			}
		}
		logDiag("sprite=" + spriteId + " isShin=" + isShinerite + " colorTag=" + tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR, Tag.TAG_INT));
		VertexConsumer vertexConsumer = sprite.wrap(vertexConsumers.getBuffer(Sheets.armorTrimsSheet()));
		model.renderToBuffer(matrices, vertexConsumer, renderLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
	}

	@Unique
	private static boolean hasShineriteTrim(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("Trim", Tag.TAG_COMPOUND)) {
			return false;
		}
		return SHINERITE_TRIM.equals(tag.getCompound("Trim").getString("material"));
	}

	@Unique
	private static String idPath(String id) {
		int i = id.indexOf(':');
		return i >= 0 ? id.substring(i + 1) : id;
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
