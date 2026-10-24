package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import net.jaams.shinerite.client.model.ShineriteTrimRenderer;
import net.jaams.shinerite.item.ShineriteArmorItem;

import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;



@Mixin(value = WearableItemLayer.class, remap = false)
public abstract class EpicFightArmorRenderMixin {

	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;

	@Shadow
	@Final
	private TextureAtlas armorTrimAtlas;

	@Unique
	private ItemStack jaams$shineriteArmorStack;

	@Unique
	private EquipmentSlot jaams$shineriteArmorSlot;

	@Unique
	private LivingEntity jaams$entity;

	@Inject(method = "getArmorModel", at = @At("HEAD"), remap = false)
	private void jaams$captureArmorStack(HumanoidArmorLayer originalRenderer, HumanoidModel originalModel,
			Model forgeHooksArmorModel, LivingEntity entityliving, ArmorItem armorItem, ItemStack itemstack,
			EquipmentSlot slot, CallbackInfoReturnable<SkinnedMesh> cir) {
		this.jaams$shineriteArmorStack = itemstack;
		this.jaams$shineriteArmorSlot = slot;
		this.jaams$entity = entityliving;
	}

	@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$renderShineriteArmor(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
			SkinnedMesh model, Armature armature, float r, float g, float b, ResourceLocation armorTexture,
			OpenMatrix4f[] poses, CallbackInfo ci) {
		ItemStack stack = this.jaams$shineriteArmorStack;
		if (stack == null) {
			return;
		}
		ShineriteArmorItem shineriteItem = stack.getItem() instanceof ShineriteArmorItem si ? si : null;
		boolean isShineriteArmor = shineriteItem != null;
		boolean isShineriteTrim = ShineriteTrimRenderer.hasShineriteTrim(stack);
		if (!isShineriteArmor && !isShineriteTrim) {
			return;
		}

		boolean overlayPass = armorTexture != null && armorTexture.getPath().contains("overlay");

		float cr = r;
		float cg = g;
		float cb = b;
		int light = packedLight;
		if (isShineriteArmor && !overlayPass) {
			int color = shineriteItem.getColor(stack);
			cr = (color >> 16 & 255) / 255.0F;
			cg = (color >> 8 & 255) / 255.0F;
			cb = (color & 255) / 255.0F;
			light = FULL_BRIGHT_LIGHT;
		}
		model.draw(poseStack, bufferSource, RenderType.armorCutoutNoCull(armorTexture), light, cr, cg, cb, 1.0F,
				OverlayTexture.NO_OVERLAY, armature, poses);

		if (!overlayPass) {
			this.jaams$drawTrim(poseStack, bufferSource, packedLight, model, armature, this.jaams$entity, stack,
					this.jaams$shineriteArmorSlot != null ? this.jaams$shineriteArmorSlot : EquipmentSlot.CHEST,
					poses);
		}

		ci.cancel();
	}



	@Inject(method = "renderTrim", at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$skipVanillaShineriteTrim(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
			SkinnedMesh model, Armature armature, ArmorMaterial material, ArmorTrim trim, EquipmentSlot slot,
			OpenMatrix4f[] poses, CallbackInfo ci) {
		ItemStack stack = this.jaams$shineriteArmorStack;
		if (stack == null) {
			return;
		}
		if (stack.getItem() instanceof ShineriteArmorItem || ShineriteTrimRenderer.hasShineriteTrim(stack)) {
			ci.cancel();
		}
	}

	@Unique
	private void jaams$drawTrim(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
			SkinnedMesh model, Armature armature, LivingEntity entity, ItemStack stack, EquipmentSlot slot,
			OpenMatrix4f[] poses) {
		if (this.armorTrimAtlas == null || !(stack.getItem() instanceof ArmorItem armorItem) || entity == null) {
			return;
		}
		ArmorTrim trim = ArmorTrim.getTrim(entity.level().registryAccess(), stack).orElse(null);
		if (trim == null) {
			return;
		}

		ArmorMaterial armorMaterial = armorItem.getMaterial();
		boolean inner = slot == EquipmentSlot.LEGS;
		TextureAtlasSprite sprite = jaams$findSprite(inner ? trim.innerTexture(armorMaterial) : trim.outerTexture(armorMaterial));
		if (sprite == null) {
			sprite = ShineriteTrimRenderer.findAnyPalettedSprite(this.armorTrimAtlas, trim.pattern().value().assetId(), inner);
		}
		if (sprite == null) {
			return;
		}

		boolean isShinerite = ShineriteTrimRenderer.isShineriteMaterial(trim);
		int renderLight = isShinerite ? FULL_BRIGHT_LIGHT : packedLight;
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		if (isShinerite) {
			int trimColor = ShineriteTrimRenderer.trimColor(stack);
			r = (trimColor >> 16 & 255) / 255.0F;
			g = (trimColor >> 8 & 255) / 255.0F;
			b = (trimColor & 255) / 255.0F;
		}

		VertexConsumer vertexConsumer = sprite.wrap(
				bufferSource.getBuffer(EpicFightRenderTypes.getTriangulated(Sheets.armorTrimsSheet())));
		model.drawPosed(poseStack, vertexConsumer, Mesh.DrawingFunction.NEW_ENTITY, renderLight, r, g, b, 1.0F,
				OverlayTexture.NO_OVERLAY, armature, poses);
	}

	@Unique
	private TextureAtlasSprite jaams$findSprite(ResourceLocation location) {
		if (this.armorTrimAtlas == null) {
			return null;
		}
		TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(location);
		if (!(sprite instanceof TextureAtlasSprite)
				|| MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
			return null;
		}
		return sprite;
	}
}
