package net.jaams.shinerite.util;

import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

public class ModTags {
	// Compat
	public static final TagKey<Item> IMBUABLES = create("compat/imbuables");
	public static final TagKey<Item> COATABLES = create("compat/coatables");
	public static final TagKey<Item> DYEABLES = create("compat/dyeables");
	public static final TagKey<Item> WASHABLES = create("compat/washables");
	// Shinerite
	public static final TagKey<Item> SHINERITE = create("shinerite/shinerite_items");
	public static final TagKey<Item> SHINERITE_WEAPONS = create("shinerite/shinerite_weapons");
	public static final TagKey<Item> SHINERITE_ARMOR = create("shinerite/shinerite_armor");
	public static final TagKey<Item> SHINERITE_TOOLS = create("shinerite/shinerite_tools");
	public static final TagKey<Item> SHINERITE_COMPAT = create("shinerite/shinerite_compat");
	public static final TagKey<Item> SHINERITE_FOOD = create("shinerite/shinerite_food");
	public static final TagKey<Item> SHINERITE_PLANTS = create("shinerite/shinerite_plants");
	public static final TagKey<Item> SHINERITE_FLOWERS = create("shinerite/shinerite_flowers");
	public static final TagKey<Item> SHINERITE_BLOCKS = create("shinerite/shinerite_blocks");
	public static final TagKey<Item> SHINERITE_MATERIALS = create("shinerite/shinerite_materials");
	public static final TagKey<Item> SHINERITE_MUSIC_DISCS = create("shinerite/shinerite_music_discs");
	// Weapons
	public static final TagKey<Item> SHINERANGS = create("item/shinerangs");
	public static final TagKey<Item> SHINERITE_STARS = create("item/shinerite_stars");
	// Enchantable
	public static final TagKey<Item> ENCHANTABLE_LIGHTBRINGER = create("enchantable/lightbringer");
	public static final TagKey<Item> ENCHANTABLE_LIGHTSTEAL = create("enchantable/lightsteal");
	public static final TagKey<Item> ENCHANTABLE_LIGHTGUARD = create("enchantable/lightguard");
	// Traits
	public static final TagKey<Item> FRAILGLOW = create("trait/frailglow");

	private static TagKey<Item> create(String path) {
		return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("jaams_shinerite", path));
	}
}
