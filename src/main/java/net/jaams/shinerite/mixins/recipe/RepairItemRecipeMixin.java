package net.jaams.shinerite.mixins.recipe;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.checkerframework.checker.units.qual.m2;
import org.checkerframework.checker.units.qual.g;

import org.apache.commons.lang3.RandomUtils;

import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModEnums;
import net.jaams.shinerite.configuration.base.JaamsShineriteCommonConfiguration;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import net.jaams.shinerite.configuration.common.RecipesConfig;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {
	@Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
	private void preserveAndMixDyeOnToolRepair(CraftingContainer container, RegistryAccess access, CallbackInfoReturnable<ItemStack> cir) {
		if (!RecipesConfig.REPAIR_ITEM_RECIPE.get()) {
			return;
		}
		boolean dyeingActive = RecipesConfig.REPAIR_DYEING_ACTIVE.get();
		boolean imbuingActive = RecipesConfig.REPAIR_IMBUING_ACTIVE.get();
		if (!dyeingActive && !imbuingActive) {
			return;
		}
		ItemStack result = cir.getReturnValue();
		if (result.isEmpty()) {
			return;
		}
		List<ItemStack> inputs = new ArrayList<>();
		List<ItemStack> imbuedInputs = new ArrayList<>();
		List<Integer> dyeColors = new ArrayList<>();
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack item = container.getItem(i);
			if (item.isEmpty())
				continue;
			if (ModUtils.isImbueableItem(item) || ModUtils.isShineriteItem(item)) {
				inputs.add(item);
				if (imbuingActive && ModUtils.isImbued(item)) {
					imbuedInputs.add(item);
				} else if (dyeingActive) {
					int color = ModUtils.getCurrentColor(item);
					if (color != -1) {
						dyeColors.add(color);
					}
				}
			}
		}
		if (inputs.size() != 2) {
			return;
		}
		CompoundTag resultTag = result.getOrCreateTag();
		int imbuedCount = imbuedInputs.size();
		if (imbuingActive && imbuedCount == 2) {
			ModEnums.RepairImbuementMode mode = RecipesConfig.REPAIR_IMBUING_MODE.get();
			if (mode == ModEnums.RepairImbuementMode.MERGE) {
				List<MobEffectInstance> all = new ArrayList<>();
				all.addAll(loadEffectsFrom(imbuedInputs.get(0)));
				all.addAll(loadEffectsFrom(imbuedInputs.get(1)));
				List<MobEffectInstance> merged = mergeSameEffects(all);
				ListTag effectsTag = new ListTag();
				for (MobEffectInstance e : merged) {
					effectsTag.add(e.save(new CompoundTag()));
				}
				resultTag.put("ImbuedEffects", effectsTag);
				int u1 = imbuedInputs.get(0).getOrCreateTag().getInt("ImbuedUses");
				int u2 = imbuedInputs.get(1).getOrCreateTag().getInt("ImbuedUses");
				int m1 = imbuedInputs.get(0).getOrCreateTag().getInt("MaxImbuedUses");
				int m2 = imbuedInputs.get(1).getOrCreateTag().getInt("MaxImbuedUses");
				int finalMax = (m1 + m2) / 2 - 1;
				finalMax = Math.max(3, finalMax);
				int finalUses = (u1 + u2) / 2 - 1;
				int softCap = (int) (finalMax * 0.9);
				finalUses = Math.min(finalUses, softCap);
				finalUses = Math.max(1, finalUses);
				finalMax = Math.max(finalUses, finalMax);
				resultTag.putInt("ImbuedUses", finalUses);
				resultTag.putInt("MaxImbuedUses", finalMax);
				if (dyeingActive) {
					int color1 = ModUtils.getImbuedColor(imbuedInputs.get(0));
					int color2 = ModUtils.getImbuedColor(imbuedInputs.get(1));
					int finalColor = calculateAverageColorSimple(color1, color2);
					if (finalColor != -1) {
						resultTag.putInt("ImbuedColor", finalColor);
						ModUtils.applyDyeColor(result, finalColor);
					} else {
						ModUtils.applyDyeColor(result, -1);
					}
				}
			} else {
				ItemStack chosen = RandomUtils.nextBoolean() ? imbuedInputs.get(0) : imbuedInputs.get(1);
				copyImbuementData(chosen, result);
			}
		} else if (imbuingActive && imbuedCount == 1) {
			ModUtils.removeImbuement(result);
		}
		if (dyeingActive && !dyeColors.isEmpty() && imbuedCount < 2) {
			ModEnums.RepairColorMode mode = RecipesConfig.REPAIR_DYE_MODE.get();
			int finalColor;
			if (mode == ModEnums.RepairColorMode.MIX) {
				finalColor = averageColors(dyeColors);
			} else {
				if (dyeColors.size() == 1) {
					finalColor = RandomUtils.nextBoolean() ? dyeColors.get(0) : -1;
				} else {
					finalColor = dyeColors.get(RandomUtils.nextInt(0, dyeColors.size()));
				}
			}
			if (finalColor != -1) {
				if (ModUtils.isShineriteItem(result)) {
					resultTag.putInt("color", finalColor);
				} else {
					resultTag.putInt("ImbuedColor", finalColor);
					ModUtils.applyDyeColor(result, finalColor);
				}
			} else {
				ModUtils.applyDyeColor(result, -1);
			}
		}
		if (dyeingActive && resultTag.contains("display", Tag.TAG_COMPOUND)) {
			CompoundTag display = resultTag.getCompound("display");
			if (!display.contains("color")) {
				if (display.isEmpty()) {
					resultTag.remove("display");
				}
			}
		}
		cir.setReturnValue(result);
	}

	private static int calculateAverageColorSimple(int c1, int c2) {
		if (c1 == -1)
			return c2;
		if (c2 == -1)
			return c1;
		int r = ((c1 >> 16 & 255) + (c2 >> 16 & 255)) / 2;
		int g = ((c1 >> 8 & 255) + (c2 >> 8 & 255)) / 2;
		int blue = ((c1 & 255) + (c2 & 255)) / 2;
		return (r << 16) | (g << 8) | blue;
	}

	private static int averageColors(List<Integer> colors) {
		int r = 0, g = 0, b = 0;
		for (int c : colors) {
			r += (c >> 16) & 255;
			g += (c >> 8) & 255;
			b += c & 255;
		}
		int size = colors.size();
		return ((r / size) << 16) | ((g / size) << 8) | (b / size);
	}

	private static void copyImbuementData(ItemStack source, ItemStack target) {
		CompoundTag srcTag = source.getOrCreateTag();
		CompoundTag tgtTag = target.getOrCreateTag();
		if (srcTag.contains("ImbuedEffects")) {
			tgtTag.put("ImbuedEffects", srcTag.getList("ImbuedEffects", Tag.TAG_COMPOUND));
		}
		if (srcTag.contains("ImbuedUses")) {
			tgtTag.putInt("ImbuedUses", srcTag.getInt("ImbuedUses"));
		}
		if (srcTag.contains("MaxImbuedUses")) {
			tgtTag.putInt("MaxImbuedUses", srcTag.getInt("MaxImbuedUses"));
		}
		int color = ModUtils.getImbuedColor(source);
		if (color != -1) {
			tgtTag.putInt("ImbuedColor", color);
			ModUtils.applyDyeColor(target, color);
		}
	}

	private static List<MobEffectInstance> loadEffectsFrom(ItemStack stack) {
		List<MobEffectInstance> list = new ArrayList<>();
		ListTag tagList = stack.getOrCreateTag().getList("ImbuedEffects", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag effectTag = tagList.getCompound(i);
			MobEffectInstance effect = ModUtils.loadImbuedEffect(effectTag);
			if (effect != null) {
				list.add(effect);
			}
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
				int newDur = Math.max(existing.getDuration(), e.getDuration()) / 2;
				MobEffectInstance upgraded = new MobEffectInstance(type, newDur, newAmp, e.isAmbient(), e.isVisible(), e.showIcon());
				map.put(type, upgraded);
			}
		}
		return new ArrayList<>(map.values());
	}
}
