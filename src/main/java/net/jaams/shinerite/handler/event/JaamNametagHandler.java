
package net.jaams.shinerite.handler.event;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.nbt.CompoundTag;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.handler.mechanic.InteractionHandler;

@Mod.EventBusSubscriber
public class JaamNametagHandler {
	@SubscribeEvent
	public static void onPlayerJaamColorTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
			Player player = event.player;
			for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
				processItemName(player.getInventory().getItem(slot), player.level());
			}
		}
	}

	@SubscribeEvent
	public static void onLivingJaamColorTick(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		Level level = entity.level();
		if (!level.isClientSide && !(entity instanceof Player)) {
			processItemName(entity.getItemBySlot(EquipmentSlot.MAINHAND), level);
			processItemName(entity.getItemBySlot(EquipmentSlot.OFFHAND), level);
		}
	}

	private static void processItemName(ItemStack stack, Level level) {
		if (stack.isEmpty() || !stack.hasCustomHoverName()) {
			return;
		}
		if (!ModUtils.isShineriteItem(stack)) {
			return;
		}
		if (ModUtils.isImbued(stack)) {
			CompoundTag tag = stack.getTag();
			if (tag != null) {
				tag.remove("TickCounter");
			}
			return;
		}
		String itemName = stack.getHoverName().getString().trim();
		int targetColor = -1;
		if ("jaam".equalsIgnoreCase(itemName)) {
			targetColor = 0xFF0000;
		} else if ("leeto".equalsIgnoreCase(itemName)) {
			targetColor = 0x000000;
		}
		if (targetColor == -1) {
			return;
		}
		CompoundTag tag = stack.getOrCreateTag();
		int currentColor = ModUtils.getCurrentColor(stack);
		if (currentColor == targetColor) {
			tag.remove("TickCounter");
			return;
		}
		int tickCounter = tag.getInt("TickCounter") + 1;
		if (tickCounter >= 5) {
			int baseColor = currentColor == InteractionHandler.DEFAULT_COLOR ? 0xFFFFFF : currentColor;
			int smoothColor = interpolateColor(baseColor, targetColor, 0.12f);
			ModUtils.applyDyeColor(stack, smoothColor);
			tickCounter = 0;
		}
		tag.putInt("TickCounter", tickCounter);
	}

	private static int interpolateColor(int color1, int color2, float factor) {
		int r1 = (color1 >> 16) & 0xFF;
		int g1 = (color1 >> 8) & 0xFF;
		int b1 = color1 & 0xFF;
		int r2 = (color2 >> 16) & 0xFF;
		int g2 = (color2 >> 8) & 0xFF;
		int b2 = color2 & 0xFF;
		int r = (int) (r1 + (r2 - r1) * factor);
		int g = (int) (g1 + (g2 - g1) * factor);
		int b = (int) (b1 + (b2 - b1) * factor);
		return (r << 16) | (g << 8) | b;
	}
}
