package net.jaams.shinerite.handler.event;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CapabilityTooltipHandler {
	private static final String MOD_ID_STEM = "jaams";

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onTooltipCapabilities(ItemTooltipEvent event) {
		List<Component> lines = event.getToolTip();
		if (lines.isEmpty())
			return;
		List<CapabilityBlock> blocks = findCapabilityBlocks(lines);
		if (blocks.isEmpty())
			return;
		if (blocks.size() > 1) {
			int insertIndex = blocks.get(0).startIndex;
			LinkedHashMap<String, Component> uniqueEntries = new LinkedHashMap<>();
			for (CapabilityBlock block : blocks) {
				for (Component entry : block.entries) {
					uniqueEntries.putIfAbsent(getKey(entry), entry);
				}
			}
			if (!uniqueEntries.isEmpty()) {
				List<Integer> toRemove = new ArrayList<>();
				for (CapabilityBlock block : blocks) {
					toRemove.add(block.startIndex);
					toRemove.add(block.headerIndex);
					toRemove.addAll(block.entryIndices);
				}
				Collections.sort(toRemove, Comparator.reverseOrder());
				for (int index : toRemove) {
					lines.remove(index);
				}
				Component header = blocks.get(0).header;
				List<Component> mergedBlock = new ArrayList<>();
				mergedBlock.add(Component.literal(""));
				mergedBlock.add(header);
				mergedBlock.addAll(uniqueEntries.values());
				if (insertIndex < 0 || insertIndex > lines.size()) {
					insertIndex = Math.min(1, lines.size());
				}
				lines.addAll(Math.min(insertIndex, lines.size()), mergedBlock);
			}
		}
		sortCapabilityEntriesByColor(lines);
		addSpacingAroundCapabilities(lines);
	}

	private static void sortCapabilityEntriesByColor(List<Component> lines) {
		int firstEntry = -1;
		int lastEntry = -1;
		for (int i = 0; i < lines.size(); i++) {
			String key = getKey(lines.get(i));
			if (key == null || !isJaamsKey(key) || !isCapabilityEntryKey(key)) {
				continue;
			}
			if (firstEntry == -1) {
				firstEntry = i;
			}
			lastEntry = i;
		}
		if (firstEntry == -1 || firstEntry == lastEntry) {
			return;
		}
		List<Component> entries = new ArrayList<>(lines.subList(firstEntry, lastEntry + 1));
		entries.sort(Comparator.comparingInt(CapabilityTooltipHandler::getColorPriority));
		for (int i = 0; i < entries.size(); i++) {
			lines.set(firstEntry + i, entries.get(i));
		}
	}

	private static int getColorPriority(Component component) {
		ChatFormatting color = getEntryColor(component);
		if (color == ChatFormatting.BLUE || color == ChatFormatting.DARK_BLUE || color == ChatFormatting.AQUA || color == ChatFormatting.DARK_AQUA) {
			return 1;
		}
		if (color == ChatFormatting.GREEN || color == ChatFormatting.DARK_GREEN) {
			return 2;
		}
		if (color == ChatFormatting.RED || color == ChatFormatting.DARK_RED) {
			return 3;
		}
		return 4;
	}

	private static ChatFormatting getEntryColor(Component component) {
		TextColor textColor = component.getStyle().getColor();
		if (textColor != null) {
			return fromValue(textColor.getValue());
		}
		for (Component sibling : component.getSiblings()) {
			TextColor siblingColor = sibling.getStyle().getColor();
			if (siblingColor != null) {
				return fromValue(siblingColor.getValue());
			}
		}
		return null;
	}

	private static ChatFormatting fromValue(int value) {
		for (ChatFormatting format : ChatFormatting.values()) {
			if (format.isColor() && format.getColor() == value) {
				return format;
			}
		}
		return null;
	}

	private static void addSpacingAroundCapabilities(List<Component> lines) {
		int first = -1;
		int last = -1;
		for (int i = 0; i < lines.size(); i++) {
			String key = getKey(lines.get(i));
			if (key != null && isJaamsKey(key) && key.contains(".capability.")) {
				if (first == -1) {
					first = i;
				}
				last = i;
			}
		}
		if (first == -1) {
			return;
		}
		if (first > 0 && !lines.get(first - 1).getString().isEmpty()) {
			lines.add(first, Component.literal(""));
			last++;
		}
		if (last < lines.size() - 1 && !lines.get(last + 1).getString().isEmpty()) {
			lines.add(last + 1, Component.literal(""));
		}
	}

	private static List<CapabilityBlock> findCapabilityBlocks(List<Component> lines) {
		List<CapabilityBlock> blocks = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			String key = getKey(lines.get(i));
			if (key == null || !isJaamsKey(key) || !isCapabilityHeaderKey(key)) {
				continue;
			}
			CapabilityBlock block = new CapabilityBlock();
			block.headerIndex = i;
			block.header = lines.get(i);
			block.startIndex = (i > 0 && lines.get(i - 1).getString().isEmpty() && !isUsedIndex(i - 1, blocks)) ? i - 1 : i;
			int j = i + 1;
			while (j < lines.size()) {
				String entryKey = getKey(lines.get(j));
				if (entryKey == null || !isJaamsKey(entryKey) || !isCapabilityEntryKey(entryKey))
					break;
				block.entryIndices.add(j);
				block.entries.add(lines.get(j));
				j++;
			}
			blocks.add(block);
		}
		return blocks;
	}

	private static boolean isUsedIndex(int index, List<CapabilityBlock> blocks) {
		for (CapabilityBlock block : blocks) {
			if (block.startIndex == index || block.headerIndex == index) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCapabilityHeaderKey(String key) {
		return key.endsWith(".capability.header");
	}

	private static boolean isCapabilityEntryKey(String key) {
		return key.contains(".capability.") && !isCapabilityHeaderKey(key);
	}

	private static boolean isJaamsKey(String key) {
		return key.contains(MOD_ID_STEM);
	}

	private static String getKey(Component c) {
		if (c.getContents() instanceof TranslatableContents tc) {
			return tc.getKey();
		}
		for (Component s : c.getSiblings()) {
			if (s.getContents() instanceof TranslatableContents tc) {
				return tc.getKey();
			}
		}
		return null;
	}

	private static class CapabilityBlock {
		int startIndex;
		int headerIndex;
		Component header;
		final List<Integer> entryIndices = new ArrayList<>();
		final List<Component> entries = new ArrayList<>();
	}
}
