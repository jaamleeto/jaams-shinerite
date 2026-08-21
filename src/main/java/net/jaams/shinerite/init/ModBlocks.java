package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.jaams.shinerite.block.ShineritiaBlock;
import net.jaams.shinerite.block.PottedShineritiaBlock;
import net.jaams.shinerite.block.ShineriteTilesBlock;
import net.jaams.shinerite.block.ShineriteTileStairsBlock;
import net.jaams.shinerite.block.ShineriteTileSlabBlock;
import net.jaams.shinerite.block.ShineriteRootsBlock;
import net.jaams.shinerite.block.ShineriteOreBlock;
import net.jaams.shinerite.block.ShineriteBlock;
import net.jaams.shinerite.block.RawShineriteOreBlock;
import net.jaams.shinerite.block.NetherShineriteOreBlock;
import net.jaams.shinerite.block.DeepslateShineriteOreBlock;
import net.jaams.shinerite.block.CutShineriteStairsBlock;
import net.jaams.shinerite.block.CutShineriteSlabBlock;
import net.jaams.shinerite.block.CutShineriteBlock;
import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.JaamsShineriteMod;

public class ModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, JaamsShineriteMod.MODID);
	public static final RegistryObject<Block> SHINERITE_BLOCK = REGISTRY.register("shinerite_block", () -> new ShineriteBlock());
	public static final RegistryObject<Block> SHINERITE_ORE = REGISTRY.register("shinerite_ore", () -> new ShineriteOreBlock());
	public static final RegistryObject<Block> DEEPSLATE_SHINERITE_ORE = REGISTRY.register("deepslate_shinerite_ore", () -> new DeepslateShineriteOreBlock());
	public static final RegistryObject<Block> NETHER_SHINERITE_ORE = REGISTRY.register("nether_shinerite_ore", () -> new NetherShineriteOreBlock());
	public static final RegistryObject<Block> RAW_SHINERITE_BLOCK = REGISTRY.register("raw_shinerite_block", () -> new RawShineriteOreBlock());
	public static final RegistryObject<Block> CUT_SHINERITE = REGISTRY.register("cut_shinerite", () -> new CutShineriteBlock());
	public static final RegistryObject<Block> CUT_SHINERITE_STAIRS = REGISTRY.register("cut_shinerite_stairs", () -> new CutShineriteStairsBlock());
	public static final RegistryObject<Block> CUT_SHINERITE_SLAB = REGISTRY.register("cut_shinerite_slab", () -> new CutShineriteSlabBlock());
	public static final RegistryObject<Block> SHINERITE_TILES = REGISTRY.register("shinerite_tiles", () -> new ShineriteTilesBlock());
	public static final RegistryObject<Block> SHINERITE_TILE_STAIRS = REGISTRY.register("shinerite_tile_stairs", () -> new ShineriteTileStairsBlock());
	public static final RegistryObject<Block> SHINERITE_TILE_SLAB = REGISTRY.register("shinerite_tile_slab", () -> new ShineriteTileSlabBlock());
	public static final RegistryObject<Block> SHINERITE_ROOTS = REGISTRY.register("shinerite_roots", () -> new ShineriteRootsBlock());
	public static final RegistryObject<Block> SHINERITIA = REGISTRY.register("shineritia", () -> new ShineritiaBlock());
	public static final RegistryObject<Block> POTTED_SHINERITIA = REGISTRY.register("potted_shineritia", () -> new PottedShineritiaBlock(ModBlockEntities.POTTED_SHINERITIA));
}
