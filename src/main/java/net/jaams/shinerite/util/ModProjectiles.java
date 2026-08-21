
package net.jaams.shinerite.util;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.component.projectile.BaseWeaponProjectileEntity;
import net.jaams.shinerite.data.ThrowableItemData;
import net.jaams.shinerite.data.TraitModifierData;

import javax.annotation.Nullable;

import java.util.Optional;

public final class ModProjectiles {
	public static void initializeWeaponItem(Entity projectile, ItemStack weaponItem, ItemStack defaultItem, CompoundTag persistentData) {
		ItemStack toSet = weaponItem != null && !weaponItem.isEmpty() ? weaponItem.copy() : defaultItem;
		persistentData.put("WeaponItem", toSet.save(new CompoundTag()));
	}

	public static void updateWeaponEnchantmentData(Entity projectile, ItemStack weaponItem, EntityDataAccessor<Byte> loyaltyAccessor, EntityDataAccessor<Boolean> foilAccessor) {
		projectile.getEntityData().set(loyaltyAccessor, (byte) EnchantmentHelper.getLoyalty(weaponItem));
		projectile.getEntityData().set(foilAccessor, weaponItem.hasFoil());
	}

	public static float calculateProjectileDamage(ItemStack weaponItem, float baseWeaponDamage, boolean isMultishotClone, boolean isCritical, Level level, Vec3 projectilePos, Entity hitEntity) {
		float itemAttackDamage = getItemAttackDamage(weaponItem);
		float baseDamage = baseWeaponDamage + itemAttackDamage + 1.0F;
		if (isMultishotClone) {
			baseDamage *= 0.5F;
		}
		if (hitEntity instanceof LivingEntity livingEntity) {
			baseDamage += EnchantmentHelper.getDamageBonus(weaponItem, livingEntity.getMobType());
			int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, weaponItem);
			baseDamage += powerLevel * 1.5F;
			if (isCritical) {
				baseDamage *= 1.5F;
			}
			float distanceSq = (float) projectilePos.distanceToSqr(livingEntity.position());
			if (level.getRandom().nextFloat() < 0.2F && distanceSq > 25.0F) {
				baseDamage *= 1.5F;
			}
		}
		return baseDamage;
	}

	public static float getItemAttackDamage(ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return 1.0F;
		}
		double total = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum();
		if (total <= 0.0) {
			total = itemStack.getAttributeModifiers(EquipmentSlot.OFFHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum();
		}
		return total > 0.0 ? (float) total : 1.0F;
	}

	public static void playPickupSoundMob(Level level, Mob mob) {
		if (!level.isClientSide) {
			level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.2F, 1.0F);
		}
	}

	public static void playPickupSoundPlayer(Level level, Player player) {
		if (!level.isClientSide) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F);
		}
	}

	public static void reverseMovementAndRotation(Entity projectile) {
		projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.08D));
		projectile.setYRot(projectile.getYRot() + 180.0F);
		projectile.yRotO += 180.0F;
	}

	public static void deflectProjectile(Entity projectile, SoundEvent sound) {
		reverseMovementAndRotation(projectile);
		projectile.playSound(sound, 1.0F, 1.0F);
	}

	public static void handleBlocking(LivingEntity livingEntity, Entity owner, ItemStack weaponItem, Level level, Entity projectile, int cooldownTicks) {
		if (livingEntity instanceof Player player) {
			if (!player.getCooldowns().isOnCooldown(player.getUseItem().getItem())) {
				player.getCooldowns().addCooldown(player.getUseItem().getItem(), cooldownTicks);
			}
			player.stopUsingItem();
		}
		level.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
		level.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
		reverseMovementAndRotation(projectile);
	}

	public static void damageShield(ItemStack shield, LivingEntity holder, int damageAmount) {
		if (shield.isDamageableItem()) {
			shield.hurtAndBreak(damageAmount, holder, entity -> {
				entity.broadcastBreakEvent(holder.getUsedItemHand());
			});
		}
	}

	public static void spawnCriticalEffects(ServerLevel level, LivingEntity hitLivingEntity) {
		for (int i = 0; i < 8; i++) {
			level.sendParticles(ParticleTypes.CRIT, hitLivingEntity.getX() + level.random.nextGaussian() * 0.5D, hitLivingEntity.getY() + hitLivingEntity.getBbHeight() * 0.5D + level.random.nextGaussian() * 0.5D,
					hitLivingEntity.getZ() + level.random.nextGaussian() * 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
		level.playSound(null, hitLivingEntity.getX(), hitLivingEntity.getY(), hitLivingEntity.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	public static void spawnEnchantedHitEffects(ServerLevel level, LivingEntity hitLivingEntity, ItemStack weaponItem) {
		int sharpness = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, weaponItem);
		int smite = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, weaponItem);
		int bane = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, weaponItem);
		if (sharpness <= 0 && smite <= 0 && bane <= 0) {
			return;
		}
		for (int i = 0; i < 8; i++) {
			level.sendParticles(ParticleTypes.ENCHANTED_HIT, hitLivingEntity.getX() + level.random.nextGaussian() * 0.5D, hitLivingEntity.getY() + hitLivingEntity.getBbHeight() * 0.5D + level.random.nextGaussian() * 0.5D,
					hitLivingEntity.getZ() + level.random.nextGaussian() * 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
	}

	public static void handleItemBreak(ServerLevel level, ItemStack weaponItem, double x, double y, double z, Entity projectile) {
		if (!weaponItem.isEmpty() && weaponItem.getDamageValue() >= weaponItem.getMaxDamage()) {
			level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, weaponItem.copy()), x, y + 0.5, z, 5, 0.1D, 0.1D, 0.1D, 0.05D);
			level.playSound(null, x, y, z, SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);
			projectile.discard();
		}
	}

	public static void handleProjectileTagBreak(ServerLevel level, Entity projectile, double x, double y, double z) {
		CompoundTag persistent = projectile.getPersistentData();
		boolean shouldBreak = false;
		if (persistent.getBoolean("ShouldBreakProjectile")) {
			shouldBreak = true;
		} else if (persistent.contains("ShouldBreakProjectileHit") && persistent.getBoolean("ShouldBreakProjectileHit")) {
			shouldBreak = true;
		} else if (persistent.contains("ShouldBreakProjectileBlock") && persistent.getBoolean("ShouldBreakProjectileBlock")) {
			shouldBreak = true;
		}
		if (!shouldBreak) {
			return;
		}
		persistent.remove("ShouldBreakProjectile");
		persistent.remove("ShouldBreakProjectileHit");
		persistent.remove("ShouldBreakProjectileBlock");
		level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, projectile instanceof BaseWeaponProjectileEntity proj ? proj.weaponItem.copy() : ItemStack.EMPTY), x, y + 0.5, z, 8, 0.15D, 0.15D, 0.15D, 0.08D);
		level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);
		projectile.discard();
	}

	public static void handleMultishotCloneBreak(ServerLevel level, ItemStack weaponItem, double x, double y, double z, Entity projectile) {
		if (!weaponItem.isEmpty()) {
			level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, weaponItem.copy()), x, y + 0.5, z, 5, 0.1D, 0.1D, 0.1D, 0.05D);
			projectile.discard();
		}
	}

	public static void dropAsItem(Level level, ItemStack weaponItem, double x, double y, double z) {
		if (level.isClientSide)
			return;
		ItemStack copy = weaponItem.copy();
		copy.setCount(1);
		ItemEntity entity = new ItemEntity(level, x, y, z, copy);
		entity.setPickUpDelay(10);
		level.addFreshEntity(entity);
	}

	public static void tryBreakBlock(Level level, BlockPos pos, BlockState state, Direction hitFace) {
		if (level.isClientSide)
			return;
		if (state.is(Blocks.POINTED_DRIPSTONE)) {
			level.destroyBlock(pos, true);
		}
		if (state.is(Blocks.COBWEB)) {
			BlockPos adj = pos.relative(hitFace);
			if (level.getBlockState(adj).is(Blocks.COBWEB)) {
				level.destroyBlock(adj, true);
			}
		}
	}

	public static void spawnBlockParticles(ServerLevel level, BlockPos pos, BlockState state, double px, double py, double pz) {
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(px, py, pz) < 256.0D) {
				level.sendParticles(player, new BlockParticleOption(ParticleTypes.BLOCK, state).setPos(pos), true, px, py, pz, 6, 0.1D, 0.1D, 0.1D, 0.05D);
			}
		}
	}

	public static void applyWeaponEnchantmentEffects(LivingEntity target, @Nullable Entity owner, ItemStack weaponItem) {
		if (weaponItem.isEmpty()) {
			return;
		}
		if (owner instanceof LivingEntity ownerLiving) {
			if (ownerLiving instanceof Player player) {
				weaponItem.hurtEnemy(target, player);
			}
			EnchantmentHelper.doPostHurtEffects(target, ownerLiving);
			EnchantmentHelper.doPostDamageEffects(ownerLiving, target);
		}
		int fireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, weaponItem);
		if (fireAspectLevel > 0) {
			target.setSecondsOnFire(fireAspectLevel * 4);
		}
		if (target.level() instanceof ServerLevel serverLevel) {
			spawnEnchantedHitEffects(serverLevel, target, weaponItem);
		}
	}

	public static boolean tryMobPickup(ItemStack pickupItemStack, Mob mob, Level level) {
		ItemStack mainHand = mob.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack offHand = mob.getItemInHand(InteractionHand.OFF_HAND);
		if (!mainHand.isEmpty() && !offHand.isEmpty()) {
			if (!mainHand.is(pickupItemStack.getItem()) || mainHand.getCount() >= mainHand.getMaxStackSize()) {
				if (!offHand.is(pickupItemStack.getItem()) || offHand.getCount() >= offHand.getMaxStackSize()) {
					return false;
				}
			}
		}
		if (!mainHand.isEmpty() && mainHand.is(pickupItemStack.getItem()) && mainHand.getCount() < mainHand.getMaxStackSize()) {
			mainHand.grow(1);
			playPickupSoundMob(level, mob);
			return true;
		} else if (!offHand.isEmpty() && offHand.is(pickupItemStack.getItem()) && offHand.getCount() < offHand.getMaxStackSize()) {
			offHand.grow(1);
			playPickupSoundMob(level, mob);
			return true;
		} else if (mainHand.isEmpty()) {
			mob.setItemInHand(InteractionHand.MAIN_HAND, pickupItemStack);
			playPickupSoundMob(level, mob);
			return true;
		} else if (offHand.isEmpty()) {
			mob.setItemInHand(InteractionHand.OFF_HAND, pickupItemStack);
			playPickupSoundMob(level, mob);
			return true;
		}
		return false;
	}

	public static boolean isAcceptableLoyaltyReturnOwner(@Nullable Entity owner) {
		return owner != null && owner.isAlive() && (!(owner instanceof ServerPlayer player) || !player.isSpectator());
	}

	public static float getBaseDamage(ItemStack weapon, double configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("BaseDamage", net.minecraft.nbt.Tag.TAG_FLOAT)) {
			return weapon.getTag().getFloat("BaseDamage");
		}
		ThrowableItemData.ThrowableEntry config = ThrowableItemData.getThrowableData(weapon);
		return config != null && config.base_damage != null ? config.base_damage : (float) configDefault;
	}

	public static float getBaseKnockback(ItemStack weapon, double configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("BaseKnockback", net.minecraft.nbt.Tag.TAG_FLOAT)) {
			return weapon.getTag().getFloat("BaseKnockback");
		}
		ThrowableItemData.ThrowableEntry config = ThrowableItemData.getThrowableData(weapon);
		return config != null && config.base_knockback != null ? config.base_knockback : (float) configDefault;
	}

	public static int getPiercingLevel(ItemStack weapon, int configDefault) {
		int level = configDefault;
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("PiercingLevel", net.minecraft.nbt.Tag.TAG_INT)) {
			level = Math.max(0, weapon.getTag().getInt("PiercingLevel"));
		} else {
			ThrowableItemData.ThrowableEntry config = ThrowableItemData.getThrowableData(weapon);
			if (config != null && config.pierce_level != null) {
				level = Math.max(0, config.pierce_level);
			}
		}
		level += EnchantmentHelper.getTagEnchantmentLevel(Enchantments.PIERCING, weapon);
		return Math.max(0, level);
	}

	public static int getDespawnTicks(ItemStack weapon, double configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("DespawnTicks", 99)) {
			return Math.max(1, weapon.getTag().getInt("DespawnTicks"));
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		int def = config != null && config.despawn_ticks != null ? config.despawn_ticks : (int) configDefault;
		return Math.max(1, def);
	}

	public static float getWaterInertia(ItemStack weapon, double configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("WaterInertia", 99)) {
			return weapon.getTag().getFloat("WaterInertia");
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		return config != null && config.water_inertia != null ? config.water_inertia : (float) configDefault;
	}

	public static boolean getAllowCriticals(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("AllowCriticals", 1)) {
			return weapon.getTag().getBoolean("AllowCriticals");
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		return config != null && config.allow_criticals != null ? config.allow_criticals : configDefault;
	}

	public static int getIgnoreHitTicks(ItemStack weapon, int configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("IgnoreHitTicks", 99)) {
			return Math.max(0, weapon.getTag().getInt("IgnoreHitTicks"));
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		int def = config != null && config.ignore_hit_ticks != null ? config.ignore_hit_ticks : configDefault;
		return Math.max(0, def);
	}

	public static boolean shouldBreakOnEntityHit(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("BreakOnEntityHit", 1)) {
			return weapon.getTag().getBoolean("BreakOnEntityHit");
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		return config != null && config.break_on_entity_hit != null ? config.break_on_entity_hit : configDefault;
	}

	public static boolean shouldBreakOnBlockHit(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("BreakOnBlockHit", 1)) {
			return weapon.getTag().getBoolean("BreakOnBlockHit");
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		return config != null && config.break_on_block_hit != null ? config.break_on_block_hit : configDefault;
	}

	public static boolean canDisableShield(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("DisableShield", 1)) {
			return weapon.getTag().getBoolean("DisableShield");
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		return config != null && config.disable_shield != null ? config.disable_shield : configDefault;
	}

	public static int getShieldDisableCooldownTicks(ItemStack weapon, int configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("DisableCooldownTicks", 99)) {
			return Math.max(20, weapon.getTag().getInt("DisableCooldownTicks"));
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		int def = config != null && config.disable_cooldown_ticks != null ? config.disable_cooldown_ticks : configDefault;
		return Math.max(20, def);
	}

	public static int getMaxBounces(ItemStack weapon, int configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("MaxBounces", 99)) {
			return Math.max(0, weapon.getTag().getInt("MaxBounces"));
		}
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		if (config != null && config.max_bounces != null) {
			return Math.max(0, config.max_bounces);
		}
		return configDefault;
	}

	public static EntityDimensions getDimensions(ItemStack weapon, float defaultWidth, float defaultHeight) {
		float width = defaultWidth;
		float height = defaultHeight;
		ThrowableItemData.ProjectileEntry config = ThrowableItemData.getProjectileConfig(weapon);
		if (config != null) {
			if (config.hitbox_width != null) {
				width = config.hitbox_width;
			}
			if (config.hitbox_height != null) {
				height = config.hitbox_height;
			}
		}
		if (weapon != null && weapon.hasTag()) {
			CompoundTag tag = weapon.getTag();
			if (tag.contains("HitboxWidth", net.minecraft.nbt.Tag.TAG_FLOAT)) {
				width = tag.getFloat("HitboxWidth");
			}
			if (tag.contains("HitboxHeight", net.minecraft.nbt.Tag.TAG_FLOAT)) {
				height = tag.getFloat("HitboxHeight");
			}
		}
		return EntityDimensions.scalable(width, height);
	}

	public static boolean getThrowbackEnabled(ItemStack weapon, boolean configDefault) {
		if (!configDefault) {
			return false;
		}
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("ThrowbackTrait", 1)) {
			return weapon.getTag().getBoolean("ThrowbackTrait");
		}
		Optional<TraitModifierData.ThrowbackEntry> trait = TraitModifierData.getThrowback(weapon);
		if (trait.isPresent() && trait.get() != null) {
			return trait.get().enabled != null ? trait.get().enabled : true;
		}
		return true;
	}

	public static boolean getReturnOnBlockHit(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("ReturnOnBlockHit", 1)) {
			return weapon.getTag().getBoolean("ReturnOnBlockHit");
		}
		Optional<TraitModifierData.ThrowbackEntry> trait = TraitModifierData.getThrowback(weapon);
		if (trait.isPresent() && trait.get() != null) {
			return trait.get().return_on_block_hit != null ? trait.get().return_on_block_hit : configDefault;
		}
		return configDefault;
	}

	public static boolean getReturnOnEntityHit(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("ReturnOnEntityHit", 1)) {
			return weapon.getTag().getBoolean("ReturnOnEntityHit");
		}
		Optional<TraitModifierData.ThrowbackEntry> trait = TraitModifierData.getThrowback(weapon);
		if (trait.isPresent() && trait.get() != null) {
			return trait.get().return_on_entity_hit != null ? trait.get().return_on_entity_hit : configDefault;
		}
		return configDefault;
	}

	public static boolean getReturnOnMaxRange(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("ReturnOnMaxRange", 1)) {
			return weapon.getTag().getBoolean("ReturnOnMaxRange");
		}
		Optional<TraitModifierData.ThrowbackEntry> trait = TraitModifierData.getThrowback(weapon);
		if (trait.isPresent() && trait.get() != null) {
			return trait.get().return_on_max_range != null ? trait.get().return_on_max_range : configDefault;
		}
		return configDefault;
	}

	public static double getThrowbackMinRange(ItemStack weapon, double configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("MinRangeBase", net.minecraft.nbt.Tag.TAG_FLOAT)) {
			return weapon.getTag().getFloat("MinRangeBase");
		}
		Optional<TraitModifierData.ThrowbackEntry> trait = TraitModifierData.getThrowback(weapon);
		if (trait.isPresent() && trait.get() != null && trait.get().min_range != null) {
			return trait.get().min_range;
		}
		return configDefault;
	}

	public static float getThrowbackMaxRange(ItemStack weapon, float configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("MaxRange", net.minecraft.nbt.Tag.TAG_FLOAT)) {
			return weapon.getTag().getFloat("MaxRange");
		}
		Optional<TraitModifierData.ThrowbackEntry> trait = TraitModifierData.getThrowback(weapon);
		if (trait.isPresent() && trait.get() != null && trait.get().max_range != null) {
			return trait.get().max_range;
		}
		return configDefault;
	}

	public static double getThrowbackReturnSpeed(ItemStack weapon, double configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("ReturnSpeed", net.minecraft.nbt.Tag.TAG_DOUBLE)) {
			return weapon.getTag().getDouble("ReturnSpeed");
		}
		Optional<TraitModifierData.ThrowbackEntry> trait = TraitModifierData.getThrowback(weapon);
		if (trait.isPresent() && trait.get() != null && trait.get().return_speed != null) {
			return trait.get().return_speed;
		}
		return configDefault;
	}

	public static int getMaxMountedEntities(ItemStack weapon, boolean collectorEnabled, int configDefault) {
		if (!collectorEnabled) {
			return 0;
		}
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("MaxMountedEntities", 3)) {
			return weapon.getTag().getInt("MaxMountedEntities");
		}
		Optional<TraitModifierData.RadiantCollectorEntry> trait = TraitModifierData.getRadiantCollector(weapon);
		if (trait.isPresent() && trait.get() != null && trait.get().max_mounted_entities != null) {
			return trait.get().max_mounted_entities;
		}
		return configDefault;
	}

	public static boolean getRadiantCollectorGlow(ItemStack weapon, boolean configDefault) {
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("RadiantCollectorGlow", 1)) {
			return weapon.getTag().getBoolean("RadiantCollectorGlow");
		}
		Optional<TraitModifierData.RadiantCollectorEntry> trait = TraitModifierData.getRadiantCollector(weapon);
		if (trait.isPresent() && trait.get() != null && trait.get().glow_on_collected_items != null) {
			return trait.get().glow_on_collected_items;
		}
		return configDefault;
	}

	public static boolean getRadiantCollectorEnabled(ItemStack weapon, boolean configDefault) {
		if (!configDefault) {
			return false;
		}
		if (weapon != null && weapon.hasTag() && weapon.getTag().contains("RadiantCollectorTrait", 1)) {
			return weapon.getTag().getBoolean("RadiantCollectorTrait");
		}
		Optional<TraitModifierData.RadiantCollectorEntry> trait = TraitModifierData.getRadiantCollector(weapon);
		if (trait.isPresent() && trait.get() != null) {
			return trait.get().enabled != null ? trait.get().enabled : true;
		}
		return true;
	}
}
