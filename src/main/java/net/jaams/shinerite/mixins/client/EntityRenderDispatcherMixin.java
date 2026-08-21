package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;

import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Inject(method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", shift = At.Shift.BEFORE))
	private void modifyGlowingColor(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
		if (entity.isCurrentlyGlowing() && bufferSource instanceof OutlineBufferSource outlineBuffer) {
			entity.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
				if (glowingColor.hasCustomColor()) {
					int color = glowingColor.getColor();
					int r = (color >> 16) & 255;
					int g = (color >> 8) & 255;
					int b = color & 255;
					outlineBuffer.setColor(r, g, b, 255);
				} else {
					outlineBuffer.setColor(255, 255, 255, 255);
				}
			});
		}
	}
}
