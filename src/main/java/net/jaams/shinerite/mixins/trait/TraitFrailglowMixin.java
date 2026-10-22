package net.jaams.shinerite.mixins.trait;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;


import net.minecraftforge.network.PacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTraits;
import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.handler.mechanic.ShineriteAttackHandler;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;
import net.jaams.shinerite.JaamsShineriteMod;

@Mixin(ItemStack.class)
public class TraitFrailglowMixin {
	@Inject(method = "hurtEnemy", at = @At("HEAD"))
	private void onFrailglowHurtEnemy(LivingEntity target, Player attacker, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		if (!TraitsConfig.FRAILGLOW.get()) {
			return;
		}
		if (!ModTraits.isFrailglowItem(stack)) {
			return;
		}
		if (!ModUtils.isHeldByPlayer(attacker, stack)) {
			return;
		}
		if (attacker.getAbilities().instabuild) {
			return;
		}
		if (!shouldBreakThisTime(stack, attacker)) {
			return;
		}
		applyFrailglowBreak(stack, attacker);
	}

	@Unique
	private net.jaams.shinerite.data.TraitModifierData.FrailglowEntry getFrailglowData(ItemStack stack) {
		return net.jaams.shinerite.data.TraitModifierData.getFrailglow(stack).orElse(null);
	}

	@Unique
	private double getFrailglowBreakChance(ItemStack stack) {
		net.jaams.shinerite.data.TraitModifierData.FrailglowEntry data = getFrailglowData(stack);
		if (data != null && data.break_chance != null) {
			return data.break_chance;
		}
		return ModUtils.getConfigOrNbtDouble(stack, "FrailglowBreakChance", TraitsConfig.FRAILGLOW_BREAK_CHANCE::get);
	}

	@Unique
	private boolean getFrailglowGlowingOnBreak(ItemStack stack) {
		net.jaams.shinerite.data.TraitModifierData.FrailglowEntry data = getFrailglowData(stack);
		if (data != null && data.glowing_on_break != null) {
			return data.glowing_on_break;
		}
		return ModUtils.getConfigOrNbtBoolean(stack, "FrailglowGlowingOnBreak", TraitsConfig.FRAILGLOW_GLOWING_ON_BREAK::get);
	}

	@Unique
	private int getFrailglowGlowDuration(ItemStack stack) {
		net.jaams.shinerite.data.TraitModifierData.FrailglowEntry data = getFrailglowData(stack);
		if (data != null && data.glow_duration != null) {
			return data.glow_duration;
		}
		return ModUtils.getConfigOrNbtInt(stack, "FrailglowGlowDuration", TraitsConfig.FRAILGLOW_GLOW_DURATION::get);
	}

	@Unique
	private int getFrailglowGlowAmplifier(ItemStack stack) {
		net.jaams.shinerite.data.TraitModifierData.FrailglowEntry data = getFrailglowData(stack);
		if (data != null && data.glow_amplifier != null) {
			return data.glow_amplifier;
		}
		return ModUtils.getConfigOrNbtInt(stack, "FrailglowGlowAmplifier", TraitsConfig.FRAILGLOW_GLOW_AMPLIFIER::get);
	}

	@Unique
	private boolean shouldBreakThisTime(ItemStack stack, Player player) {
		double breakChance = getFrailglowBreakChance(stack);
		return player.getRandom().nextDouble() < breakChance;
	}

	@Unique
	private void applyFrailglowBreak(ItemStack stack, Player player) {
		int dyeColor = -1;
		if (stack.getItem() instanceof IDyeableItem dyeable) {
			dyeColor = dyeable.getColor(stack);
		}
		final int itemColor = (dyeColor == -1) ? 0xA0FFFF : dyeColor;
		Level level = player.level();
		if (getFrailglowGlowingOnBreak(stack)) {
			int duration = getFrailglowGlowDuration(stack);
			int amplifier = getFrailglowGlowAmplifier(stack);
			applyGlowingVisualEffects(player, itemColor, duration, amplifier, level);
		}
		int particleCount = ModUtils.getConfigOrNbtInt(stack, "FrailglowBreakParticleCount", () -> 5);
		float particleSpeed = ModUtils.getConfigOrNbtFloat(stack, "FrailglowBreakParticleSpeed", () -> 1.0F);
		String breakSound = ModUtils.getConfigOrNbtString(stack, "FrailglowBreakSound", () -> "minecraft:block.amethyst_block.break");
		ModUtils.handleItemBreak(stack, player, particleSpeed, particleCount, breakSound);
		float soundPitch = 0.8F + level.random.nextFloat() * 0.4F;
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, soundPitch);
	}

	@Unique
	private void applyGlowingVisualEffects(Player player, int itemColor, int duration, int amplifier, Level level) {
		player.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			glowingColor.setColor(itemColor);
			player.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, amplifier, false, false, true));
			if (level instanceof ServerLevel serverLevel) {
				JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new GlowColorPacket(player.getId(), itemColor));
				float r = ((itemColor >> 16) & 255) / 255.0F;
				float g = ((itemColor >> 8) & 255) / 255.0F;
				float b = (itemColor & 255) / 255.0F;
				ShineriteAttackHandler.spawnFireworkBurst(serverLevel, player, r, g, b, 0.7f, 20);
			}
		});
	}
}
