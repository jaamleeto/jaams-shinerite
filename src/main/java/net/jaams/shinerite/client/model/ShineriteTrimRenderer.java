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



public final class ShineriteTrimRenderer {

	public static final String SHINERITE_TRIM = "jaams_shinerite:shinerite";
	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;

	private static final int SHINERITE_TRIM_COLOR = 0xFFFFFF;

	private static final int SHINERITE_ARMOR_TRIM_COLOR = 0x949494;

	private ShineriteTrimRenderer() {
	}

	public static boolean isShineriteMaterial(String materialId) {
		return SHINERITE_TRIM.equals(materialId);
	}

	public static boolean isShineriteMaterial(ArmorTrim trim) {
		return trim != null && isShineriteMaterial(trim.material().unwrapKey().map(key -> key.location().toString()).orElse(""));
	}


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



	public static boolean draw(PoseStack matrices, MultiBufferSource vertexConsumers, TextureAtlas atlas, ArmorMaterial material, ItemStack stack,
			ArmorTrim trim, Model model, boolean inner, int light) {
		if (matrices == null || vertexConsumers == null || atlas == null || model == null || trim == null) {
			return false;
		}

		ResourceLocation pattern = trim.pattern().value().assetId();
		TextureAtlasSprite sprite = findSprite(atlas, inner ? trim.innerTexture(material) : trim.outerTexture(material));
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

	
	private static int trimColor(ItemStack stack) {
		int base = stack != null && stack.getItem() instanceof ShineriteArmorItem ? SHINERITE_ARMOR_TRIM_COLOR : SHINERITE_TRIM_COLOR;
		CompoundTag tag = stack == null ? null : stack.getTag();
		if (tag == null || !tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR, Tag.TAG_INT)) {
			return base;
		}
		int color = tag.getInt(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR);
		return color == 0 || ShineriteTrimSupport.isPlainColor(color) ? base : color;
	}

	
	public static TextureAtlasSprite findSprite(TextureAtlas atlas, ResourceLocation location) {
		if (atlas == null || location == null) {
			return null;
		}
		TextureAtlasSprite sprite = atlas.getSprite(location);
		if (!(sprite instanceof TextureAtlasSprite)
				|| MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
			return null;
		}
		return sprite;
	}

	
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

	
	private static final String[] PALETTE_SUFFIXES = { "iron_darker", "iron", "netherite_darker", "netherite", "gold_darker", "gold",
			"diamond_darker", "diamond", "quartz", "amethyst", "copper", "emerald", "lapis", "redstone" };

	}
