package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;

import net.jaams.shinerite.client.model.ShineriteTrimRenderer;
import net.jaams.shinerite.item.ShineriteArmorItem;
import net.jaams.shinerite.util.ShineriteTrimCompat;

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

	@Unique
	private T jaams$entity;

	/** The slot being rendered right now, needed to pick the right stack in {@link #jaams$shineriteRenderTrim}. */
	@Unique
	private EquipmentSlot jaams$renderSlot;

	public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(RenderLayerParent<T, M> context, A pInnerModel, A pOuterModel, ModelManager pModelManager, CallbackInfo ci) {
		this.innerModel = pInnerModel;
		this.outerModel = pOuterModel;
		this.armorTrimAtlas = pModelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
	}

	// Explicit descriptor: the layer has two render overloads and only the LivingEntity one is this layer.
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			at = @At("HEAD"))
	private void captureEntity(PoseStack matrices, MultiBufferSource vertexConsumers, int light, T entity, float limbSwing,
			float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch,
			CallbackInfo ci) {
		this.jaams$entity = entity;
	}

	/**
	 * Takes over the piece when it is Shinerite armor or carries a Shinerite trim, to give the trim the mod
	 * glow and the dye color of the ingot. The armor is drawn with the model of the Forge armor model hook and
	 * the trim on that very same model, so it follows the pose and the extra parts of the armor exactly like
	 * vanilla does.
	 *
	 * <p>Pieces of the mods with a fully custom armor renderer are left alone (see {@link ShineriteTrimCompat}),
	 * they end up in {@link #jaams$shineriteRenderTrim} instead.
	 */
	@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
	private void renderArmorPiece(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci) {
		ItemStack armorStack = entity.getItemBySlot(slot);
		this.jaams$renderSlot = slot;
		if (ShineriteTrimCompat.isExcluded(armorStack)) {
			return;
		}
		ShineriteArmorItem shineriteItem = armorStack.getItem() instanceof ShineriteArmorItem si ? si : null;
		boolean isShineriteArmor = shineriteItem != null && shineriteItem.getEquipmentSlot() == slot;
		if (!isShineriteArmor && !ShineriteTrimRenderer.hasShineriteTrim(armorStack)) {
			return;
		}

		A armorModel = (A) (Object) this.getArmorModelHook(entity, armorStack, slot, model);
		this.getParentModel().copyPropertiesTo(armorModel);
		this.setPartVisibility(armorModel, slot);
		renderColoredModel(matrices, vertexConsumers, armorModel, entity, shineriteItem, armorStack, slot, light);
		renderTrimIfNeeded(matrices, vertexConsumers, entity, armorModel, armorStack, slot, light);
		renderGlintIfNeeded(matrices, vertexConsumers, armorStack, armorModel);
		ci.cancel();
	}

	/**
	 * Draws the trim of the taken over piece. Since vanilla is skipped for these pieces, the trim has to be drawn
	 * here whatever its material is, and not only the Shinerite one: {@link ShineriteTrimRenderer} gives the trim
	 * of the Shinerite material the glow and the dye color of the ingot and leaves the other materials as vanilla
	 * draws them.
	 */
	@Unique
	private void renderTrimIfNeeded(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, A armorModel, ItemStack armorStack,
			EquipmentSlot slot, int light) {
		if (!(armorStack.getItem() instanceof ArmorItem armorItem)) {
			return;
		}
		ArmorTrim.getTrim(entity.level().registryAccess(), armorStack).ifPresent(trim -> ShineriteTrimRenderer.draw(matrices, vertexConsumers,
				this.armorTrimAtlas, armorItem.getMaterial(), armorStack, trim, armorModel, this.usesInnerModel(slot), light));
	}

	/**
	 * Vanilla and Forge draw the trims here. When the trim is the Shinerite one it is skipped, because it has
	 * to be drawn full bright and with the color of the ingot; {@link ShineriteTrimRenderer} does it instead,
	 * on the very model the caller passes, so the mods that render their armor by themselves (custom armor
	 * models, extra pieces, ...) get exactly the same result without their renderer having to change.
	 */
	@Inject(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
			at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$shineriteRenderTrim(ArmorMaterial material, PoseStack matrices, MultiBufferSource vertexConsumers,
			int light, ArmorTrim trim, Model model, boolean inner, CallbackInfo ci) {
		if (!ShineriteTrimRenderer.isShineriteMaterial(trim)) {
			return;
		}
		ItemStack stack = findEquippedTrim(trim);
		if (stack != null && ShineriteTrimRenderer.draw(matrices, vertexConsumers, this.armorTrimAtlas, material, stack, trim, model, inner, light)) {
			ci.cancel();
		}
	}

	/** The equipped piece whose trim is the given one, needed because renderTrim does not receive the stack. */
	@Unique
	private ItemStack findEquippedTrim(ArmorTrim trim) {
		T entity = this.jaams$entity;
		if (entity == null) {
			return null;
		}
		// The slot currently being rendered always wins, otherwise a piece with the same trim but another dye
		// color (e.g. an electrum helmet and its red boots) would lend its color to this one.
		ItemStack rendered = this.jaams$renderSlot == null ? ItemStack.EMPTY : entity.getItemBySlot(this.jaams$renderSlot);
		if (matchesTrim(entity, rendered, trim)) {
			return rendered;
		}
		for (ItemStack armor : entity.getArmorSlots()) {
			if (matchesTrim(entity, armor, trim)) {
				return armor;
			}
		}
		return null;
	}

	@Unique
	private static boolean matchesTrim(LivingEntity entity, ItemStack stack, ArmorTrim trim) {
		if (!ShineriteTrimRenderer.hasShineriteTrim(stack)) {
			return false;
		}
		return ArmorTrim.getTrim(entity.level().registryAccess(), stack)
				.map(t -> t.hasPatternAndMaterial(trim.pattern(), trim.material())).orElse(false);
	}

	@Unique
	private void renderColoredModel(PoseStack matrices, MultiBufferSource vertexConsumers, A model, T entity, ShineriteArmorItem shineriteItem, ItemStack armorStack, EquipmentSlot slot, int light) {
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
		// The entity is passed through, some mods pick their armor texture based on it.
		ResourceLocation armorResource = ((HumanoidArmorLayer) (Object) this).getArmorResource(entity, armorStack, slot, null);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(armorResource));
		model.renderToBuffer(matrices, vertexConsumer, renderLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);

		// Leather armor has a second (overlay) pass, exactly like vanilla does. Without it the
		// leather overlay texture is lost whenever we take over the rendering.
		if (armorStack.getItem() instanceof DyeableLeatherItem) {
			ResourceLocation overlayResource = ((HumanoidArmorLayer) (Object) this).getArmorResource(entity, armorStack, slot, "overlay");
			VertexConsumer overlayConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(overlayResource));
			model.renderToBuffer(matrices, overlayConsumer, renderLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		}
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

	/**
	 * Real Forge armor model hook. It has to be shadowed, not stubbed: declaring a method with this
	 * signature here would have overwritten the hook of the layer and every mod that adds parts to its armor
	 * (horns, pauldrons, ...) would have lost them.
	 */
	@Shadow(remap = false)
	protected abstract Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model);

	@Shadow
	protected abstract boolean usesInnerModel(EquipmentSlot slot);

	@Unique
	private void renderGlint(PoseStack matrices, MultiBufferSource vertexConsumers, int light, A armorModel) {
		armorModel.renderToBuffer(matrices, vertexConsumers.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}
}