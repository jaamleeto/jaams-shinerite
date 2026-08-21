package net.jaams.shinerite.client.renderer;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;

import net.jaams.shinerite.entity.ShineriteStarProjectileEntity;
import net.jaams.shinerite.data.ProjectileRenderData;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class ShineriteStarProjectileRenderer extends EntityRenderer<ShineriteStarProjectileEntity> {
	public ShineriteStarProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(ShineriteStarProjectileEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		matrixStack.pushPose();
		ItemStack weapon = entity.getWeaponItem();
		ProjectileRenderData data = new ProjectileRenderData(weapon);
		float scale = data.getFloat("ProjectileScale", 1.0f);
		float rotX = data.getFloat("ProjectileRotX", 0.0f);
		float rotY = data.getFloat("ProjectileRotY", -90.0f);
		float rotZ = data.getFloat("ProjectileRotZ", -135.0f);
		float offsetX = data.getFloat("ProjectileOffsetX", 0.0f);
		float offsetY = data.getFloat("ProjectileOffsetY", -0.175f);
		float offsetZ = data.getFloat("ProjectileOffsetZ", 0.0f);
		float spinSpeed = data.getFloat("ProjectileSpin", 0.0f);
		String spinAxis = data.getString("ProjectileSpinAxis", "Z");
		float spinOffset = data.getFloat("ProjectileSpinOffset", 0.0f);
		float pitchOffset = data.getFloat("ProjectilePitchOffset", 0.0f);
		ItemDisplayContext display = data.getDisplay("ProjectileDisplay", ItemDisplayContext.GROUND);
		matrixStack.scale(scale, scale, scale);
		matrixStack.mulPose(Axis.XP.rotationDegrees(rotX));
		float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
		if (pitch > 45.0F || pitch < -45.0F) {
			matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F + rotY));
		} else {
			matrixStack.mulPose(Axis.YP.rotationDegrees(rotY));
		}
		matrixStack.mulPose(Axis.XP.rotationDegrees(pitchOffset));
		float spinRotation;
		if (spinSpeed > 0 && !entity.hasImpacted()) {
			float velocityFactor = (float) entity.getDeltaMovement().length() * 10f;
			float finalSpinSpeed = spinSpeed + velocityFactor;
			spinRotation = (entity.tickCount + partialTicks) * finalSpinSpeed + spinOffset;
		} else {
			if (entity.getSpinTicks() > 0) {
				spinRotation = (entity.getSpinTicks() + partialTicks) * -60.0f + spinOffset;
			} else {
				spinRotation = entity.getLastSpinRotation() + spinOffset;
			}
		}
		switch (spinAxis.toUpperCase()) {
			case "X" -> matrixStack.mulPose(Axis.XP.rotationDegrees(rotZ + spinRotation));
			case "Y" -> matrixStack.mulPose(Axis.YP.rotationDegrees(rotZ + spinRotation));
			default -> matrixStack.mulPose(Axis.ZP.rotationDegrees(rotZ + spinRotation));
		}
		matrixStack.translate(offsetX, offsetY, offsetZ);
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		itemRenderer.renderStatic(weapon, display, packedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer, entity.level(), entity.getId());
		matrixStack.popPose();
		super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(ShineriteStarProjectileEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
