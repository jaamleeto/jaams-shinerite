package net.jaams.shinerite.mixins.common;

import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;


import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.init.ModEnchantments;
import net.jaams.shinerite.handler.mechanic.ShineriteAttackHandler;
import net.jaams.shinerite.handler.mechanic.InteractionHandler;
import net.jaams.shinerite.configuration.common.EnchantmentsConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.client.TooltipsConfig;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void addShineriteDyedTooltip(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, List<Component> list, MutableComponent nombre, int hideFlags) {
		ItemStack stack = (ItemStack) (Object) this;
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			return;
		}
		if (TooltipsConfig.TOOLTIP_COATED_STATUS.get() && MechanicsConfig.COATING.get() && ModUtils.isCoatableItem(stack) && tag.getBoolean("Coated")) {
			list.add(Component.translatable("tooltip.jaams_shinerite.coated").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
		}
		if (ModUtils.isDyeableItem(stack) && MechanicsConfig.DYEING.get() && ModUtils.isDyed(stack)) {
			int color = ModUtils.getCurrentColor(stack);
			if (!flag.isAdvanced()) {
				if (TooltipsConfig.TOOLTIP_DYED_STATUS.get()) {
					list.add(Component.translatable("tooltip.jaams_shinerite.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				}
			} else {
				String hex = String.format("#%06X", 0xFFFFFF & color);
				MutableComponent hexText = Component.literal(" " + hex);
				if (TooltipsConfig.TOOLTIP_COLOR_HEX_COLORED.get()) {
					hexText.withStyle(s -> s.withColor(color));
				} else {
					hexText.withStyle(ChatFormatting.GRAY);
				}
				list.add(Component.translatable("tooltip.jaams_shinerite.color").withStyle(ChatFormatting.GRAY).append(hexText));
			}
		}
	}

	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 1, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
		private void addImbuedUses(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, List<Component> list, MutableComponent nombre, int hideFlags) {
		ItemStack stack = (ItemStack) (Object) this;
		if (!MechanicsConfig.IMBUING.get() || !ModUtils.isImbued(stack)) {
			return;
		}
		if (!flag.isAdvanced() || stack.getItem().isEdible()) {
			return;
		}
		if (!TooltipsConfig.TOOLTIP_IMBUED_USES.get()) {
			return;
		}
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("ImbuedUses", Tag.TAG_INT) || !tag.contains("MaxImbuedUses", Tag.TAG_INT)) {
			return;
		}
		int current = tag.getInt("ImbuedUses");
		int max = tag.getInt("MaxImbuedUses");
		list.add(Component.translatable("tooltip.jaams_shinerite.imbued.uses", current, max).withStyle(ChatFormatting.WHITE));
	}

	@Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shouldShowInTooltip(ILnet/minecraft/world/item/ItemStack$TooltipPart;)Z", ordinal = 4, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void addImbuedTooltipDirecto(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, List<Component> list, MutableComponent nombre, int hideFlags) {
		ItemStack stack = (ItemStack) (Object) this;
		addCapabilitiesSection(list, stack);
		if (!MechanicsConfig.IMBUING.get() || !ModUtils.isImbued(stack)) {
			return;
		}
		if (!TooltipsConfig.TOOLTIP_IMBUED_EFFECTS.get()) {
			return;
		}
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
			return;
		}
		ListTag effectsTag = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
		if (effectsTag.isEmpty()) {
			return;
		}
		int uses = tag.getInt("ImbuedUses");
		if (uses <= 0) {
			return;
		}
		list.add(CommonComponents.EMPTY);
		list.add(Component.translatable("tooltip.jaams_shinerite.imbued.with").withStyle(ChatFormatting.GRAY));
		for (int i = 0; i < effectsTag.size(); i++) {
			CompoundTag effectData = effectsTag.getCompound(i);
			MobEffectInstance effectInst = ModUtils.loadImbuedEffect(effectData);
			if (effectInst == null)
				continue;
			MobEffect effect = effectInst.getEffect();
			ChatFormatting color = effect.isBeneficial() ? ChatFormatting.BLUE : ChatFormatting.RED;
			MutableComponent line = Component.translatable(effect.getDescriptionId()).withStyle(s -> s.withItalic(false).withColor(color));
			int level = effectInst.getAmplifier() + 1;
			if (level >= 2 && level <= 10) {
				line.append(Component.literal(" " + ModUtils.toRoman(level)).withStyle(color));
			}
			int duration = effectInst.getDuration();
			if (duration > 20) {
				int totalSeconds = duration / 20;
				int minutes = totalSeconds / 60;
				int seconds = totalSeconds % 60;
				String time = String.format("%02d:%02d", minutes, seconds);
				line.append(Component.literal(" (" + time + ")").withStyle(color));
			}
			list.add(line);
		}
	}

	@Inject(method = "finishUsingItem", at = @At("TAIL"))
	private void consumeImbueOnEat(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		if (!level.isClientSide() && stack.getItem().isEdible()) {
			if (entity instanceof Player player && player.getAbilities().instabuild && !MechanicsConfig.IMBUED_CONSUME_USES_IN_CREATIVE.get()) {
				return;
			}
			CompoundTag tag = stack.getTag();
			if (tag == null || !MechanicsConfig.IMBUING.get() || !ModUtils.isImbueableItem(stack) || !ModUtils.isImbued(stack)) {
				return;
			}
			int uses = tag.getInt("ImbuedUses") - 1;
			if (uses <= 0) {
				ModUtils.removeImbuement(stack);
			} else {
				tag.putInt("ImbuedUses", uses);
			}
		}
	}

	@Inject(method = "hurtEnemy", at = @At("HEAD"))
	private void injectImbuedLogic(LivingEntity target, Player attacker, CallbackInfo ci) {
		ItemStack itemStack = (ItemStack) (Object) this;
		Level world = attacker.level();
		if (itemStack.is(ModTags.SHINERITE_WEAPONS) || itemStack.is(ModTags.SHINERITE_TOOLS)) {
			float pitch = 0.9f + world.random.nextFloat() * 0.4f;
			ModUtils.playSound(target, "jaams_shinerite:shinerite_tool_hit", SoundSource.PLAYERS, 1.0f, pitch);
		}
		int lightbringerLevel = EnchantmentsConfig.LIGHTBRINGER.get() ? EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTBRINGER.get(), itemStack) : 0;
		int color = ModUtils.getCurrentColor(itemStack);
		if (lightbringerLevel > 0) {
			int glowColor = (color != InteractionHandler.DEFAULT_COLOR) ? color : 0xFFFFFF;
			ShineriteAttackHandler.applyGlowEffect(target, world, glowColor, lightbringerLevel);
		} else if (target.hasEffect(MobEffects.GLOWING) && color != InteractionHandler.DEFAULT_COLOR && (itemStack.is(ModTags.SHINERITE))) {
			ShineriteAttackHandler.updateGlowColor(target, world, color);
		}
		if (ModUtils.isImbueableItem(itemStack)) {
			ShineriteAttackHandler.handleImbuedEffect(itemStack, target, attacker, world);
		}
		if (target.hasEffect(MobEffects.GLOWING)) {
			int lightstealLevel = EnchantmentsConfig.LIGHTSTEAL.get() ? EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTSTEAL.get(), itemStack) : 0;
			if (lightstealLevel > 0) {
				ShineriteAttackHandler.applyLightsteal(itemStack, target, attacker, world);
			}
		}
	}

	private void addCapabilitiesSection(List<Component> list, ItemStack stack) {
		boolean coatable = MechanicsConfig.COATING.get() && ModUtils.isCoatableItem(stack) && TooltipsConfig.TOOLTIP_COATABLE_STATUS.get();
		boolean imbuable = MechanicsConfig.IMBUING.get() && ModUtils.isImbueableItem(stack) && TooltipsConfig.TOOLTIP_IMBUABLE_STATUS.get();
		if (!coatable && !imbuable) {
			return;
		}
		list.add(CommonComponents.EMPTY);
		list.add(Component.translatable("tooltip.jaams_shinerite.capability.header").withStyle(ChatFormatting.GRAY));
		if (coatable) {
			list.add(Component.literal(" ").append(Component.translatable("tooltip.jaams_shinerite.capability.coatable")).withStyle(ChatFormatting.BLUE));
		}
		if (imbuable) {
			list.add(Component.literal(" ").append(Component.translatable("tooltip.jaams_shinerite.capability.imbuable")).withStyle(ChatFormatting.BLUE));
		}
	}
}
