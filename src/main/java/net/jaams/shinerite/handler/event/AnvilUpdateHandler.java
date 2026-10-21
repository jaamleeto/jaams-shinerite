package net.jaams.shinerite.handler.event;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.AnvilUpdateEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.base.JaamsShineriteCommonConfiguration;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import net.jaams.shinerite.configuration.common.RecipesConfig;

@Mod.EventBusSubscriber(modid = "jaams_shinerite", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilUpdateHandler {
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onAnvilUpdate(AnvilUpdateEvent event) {
		if (!RecipesConfig.ANVIL_ITEM_RECIPE.get())
			return;
		boolean dyeing = MechanicsConfig.DYEING.get();
		boolean imbuing = MechanicsConfig.IMBUING.get();
		if (!dyeing && !imbuing)
			return;
		ItemStack left = event.getLeft();
		ItemStack right = event.getRight();
		if (left.isEmpty() || right.isEmpty())
			return;
		if (!ModUtils.isImbueableItem(left) || !ModUtils.isImbueableItem(right))
			return;
		boolean leftImbued = imbuing && ModUtils.isImbued(left);
		boolean rightImbued = imbuing && ModUtils.isImbued(right);
		if (!leftImbued && !rightImbued && (!dyeing || (ModUtils.getCurrentColor(left) == -1 && ModUtils.getCurrentColor(right) == -1))) {
			return;
		}
		ItemStack output = left.copy();
		CompoundTag tag = output.getOrCreateTag();
		int cost = 0;
		if (leftImbued || rightImbued) {
			cost = switch (RecipesConfig.ANVIL_IMBUE_COST_LEVEL.get()) {
				case 1 -> 2;
				case 2 -> 4;
				case 3 -> 7;
				case 4 -> 11;
				case 5 -> 16;
				default -> 7;
			};
		}
		if (!leftImbued && !rightImbued) {
			if (dyeing && RecipesConfig.ANVIL_DYE_ONLY_MIX.get()) {
				int color = calculateAverageColor(left, right, false, false);
				if (color != -1) {
					ModUtils.applyDyeColor(output, color);
				}
				cost += 1;
			} else {
				return;
			}
		} else {
			if (dyeing && RecipesConfig.ANVIL_IMBUING_TRANSFER_COLOR.get()) {
				int finalColor = calculateAverageColor(left, right, leftImbued, rightImbued);
				if (finalColor != -1) {
					tag.putInt("ImbuedColor", finalColor);
					ModUtils.applyDyeColor(output, finalColor);
				}
			}
			if (imbuing && RecipesConfig.ANVIL_IMBUING_MERGE_EFFECTS.get() && leftImbued && rightImbued) {
				List<MobEffectInstance> all = new ArrayList<>();
				if (leftImbued)
					all.addAll(loadEffectsFrom(left));
				if (rightImbued)
					all.addAll(loadEffectsFrom(right));
				if (!all.isEmpty()) {
					List<MobEffectInstance> merged = mergeSameEffects(all);
					ListTag tagList = new ListTag();
					merged.forEach(e -> tagList.add(e.save(new CompoundTag())));
					tag.put("ImbuedEffects", tagList);
				}
			}
			if (imbuing && RecipesConfig.ANVIL_IMBUING_PRESERVE_USES.get() && (leftImbued || rightImbued)) {
				int usesL = leftImbued ? left.getOrCreateTag().getInt("ImbuedUses") : 0;
				int usesR = rightImbued ? right.getOrCreateTag().getInt("ImbuedUses") : 0;
				int maxL = leftImbued ? left.getOrCreateTag().getInt("MaxImbuedUses") : 0;
				int maxR = rightImbued ? right.getOrCreateTag().getInt("MaxImbuedUses") : 0;
				int sumMax = maxL + maxR;
				if (sumMax <= 0) {
					tag.putInt("ImbuedUses", 0);
					tag.putInt("MaxImbuedUses", 0);
				} else {
					int finalMax = Math.max(1, (int) Math.ceil(sumMax / 2.0));
					int finalUses = (usesL == maxL && usesR == maxR && maxL > 0 && maxR > 0)
							? finalMax
							: Math.min((int) Math.ceil((usesL + usesR) / 2.0), finalMax);
					finalUses = Math.max(1, Math.min(finalUses, finalMax));
					tag.putInt("ImbuedUses", finalUses);
					tag.putInt("MaxImbuedUses", finalMax);
				}
			}
		}
		event.setOutput(output);
		event.setCost(Math.max(1, cost));
		event.setMaterialCost(1);
	}

	private static List<MobEffectInstance> loadEffectsFrom(ItemStack stack) {
		List<MobEffectInstance> list = new ArrayList<>();
		ListTag tagList = stack.getOrCreateTag().getList("ImbuedEffects", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			MobEffectInstance effect = ModUtils.loadImbuedEffect(tagList.getCompound(i));
			if (effect != null)
				list.add(effect);
		}
		return list;
	}

	private static List<MobEffectInstance> mergeSameEffects(List<MobEffectInstance> effects) {
		Map<MobEffect, MobEffectInstance> map = new LinkedHashMap<>();
		for (MobEffectInstance e : effects) {
			MobEffect type = e.getEffect();
			MobEffectInstance existing = map.get(type);
			if (existing == null) {
				map.put(type, e);
			} else {
				int newAmp = existing.getAmplifier() + 1;
				int newDur = Math.max(1, Math.max(existing.getDuration(), e.getDuration()) / 2);
				MobEffectInstance upgraded = new MobEffectInstance(type, newDur, newAmp, e.isAmbient(), e.isVisible(), e.showIcon());
				map.put(type, upgraded);
			}
		}
		return new ArrayList<>(map.values());
	}

	private static int calculateAverageColor(ItemStack a, ItemStack b, boolean aImbued, boolean bImbued) {
		int c1 = aImbued ? ModUtils.getImbuedColor(a) : ModUtils.getCurrentColor(a);
		int c2 = bImbued ? ModUtils.getImbuedColor(b) : ModUtils.getCurrentColor(b);
		if (c1 == -1)
			return c2;
		if (c2 == -1)
			return c1;
		int r = ((c1 >> 16 & 255) + (c2 >> 16 & 255)) / 2;
		int g = ((c1 >> 8 & 255) + (c2 >> 8 & 255)) / 2;
		int blue = ((c1 & 255) + (c2 & 255)) / 2;
		return (r << 16) | (g << 8) | blue;
	}
}
