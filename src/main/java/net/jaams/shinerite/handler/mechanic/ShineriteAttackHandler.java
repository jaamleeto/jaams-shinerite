package net.jaams.shinerite.handler.mechanic;

import org.joml.Vector3f;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.network.PacketDistributor;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.particle.CustomHeartParticleData;
import net.jaams.shinerite.particle.CustomFireworkParticleData;
import net.jaams.shinerite.particle.CustomCritParticleData;
import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.init.ModEnchantments;
import net.jaams.shinerite.configuration.common.EnchantmentsConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;
import net.jaams.shinerite.JaamsShineriteMod;

import javax.annotation.Nullable;

public class ShineriteAttackHandler {
	public static final double BURST_RADIUS = 0.5;
	public static final float EFFECT_DECAY_EXPONENT = 0.5f;

	public static void applyLightsteal(ItemStack itemStack, LivingEntity target, LivingEntity attacker, Level world) {
		int lightstealLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTSTEAL.get(), itemStack);
		if (lightstealLevel <= 0 || !EnchantmentsConfig.LIGHTSTEAL.get())
			return;
		float baseChance = EnchantmentsConfig.LIGHTSTEAL_CHANCE_PER_LEVEL.get().floatValue();
		float chance = Math.min(EnchantmentsConfig.LIGHTSTEAL_MAX_CHANCE.get().floatValue(), baseChance * lightstealLevel);
		float baseHeal = EnchantmentsConfig.LIGHTSTEAL_HEAL_AMOUNT.get().floatValue();
		float healPerLevel = EnchantmentsConfig.LIGHTSTEAL_HEAL_PER_LEVEL.get().floatValue();
		float totalHealAmount = baseHeal + (healPerLevel * (lightstealLevel - 1));
		int successes = 0;
		for (int i = 0; i < lightstealLevel; i++) {
			if (world.random.nextFloat() < chance) {
				successes++;
			}
		}
		if (successes > 0) {
			float finalHeal = totalHealAmount * successes;
			attacker.heal(finalHeal);
			float pitch = 0.9f + (successes * 0.1f);
			float volume = 0.8f + (successes * 0.2f);
			ModUtils.playSound(target, "jaams_shinerite:lightsteal_hit", SoundSource.PLAYERS, volume, pitch);
			if (world instanceof ServerLevel serverLevel) {
				final int finalSuccesses = successes;
				final int finalLightstealLevel = lightstealLevel;
				target.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(targetGlow -> {
					int stolenColor = targetGlow.getColor();
					int particleCount = 2 + finalSuccesses * 2 + finalLightstealLevel;
					double offsetScale = 0.4 + (finalLightstealLevel * 0.05);
					double speedScale = 0.3 + (finalLightstealLevel * 0.05);
					spawnHeartParticles(serverLevel, target, stolenColor, particleCount, offsetScale, speedScale, finalLightstealLevel);
					int stolenGlowDuration = EnchantmentsConfig.LIGHTSTEAL_STOLEN_GLOW_BASE_DURATION.get() + (finalSuccesses * EnchantmentsConfig.LIGHTSTEAL_STOLEN_GLOW_DURATION_PER_SUCCESS.get());
					attacker.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(attackerGlow -> {
						attackerGlow.setColor(stolenColor);
						attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING, stolenGlowDuration, 0, false, false, true));
						JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> attacker), new GlowColorPacket(attacker.getId(), stolenColor));
					});
				});
			}
		}
	}

	public static boolean handleImbuedEffect(ItemStack itemStack, LivingEntity target, LivingEntity attacker, Level world) {
		if (!MechanicsConfig.IMBUING.get()) {
			return false;
		}
		if (itemStack.getItem().isEdible()) {
			return false;
		}
		CompoundTag tag = itemStack.getOrCreateTag();
		if (!tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
			return false;
		}
		ListTag effectsTag = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
		if (effectsTag.isEmpty()) {
			return false;
		}
		int uses = tag.getInt("ImbuedUses");
		int maxUses = tag.getInt("MaxImbuedUses");
		if (uses <= 0 || maxUses <= 0) {
			return false;
		}
		float useFraction = Math.min(1.0F, (float) uses / maxUses);
		float scaledFraction = (float) Math.pow(useFraction, MechanicsConfig.IMBUED_EFFECT_DECAY_EXPONENT.get());
		boolean isCreative = attacker instanceof Player player && player.isCreative();
		boolean shouldConsume = !isCreative || MechanicsConfig.IMBUED_CONSUME_USES_IN_CREATIVE.get();
		int imbuedColor = ModUtils.getImbuedParticleColor(itemStack);
		boolean applied = applyImbuedEffects(effectsTag, attacker, target, scaledFraction, MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get(), true);
		if (!applied) {
			return false;
		}
		if (!world.isClientSide()) {
			spawnImbuedCritBurst(world, target, imbuedColor, 0.15f, 6);
			world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (attacker instanceof Player player && shouldConsume) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack offHand = player.getOffhandItem();
			if (mainHand != itemStack && offHand != itemStack) {
				return true;
			}
			ItemStack activeStack = itemStack;
			if (uses == 1 && activeStack.getCount() > 1) {
				activeStack = activeStack.split(1);
			}
			CompoundTag activeTag = activeStack.getOrCreateTag();
			uses = activeTag.getInt("ImbuedUses");
			uses--;
			activeTag.putInt("ImbuedUses", uses);
			if (uses > 0) {
				updateImbueColor(activeStack, activeTag, uses, maxUses);
			} else {
				ModUtils.removeImbuement(activeStack);
				resetItemImbue(activeStack, world, attacker);
			}
			if (activeStack.getMaxStackSize() == 1) {
				int extraDamage = MechanicsConfig.IMBUED_DAMAGE_PER_HIT.get();
				activeStack.hurtAndBreak(2 + extraDamage, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
			}
			if (activeStack != itemStack) {
				if (!activeStack.isEmpty()) {
					if (!player.addItem(activeStack)) {
						player.drop(activeStack, false);
					}
				}
			}
		} else if (!(attacker instanceof Player) && shouldConsume) {
			int usesAfter = uses - 1;
			if (usesAfter > 0) {
				tag.putInt("ImbuedUses", usesAfter);
				updateImbueColor(itemStack, tag, usesAfter, maxUses);
			} else {
				ModUtils.removeImbuement(itemStack);
				resetItemImbue(itemStack, world, attacker);
			}
		}
		return true;
	}

	/**
	 * Central, defensive imbuement application used by every source (melee, projectiles, armor, ...).
	 * Prevents the classic bugs: applying to the source itself, applying on the client side, refreshing
	 * effects every tick (which leaves them stuck at 0:00 forever) and stacking zero/negative durations.
	 *
	 * @return true if at least one effect was actually applied.
	 */
	public static boolean applyImbuedEffects(ListTag effectsTag, @Nullable Entity source, LivingEntity target, float scaledFraction, int minDurationTicks, boolean allowInstantaneous) {
		return applyImbuedEffectsInternal(effectsTag, source, target, scaledFraction, minDurationTicks, allowInstantaneous, false);
	}

	/**
	 * Variant used when a player consumes an imbued edible: the eater is the target, so the
	 * protection against self-application must be bypassed and instantaneous effects (harm/heal)
	 * must be allowed against oneself.
	 */
	public static boolean applyImbuedEffectsOnSelf(ListTag effectsTag, LivingEntity target, float scaledFraction, int minDurationTicks) {
		return applyImbuedEffectsInternal(effectsTag, target, target, scaledFraction, minDurationTicks, true, true);
	}

	private static boolean applyImbuedEffectsInternal(ListTag effectsTag, @Nullable Entity source, LivingEntity target, float scaledFraction, int minDurationTicks, boolean allowInstantaneous, boolean allowSelfTarget) {
		if (effectsTag == null || effectsTag.isEmpty() || target == null || !target.isAlive() || target.isRemoved()) {
			return false;
		}
		if (!allowSelfTarget && isSelfOrRiddenTarget(source, target)) {
			return false;
		}
		int minTicks = Math.max(1, minDurationTicks);
		float fraction = Math.min(1.0F, Math.max(0.0F, scaledFraction));
		boolean applied = false;
		for (int i = 0; i < effectsTag.size(); i++) {
			CompoundTag effectData = effectsTag.getCompound(i);
			if (effectData.isEmpty()) {
				continue;
			}
			MobEffectInstance savedEffect = ModUtils.loadImbuedEffect(effectData);
			if (savedEffect == null) {
				continue;
			}
			MobEffect effect = savedEffect.getEffect();
			if (effect == null) {
				continue;
			}
			int finalDuration = Math.max(minTicks, (int) (savedEffect.getDuration() * fraction));
			int finalAmplifier = Math.max(0, (int) (savedEffect.getAmplifier() * fraction));
			if (effect.isInstantenous()) {
				if (!allowInstantaneous || !(source instanceof LivingEntity livingSource)) {
					continue;
				}
				applyInstantaneousImbuementEffect(effect, target, livingSource, finalAmplifier);
				applied = true;
				continue;
			}
			if (!target.canBeAffected(new MobEffectInstance(effect, finalDuration, finalAmplifier))) {
				continue;
			}
			MobEffectInstance current = target.getEffect(effect);
			if (current != null && current.getAmplifier() >= finalAmplifier && current.getDuration() >= finalDuration) {
				continue;
			}
			if (target.addEffect(new MobEffectInstance(effect, finalDuration, finalAmplifier, savedEffect.isAmbient(), savedEffect.isVisible(), savedEffect.showIcon()))) {
				applied = true;
			}
		}
		return applied;
	}

	private static void applyInstantaneousImbuementEffect(MobEffect effect, LivingEntity target, LivingEntity source, int amplifier) {
		if (effect == MobEffects.HARM) {
			damageOrHealInstant(target, source, 6.0F * (amplifier + 1), false);
		} else if (effect == MobEffects.HEAL) {
			damageOrHealInstant(target, source, 4.0F * (amplifier + 1), true);
		} else {
			effect.applyInstantenousEffect(source, source, target, amplifier, 1.0);
		}
	}

	private static void damageOrHealInstant(LivingEntity target, LivingEntity source, float amount, boolean heal) {
		int originalInvulnerableTime = target.invulnerableTime;
		target.invulnerableTime = 0;
		if (target.isInvertedHealAndHarm()) {
			if (heal) {
				target.hurt(source.damageSources().magic(), amount);
			} else {
				target.heal(amount);
			}
		} else {
			if (heal) {
				target.heal(amount);
			} else {
				target.hurt(source.damageSources().magic(), amount);
			}
		}
		target.invulnerableTime = originalInvulnerableTime;
	}

	public static boolean isSelfOrRiddenTarget(@Nullable Entity source, LivingEntity target) {
		if (source == null || target == null) {
			return false;
		}
		return source == target || source.getVehicle() == target || target.getVehicle() == source;
	}

	public static void applyGlowEffect(LivingEntity target, Level world, int color, int enchantmentLevel) {
		target.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			glowingColor.setColor(color);
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, EnchantmentsConfig.LIGHTBRINGER_DURATION_PER_LEVEL.get() * enchantmentLevel, 0, false, false, true));
			if (world instanceof ServerLevel serverLevel) {
				JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new GlowColorPacket(target.getId(), color));
				float r = ((color >> 16) & 255) / 255.0F;
				float g = ((color >> 8) & 255) / 255.0F;
				float b = (color & 255) / 255.0F;
				spawnFireworkBurst(serverLevel, target, r, g, b, 0.7f, 20);
			}
		});
	}

	public static void updateGlowColor(LivingEntity target, Level world, int color) {
		target.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			glowingColor.setColor(color);
			if (world instanceof ServerLevel serverLevel) {
				JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new GlowColorPacket(target.getId(), color));
			}
		});
	}

	public static void spawnFireworkBurst(ServerLevel level, LivingEntity entity, float r, float g, float b, float baseSize, int baseCount) {
		Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
		int count = 3;
		float minSize = 0.2f;
		float maxSize = 0.5f;
		for (int i = 0; i < count; i++) {
			double theta = level.random.nextDouble() * 2 * Math.PI;
			double phi = Math.acos(2 * level.random.nextDouble() - 1);
			float speed = 0.25f + level.random.nextFloat() * 0.3f;
			double vx = speed * Math.sin(phi) * Math.cos(theta);
			double vy = speed * Math.sin(phi) * Math.sin(theta);
			double vz = speed * Math.cos(phi);
			double offsetX = level.random.nextGaussian() * 0.15;
			double offsetY = level.random.nextGaussian() * 0.15;
			double offsetZ = level.random.nextGaussian() * 0.15;
			float size = minSize + level.random.nextFloat() * (maxSize - minSize);
			float colorVar = 0.8f + level.random.nextFloat() * 0.4f;
			float particleR = Math.min(1.0f, r * colorVar);
			float particleG = Math.min(1.0f, g * colorVar);
			float particleB = Math.min(1.0f, b * colorVar);
			level.sendParticles(new CustomFireworkParticleData(particleR, particleG, particleB, size), center.x + offsetX, center.y + offsetY, center.z + offsetZ, 1, vx, vy, vz, 0.0);
		}
	}

	public static void spawnImbuedCritBurst(Level world, LivingEntity entity, int color, float particleSize, int baseParticleCount) {
		int safeColor = sanitizeParticleColor(color);
		float r = ((safeColor >> 16) & 255) / 255.0F;
		float g = ((safeColor >> 8) & 255) / 255.0F;
		float b = (safeColor & 255) / 255.0F;
		spawnCritBurst(world, entity, r, g, b, particleSize, baseParticleCount);
	}

	public static void spawnImbuedCritBurst(Level world, LivingEntity entity, ItemStack weapon, float particleSize, int baseParticleCount) {
		spawnImbuedCritBurst(world, entity, ModUtils.getImbuedParticleColor(weapon), particleSize, baseParticleCount);
	}

	private static int sanitizeParticleColor(int color) {
		return color >= 0 && color <= 0xFFFFFF ? color : ModUtils.IMBUE_FALLBACK_COLOR;
	}

	public static void spawnCritBurst(Level world, LivingEntity entity, float r, float g, float b, float particleSize, int baseParticleCount) {
		if (!(world instanceof ServerLevel serverLevel))
			return;
		float bbWidth = entity.getBbWidth();
		float bbHeight = entity.getBbHeight();
		int particleCount = Math.max(baseParticleCount, Math.min(baseParticleCount * 2, (int) (baseParticleCount + baseParticleCount * bbWidth * bbHeight / 2.0f)));
		Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
		float darkenChance = 0.35f;
		float darkenFactor = 0.6f;
		for (int i = 0; i < particleCount; i++) {
			double[] offsets = generateSpacedOffsets(world.random, bbWidth, bbHeight, i, particleCount);
			double[] speeds = generateParticleSpeeds(world.random);
			double xPos = center.x + offsets[0];
			double yPos = center.y + offsets[1];
			double zPos = center.z + offsets[2];
			float particleR = r, particleG = g, particleB = b;
			if (world.random.nextFloat() < darkenChance) {
				particleR = Math.max(0.0f, r * darkenFactor);
				particleG = Math.max(0.0f, g * darkenFactor);
				particleB = Math.max(0.0f, b * darkenFactor);
			}
			serverLevel.sendParticles(new CustomCritParticleData(particleR, particleG, particleB, particleSize), xPos, yPos, zPos, 1, speeds[0], speeds[1], speeds[2], 0.0f);
		}
	}

	public static void updateImbueColor(ItemStack itemStack, CompoundTag tag, int uses, int maxUses) {
		ModUtils.updateImbuedUseColor(itemStack, tag, uses, maxUses);
	}

	private static void resetItemImbue(ItemStack itemStack, Level world, LivingEntity attacker) {
		ModUtils.removeColor(itemStack);
		if (!world.isClientSide() && attacker instanceof Player player) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack offHand = player.getOffhandItem();
			if (mainHand != itemStack && offHand != itemStack) {
				playSound(world, attacker, SoundEvents.BREWING_STAND_BREW);
			}
			Vec3 handPos = getHandPosition(attacker, player.getUsedItemHand());
			((ServerLevel) world).sendParticles(new DustParticleOptions(new Vector3f(1.0f, 1.0f, 1.0f), 1.0f), handPos.x, handPos.y, handPos.z, 10, 0.2, 0.2, 0.2, 0.05);
		}
	}

	private static void spawnHeartParticles(ServerLevel serverLevel, LivingEntity target, int color, int count, double offsetScale, double speedScale, int lightstealLevel) {
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		Vec3 pos = target.position().add(0, target.getBbHeight() * 0.5, 0);
		float baseSize = 0.25f;
		float size = baseSize * (1.0f + 0.2f * (lightstealLevel - 1));
		float maxSize = baseSize * 1.8f;
		size = Math.min(size, maxSize);
		for (int i = 0; i < count; i++) {
			double xOffset = serverLevel.random.nextGaussian() * offsetScale;
			double yOffset = serverLevel.random.nextGaussian() * offsetScale;
			double zOffset = serverLevel.random.nextGaussian() * offsetScale;
			double xSpeed = serverLevel.random.nextGaussian() * speedScale;
			double ySpeed = serverLevel.random.nextGaussian() * speedScale;
			double zSpeed = serverLevel.random.nextGaussian() * speedScale;
			serverLevel.sendParticles(new CustomHeartParticleData(r, g, b, size), pos.x + xOffset, pos.y + yOffset, pos.z + zOffset, 1, xSpeed, ySpeed, zSpeed, 0.0);
		}
	}

	private static double[] generateSpacedOffsets(RandomSource random, float bbWidth, float bbHeight, int index, int particleCount) {
		float xzScale = Math.max(bbWidth * 0.8f, 0.3f);
		float yScale = Math.max(bbHeight * 0.5f, 0.2f);
		double theta = 2 * Math.PI * index / particleCount;
		double yOffset = random.nextDouble() * yScale * (random.nextBoolean() ? 1 : -1);
		return new double[]{xzScale * Math.cos(theta), yOffset, xzScale * Math.sin(theta)};
	}

	private static double[] generateParticleSpeeds(RandomSource random) {
		double speed = 0.05 + random.nextDouble() * 0.15;
		double theta = random.nextDouble() * 2 * Math.PI;
		double phi = random.nextDouble() * Math.PI;
		return new double[]{speed * Math.sin(phi) * Math.cos(theta) * (random.nextBoolean() ? 1 : -1), speed * Math.cos(phi) + random.nextDouble() * 0.05, speed * Math.sin(phi) * Math.sin(theta) * (random.nextBoolean() ? 1 : -1)};
	}

	private static void playSound(Level world, LivingEntity entity, SoundEvent sound) {
		world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.NEUTRAL, 1.0f, 1.0f);
	}

	private static Vec3 getHandPosition(LivingEntity entity, InteractionHand hand) {
		boolean isMainHand = hand == InteractionHand.MAIN_HAND;
		Vec3 eyePosition = entity.getEyePosition(1.0F);
		Vec3 lookDirection = entity.getLookAngle();
		float handOffset = isMainHand ? 0.2F : -0.2F;
		return eyePosition.add(lookDirection.cross(new Vec3(0, 1, 0)).scale(handOffset)).add(lookDirection.scale(0.6));
	}

	private static int lightenImbuedColor(int color, float fraction) {
		return ModUtils.lightenImbuedColor(color, fraction);
	}
}
