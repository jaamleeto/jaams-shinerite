package net.jaams.shinerite.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.jaams.shinerite.item.ShineriteArmorItem;
import net.jaams.shinerite.recipe.ShineriteTrimSmithingRecipe;
import net.jaams.shinerite.util.ShineriteTrimSupport;

/**
 * Draws the armor trim of a worn piece on the armor model of the layer.
 *
 * <p>Every trim material is supported, because the mixin takes over the whole piece when the armor is Shinerite
 * armor (or carries a Shinerite trim) and therefore has to draw the trim of any material itself. A trim of another
 * material is drawn exactly like vanilla does it, with the light of the piece and no tint; the trim of the
 * Shinerite material is the only special one: it is drawn full bright and tinted with the color the smithing recipe
 * froze into the stack.
 *
 * <p>It is shared by every path that can end up drawing a trim, so the worn trim of an armor is always drawn the
 * same way: the armor layer mixin and the {@code renderTrim} of Forge, which is what the mods with their own armor
 * renderer (custom models, extra pieces, ...) end up calling with their own model.
 */
public final class ShineriteTrimRenderer {

	public static final String SHINERITE_TRIM = "jaams_shinerite:shinerite";
	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
	/** Base trim color of the Shinerite material on the armor of other mods, no tint applied. */
	private static final int SHINERITE_TRIM_COLOR = 0xFFFFFF;
	/** Base trim color on the Shinerite armor itself, which is drawn full bright and needs a readable tint. */
	private static final int SHINERITE_ARMOR_TRIM_COLOR = 0x949494;

	private ShineriteTrimRenderer() {
	}

	public static boolean isShineriteMaterial(String materialId) {
		return SHINERITE_TRIM.equals(materialId);
	}

	public static boolean isShineriteMaterial(ArmorTrim trim) {
		return trim != null && isShineriteMaterial(trim.material().unwrapKey().map(key -> key.location().toString()).orElse(""));
	}

	/** {@code true} when the trim stored in the stack uses the Shinerite material of this mod. */
	public static boolean hasShineriteTrim(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("Trim", Tag.TAG_COMPOUND)) {
			return false;
		}
		return isShineriteMaterial(tag.getCompound("Trim").getString("material"));
	}

	/**
	 * Draws the trim of {@code stack} on {@code model}, whatever the material of the trim is.
	 *
	 * @param material the armor material of the worn piece, it picks the palette override of the trim material
	 * @param trim     the trim to draw, as resolved from the stack
	 * @return {@code false} when there is no trim to draw or no texture for it, nothing is drawn then
	 */
	public static boolean draw(PoseStack matrices, MultiBufferSource vertexConsumers, TextureAtlas atlas, ArmorMaterial material, ItemStack stack,
			ArmorTrim trim, Model model, boolean inner, int light) {
		if (matrices == null || vertexConsumers == null || atlas == null || model == null || trim == null) {
			return false;
		}

		// Trim patterns of other mods keep their own namespace, the sprite is not in minecraft's folder.
		ResourceLocation pattern = trim.pattern().value().assetId();
		TextureAtlasSprite sprite = findSprite(atlas, inner ? trim.innerTexture(material) : trim.outerTexture(material));
		// The atlas source of that mod may not know our trim material, so its paletted sprite does not exist.
		// Any other paletted variant of the pattern does: drawing it full bright and tinted with the Shinerite
		// color (the color of a trim of the Shinerite material) still colors it like expected.
		if (sprite == null) {
			sprite = findAnyPalettedSprite(atlas, pattern, inner);
		}
		if (sprite == null) {
			return false;
		}

		int color = 0xFFFFFF;
		int renderLight = light;
		if (isShineriteMaterial(trim)) {
			renderLight = FULL_BRIGHT_LIGHT;
			color = trimColor(stack);
		}

		VertexConsumer vertexConsumer = sprite.wrap(vertexConsumers.getBuffer(Sheets.armorTrimsSheet()));
		model.renderToBuffer(matrices, vertexConsumer, renderLight, OverlayTexture.NO_OVERLAY, (color >> 16 & 255) / 255.0F,
				(color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
		return true;
	}

	/**
	 * The dye color frozen into the stack by the smithing recipe. An undyed (or white) ingot has no color stored,
	 * in which case the natural color of the Shinerite palette is used instead of plain white, so the worn trim
	 * always matches the icon.
	 */
	private static int trimColor(ItemStack stack) {
		int base = stack != null && stack.getItem() instanceof ShineriteArmorItem ? SHINERITE_ARMOR_TRIM_COLOR : SHINERITE_TRIM_COLOR;
		CompoundTag tag = stack == null ? null : stack.getTag();
		if (tag == null || !tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR, Tag.TAG_INT)) {
			return base;
		}
		int color = tag.getInt(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR);
		return color == 0 || ShineriteTrimSupport.isPlainColor(color) ? base : color;
	}

	/** Looks a sprite up, treating the missing texture sprite as absent. */
	public static TextureAtlasSprite findSprite(TextureAtlas atlas, ResourceLocation location) {
		if (atlas == null || location == null) {
			return null;
		}
		TextureAtlasSprite sprite = atlas.getSprite(location);
		// The atlas hands out the missing sprite instead of null, and it is only a plain Sprite, so it can
		// only be recognized by its type: drawing it would show the pink and black square.
		if (!(sprite instanceof TextureAtlasSprite)
				|| MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
			return null;
		}
		return sprite;
	}

	/**
	 * Looks a sprite of the pattern up for a material the atlas of its owner actually knows. The proxy list
	 * holds the palette suffixes of vanilla: the textures of the trims of virtually every mod are drawn
	 * against the vanilla palette, so one of those variants exists even when its own material is unknown.
	 */
	private static TextureAtlasSprite findAnyPalettedSprite(TextureAtlas atlas, ResourceLocation pattern, boolean inner) {
		String baseSprite = "trims/models/armor/" + pattern.getPath() + (inner ? "_leggings" : "");
		for (String suffix : PALETTE_SUFFIXES) {
			TextureAtlasSprite sprite = findSprite(atlas, ResourceLocation.fromNamespaceAndPath(pattern.getNamespace(), baseSprite + "_" + suffix));
			if (sprite != null) {
				return sprite;
			}
		}
		return null;
	}

	/** Palette suffixes of vanilla, in the order they are picked as proxy for an unknown material. */
	private static final String[] PALETTE_SUFFIXES = { "iron_darker", "iron", "netherite_darker", "netherite", "gold_darker", "gold",
			"diamond_darker", "diamond", "quartz", "amethyst", "copper", "emerald", "lapis", "redstone" };

	}