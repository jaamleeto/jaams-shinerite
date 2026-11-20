package net.jaams.shinerite.mixins.compat;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.mehvahdjukaar.vsc.CutBlockType;
import net.mehvahdjukaar.vsc.CutBlockTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * Vertical Slabs Compat automatically creates a vertical slab for every slab it
 * detects. Our shinerite slabs are registered as real shinerite vertical slabs
 * by this mod instead, so we hide the two source slabs from VSC's detection.
 */
@Mixin(value = CutBlockTypeRegistry.class, remap = false)
public abstract class VSlabDetectionMixin {
	@Inject(method = "detectTypeFromBlock", at = @At("HEAD"), cancellable = true, remap = false)
	private void jaams$skipShineriteSlabDetection(Block block, ResourceLocation id, CallbackInfoReturnable<Optional<CutBlockType>> cir) {
		if (id != null && "jaams_shinerite".equals(id.getNamespace())) {
			String path = id.getPath();
			if ("cut_shinerite_slab".equals(path) || "shinerite_tile_slab".equals(path)) {
				cir.setReturnValue(Optional.empty());
			}
		}
	}
}
