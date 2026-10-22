package net.jaams.shinerite.client.model;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraftforge.client.model.ForgeFaceData;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import net.minecraftforge.registries.ForgeRegistries;

import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.item.ShineriteArmorItem;
import net.jaams.shinerite.recipe.ShineriteTrimSmithingRecipe;
import net.jaams.shinerite.util.ShineriteTrimSupport;



public final class ArmorTrimModelGenerator {


	private static final String ITEM_TRIM_PATH = "trims/items/";
	private static final ResourceLocation TRIM_MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath(JaamsShineriteMod.MODID, "trim_icon");



	private static final int UNTINTED_LAYER = -1;
	private static final ModelState IDENTITY_STATE = new ModelState() {
	};


	private static final int VERTEX_INTS = 8;
	private static final int COLOR_INT = 3;


	private static final Map<BakedModel, Map<String, BakedModel>> CACHE = new IdentityHashMap<>();

	private ArmorTrimModelGenerator() {
	}



	@Nullable
	public static BakedModel generate(ItemStack stack, @Nullable BakedModel base, @Nullable BakedModel resolved) {
		if (stack.isEmpty() || base == null || base.isCustomRenderer()) {
			return null;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return null;
		}

		Optional<ArmorTrim> optionalTrim = ArmorTrim.getTrim(mc.level.registryAccess(), stack);
		if (optionalTrim.isEmpty()) {
			return null;
		}

		ArmorTrim trim = optionalTrim.get();
		boolean shinerite = isShineriteTrim(trim);



		if (!shinerite && resolved != null && resolved != base) {
			return null;
		}

		Item item = stack.getItem();
		ResourceLocation spriteLocation = ResourceLocation.fromNamespaceAndPath("minecraft",
				ITEM_TRIM_PATH + slot(item) + "_trim_" + paletteSuffix(trim.material().value(), item));
		TextureAtlasSprite sprite = sprite(mc, spriteLocation);
		if (sprite == null) {
			return null;
		}

		int color = shinerite ? trimColor(stack) : 0xFFFFFF;
		String key = spriteLocation + "#" + color;
		return cached(base, key, () -> {
			List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(
					UnbakedGeometryHelper.createUnbakedItemElements(UNTINTED_LAYER, sprite.contents(), ForgeFaceData.DEFAULT),
					material -> sprite, IDENTITY_STATE, TRIM_MODEL_LOCATION);
			applyColor(quads, color);
			return new TrimmedArmorBakedModel(base, quads);
		});
	}


	public static void clearCache() {
		CACHE.clear();
	}


	public static boolean isShineriteTrim(ArmorTrim trim) {
		return trim.material().unwrapKey().map(key -> key.location().equals(SHINERITE_TRIM_MATERIAL)).orElse(false);
	}


	public static boolean hasShineriteTrim(ItemStack stack) {
		if (stack.isEmpty() || stack.getTag() == null || !stack.getTag().contains(ArmorTrim.TAG_TRIM_ID)) {
			return false;
		}
		return SHINERITE_TRIM_MATERIAL.toString()
				.equals(stack.getTag().getCompound(ArmorTrim.TAG_TRIM_ID).getString("material"));
	}

	private static final ResourceLocation SHINERITE_TRIM_MATERIAL = ResourceLocation.fromNamespaceAndPath(JaamsShineriteMod.MODID, "shinerite");


	private static final int SHINERITE_TRIM_COLOR = 0xFFFFFF;

	private static final int SHINERITE_ARMOR_TRIM_COLOR = 0x949494;



	private static int trimColor(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		int base = stack.getItem() instanceof ShineriteArmorItem ? SHINERITE_ARMOR_TRIM_COLOR : SHINERITE_TRIM_COLOR;
		if (tag == null || !tag.contains(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR)) {
			return base;
		}
		int color = tag.getInt(ShineriteTrimSmithingRecipe.TAG_TRIM_COLOR);
		return color == 0 || ShineriteTrimSupport.isPlainColor(color) ? base : color;
	}

	private static BakedModel cached(BakedModel base, String key, Supplier<BakedModel> supplier) {
		return CACHE.computeIfAbsent(base, k -> new HashMap<>()).computeIfAbsent(key, k -> supplier.get());
	}

	@Nullable
	private static TextureAtlasSprite sprite(Minecraft mc, ResourceLocation location) {
		TextureAtlasSprite sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(location);
		if (!(sprite instanceof TextureAtlasSprite)
				|| MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
			return null;
		}
		return sprite;
	}



	private static void applyColor(List<BakedQuad> quads, int color) {
		if (color == 0xFFFFFF) {
			return;
		}

		int rgb = (color & 0xFF) << 16 | color & 0x00FF00 | (color >> 16 & 0xFF);

		for (BakedQuad quad : quads) {
			int[] vertices = quad.getVertices();
			if (vertices.length % VERTEX_INTS != 0) {
				continue;
			}
			for (int i = COLOR_INT; i < vertices.length; i += VERTEX_INTS) {
				vertices[i] = (vertices[i] & 0xFF000000) | rgb;
			}
		}
	}



	private static String paletteSuffix(TrimMaterial material, Item item) {
		Map<ArmorMaterials, String> overrides = material.overrideArmorMaterials();
		if (!overrides.isEmpty() && item instanceof ArmorItem armorItem) {
			ArmorMaterial armorMaterial = armorItem.getMaterial();
			if (armorMaterial instanceof ArmorMaterials vanillaMaterial) {
				String override = overrides.get(vanillaMaterial);
				if (override != null) {
					return override;
				}
			}
		}
		return material.assetName();
	}



	private static String slot(Item item) {
		if (item instanceof ArmorItem armorItem) {
			return switch (armorItem.getType()) {
			case BOOTS -> "boots";
			case CHESTPLATE -> "chestplate";
			case HELMET -> "helmet";
			case LEGGINGS -> "leggings";
			};
		}

		if (item == Items.TURTLE_HELMET) {
			return "helmet";
		}

		ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
		String path = id == null ? "" : id.getPath();
		if (path.contains("chestplate")) {
			return "chestplate";
		}
		if (path.contains("leggings") || path.contains("_legs") || path.endsWith("pants")) {
			return "leggings";
		}
		if (path.contains("boots") || path.contains("shoes")) {
			return "boots";
		}
		return "helmet";
	}

	}
