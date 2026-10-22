
package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.item.ShinyRootsItem;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.item.ShineriteSwordItem;
import net.jaams.shinerite.item.ShineriteStarItem;
import net.jaams.shinerite.item.ShineriteShovelItem;
import net.jaams.shinerite.item.ShineritePickaxeItem;
import net.jaams.shinerite.item.ShineriteNuggetItem;
import net.jaams.shinerite.item.ShineriteIngotItem;
import net.jaams.shinerite.item.ShineriteHoeItem;
import net.jaams.shinerite.item.ShineriteAxeItem;
import net.jaams.shinerite.item.ShineriteArmorItem;
import net.jaams.shinerite.item.ShinerangItem;
import net.jaams.shinerite.item.RawShineriteItem;
import net.jaams.shinerite.item.MusicDiscShinyItem;
import net.jaams.shinerite.item.GlowSaladItem;
import net.jaams.shinerite.item.DiscFragmentShinyItem;
import net.jaams.shinerite.component.block.BaseShineriteBlockItem;
import net.jaams.shinerite.JaamsShineriteMod;

public class ModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, JaamsShineriteMod.MODID);
	public static final RegistryObject<Item> SHINERITE_INGOT = REGISTRY.register("shinerite_ingot", () -> new ShineriteIngotItem());
	public static final RegistryObject<Item> RAW_SHINERITE = REGISTRY.register("raw_shinerite", () -> new RawShineriteItem());
	public static final RegistryObject<Item> SHINERITE_NUGGET = REGISTRY.register("shinerite_nugget", () -> new ShineriteNuggetItem());
	public static final RegistryObject<Item> GLOW_SALAD = REGISTRY.register("glow_salad", () -> new GlowSaladItem());
	public static final RegistryObject<Item> SHINERITE_SWORD = REGISTRY.register("shinerite_sword", () -> new ShineriteSwordItem());
	public static final RegistryObject<Item> SHINERITE_AXE = REGISTRY.register("shinerite_axe", () -> new ShineriteAxeItem());
	public static final RegistryObject<Item> SHINERITE_PICKAXE = REGISTRY.register("shinerite_pickaxe", () -> new ShineritePickaxeItem());
	public static final RegistryObject<Item> SHINERITE_HOE = REGISTRY.register("shinerite_hoe", () -> new ShineriteHoeItem());
	public static final RegistryObject<Item> SHINERITE_SHOVEL = REGISTRY.register("shinerite_shovel", () -> new ShineriteShovelItem());
	public static final RegistryObject<Item> SHINERITE_STAR = REGISTRY.register("shinerite_star", () -> new ShineriteStarItem());
	public static final RegistryObject<Item> SHINERANG = REGISTRY.register("shinerang", () -> new ShinerangItem());
	public static final RegistryObject<Item> SHINY_ROOTS = REGISTRY.register("shiny_roots", () -> new ShinyRootsItem());
	public static final RegistryObject<Item> SHINY_PAINT_BRUSH = REGISTRY.register("shiny_paint_brush", () -> new ShinyPaintBrushItem());
	public static final RegistryObject<Item> SHINY_SCRAPER = REGISTRY.register("shiny_scraper", () -> new ShinyScraperItem());
	public static final RegistryObject<Item> SHINERITE_ARMOR_HELMET = REGISTRY.register("shinerite_helmet", () -> new ShineriteArmorItem.Helmet());
	public static final RegistryObject<Item> SHINERITE_ARMOR_CHESTPLATE = REGISTRY.register("shinerite_chestplate", () -> new ShineriteArmorItem.Chestplate());
	public static final RegistryObject<Item> SHINERITE_ARMOR_LEGGINGS = REGISTRY.register("shinerite_leggings", () -> new ShineriteArmorItem.Leggings());
	public static final RegistryObject<Item> SHINERITE_ARMOR_BOOTS = REGISTRY.register("shinerite_boots", () -> new ShineriteArmorItem.Boots());
	public static final RegistryObject<Item> MUSIC_DISC_SHINY = REGISTRY.register("music_disc_shiny", () -> new MusicDiscShinyItem());
	public static final RegistryObject<Item> DISC_FRAGMENT_SHINY = REGISTRY.register("disc_fragment_shiny", () -> new DiscFragmentShinyItem());
	public static final RegistryObject<Item> SHINERITE_BLOCK = REGISTRY.register("shinerite_block", () -> new BaseShineriteBlockItem(ModBlocks.SHINERITE_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<Item> SHINERITE_ORE = REGISTRY.register("shinerite_ore", () -> new BaseShineriteBlockItem(ModBlocks.SHINERITE_ORE.get(), new Item.Properties()));
	public static final RegistryObject<Item> DEEPSLATE_SHINERITE_ORE = REGISTRY.register("deepslate_shinerite_ore", () -> new BaseShineriteBlockItem(ModBlocks.DEEPSLATE_SHINERITE_ORE.get(), new Item.Properties()));
	public static final RegistryObject<Item> NETHER_SHINERITE_ORE = REGISTRY.register("nether_shinerite_ore", () -> new BaseShineriteBlockItem(ModBlocks.NETHER_SHINERITE_ORE.get(), new Item.Properties()));
	public static final RegistryObject<Item> RAW_SHINERITE_BLOCK = REGISTRY.register("raw_shinerite_block", () -> new BaseShineriteBlockItem(ModBlocks.RAW_SHINERITE_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<Item> CUT_SHINERITE = REGISTRY.register("cut_shinerite", () -> new BaseShineriteBlockItem(ModBlocks.CUT_SHINERITE.get(), new Item.Properties()));
	public static final RegistryObject<Item> CUT_SHINERITE_STAIRS = REGISTRY.register("cut_shinerite_stairs", () -> new BaseShineriteBlockItem(ModBlocks.CUT_SHINERITE_STAIRS.get(), new Item.Properties()));
	public static final RegistryObject<Item> CUT_SHINERITE_SLAB = REGISTRY.register("cut_shinerite_slab", () -> new BaseShineriteBlockItem(ModBlocks.CUT_SHINERITE_SLAB.get(), new Item.Properties()));
	public static final RegistryObject<Item> SHINERITE_TILES = REGISTRY.register("shinerite_tiles", () -> new BaseShineriteBlockItem(ModBlocks.SHINERITE_TILES.get(), new Item.Properties()));
	public static final RegistryObject<Item> SHINERITE_TILE_STAIRS = REGISTRY.register("shinerite_tile_stairs", () -> new BaseShineriteBlockItem(ModBlocks.SHINERITE_TILE_STAIRS.get(), new Item.Properties()));
	public static final RegistryObject<Item> SHINERITE_TILE_SLAB = REGISTRY.register("shinerite_tile_slab", () -> new BaseShineriteBlockItem(ModBlocks.SHINERITE_TILE_SLAB.get(), new Item.Properties()));
	public static final RegistryObject<Item> SHINERITE_ROOTS = REGISTRY.register("shinerite_roots", () -> new BaseShineriteBlockItem(ModBlocks.SHINERITE_ROOTS.get(), new Item.Properties()));
	public static final RegistryObject<Item> SHINERITIA = REGISTRY.register("shineritia", () -> new BaseShineriteBlockItem(ModBlocks.SHINERITIA.get(), new Item.Properties()));

	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
