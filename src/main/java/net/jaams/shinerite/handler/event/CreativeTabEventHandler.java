package net.jaams.shinerite.handler.event;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.configuration.client.CreativeTabConfig;
import net.jaams.shinerite.init.ModItems;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabEventHandler {
	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (!CreativeTabConfig.ADD_ITEMS_TO_VANILLA_TABS.get())
			return;

		MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries = tabData.getEntries();
		if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			putAfter(entries, Items.GOLDEN_SWORD, ModItems.SHINERITE_SWORD);
			putAfter(entries, Items.GOLDEN_AXE, ModItems.SHINERITE_AXE);
			putAfter(entries, Items.GOLDEN_BOOTS, ModItems.SHINERITE_ARMOR_HELMET);
			putAfter(entries, ModItems.SHINERITE_ARMOR_HELMET.get(), ModItems.SHINERITE_ARMOR_CHESTPLATE);
			putAfter(entries, ModItems.SHINERITE_ARMOR_CHESTPLATE.get(), ModItems.SHINERITE_ARMOR_LEGGINGS);
			putAfter(entries, ModItems.SHINERITE_ARMOR_LEGGINGS.get(), ModItems.SHINERITE_ARMOR_BOOTS);
			putAfter(entries, Items.BOW, ModItems.SHINERANG);
			putAfter(entries, ModItems.SHINERANG.get(), ModItems.SHINERITE_STAR);
		}
		if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			putAfter(entries, Items.GOLDEN_PICKAXE, ModItems.SHINERITE_PICKAXE);
			putAfter(entries, Items.GOLDEN_AXE, ModItems.SHINERITE_AXE);
			putAfter(entries, Items.GOLDEN_SHOVEL, ModItems.SHINERITE_SHOVEL);
			putAfter(entries, Items.GOLDEN_HOE, ModItems.SHINERITE_HOE);
			putAfter(entries, Items.BRUSH, ModItems.SHINY_PAINT_BRUSH);
			putAfter(entries, ModItems.SHINY_PAINT_BRUSH.get(), ModItems.SHINY_SCRAPER);
		}
		if (tabData.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
			putAfter(entries, Items.GOLD_BLOCK, ModItems.SHINERITE_BLOCK);
			putAfter(entries, ModItems.SHINERITE_BLOCK.get(), ModItems.CUT_SHINERITE);
			putAfter(entries, ModItems.CUT_SHINERITE.get(), ModItems.CUT_SHINERITE_STAIRS);
			putAfter(entries, ModItems.CUT_SHINERITE_STAIRS.get(), ModItems.CUT_SHINERITE_SLAB);
			putAfter(entries, ModItems.CUT_SHINERITE_SLAB.get(), ModItems.CUT_SHINERITE_VERTICAL_SLAB);
			putAfter(entries, ModItems.CUT_SHINERITE_VERTICAL_SLAB.get(), ModItems.SHINERITE_TILES);
			putAfter(entries, ModItems.SHINERITE_TILES.get(), ModItems.SHINERITE_TILE_STAIRS);
			putAfter(entries, ModItems.SHINERITE_TILE_STAIRS.get(), ModItems.SHINERITE_TILE_SLAB);
			putAfter(entries, ModItems.SHINERITE_TILE_SLAB.get(), ModItems.SHINERITE_TILE_VERTICAL_SLAB);
		}
		if (tabData.getTabKey() == CreativeModeTabs.INGREDIENTS) {
			putAfter(entries, Items.GOLD_INGOT, ModItems.SHINERITE_INGOT);
			putAfter(entries, Items.GOLD_NUGGET, ModItems.SHINERITE_NUGGET);
			putAfter(entries, Items.RAW_GOLD, ModItems.RAW_SHINERITE);
			putAfter(entries, Items.DISC_FRAGMENT_5, ModItems.DISC_FRAGMENT_SHINY);
		}
		if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
			putAfter(entries, Items.GOLD_ORE, ModItems.SHINERITE_ORE);
			putAfter(entries, ModItems.SHINERITE_ORE.get(), ModItems.DEEPSLATE_SHINERITE_ORE);
			putAfter(entries, ModItems.DEEPSLATE_SHINERITE_ORE.get(), ModItems.NETHER_SHINERITE_ORE);
			putAfter(entries, Items.RAW_GOLD_BLOCK, ModItems.RAW_SHINERITE_BLOCK);
			putAfter(entries, Items.GLOW_LICHEN, ModItems.SHINERITE_ROOTS);
			putAfter(entries, ModItems.SHINERITE_ROOTS.get(), ModItems.SHINERITIA);
		}
		if (tabData.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
			putAfter(entries, Items.GLOW_BERRIES, ModItems.GLOW_SALAD);
			putAfter(entries, ModItems.GLOW_SALAD.get(), ModItems.SHINY_ROOTS);
		}
		if (tabData.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			putAfter(entries, Items.MUSIC_DISC_5, ModItems.MUSIC_DISC_SHINY);
		}
	}

	private static void putAfter(MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries, ItemLike after, Supplier<? extends ItemLike> supplier) {
		ItemLike key = supplier.get();
		if (!entries.contains(new ItemStack(after)))
			return;
		entries.putAfter(new ItemStack(after), new ItemStack(key), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	private static void putBefore(MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries, ItemLike before, Supplier<? extends ItemLike> supplier) {
		ItemLike key = supplier.get();
		if (!entries.contains(new ItemStack(before)))
			return;
		entries.putBefore(new ItemStack(before), new ItemStack(key), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}
}
