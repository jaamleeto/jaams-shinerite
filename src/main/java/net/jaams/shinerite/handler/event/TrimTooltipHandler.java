package net.jaams.shinerite.handler.event;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.jaams.shinerite.client.model.ShineriteTrimRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TrimTooltipHandler {
	private static final String ARMOR_TRIM_TITLE_KEY = "item.minecraft.smithing_template.upgrade";

	private TrimTooltipHandler() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onItemTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (!ShineriteTrimRenderer.hasShineriteTrim(stack)) {
			return;
		}
		List<Component> lines = event.getToolTip();
		if (lines.isEmpty()) {
			return;
		}
		TextColor trimColor = TextColor.fromRgb(ShineriteTrimRenderer.trimColor(stack));
		for (int i = 0; i < lines.size(); i++) {
			Component line = lines.get(i);
			if (isArmorTrimLine(line)) {
				lines.set(i, withColorRecursively(line, trimColor));
			}
		}
	}

	private static boolean isArmorTrimLine(Component line) {
		String key = getKey(line);
		if (key == null) {
			return false;
		}
		return ARMOR_TRIM_TITLE_KEY.equals(key) || key.startsWith("trim_pattern.") || key.startsWith("trim_material.");
	}

	private static String getKey(Component component) {
		if (component.getContents() instanceof TranslatableContents contents) {
			return contents.getKey();
		}
		for (Component sibling : component.getSiblings()) {
			String key = getKey(sibling);
			if (key != null) {
				return key;
			}
		}
		return null;
	}

	private static Component withColorRecursively(Component component, TextColor color) {
		MutableComponent copy = component.copy().withStyle(style -> style.withColor(color));
		List<Component> siblings = copy.getSiblings();
		for (int i = 0; i < siblings.size(); i++) {
			siblings.set(i, withColorRecursively(siblings.get(i), color));
		}
		return copy;
	}
}
