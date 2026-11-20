package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import net.jaams.shinerite.client.model.ShineriteTrimRenderer;
import net.jaams.shinerite.item.ShineriteArmorItem;

/**
 * Punchy draws the first-person arms itself instead of going through
 * {@code HumanoidArmorLayer}, so the shinerite armor loses both the full-bright
 * light and the dye tint applied to the normal third-person render.
 */
@Mixin(targets = "punchy.client.render.PunchyArmRenderer")
public abstract class PunchyArmorRenderMixin {
	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;

	@Inject(method = "original$renderArmorPart", at = @At("HEAD"), cancellable = true, remap = false)
	private static void jaams$renderShineriteArmorPart(ModelPart part, PoseStack poseStack, MultiBufferSource buffers,
			int light, ResourceLocation texture, float red, float green, float blue, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chestplate.getItem() instanceof ShineriteArmorItem shinerite)) {
			return;
		}
		ResourceLocation expected = ResourceLocation.parse(
				shinerite.getArmorTexture(chestplate, player, EquipmentSlot.CHEST, null));
		if (!expected.equals(texture)) {
			return;
		}

		int color = shinerite.getColor(chestplate);
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		VertexConsumer consumer = buffers.getBuffer(RenderType.armorCutoutNoCull(texture));
		part.render(poseStack, consumer, FULL_BRIGHT_LIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
		ci.cancel();
	}

	@Inject(method = "lambda$renderArmArmor$6", at = @At("HEAD"), cancellable = true, remap = false)
	private static void jaams$renderShineriteTrim(ArmorItem armorItem, ModelPart arm, PoseStack poseStack,
			MultiBufferSource buffers, int light, ArmorTrim trim, CallbackInfo ci) {
		if (!ShineriteTrimRenderer.isShineriteMaterial(trim)) {
			return;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!ShineriteTrimRenderer.hasShineriteTrim(chestplate)) {
			return;
		}

		TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
		TextureAtlasSprite sprite = ShineriteTrimRenderer.findSprite(atlas, trim.outerTexture(armorItem.getMaterial()));
		if (sprite == null) {
			sprite = ShineriteTrimRenderer.findAnyPalettedSprite(atlas, trim.pattern().value().assetId(), false);
		}
		if (sprite == null) {
			return;
		}

		int color = ShineriteTrimRenderer.trimColor(chestplate);
		VertexConsumer consumer = sprite.wrap(buffers.getBuffer(Sheets.armorTrimsSheet()));
		arm.render(poseStack, consumer, FULL_BRIGHT_LIGHT, OverlayTexture.NO_OVERLAY,
				(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
		ci.cancel();
	}
}
