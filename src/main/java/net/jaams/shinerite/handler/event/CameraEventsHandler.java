package net.jaams.shinerite.handler.event;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;

import net.jaams.shinerite.item.ShineriteStarItem;
import net.jaams.shinerite.item.ShinemerangItem;
import net.jaams.shinerite.configuration.client.CameraEffectsConfig;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CameraEventsHandler {
	@SubscribeEvent
	public static void onFOVModifier(ComputeFovModifierEvent event) {
		Player player = event.getPlayer();
		if (player == null || !player.isUsingItem()) {
			return;
		}
		if (!CameraEffectsConfig.FOV_MODIFIER.get()) {
			event.setCanceled(true);
			return;
		}
		ItemStack useStack = player.getUseItem();
		Item useItem = useStack.getItem();
		applyFovModifier(event, useItem, player, ShinemerangItem.class, 30.0f);
		applyFovModifier(event, useItem, player, ShineriteStarItem.class, 20.0f);
	}

	private static void applyFovModifier(ComputeFovModifierEvent event, Item useItem, Player player, Class<?> itemClass, float timeDivider) {
		if (itemClass.isInstance(useItem)) {
			float f1 = player.getTicksUsingItem() / timeDivider;
			f1 = Math.min(f1, 1.0F);
			f1 = (float) Math.pow(f1, 1.6D);
			float newModifier = event.getFovModifier() * (1.0F - f1 * 0.2F);
			event.setNewFovModifier(newModifier);
		}
	}
}
