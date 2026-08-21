package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.common.util.MutableHashedLinkedMap;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistries;

import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.init.ModEnchantments;
import net.jaams.shinerite.configuration.client.CreativeTabConfig;
import net.jaams.shinerite.data.CreativeTabData;
import net.jaams.shinerite.loader.TabModifierLoader;
import net.jaams.shinerite.JaamsShineriteMod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

@Mod.EventBusSubscriber
public class ModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JaamsShineriteMod.MODID);
	public static final RegistryObject<CreativeModeTab> SHINERITE_TAB = REGISTRY.register("shinerite_tab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.jaams_shinerite.tab_icon").withStyle(style -> style.withFont(new ResourceLocation("jaams_shinerite", "default")))).icon(() -> getTabIcon())
					.withTabsBefore(getPreferredTabBefore()).displayItems((parameters, tabData) -> {
						List<ItemStack> items = new ArrayList<>();
						// Shinerite Items
						items.add(new ItemStack(ModItems.SHINERITE_SWORD.get()));
						items.add(new ItemStack(ModItems.SHINERITE_AXE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_PICKAXE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_HOE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_SHOVEL.get()));
						items.add(new ItemStack(ModItems.SHINEMERANG.get()));
						items.add(new ItemStack(ModItems.SHINERITE_STAR.get()));
						items.add(new ItemStack(ModItems.SHINY_PAINT_BRUSH.get()));
						items.add(new ItemStack(ModItems.SHINY_SCRAPER.get()));
						items.add(new ItemStack(ModItems.SHINERITE_ARMOR_HELMET.get()));
						items.add(new ItemStack(ModItems.SHINERITE_ARMOR_CHESTPLATE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_ARMOR_LEGGINGS.get()));
						items.add(new ItemStack(ModItems.SHINERITE_ARMOR_BOOTS.get()));
						items.add(new ItemStack(ModItems.GLOW_SALAD.get()));
						items.add(new ItemStack(ModItems.SHINY_ROOTS.get()));
						items.add(new ItemStack(ModItems.RAW_SHINERITE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_INGOT.get()));
						items.add(new ItemStack(ModItems.SHINERITE_NUGGET.get()));
						items.add(new ItemStack(ModItems.MUSIC_DISC_SHINY.get()));
						items.add(new ItemStack(ModItems.DISC_FRAGMENT_SHINY.get()));
						items.add(new ItemStack(ModItems.SHINERITE_ORE.get()));
						items.add(new ItemStack(ModItems.DEEPSLATE_SHINERITE_ORE.get()));
						items.add(new ItemStack(ModItems.NETHER_SHINERITE_ORE.get()));
						items.add(new ItemStack(ModItems.RAW_SHINERITE_BLOCK.get()));
						items.add(new ItemStack(ModItems.SHINERITE_BLOCK.get()));
						items.add(new ItemStack(ModItems.CUT_SHINERITE.get()));
						items.add(new ItemStack(ModItems.CUT_SHINERITE_STAIRS.get()));
						items.add(new ItemStack(ModItems.CUT_SHINERITE_SLAB.get()));
						items.add(new ItemStack(ModItems.SHINERITE_TILES.get()));
						items.add(new ItemStack(ModItems.SHINERITE_TILE_STAIRS.get()));
						items.add(new ItemStack(ModItems.SHINERITE_TILE_SLAB.get()));
						items.add(new ItemStack(ModItems.SHINERITE_ROOTS.get()));
						items.add(new ItemStack(ModItems.SHINERITIA.get()));
						// Enchanted Books
						items.add(addEnchantments(new ItemStack(Items.ENCHANTED_BOOK), ModEnchantments.LIGHTBRINGER.get(), 2));
						items.add(addEnchantments(new ItemStack(Items.ENCHANTED_BOOK), ModEnchantments.LIGHTSTEAL.get(), 3));
						items.add(addEnchantments(new ItemStack(Items.ENCHANTED_BOOK), ModEnchantments.LIGHTGUARD.get(), 4));
						processDataDrivenEntries(items);
						for (ItemStack stack : items) {
							tabData.accept(stack);
						}
					}).build());

	private static ItemStack addEnchantments(ItemStack itemStack, Enchantment enchantment, int level) {
		EnchantmentHelper.setEnchantments(ImmutableMap.of(enchantment, level), itemStack);
		return itemStack;
	}

	private static void processDataDrivenEntries(List<ItemStack> items) {
		List<CreativeTabData.Entry> entries = TabModifierLoader.INSTANCE.getAllEntries();
		List<CreativeTabData.Entry> toRemove = new ArrayList<>();
		List<CreativeTabData.Entry> toAdd = new ArrayList<>();
		boolean cleared = false;
		for (CreativeTabData.Entry entry : entries) {
			if (!TabModifierLoader.INSTANCE.evaluateConditions(entry))
				continue;
			if (entry.clear_defaults) {
				items.clear();
				cleared = true;
				break;
			}
		}
		for (CreativeTabData.Entry entry : entries) {
			if (!TabModifierLoader.INSTANCE.evaluateConditions(entry))
				continue;
			if (entry.clear_defaults)
				continue;
			if (entry.remove) {
				toRemove.add(entry);
			} else if (entry.item != null && !entry.item.isEmpty()) {
				toAdd.add(entry);
			}
		}
		if (!cleared) {
			for (CreativeTabData.Entry entry : toRemove) {
				removeItemsFromList(items, entry.item);
			}
		}
		toAdd.sort((a, b) -> Integer.compare(b.weight, a.weight));
		for (CreativeTabData.Entry entry : toAdd) {
			addEntryToList(items, entry);
		}
	}

	private static void addEntryToList(List<ItemStack> items, CreativeTabData.Entry entry) {
		try {
			List<ItemStack> stacksToAdd = new ArrayList<>();
			if (entry.item.startsWith("#")) {
				String tagId = entry.item.substring(1);
				TagKey<Item> tagKey = TagKey.create(Registries.ITEM, new ResourceLocation(tagId));
				for (Item item : ForgeRegistries.ITEMS) {
					ItemStack stack = new ItemStack(item);
					if (stack.is(tagKey)) {
						applyNBT(stack, entry.nbt);
						stacksToAdd.add(stack);
					}
				}
			} else {
				ItemStack stack = createSingleItemStack(entry);
				if (stack != null) {
					stacksToAdd.add(stack);
				}
			}
			for (ItemStack stack : stacksToAdd) {
				insertItemOrdered(items, stack, entry);
			}
		} catch (Exception e) {
			JaamsShineriteMod.LOGGER.error("Failed to process data-driven tab entry: {}", entry.item, e);
		}
	}

	private static void insertItemOrdered(List<ItemStack> items, ItemStack stack, CreativeTabData.Entry entry) {
		if (entry.after != null && !entry.after.isEmpty()) {
			ResourceLocation afterLoc = new ResourceLocation(entry.after);
			for (int i = 0; i < items.size(); i++) {
				ResourceLocation cur = ForgeRegistries.ITEMS.getKey(items.get(i).getItem());
				if (cur != null && cur.equals(afterLoc)) {
					items.add(i + 1, stack);
					return;
				}
			}
		}
		if (entry.before != null && !entry.before.isEmpty()) {
			ResourceLocation beforeLoc = new ResourceLocation(entry.before);
			for (int i = 0; i < items.size(); i++) {
				ResourceLocation cur = ForgeRegistries.ITEMS.getKey(items.get(i).getItem());
				if (cur != null && cur.equals(beforeLoc)) {
					items.add(i, stack);
					return;
				}
			}
		}
		items.add(stack);
	}

	private static void removeItemsFromList(List<ItemStack> items, String itemId) {
		if (itemId == null || itemId.isEmpty())
			return;
		try {
			if (itemId.startsWith("#")) {
				TagKey<Item> tagKey = TagKey.create(Registries.ITEM, new ResourceLocation(itemId.substring(1)));
				items.removeIf(stack -> stack.is(tagKey));
			} else {
				ResourceLocation target = new ResourceLocation(itemId);
				items.removeIf(stack -> {
					ResourceLocation cur = ForgeRegistries.ITEMS.getKey(stack.getItem());
					return cur != null && cur.equals(target);
				});
			}
		} catch (Exception e) {
			JaamsShineriteMod.LOGGER.error("Failed to remove {}", itemId, e);
		}
	}

	private static ItemStack createSingleItemStack(CreativeTabData.Entry entry) {
		try {
			ResourceLocation loc = new ResourceLocation(entry.item);
			Item item = ForgeRegistries.ITEMS.getValue(loc);
			if (item == null || item == Items.AIR)
				return null;
			ItemStack stack = new ItemStack(item);
			applyNBT(stack, entry.nbt);
			return stack;
		} catch (Exception e) {
			JaamsShineriteMod.LOGGER.warn("Failed to create stack: {}", entry.item);
			return null;
		}
	}

	private static void applyNBT(ItemStack stack, String nbtString) {
		if (nbtString == null || nbtString.isEmpty())
			return;
		try {
			stack.setTag(net.minecraft.nbt.TagParser.parseTag(nbtString));
		} catch (Exception e) {
			JaamsShineriteMod.LOGGER.warn("Invalid NBT in tab modifier entry");
		}
	}

	private static ResourceLocation getPreferredTabBefore() {
		if (ModList.get().isLoaded("jaams_cavenry")) {
			return new ResourceLocation("jaams_cavenry", "cavenry");
		} else if (ModList.get().isLoaded("jaams_weaponry")) {
			return new ResourceLocation("jaams_weaponry", "weaponry");
		} else if (ModList.get().isLoaded("jaams_archaeology")) {
			return new ResourceLocation("jaams_archaeology", "archaeology");
		}
		return CreativeModeTabs.SPAWN_EGGS.location();
	}

	private static ItemStack getTabIcon() {
		try {
			String itemId = CreativeTabConfig.CREATIVE_TAB_ICON.get();
			ResourceLocation resourceLocation = new ResourceLocation(itemId);
			Item item = BuiltInRegistries.ITEM.get(resourceLocation);
			if (item != null && item != Items.AIR) {
				return new ItemStack(item);
			} else {
				return new ItemStack(ModItems.SHINERITE_INGOT.get());
			}
		} catch (Exception e) {
			return new ItemStack(ModItems.SHINERITE_INGOT.get());
		}
	}

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries = tabData.getEntries();
		// Combat Tab
		if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			putAfter(entries, Items.GOLDEN_SWORD, ModItems.SHINERITE_SWORD);
			putAfter(entries, Items.GOLDEN_AXE, ModItems.SHINERITE_AXE);
			putAfter(entries, Items.GOLDEN_BOOTS, ModItems.SHINERITE_ARMOR_HELMET);
			putAfter(entries, ModItems.SHINERITE_ARMOR_HELMET.get(), ModItems.SHINERITE_ARMOR_CHESTPLATE);
			putAfter(entries, ModItems.SHINERITE_ARMOR_CHESTPLATE.get(), ModItems.SHINERITE_ARMOR_LEGGINGS);
			putAfter(entries, ModItems.SHINERITE_ARMOR_LEGGINGS.get(), ModItems.SHINERITE_ARMOR_BOOTS);
			putAfter(entries, Items.BOW, ModItems.SHINEMERANG);
			putAfter(entries, ModItems.SHINEMERANG.get(), ModItems.SHINERITE_STAR);
		}
		// Tools and Utilities Tab
		if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			putAfter(entries, Items.GOLDEN_PICKAXE, ModItems.SHINERITE_PICKAXE);
			putAfter(entries, Items.GOLDEN_AXE, ModItems.SHINERITE_AXE);
			putAfter(entries, Items.GOLDEN_SHOVEL, ModItems.SHINERITE_SHOVEL);
			putAfter(entries, Items.GOLDEN_HOE, ModItems.SHINERITE_HOE);
			putAfter(entries, Items.BRUSH, ModItems.SHINY_PAINT_BRUSH);
			putAfter(entries, ModItems.SHINY_PAINT_BRUSH.get(), ModItems.SHINY_SCRAPER);
		}
		// Building Blocks Tab
		if (tabData.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
			putAfter(entries, Items.GOLD_BLOCK, ModItems.SHINERITE_BLOCK);
			putAfter(entries, ModItems.SHINERITE_BLOCK.get(), ModItems.CUT_SHINERITE);
			putAfter(entries, ModItems.CUT_SHINERITE.get(), ModItems.CUT_SHINERITE_STAIRS);
			putAfter(entries, ModItems.CUT_SHINERITE_STAIRS.get(), ModItems.CUT_SHINERITE_SLAB);
			putAfter(entries, ModItems.CUT_SHINERITE_SLAB.get(), ModItems.SHINERITE_TILES);
			putAfter(entries, ModItems.SHINERITE_TILES.get(), ModItems.SHINERITE_TILE_STAIRS);
			putAfter(entries, ModItems.SHINERITE_TILE_STAIRS.get(), ModItems.SHINERITE_TILE_SLAB);
		}
		// Ingredients Tab
		if (tabData.getTabKey() == CreativeModeTabs.INGREDIENTS) {
			putAfter(entries, Items.GOLD_INGOT, ModItems.SHINERITE_INGOT);
			putAfter(entries, Items.GOLD_NUGGET, ModItems.SHINERITE_NUGGET);
			putAfter(entries, Items.RAW_GOLD, ModItems.RAW_SHINERITE);
			putAfter(entries, Items.DISC_FRAGMENT_5, ModItems.DISC_FRAGMENT_SHINY);
		}
		// Natural Blocks Tab
		if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
			putAfter(entries, Items.GOLD_ORE, ModItems.SHINERITE_ORE);
			putAfter(entries, ModItems.SHINERITE_ORE.get(), ModItems.DEEPSLATE_SHINERITE_ORE);
			putAfter(entries, ModItems.DEEPSLATE_SHINERITE_ORE.get(), ModItems.NETHER_SHINERITE_ORE);
			putAfter(entries, Items.RAW_GOLD_BLOCK, ModItems.RAW_SHINERITE_BLOCK);
			putAfter(entries, Items.GLOW_LICHEN, ModItems.SHINERITE_ROOTS);
			putAfter(entries, ModItems.SHINERITE_ROOTS.get(), ModItems.SHINERITIA);
		}
		// Food and Drinks Tab
		if (tabData.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
			putAfter(entries, Items.GLOW_BERRIES, ModItems.GLOW_SALAD);
			putAfter(entries, ModItems.GLOW_SALAD.get(), ModItems.SHINY_ROOTS);
		}
		// Functional Blocks Tab
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
