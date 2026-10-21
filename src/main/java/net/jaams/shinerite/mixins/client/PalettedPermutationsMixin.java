package net.jaams.shinerite.mixins.client;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.InputStream;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(PalettedPermutations.class)
public class PalettedPermutationsMixin {

	@Shadow
	@Final
	private java.util.List<ResourceLocation> textures;

	@Shadow
	@Final
	private java.util.Map<String, ResourceLocation> permutations;

	@Shadow
	@Final
	private ResourceLocation paletteKey;

	private static final ResourceLocation SHINERITE_PALETTE = ResourceLocation.fromNamespaceAndPath("minecraft", "trims/color_palettes/shinerite");
	private static final String SHINERITE_KEY = "shinerite";
	private static final String TRIM_ARMOR_PREFIX = "trims/models/armor/";

	@Inject(method = "run", at = @At("TAIL"))
	private void jaams$addShineritePermutations(ResourceManager resourceManager, SpriteSource.Output output, CallbackInfo ci) {
		if (this.permutations.containsKey(SHINERITE_KEY)) {
			return;
		}
		Supplier<int[]> sourcePalette = Suppliers.memoize(() ->
				PalettedPermutations.loadPaletteEntryFromImage(resourceManager, this.paletteKey));
		Supplier<IntUnaryOperator> shineriteMapping = Suppliers.memoize(() ->
				createPaletteMapping(sourcePalette.get(),
						PalettedPermutations.loadPaletteEntryFromImage(resourceManager, SHINERITE_PALETTE)));

		for (ResourceLocation texture : this.textures) {
			if (!texture.getPath().startsWith(TRIM_ARMOR_PREFIX)) {
				continue;
			}
			ResourceLocation file = SpriteSource.TEXTURE_ID_CONVERTER.idToFile(texture);
			java.util.Optional<Resource> resource = resourceManager.getResource(file);
			if (resource.isEmpty()) {
				continue;
			}
			ResourceLocation spriteId = texture.withSuffix("_" + SHINERITE_KEY);
			Resource res = resource.get();
			output.add(spriteId, new ShineriteSpriteSupplier(res, shineriteMapping, spriteId));
		}
	}

	private static IntUnaryOperator createPaletteMapping(int[] sourcePalette, int[] targetPalette) {
		if (targetPalette.length != sourcePalette.length) {
			throw new IllegalArgumentException("Palette size mismatch: " + sourcePalette.length + " vs " + targetPalette.length);
		}
		Int2IntMap map = new Int2IntOpenHashMap(targetPalette.length);
		for (int i = 0; i < sourcePalette.length; i++) {
			int j = sourcePalette[i];
			if (FastColor.ABGR32.alpha(j) != 0) {
				map.put(FastColor.ABGR32.transparent(j), targetPalette[i]);
			}
		}
		return pixel -> {
			int a = FastColor.ABGR32.alpha(pixel);
			if (a == 0) {
				return pixel;
			}
			int t = FastColor.ABGR32.transparent(pixel);
			int c = map.getOrDefault(t, FastColor.ABGR32.opaque(t));
			return FastColor.ABGR32.color(a * FastColor.ABGR32.alpha(c) / 255, c);
		};
	}

	static class ShineriteSpriteSupplier implements SpriteSource.SpriteSupplier {
		private final Resource resource;
		private final Supplier<IntUnaryOperator> palette;
		private final ResourceLocation id;

		ShineriteSpriteSupplier(Resource resource, Supplier<IntUnaryOperator> palette, ResourceLocation id) {
			this.resource = resource;
			this.palette = palette;
			this.id = id;
		}

		@Override
		public SpriteContents get() {
			try (InputStream is = this.resource.open()) {
				NativeImage base = NativeImage.read(is);
				NativeImage mapped = base.mappedCopy(this.palette.get());
				base.close();
				return new SpriteContents(this.id, new FrameSize(mapped.getWidth(), mapped.getHeight()), mapped, AnimationMetadataSection.EMPTY);
			} catch (Exception e) {
				return null;
			}
		}
	}
}
