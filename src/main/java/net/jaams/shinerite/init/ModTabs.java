package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.ModList;

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
import net.jaams.shinerite.configuration.common.EnchantmentsConfig;
import net.jaams.shinerite.data.CreativeTabData;
import net.jaams.shinerite.loader.TabModifierLoader;
import net.jaams.shinerite.JaamsShineriteMod;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;

public class ModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JaamsShineriteMod.MODID);
	public static final RegistryObject<CreativeModeTab> SHINERITE_TAB = REGISTRY.register("shinerite_tab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.jaams_shinerite.tab_icon").withStyle(style -> style.withFont(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "default")))).icon(() -> getTabIcon())
					.withTabsBefore(getPreferredTabBefore()).displayItems((parameters, tabData) -> {
						List<ItemStack> items = new ArrayList<>();
						items.add(new ItemStack(ModItems.SHINERITE_SWORD.get()));
						items.add(new ItemStack(ModItems.SHINERITE_AXE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_PICKAXE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_HOE.get()));
						items.add(new ItemStack(ModItems.SHINERITE_SHOVEL.get()));
						items.add(new ItemStack(ModItems.SHINERANG.get()));
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
						items.add(new ItemStack(ModItems.CUT_SHINERITE_VERTICAL_SLAB.get()));
						items.add(new ItemStack(ModItems.SHINERITE_TILES.get()));
						items.add(new ItemStack(ModItems.SHINERITE_TILE_STAIRS.get()));
						items.add(new ItemStack(ModItems.SHINERITE_TILE_SLAB.get()));
						items.add(new ItemStack(ModItems.SHINERITE_TILE_VERTICAL_SLAB.get()));
						items.add(new ItemStack(ModItems.SHINERITE_ROOTS.get()));
						items.add(new ItemStack(ModItems.SHINERITIA.get()));
						if (EnchantmentsConfig.LIGHTBRINGER.get()) {
							items.add(addEnchantments(new ItemStack(Items.ENCHANTED_BOOK), ModEnchantments.LIGHTBRINGER.get(), EnchantmentsConfig.LIGHTBRINGER_MAX_LEVEL.get()));
						}
						if (EnchantmentsConfig.LIGHTSTEAL.get()) {
							items.add(addEnchantments(new ItemStack(Items.ENCHANTED_BOOK), ModEnchantments.LIGHTSTEAL.get(), EnchantmentsConfig.LIGHTSTEAL_MAX_LEVEL.get()));
						}
						if (EnchantmentsConfig.LIGHTGUARD.get()) {
							items.add(addEnchantments(new ItemStack(Items.ENCHANTED_BOOK), ModEnchantments.LIGHTGUARD.get(), EnchantmentsConfig.LIGHTGUARD_MAX_LEVEL.get()));
						}
						processDataDrivenEntries(items);
						for (ItemStack stack : items) {
							tabData.accept(stack);
						}
					}).build());

	private static ItemStack addEnchantments(ItemStack itemStack, Enchantment enchantment, int level) {
		EnchantmentHelper.setEnchantments(ImmutableMap.of(enchantment, level), itemStack);
		return itemStack;
	}

	private static final String SHINERITE_TAB_ID = "shinerite";

	private static boolean matchesShineriteTab(CreativeTabData.Entry entry) {
		if (entry == null)
			return false;
		String id = entry.tab_id == null || entry.tab_id.isEmpty() ? SHINERITE_TAB_ID : entry.tab_id;
		return SHINERITE_TAB_ID.equals(id);
	}

 	private static void processDataDrivenEntries(List<ItemStack> items) {
		List<CreativeTabData.Entry> entries = TabModifierLoader.INSTANCE.getAllEntries();
		List<CreativeTabData.Entry> toRemove = new ArrayList<>();
		List<CreativeTabData.Entry> toAdd = new ArrayList<>();
		boolean cleared = false;
		for (CreativeTabData.Entry entry : entries) {
			if (!matchesShineriteTab(entry))
				continue;
			if (!TabModifierLoader.INSTANCE.evaluateConditions(entry))
				continue;
			if (entry.clear_defaults) {
				items.clear();
				cleared = true;
				break;
			}
		}
		for (CreativeTabData.Entry entry : entries) {
			if (!matchesShineriteTab(entry))
				continue;
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
				TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));
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
			ResourceLocation afterLoc = ResourceLocation.parse(entry.after);
			for (int i = 0; i < items.size(); i++) {
				ResourceLocation cur = ForgeRegistries.ITEMS.getKey(items.get(i).getItem());
				if (cur != null && cur.equals(afterLoc)) {
					items.add(i + 1, stack);
					return;
				}
			}
		}
		if (entry.before != null && !entry.before.isEmpty()) {
			ResourceLocation beforeLoc = ResourceLocation.parse(entry.before);
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
				TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(itemId.substring(1)));
				items.removeIf(stack -> stack.is(tagKey));
			} else {
				ResourceLocation target = ResourceLocation.parse(itemId);
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
			ResourceLocation loc = ResourceLocation.parse(entry.item);
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
			return ResourceLocation.fromNamespaceAndPath("jaams_cavenry", "cavenry");
		} else if (ModList.get().isLoaded("jaams_weaponry")) {
			return ResourceLocation.fromNamespaceAndPath("jaams_weaponry", "weaponry");
		} else if (ModList.get().isLoaded("jaams_archaeology")) {
			return ResourceLocation.fromNamespaceAndPath("jaams_archaeology", "archaeology");
		}
		return CreativeModeTabs.SPAWN_EGGS.location();
	}

	private static ItemStack getTabIcon() {
		try {
			String itemId = CreativeTabConfig.CREATIVE_TAB_ICON.get();
			ResourceLocation resourceLocation = ResourceLocation.parse(itemId);
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

}
