package net.jaams.shinerite.handler.enchant;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.init.ModEnchantments;
import net.jaams.shinerite.handler.mechanic.ShineriteAttackHandler;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;
import net.jaams.shinerite.block.ShineriteOreBlock;
import net.jaams.shinerite.block.DeepslateShineriteOreBlock;
import net.jaams.shinerite.JaamsShineriteMod;

import java.util.Map;

@Mod.EventBusSubscriber
public class EnchantmentsHandler {
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (event.getState().getBlock() instanceof ShineriteOreBlock || event.getState().getBlock() instanceof DeepslateShineriteOreBlock) {
			Level level = (Level) event.getLevel();
			BlockPos pos = event.getPos();
			Player player = event.getPlayer();
			if (player.isCreative()) {
				return;
			}
			ItemStack tool = player.getMainHandItem();
			RandomSource random = RandomSource.create();
			if (!level.isClientSide()) {
				boolean hasSilkTouch = !tool.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
				boolean hasMajruszsSmelter = false;
				boolean hasOtherSmeltingEnchantment = false;
				boolean hasPickaxeHeater = ModUtils.isItemInCuriosSlot(player, "artifacts:pickaxe_heater", "hands");
				if (!tool.isEmpty()) {
					for (Map.Entry<Enchantment, Integer> enchantmentEntry : EnchantmentHelper.getEnchantments(tool).entrySet()) {
						Enchantment enchantment = enchantmentEntry.getKey();
						ResourceLocation enchantmentId = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
						if (enchantmentId != null) {
							String enchantmentIdStr = enchantmentId.toString().toLowerCase();
							if (enchantmentIdStr.equals("majruszsenchantments:smelter")) {
								hasMajruszsSmelter = true;
							} else if (ModUtils.hasSmeltingEnchantment(enchantmentIdStr)) {
								hasOtherSmeltingEnchantment = true;
							}
						}
					}
				}
				if (hasMajruszsSmelter && !hasOtherSmeltingEnchantment && !hasPickaxeHeater && !hasSilkTouch && !player.isCrouching() && event.getState().canHarvestBlock(level, pos, player)) {
					int particleCount = 1 + level.random.nextInt(4);
					double centerX = pos.getX() + 0.5;
					double centerY = pos.getY() + 0.5;
					double centerZ = pos.getZ() + 0.5;
					for (int i = 0; i < particleCount; i++) {
						double offsetX = centerX + (level.random.nextDouble() * 0.2 - 0.1);
						double offsetY = centerY + (level.random.nextDouble() * 0.2 - 0.1);
						double offsetZ = centerZ + (level.random.nextDouble() * 0.2 - 0.1);
						double velocityX = level.random.nextDouble() * 0.1 - 0.05;
						double velocityY = level.random.nextDouble() * 0.1;
						double velocityZ = level.random.nextDouble() * 0.1 - 0.05;
						((ServerLevel) level).sendParticles(ParticleTypes.FLAME, offsetX, offsetY, offsetZ, 1, velocityX, velocityY, velocityZ, 0.0);
					}
				}
			}
		}
	}

	private static final int GLOW_DURATION_PER_LEVEL = 20;
	private static final int DEFAULT_GLOW_COLOR = -1;
	private static final float DAMAGE_CHANCE_PER_LEVEL = 0.1f;
	private static final float MAGIC_DAMAGE_PER_PIECE = 1.0f;
	private static final float KNOCKBACK_STRENGTH = 0.5f;
	private static final int LIGHTGUARD_PARTICLE_COUNT = 6;
	private static final float LIGHTGUARD_PARTICLE_SIZE = 0.15f;
	private static final float RETALIATION_SOUND_VOLUME = 0.7F;
	private static final float RETALIATION_SOUND_PITCH = 1.3F;
	private static final int RETALIATION_WEARER_PARTICLE_COUNT = 10;
	private static final float RETALIATION_WEARER_PARTICLE_SIZE = 0.12f;

	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof LivingEntity))
			return;
		LivingEntity target = (LivingEntity) event.getEntity();
		if (!(event.getSource().getEntity() instanceof LivingEntity))
			return;
		LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
		if (attacker == target)
			return;
		Level world = target.level();
		handleLightguard(target, attacker, world);
		handleArmorImbuedEffects(target, attacker, world);
	}

	private static void handleLightguard(LivingEntity target, LivingEntity attacker, Level world) {
		int totalEnchantLevel = 0;
		int enchantedArmorPieces = 0;
		float redSum = 0, greenSum = 0, blueSum = 0;
		for (ItemStack armor : target.getArmorSlots()) {
			int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTGUARD.get(), armor);
			if (enchantLevel > 0) {
				totalEnchantLevel += enchantLevel;
				enchantedArmorPieces++;
				int armorColor = ModUtils.getCurrentColor(armor);
				redSum += ((armorColor >> 16) & 0xFF) / 255.0f;
				greenSum += ((armorColor >> 8) & 0xFF) / 255.0f;
				blueSum += (armorColor & 0xFF) / 255.0f;
			}
		}
		if (totalEnchantLevel == 0)
			return;
		int glowColor = DEFAULT_GLOW_COLOR;
		if (enchantedArmorPieces > 0) {
			int r = Math.min(255, (int) (redSum / enchantedArmorPieces * 255));
			int g = Math.min(255, (int) (greenSum / enchantedArmorPieces * 255));
			int b = Math.min(255, (int) (blueSum / enchantedArmorPieces * 255));
			glowColor = (r << 16) | (g << 8) | b;
		}
		applyGlowEffect(attacker, world, glowColor, totalEnchantLevel);
		float damageChance = totalEnchantLevel * DAMAGE_CHANCE_PER_LEVEL;
		if (target.getRandom().nextFloat() < damageChance) {
			float magicDamage = enchantedArmorPieces * MAGIC_DAMAGE_PER_PIECE;
			attacker.hurt(target.level().damageSources().magic(), magicDamage);
			attacker.knockback(KNOCKBACK_STRENGTH, target.getX() - attacker.getX(), target.getZ() - attacker.getZ());
		}
	}

	private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private static void handleArmorImbuedEffects(LivingEntity target, LivingEntity attacker, Level world) {
		if (!MechanicsConfig.IMBUING.get() || !MechanicsConfig.IMBUED_ARMOR_RETALIATE_ON_HIT.get() || world.isClientSide())
			return;
		if (MechanicsConfig.IMBUED_ARMOR_RETALIATE_HOSTILE_ONLY.get() && !(attacker instanceof net.minecraft.world.entity.monster.Monster))
			return;
		boolean isCreative = attacker instanceof Player player && player.isCreative();
		boolean shouldConsume = !isCreative || MechanicsConfig.IMBUED_CONSUME_USES_IN_CREATIVE.get();
		int minTicks = MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get();
		double decayExponent = MechanicsConfig.IMBUED_EFFECT_DECAY_EXPONENT.get();
		boolean retaliated = false;
		int redSum = 0, greenSum = 0, blueSum = 0, coloredPieces = 0;
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack armor = target.getItemBySlot(slot);
			if (!ModUtils.isImbued(armor))
				continue;
			CompoundTag tag = armor.getTag();
			if (tag == null || !tag.contains("ImbuedEffects", Tag.TAG_LIST))
				continue;
			ListTag effectsTag = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
			if (effectsTag.isEmpty())
				continue;
			int uses = tag.getInt("ImbuedUses");
			int maxUses = tag.getInt("MaxImbuedUses");
			if (uses <= 0 || maxUses <= 0)
				continue;
			float scaledFraction = (float) Math.pow((float) uses / maxUses, decayExponent);
			int imbuedColor = tag.getInt("ImbuedColor");
			applyImbuedEffects(effectsTag, attacker, scaledFraction, minTicks);
			retaliated = true;
			if (imbuedColor != -1) {
				redSum += (imbuedColor >> 16) & 255;
				greenSum += (imbuedColor >> 8) & 255;
				blueSum += imbuedColor & 255;
				coloredPieces++;
			}
			if (shouldConsume) {
				consumeUse(target, slot, armor, tag, uses, maxUses);
			}
		}
		if (!retaliated)
			return;
		playRetaliationFeedback(world, target, attacker, redSum, greenSum, blueSum, coloredPieces);
	}

	private static void playRetaliationFeedback(Level world, LivingEntity target, LivingEntity attacker, int redSum, int greenSum, int blueSum, int coloredPieces) {
		world.playSound(null, target.getX(), target.getY(), target.getZ(),
			SoundEvents.BREWING_STAND_BREW, target.getSoundSource(), RETALIATION_SOUND_VOLUME, RETALIATION_SOUND_PITCH);
		if (coloredPieces <= 0)
			return;
		float r = redSum / (coloredPieces * 255.0F);
		float g = greenSum / (coloredPieces * 255.0F);
		float b = blueSum / (coloredPieces * 255.0F);
		ShineriteAttackHandler.spawnCritBurst(world, target, r, g, b, RETALIATION_WEARER_PARTICLE_SIZE, RETALIATION_WEARER_PARTICLE_COUNT);
		ShineriteAttackHandler.spawnCritBurst(world, attacker, r, g, b, LIGHTGUARD_PARTICLE_SIZE, LIGHTGUARD_PARTICLE_COUNT);
	}

	private static void applyImbuedEffects(ListTag effectsTag, LivingEntity attacker, float scaledFraction, int minTicks) {
		for (int i = 0; i < effectsTag.size(); i++) {
			MobEffectInstance savedEffect = MobEffectInstance.load(effectsTag.getCompound(i));
			if (savedEffect == null)
				continue;
			MobEffect effect = savedEffect.getEffect();
			int finalDuration = Math.max(minTicks, (int) (savedEffect.getDuration() * scaledFraction));
			int finalAmplifier = Math.max(0, (int) (savedEffect.getAmplifier() * scaledFraction));
			if (effect.isInstantenous()) {
				effect.applyInstantenousEffect(null, null, attacker, finalAmplifier, 1.0);
			} else {
				attacker.addEffect(new MobEffectInstance(effect, finalDuration, finalAmplifier, savedEffect.isAmbient(), savedEffect.isVisible(), savedEffect.showIcon()));
			}
		}
	}

	private static void consumeUse(LivingEntity target, EquipmentSlot slot, ItemStack armor, CompoundTag tag, int uses, int maxUses) {
		uses--;
		if (uses <= 0) {
			ModUtils.removeImbuement(armor);
			ModUtils.removeColor(armor);
		} else {
			tag.putInt("ImbuedUses", uses);
			ShineriteAttackHandler.updateImbueColor(armor, tag, uses, maxUses);
		}
		target.setItemSlot(slot, armor);
	}

	private static void applyGlowEffect(LivingEntity target, Level world, int color, int enchantmentLevel) {
		target.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glowingColor -> {
			glowingColor.setColor(color);
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION_PER_LEVEL * enchantmentLevel, 0, false, false, true));
			if (world instanceof ServerLevel serverLevel) {
				JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> target), new GlowColorPacket(target.getId(), color));
				if (target instanceof ServerPlayer serverPlayer) {
					JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new GlowColorPacket(target.getId(), color));
				}
				spawnCritParticles(serverLevel, target, color);
			}
		});
	}

	private static void spawnCritParticles(ServerLevel world, LivingEntity entity, int baseColor) {
		float r = ((baseColor >> 16) & 0xFF) / 255.0f;
		float g = ((baseColor >> 8) & 0xFF) / 255.0f;
		float b = (baseColor & 0xFF) / 255.0f;
		ShineriteAttackHandler.spawnCritBurst(world, entity, r, g, b, LIGHTGUARD_PARTICLE_SIZE, LIGHTGUARD_PARTICLE_COUNT);
	}
}
