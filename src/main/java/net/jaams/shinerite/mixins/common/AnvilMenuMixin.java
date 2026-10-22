package net.jaams.shinerite.mixins.common;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;


import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.RecipesConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Inject(method = "createResult", at = @At("TAIL"))
	private void smartFusionShinerite(CallbackInfo ci) {
		if (!RecipesConfig.ANVIL_ITEM_RECIPE.get()) {
			return;
		}
		boolean dyeingEnabled = MechanicsConfig.DYEING.get();
		boolean imbuingEnabled = MechanicsConfig.IMBUING.get();
		if (!dyeingEnabled && !imbuingEnabled) {
			return;
		}
		AnvilMenu anvil = (AnvilMenu) (Object) this;
		ItemStack left = anvil.getSlot(0).getItem().copy();
		ItemStack right = anvil.getSlot(1).getItem().copy();
		ItemStack output = anvil.getSlot(2).getItem();
		if (left.isEmpty() || right.isEmpty() || output.isEmpty()) {
			return;
		}
		boolean leftImbued = imbuingEnabled && ModUtils.isImbued(left);
		boolean rightImbued = imbuingEnabled && ModUtils.isImbued(right);
		if (!leftImbued && !rightImbued) {
			if (dyeingEnabled && RecipesConfig.ANVIL_DYE_ONLY_MIX.get()) {
				int color = calculateAverageColor(left, right, false, false);
				if (color != -1 && ModUtils.isImbueableItem(output)) {
					ModUtils.applyDyeColor(output, color);
				}
			}
			return;
		}
		if (!ModUtils.isImbueableItem(left) && !ModUtils.isImbueableItem(right)) {
			return;
		}
		if (!ModUtils.isImbueableItem(output)) {
			return;
		}
		ItemStack result = output.copy();
		CompoundTag tag = result.getOrCreateTag();
		if (dyeingEnabled && RecipesConfig.ANVIL_IMBUING_TRANSFER_COLOR.get()) {
			int finalColor = calculateAverageColor(left, right, leftImbued, rightImbued);
			if (finalColor != -1) {
				tag.putInt("ImbuedColor", finalColor);
				ModUtils.applyDyeColor(result, finalColor);
			}
		}
		if (imbuingEnabled && RecipesConfig.ANVIL_IMBUING_MERGE_EFFECTS.get()) {
			List<MobEffectInstance> allEffects = new ArrayList<>();
			if (leftImbued)
				allEffects.addAll(loadEffectsFrom(left));
			if (rightImbued)
				allEffects.addAll(loadEffectsFrom(right));
			if (!allEffects.isEmpty()) {
				List<MobEffectInstance> merged = mergeSameEffects(allEffects);
				ListTag effectsTag = new ListTag();
				for (MobEffectInstance effect : merged) {
					effectsTag.add(effect.save(new CompoundTag()));
				}
				tag.put("ImbuedEffects", effectsTag);
			}
		}
		if (imbuingEnabled && (leftImbued || rightImbued)) {
			int usesL = leftImbued ? left.getOrCreateTag().getInt("ImbuedUses") : 0;
			int usesR = rightImbued ? right.getOrCreateTag().getInt("ImbuedUses") : 0;
			int maxL = leftImbued ? left.getOrCreateTag().getInt("MaxImbuedUses") : 0;
			int maxR = rightImbued ? right.getOrCreateTag().getInt("MaxImbuedUses") : 0;
			int finalUses = Math.max(1, (usesL + usesR) / 2);
			int finalMax = Math.max(maxL, maxR);
			tag.putInt("ImbuedUses", finalUses);
			tag.putInt("MaxImbuedUses", Math.max(finalMax, finalUses));
		}
		anvil.getSlot(2).set(result);
		anvil.broadcastChanges();
	}

	@Unique
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

	@Unique
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

	@Unique
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
