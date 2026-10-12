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

import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;

/**
 * Makes Shinerite armor (and its armor trim) render with the mod's stored colors and a full-bright
 * (emissive) look while Epic Fight draws the player with its own skinned mesh.
 *
 * <p>Epic Fight's {@link WearableItemLayer} only applies a per-stack tint when the equipped armor
 * implements vanilla's {@code DyeableLeatherItem}. Shinerite armor uses the mod's own
 * {@link net.jaams.shinerite.dyeable.IDyeableItem} color storage, so Epic Fight falls back to an
 * untinted (white) draw and also uses the world light, leaving the armor dark in the shadows.
 *
 * <p>The trim layer is drawn here directly from the armor's NBT (pattern + material ids) instead
 * of relying on {@code ArmorTrim.getTrim}, because that lookup can fail on the client when the
 * custom trim material is not present in the client's synced registry. This guarantees the Shinerite
 * trim always renders, and always renders full-bright with the dye color captured from the ingot at
 * smithing time.
 */
@Mixin(value = WearableItemLayer.class, remap = false)
public abstract class EpicFightArmorRenderMixin {

	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
	private static final String SHINERITE_TRIM = "jaams_shinerite:shinerite";

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

		// Epic Fight calls renderArmor twice for DyeableLeatherItem (base pass + overlay pass).
		// The trim must only be drawn once, on the base pass, otherwise it is drawn twice.
		boolean overlayPass = armorTexture != null && armorTexture.getPath().contains("overlay");

		// ---- base pass ----
		// Order matters: the armor piece has to be drawn BEFORE the trim, otherwise the piece
		// covers the trim layer (which is what made the Shinerite trims disappear).
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

		// ---- trim pass (after the armor piece) ----
		if (isShineriteTrim && !overlayPass) {
			this.jaams$drawTrim(poseStack, bufferSource, packedLight, model, armature, stack,
					this.jaams$shineriteArmorSlot != null ? this.jaams$shineriteArmorSlot : EquipmentSlot.CHEST,
					poses);
		}

		ci.cancel();
	}

	/**
	 * Epic Fight draws its own trim via {@code renderTrim}, using the world light and a hardcoded
	 * white color. The Shinerite trim is already drawn right after the armor piece in
	 * {@code renderArmor} (so it also works when the trim material is missing from the client
	 * registry), therefore skip Epic Fight's unlit/white trim draw to avoid drawing it twice.
	 */
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
		String patternName = jaams$idPath(patternId);
		boolean inner = slot == EquipmentSlot.LEGS;
		ResourceLocation spriteId = ResourceLocation.fromNamespaceAndPath("minecraft",
				"trims/models/armor/" + patternName + (inner ? "_leggings" : "") + "_" + materialName);
		TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(spriteId);
		if (sprite == null) {
			return;
		}

		int renderLight = isShinerite ? FULL_BRIGHT_LIGHT : packedLight;
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
		if (isShinerite && tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR, Tag.TAG_INT)) {
			int trimColor = tag.getInt(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR);
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
}
