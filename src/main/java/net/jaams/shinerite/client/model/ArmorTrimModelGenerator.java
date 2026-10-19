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

/**
 * Runtime generator of the armor trim <b>item icon</b>: the flat sprite that vanilla draws on top of an armor
 * piece in the inventory, on the ground or in the hand.
 *
 * <p>Vanilla ships one hand written model per (armor piece x trim material) pair, which is why armor from other
 * mods never shows a trim. Instead of shipping those models, this generator builds the trim layer on the fly for
 * every trimmable stack: it takes the armor's own model, bakes
 * {@code minecraft:trims/items/&lt;slot&gt;_trim_&lt;palette&gt;} as an extra layer and wraps the result in a
 * {@link TrimmedArmorBakedModel}.
 *
 * <p>The Shinerite palette has one extra step: when a colored Shinerite ingot was used, the smithing recipe
 * freezes that dye color into the stack NBT ({@link ShineriteTrimSmithingRecipe#TAG_TRIM_COLOR}) and the generator
 * bakes it into the trim quads, so the icon always shows the same color as the worn armor.
 */
public final class ArmorTrimModelGenerator {

	/** Flat icon layers of every armor slot, paletted once per trim material by the texture atlas. */
	private static final String ITEM_TRIM_PATH = "trims/items/";
	private static final ResourceLocation TRIM_MODEL_LOCATION = ResourceLocation.fromNamespaceAndPath(JaamsShineriteMod.MODID, "trim_icon");

	/**
	 * The layer is baked with tint index {@code -1} on purpose: an untinted quad is rendered with the vertex
	 * color baked into it, so no item color handler has to be registered for every (possibly foreign) armor item.
	 */
	private static final int UNTINTED_LAYER = -1;
	private static final ModelState IDENTITY_STATE = new ModelState() {
	};

	/** Baked vertex layout: 8 ints per vertex, the RGBA color lives in the int number 3. */
	private static final int VERTEX_INTS = 8;
	private static final int COLOR_INT = 3;

	/** Baked models are cached per base model + sprite + color, they must be dropped on resource reload. */
	private static final Map<BakedModel, Map<String, BakedModel>> CACHE = new IdentityHashMap<>();

	private ArmorTrimModelGenerator() {
	}

	/**
	 * Builds (or reuses) the item model of {@code stack} with its armor trim drawn on top.
	 *
	 * @param base     the plain, non resolved model of the item
	 * @param resolved the model vanilla picked for this stack, i.e. its own trim model when it has one
	 * @return the trimmed model, or {@code null} when there is nothing to generate
	 */
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

		/*
		 * A Shinerite trim always rebuilds on top of the plain model, otherwise the trim would be drawn twice on
		 * armor that already resolves to a trim model of its own. Any other material only steps in when vanilla
		 * resolved nothing, i.e. when the item model has no trim override at all (armor from another mod).
		 */
		if (!shinerite && resolved != null && resolved != base) {
			return null;
		}

		Item item = stack.getItem();
		ResourceLocation spriteLocation = ResourceLocation.fromNamespaceAndPath("minecraft",
				ITEM_TRIM_PATH + slot(item) + "_trim_" + paletteSuffix(trim.material().value(), item));
		TextureAtlasSprite sprite = sprite(mc, spriteLocation);
		if (sprite == null) {
			// No trim sprite for this slot/material, nothing to draw.
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

	/** Drops every cached model, called whenever the resource packs (and thus the models) are reloaded. */
	public static void clearCache() {
		CACHE.clear();
	}

	/** {@code true} when the trim uses the Shinerite material of this mod. */
	public static boolean isShineriteTrim(ArmorTrim trim) {
		return trim.material().unwrapKey().map(key -> key.location().equals(SHINERITE_TRIM_MATERIAL)).orElse(false);
	}

	/** {@code true} when the trim of this stack uses the Shinerite material of this mod. */
	public static boolean hasShineriteTrim(ItemStack stack) {
		if (stack.isEmpty() || stack.getTag() == null || !stack.getTag().contains(ArmorTrim.TAG_TRIM_ID)) {
			return false;
		}
		return SHINERITE_TRIM_MATERIAL.toString()
				.equals(stack.getTag().getCompound(ArmorTrim.TAG_TRIM_ID).getString("material"));
	}

	private static final ResourceLocation SHINERITE_TRIM_MATERIAL = ResourceLocation.fromNamespaceAndPath(JaamsShineriteMod.MODID, "shinerite");

	/** Base trim color of the Shinerite material on the armor of other mods, no tint applied. */
	private static final int SHINERITE_TRIM_COLOR = 0xFFFFFF;
	/** Base trim color on the Shinerite armor itself, which is drawn full bright and needs a readable tint. */
	private static final int SHINERITE_ARMOR_TRIM_COLOR = 0x949494;

	/**
	 * The dye color frozen into the stack by the smithing recipe. An undyed (or white) ingot has no
	 * color stored, in which case the natural color of the Shinerite palette is used instead of plain white,
	 * so the icon always matches the worn trim.
	 */
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

	/**
	 * Multiplies the vertex colors of the quads with the trim color. Since the quads are untinted, vanilla
	 * renders them with this baked color, which works for any item without registering an item color handler.
	 *
	 * <p>A baked vertex is 8 ints: position (3 floats = bytes 0-11), color (4 bytes, RGBA, stored in the int
	 * number 3 as {@code R | G << 8 | B << 16 | A << 24}), UV, overlay, light and normal. The color of the
	 * stack is a plain {@code 0xRRGGBB} value, so its red and blue channels have to be swapped when packing it
	 * into the vertex, otherwise a red trim comes out blue.
	 */
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

	/**
	 * The palette suffix of the trim, honoring the {@code override_armor_materials} field so that e.g. an iron
	 * trim on iron armor keeps using the darker iron palette, exactly like vanilla does.
	 */
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

	/**
	 * The armor slot of an item, used to pick the trim sprite ({@code helmet_trim_*}, {@code boots_trim_*}...).
	 * Items that are not armor pieces fall back to the helmet sprite, which is what vanilla does for the turtle
	 * helmet, and are then guessed from the registry name.
	 */
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