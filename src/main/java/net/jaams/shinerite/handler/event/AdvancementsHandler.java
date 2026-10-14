package net.jaams.shinerite.handler.event;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Advancement;

@Mod.EventBusSubscriber(modid = "jaams_shinerite", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AdvancementsHandler {
	@SubscribeEvent
	public static void onItemAdvancementCrafted(PlayerEvent.ItemCraftedEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ItemStack stack = event.getCrafting();
			ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
			if (itemId != null && itemId.getNamespace().equals("jaams_shinerite")) {
				grantAdvancement(player, "shinerite_ad");
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerAdvancementTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
			checkShineriteAdvancement(player);
			checkShineInTheCruelNight(player);
		}
	}

	private static void checkShineriteAdvancement(ServerPlayer player) {
		Advancement advancement = player.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", "shinerite_ad"));
		if (advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone()) {
			for (ItemStack stack : player.getInventory().items) {
				if (!stack.isEmpty()) {
					ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
					if (itemId != null && itemId.getNamespace().equals("jaams_shinerite")) {
						grantAdvancement(player, "shinerite_ad");
						break;
					}
				}
			}
		}
	}

	private static void checkShineInTheCruelNight(ServerPlayer player) {
		if (hasAdvancement(player, "shine_in_the_cruel_night"))
			return;
		Level level = player.level();
		if (!level.isNight())
			return;
		if (!level.canSeeSky(player.blockPosition().above()))
			return;
		boolean fullShineriteArmor = true;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (!slot.isArmor())
				continue;
			ItemStack armor = player.getItemBySlot(slot);
			if (armor.isEmpty()) {
				fullShineriteArmor = false;
				break;
			}
			ResourceLocation id = BuiltInRegistries.ITEM.getKey(armor.getItem());
			if (id == null || !id.getPath().contains("shinerite")) {
				fullShineriteArmor = false;
				break;
			}
		}
		if (fullShineriteArmor) {
			grantAdvancement(player, "shine_in_the_cruel_night");
		}
	}

	public static boolean hasAdvancement(ServerPlayer player, String advancementId) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath("jaams_shinerite", advancementId);
		Advancement advancement = player.getServer().getAdvancements().getAdvancement(id);
		if (advancement != null) {
			return player.getAdvancements().getOrStartProgress(advancement).isDone();
		}
		return false;
	}

	public static void grantAdvancement(ServerPlayer player, String advancementId) {
		Advancement advancement = player.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", advancementId));
		if (advancement != null) {
			AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
			if (!progress.isDone()) {
				for (String criteria : progress.getRemainingCriteria()) {
					player.getAdvancements().award(advancement, criteria);
				}
			}
		}
	}

	public static void incrementCounterAndCheckAdvancement(ServerPlayer player, String counterKey, String advancementId, int threshold) {
		Advancement advancement = player.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", advancementId));
		if (advancement == null) {
			return;
		}
		if (player.getAdvancements().getOrStartProgress(advancement).isDone()) {
			return;
		}
		CompoundTag persistentData = player.getPersistentData();
		ResourceLocation key = ResourceLocation.fromNamespaceAndPath("jaams_shinerite", counterKey);
		int counter = persistentData.getInt(key.toString());
		counter++;
		persistentData.putInt(key.toString(), counter);
		if (counter >= threshold) {
			grantAdvancement(player, advancementId);
			persistentData.putInt(key.toString(), 0);
		}
	}

	public static void incrementEntityCounterAndCheckAdvancement(ServerPlayer player, LivingEntity entity, String counterKey, String advancementId, int threshold) {
		Advancement advancement = player.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath("jaams_shinerite", advancementId));
		if (advancement == null) {
			return;
		}
		if (player.getAdvancements().getOrStartProgress(advancement).isDone()) {
			return;
		}
		CompoundTag entityData = entity.getPersistentData();
		ResourceLocation key = ResourceLocation.fromNamespaceAndPath("jaams_shinerite", counterKey);
		int counter = entityData.getInt(key.toString());
		counter++;
		entityData.putInt(key.toString(), counter);
		if (counter >= threshold) {
			grantAdvancement(player, advancementId);
			entityData.putInt(key.toString(), 0);
		}
	}
}
