package net.jaams.shinerite.mixins.client;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;


import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

import net.jaams.shinerite.configuration.client.ItemStatusBarConfig;
import net.jaams.shinerite.util.ModUtils;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
	@Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
	public void renderImbuedUsesBar(Font font, ItemStack itemStack, int x, int y, @Nullable String customText, CallbackInfo ci) {
		if (!ItemStatusBarConfig.SHOW_IMBUED_BAR.get()) {
			return;
		}
		if (itemStack.isEmpty() || itemStack.getMaxStackSize() > 1) {
			return;
		}
		if (itemStack.getItem().isEdible()) {
			return;
		}
		CompoundTag tag = itemStack.getTag();
		if (tag == null) {
			return;
		}
		if (!tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
			return;
		}
		if (!tag.contains("ImbuedUses", Tag.TAG_INT) || !tag.contains("MaxImbuedUses", Tag.TAG_INT)) {
			return;
		}
		if (tag.getInt("ImbuedUses") <= 0) {
			return;
		}
		GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
		guiGraphics.pose().pushPose();
		RenderSystem.disableDepthTest();
		renderImbuedBar(itemStack, x, y);
		RenderSystem.enableDepthTest();
		guiGraphics.pose().popPose();
	}

	private void renderImbuedBar(ItemStack itemStack, int x, int y) {
		CompoundTag tag = itemStack.getOrCreateTag();
		int uses = tag.getInt("ImbuedUses");
		int maxUses = tag.getInt("MaxImbuedUses");
		if (maxUses <= 0) {
			maxUses = 1;
		}
		float pct = (float) uses / (float) maxUses;
		int barWidth = 13;
		int barHeight = 2;
		int barX = x + ItemStatusBarConfig.IMBUED_BAR_X.get();
		int barY = y + ItemStatusBarConfig.IMBUED_BAR_Y.get();
		int w = (int) (pct * barWidth);
		if (w == 0 && pct > 0) {
			w = 1;
		}
		int baseColor = ModUtils.getImbuedColor(itemStack);
		if (baseColor == -1) {
			baseColor = 0x55FFFF;
		}
		int brighter = lightenColor(baseColor);
		GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 200);
		guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF000000);
		guiGraphics.fill(barX, barY, barX + w, barY + barHeight - 1, 0xFF000000 | brighter);
		guiGraphics.pose().popPose();
	}

	private static int lightenColor(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		r = Math.min(255, r + 50);
		g = Math.min(255, g + 50);
		b = Math.min(255, b + 50);
		return (r << 16) | (g << 8) | b;
	}
}
