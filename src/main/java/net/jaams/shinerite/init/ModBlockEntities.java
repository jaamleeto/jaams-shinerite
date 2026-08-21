package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

import net.jaams.shinerite.JaamsShineriteMod;

import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;

public class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, JaamsShineriteMod.MODID);
	public static final RegistryObject<BlockEntityType<?>> SHINERITE_BLOCK = register("shinerite_block", ModBlocks.SHINERITE_BLOCK, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.SHINERITE_BLOCK.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> SHINERITE_ORE = register("shinerite_ore", ModBlocks.SHINERITE_ORE, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.SHINERITE_ORE.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> DEEPSLATE_SHINERITE_ORE = register("deepslate_shinerite_ore", ModBlocks.DEEPSLATE_SHINERITE_ORE, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.DEEPSLATE_SHINERITE_ORE.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> NETHER_SHINERITE_ORE = register("nether_shinerite_ore", ModBlocks.NETHER_SHINERITE_ORE, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.NETHER_SHINERITE_ORE.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> RAW_SHINERITE_BLOCK = register("raw_shinerite_block", ModBlocks.RAW_SHINERITE_BLOCK, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.RAW_SHINERITE_BLOCK.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> CUT_SHINERITE = register("cut_shinerite", ModBlocks.CUT_SHINERITE, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.CUT_SHINERITE.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> CUT_SHINERITE_STAIRS = register("cut_shinerite_stairs", ModBlocks.CUT_SHINERITE_STAIRS, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.CUT_SHINERITE_STAIRS.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> CUT_SHINERITE_SLAB = register("cut_shinerite_slab", ModBlocks.CUT_SHINERITE_SLAB, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.CUT_SHINERITE_SLAB.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> SHINERITE_TILES = register("shinerite_tiles", ModBlocks.SHINERITE_TILES, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.SHINERITE_TILES.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> SHINERITE_TILE_STAIRS = register("shinerite_tile_stairs", ModBlocks.SHINERITE_TILE_STAIRS, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.SHINERITE_TILE_STAIRS.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> SHINERITE_TILE_SLAB = register("shinerite_tile_slab", ModBlocks.SHINERITE_TILE_SLAB, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.SHINERITE_TILE_SLAB.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> SHINERITE_ROOTS = register("shinerite_roots", ModBlocks.SHINERITE_ROOTS, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.SHINERITE_ROOTS.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> SHINERITIA = register("shineritia", ModBlocks.SHINERITIA, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.SHINERITIA.get(), pos, state));
	public static final RegistryObject<BlockEntityType<?>> POTTED_SHINERITIA = register("potted_shineritia", ModBlocks.POTTED_SHINERITIA, (pos, state) -> new BaseShineriteBlockEntity(ModBlockEntities.POTTED_SHINERITIA.get(), pos, state));

	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}