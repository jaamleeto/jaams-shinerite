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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import net.jaams.shinerite.item.ShineriteArmorItem;
import net.jaams.shinerite.recipe.ShineriteTrimSmithingRecipe;
import net.jaams.shinerite.util.ShineriteTrimSupport;

import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;



@Mixin(value = WearableItemLayer.class, remap = false)
public abstract class EpicFightArmorRenderMixin {

	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
	private static final String SHINERITE_TRIM = "jaams_shinerite:shinerite";

	private static final int SHINERITE_TRIM_COLOR = 0xFFFFFF;

	private static final int SHINERITE_ARMOR_TRIM_COLOR = 0x949494;

	@Shadow
	@Final
	private TextureAtlas armorTrimAtlas;

	@Unique
	private ItemStack jaams$shineriteArmorStack;

	@Unique
	private EquipmentSlot jaams$shineriteArmorSlot;

	@Inject(method = "getArmorModel", at = @At("HEAD"), remap = false)
	private void jaams$captureArmorStack(HumanoidArmorLayer originalRenderer, HumanoidModel originalModel,
			Model forgeHooksArmorModel, LivingEntity entityliving, ArmorItem armorItem, ItemStack itemstack,
			EquipmentSlot slot, CallbackInfoReturnable<SkinnedMesh> cir) {
		this.jaams$shineriteArmorStack = itemstack;
		this.jaams$shineriteArmorSlot = slot;
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
		boolean isShineriteTrim = jaams$hasShineriteTrim(stack);
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

		if (isShineriteTrim && !overlayPass) {
			this.jaams$drawTrim(poseStack, bufferSource, packedLight, model, armature, stack,
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
		if (stack != null && jaams$hasShineriteTrim(stack)) {
			ci.cancel();
		}
	}

	@Unique
	private static boolean jaams$hasShineriteTrim(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("Trim", Tag.TAG_COMPOUND)) {
			return false;
		}
		return SHINERITE_TRIM.equals(tag.getCompound("Trim").getString("material"));
	}

	@Unique
	private void jaams$drawTrim(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
			SkinnedMesh model, Armature armature, ItemStack stack, EquipmentSlot slot, OpenMatrix4f[] poses) {
		if (this.armorTrimAtlas == null) {
			return;
		}
		CompoundTag tag = stack.getTag();
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
		String materialName = jaams$idPath(materialId);
		boolean inner = slot == EquipmentSlot.LEGS;

		String namespace = patternId.contains(":") ? patternId.substring(0, patternId.indexOf(':')) : "minecraft";
		String baseSprite = "trims/models/armor/" + jaams$idPath(patternId) + (inner ? "_leggings" : "");
		ResourceLocation spriteId = ResourceLocation.fromNamespaceAndPath(namespace, baseSprite + "_" + materialName);
		TextureAtlasSprite sprite = jaams$findSprite(spriteId);
		boolean palettedSprite = sprite != null;
		if (!palettedSprite) {
			sprite = jaams$findSprite(ResourceLocation.fromNamespaceAndPath(namespace, baseSprite));
		}
		if (sprite == null) {
			return;
		}

		int renderLight = isShinerite ? FULL_BRIGHT_LIGHT : packedLight;
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		if (isShinerite) {
			int trimColor = tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR, Tag.TAG_INT)
					? tag.getInt(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR)
					: 0;
			if (trimColor == 0 || ShineriteTrimSupport.isPlainColor(trimColor)) {
				trimColor = stack.getItem() instanceof ShineriteArmorItem ? SHINERITE_ARMOR_TRIM_COLOR : SHINERITE_TRIM_COLOR;
			}
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
	private static String jaams$idPath(String id) {
		int i = id.indexOf(':');
		return i >= 0 ? id.substring(i + 1) : id;
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
