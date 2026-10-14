package net.jaams.shinerite.handler.behavior.item;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.init.ModEnchantments;
import net.jaams.shinerite.handler.mechanic.ShineriteAttackHandler;
import net.jaams.shinerite.handler.mechanic.InteractionHandler;
import net.jaams.shinerite.configuration.common.EnchantmentsConfig;

@Mod.EventBusSubscriber
public class ShineriteAttackBehaviorHandler {
	@SubscribeEvent
	public static void onEntityAttack(AttackEntityEvent event) {
		if (event.isCanceled())
			return;
		Entity attacker = event.getEntity();
		Entity target = event.getTarget();
		if (!(attacker instanceof LivingEntity livingAttacker) || attacker instanceof Player) {
			return;
		}
		if (!(target instanceof LivingEntity livingTarget)) {
			return;
		}
		Level level = attacker.level();
		if (level.isClientSide)
			return;
		ItemStack stack = livingAttacker.getMainHandItem();
		if (stack.isEmpty())
			return;
		if (stack.is(ModTags.SHINERITE_WEAPONS) || stack.is(ModTags.SHINERITE_TOOLS)) {
			float pitch = 0.9f + level.random.nextFloat() * 0.4f;
			ModUtils.playSound(livingTarget, "jaams_shinerite:shinerite_tool_hit", SoundSource.HOSTILE, 1.0F, pitch);
		}
		int lightbringerLevel = EnchantmentsConfig.LIGHTBRINGER.get() ? EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTBRINGER.get(), stack) : 0;
		int color = ModUtils.getCurrentColor(stack);
		if (lightbringerLevel > 0) {
			int glowColor = (color != InteractionHandler.DEFAULT_COLOR) ? color : 0xFFFFFF;
			ShineriteAttackHandler.applyGlowEffect(livingTarget, level, glowColor, lightbringerLevel);
		} else if (livingTarget.hasEffect(MobEffects.GLOWING) && color != InteractionHandler.DEFAULT_COLOR && stack.is(ModTags.SHINERITE)) {
			ShineriteAttackHandler.updateGlowColor(livingTarget, level, color);
		}
		if (ModUtils.isImbueableItem(stack)) {
			ShineriteAttackHandler.handleImbuedEffect(stack, livingTarget, livingAttacker, level);
		}
		if (livingTarget.hasEffect(MobEffects.GLOWING)) {
			int lightstealLevel = EnchantmentsConfig.LIGHTSTEAL.get() ? EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTSTEAL.get(), stack) : 0;
			if (lightstealLevel > 0) {
				ShineriteAttackHandler.applyLightsteal(stack, livingTarget, livingAttacker, level);
			}
		}
	}
}
