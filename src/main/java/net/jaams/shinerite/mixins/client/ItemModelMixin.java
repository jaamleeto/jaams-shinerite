package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import net.jaams.shinerite.init.ModItems;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(ItemRenderer.class)
public abstract class ItemModelMixin {
	@Shadow
	public void render(ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model) {
	}

	@ModifyVariable(method = "renderModelLists", at = @At("HEAD"), ordinal = 0)
	private int modifyLightForShineriteItems(int light, BakedModel model, ItemStack itemStack) {
		if (isShineriteItem(itemStack)) {
			return 0xF000F0;
		}
		return light;
	}

	private boolean isShineriteItem(ItemStack itemStack) {
		return itemStack.is(ModItems.SHINERITE_ARMOR_HELMET.get()) || itemStack.is(ModItems.SHINERITE_ARMOR_CHESTPLATE.get()) || itemStack.is(ModItems.SHINERITE_ARMOR_LEGGINGS.get())
				|| itemStack.is(ModItems.SHINERITE_ARMOR_BOOTS.get());
	}
}
