package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.mehvahdjukaar.amendments.client.renderers.JukeboxTileRenderer;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.item.MusicDiscShinyItem;
import net.jaams.shinerite.util.ModUtils;

/**
 * Tints the record rendered by Amendments' jukebox renderer with the color of
 * Shinerite's dyeable/imbued music discs, and renders Shinerite discs at full
 * brightness so they glow in the dark.
 */
@Mixin(value = JukeboxTileRenderer.class, remap = false)
public abstract class AmendmentsJukeboxTileRendererMixin {

	/**
	 * Disc captured in {@code render} when it needs custom tinting (dyeable with a
	 * color) or full-bright rendering (Shinerite discs); null otherwise.
	 */
	@Unique
	private static ItemStack jaams$capturedDisc;

	@Inject(method = "render", at = @At("HEAD"), remap = false)
	private void jaams$captureTintedDisc(JukeboxBlockEntity blockEntity, float partialTick, PoseStack poseStack,
			MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
		ItemStack item = blockEntity.getFirstItem();
		if (!item.isEmpty() && item.getItem() instanceof IDyeableItem
				&& (item.getItem() instanceof MusicDiscShinyItem || ModUtils.getCurrentColor(item) != -1)) {
			jaams$capturedDisc = item;
		} else {
			jaams$capturedDisc = null;
		}
	}

	// Handler must be non-static because the enclosing method "render" is an
	// instance method (Mixin checks the staticness against the containing
	// method, not against the redirected call, even for invokestatic targets).
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/moonlight/api/client/util/VertexUtil;"
			+ "addQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFFII)V"), remap = false)
	private void jaams$tintDiscQuad(VertexConsumer builder, PoseStack poseStack, float x0, float y0, float x1,
			float y1, int lu, int lv) {
		ItemStack disc = jaams$capturedDisc;
		if (disc == null) {
			VertexUtil.addQuad(builder, poseStack, x0, y0, x1, y1, lu, lv);
			return;
		}
		if (disc.getItem() instanceof MusicDiscShinyItem) {
			// Render at full brightness so the disc glows in the dark; the two light
			// coords are the block/sky halves of the packed light (same split the
			// renderer does over LevelRenderer.getLightColor).
			lu = LightTexture.FULL_BRIGHT & 0xFFFF;
			lv = (LightTexture.FULL_BRIGHT >>> 16) & 0xFFFF;
		}
		int color = ModUtils.getCurrentColor(disc);
		if (color != -1) {
			VertexUtil.addQuad(builder, poseStack, x0, y0, x1, y1, FastColor.ARGB32.red(color),
					FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), 255, lu, lv);
			return;
		}
		VertexUtil.addQuad(builder, poseStack, x0, y0, x1, y1, lu, lv);
	}
}
