package net.jaams.shinerite.handler.mechanic;

import org.joml.Vector3f;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.compat.BackItemCompat;
import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.block.ShineritiaBlock;
import net.jaams.shinerite.block.ShineriteRootsBlock;

import javax.annotation.Nullable;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Mod.EventBusSubscriber
public class ProjectileImpactHandler {
	public static final String IMBUE_LAST_TARGET = "ImbueLastTargetUUID";
	public static final String IMBUE_LAST_TARGET_TICKS = "ImbueLastTargetTicks";
	public static final int IMBUE_HIT_COOLDOWN_TICKS = 20;

	@SubscribeEvent
	public static void onProjectileImbuedImpact(ProjectileImpactEvent event) {
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.PROJECTILE_IMPACT_IMBUING.get()) {
			return;
		}
		Projectile projectile = event.getProjectile();
		if (projectile == null || projectile.level().isClientSide) {
			return;
		}
		CompoundTag nbt = projectile.getPersistentData();
		if (!nbt.contains("WeaponItem", Tag.TAG_COMPOUND))
			return;
		CompoundTag weaponTag = nbt.getCompound("WeaponItem");
		if (weaponTag.isEmpty())
			return;
		ItemStack weaponItem = ItemStack.of(weaponTag);
		if (weaponItem.isEmpty() || !ModUtils.isImbueableItem(weaponItem) || !ModUtils.isImbued(weaponItem)) {
			return;
		}
		CompoundTag tag = weaponItem.getOrCreateTag();
		if (!tag.contains("ImbuedEffects", Tag.TAG_LIST) || !tag.contains("ImbuedUses", Tag.TAG_INT) || !tag.contains("MaxImbuedUses", Tag.TAG_INT)) {
			ModUtils.removeImbuement(weaponItem);
			ModUtils.removeColor(weaponItem);
			nbt.put("WeaponItem", weaponItem.save(new CompoundTag()));
			return;
		}
		ListTag effectsTag = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
		int uses = tag.getInt("ImbuedUses");
		int maxUses = tag.getInt("MaxImbuedUses");
		if (maxUses <= 0 || uses <= 0 || uses > maxUses) {
			ModUtils.removeImbuement(weaponItem);
			ModUtils.removeColor(weaponItem);
			nbt.put("WeaponItem", weaponItem.save(new CompoundTag()));
			return;
		}
		Entity ownerEntity = projectile.getOwner();
		Player owner = ownerEntity instanceof Player p ? p : null;
		boolean isCreative = owner != null && owner.isCreative();
		HitResult hitResult = event.getRayTraceResult();
		boolean hitEntity = hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity;
		boolean hitBlock = hitResult instanceof BlockHitResult;
		int usesToConsume = 0;
		if (hitEntity && InteractionsConfig.PROJECTILE_IMPACT_IMBUING_CONSUME_ON_ENTITY_HIT.get()) {
			usesToConsume = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_USES_PER_HIT.get();
		}
		if (hitBlock && InteractionsConfig.PROJECTILE_IMPACT_IMBUING_CONSUME_ON_BLOCK_HIT.get()) {
			usesToConsume = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_USES_PER_HIT.get();
		}
		boolean effectApplied = false;
		if (hitEntity) {
			LivingEntity hitTarget = (LivingEntity) ((EntityHitResult) hitResult).getEntity();
			effectApplied = applyImbuedEffectsToHitTarget(nbt, effectsTag, projectile, ownerEntity, hitTarget, weaponItem, uses, maxUses);
		}
		if (effectApplied && usesToConsume > 0 && !isCreative) {
			int usesAfter = uses - usesToConsume;
			boolean isStackable = weaponItem.getMaxStackSize() > 1;
			if (isStackable || usesAfter <= 0) {
				ModUtils.removeImbuement(weaponItem);
				ModUtils.removeColor(weaponItem);
				resetItemImbue(weaponItem, projectile.level(), projectile);
			} else {
				tag.putInt("ImbuedUses", usesAfter);
				updateImbueColor(weaponItem, tag, usesAfter, maxUses);
			}
			nbt.put("WeaponItem", weaponItem.save(new CompoundTag()));
		}
	}

	/**
	 * Applies the imbued effects of the weapon carried by the projectile to the entity that was hit.
	 * Strictly server side, never the shooter itself, never the projectile, and at most once per target
	 * within the cooldown so returning projectiles can't re-apply the effect every tick.
	 */
	private static boolean applyImbuedEffectsToHitTarget(CompoundTag nbt, ListTag effectsTag, Projectile projectile, @Nullable Entity ownerEntity, LivingEntity hitTarget, ItemStack weaponItem, int uses, int maxUses) {
		if (projectile == null || hitTarget == null || !hitTarget.isAlive() || hitTarget.isRemoved() || effectsTag.isEmpty()) {
			return false;
		}
		if (hitTarget.getUUID().equals(projectile.getUUID())) {
			return false;
		}
		if (ownerEntity != null && (hitTarget == ownerEntity || hitTarget.getVehicle() == ownerEntity || ownerEntity.getVehicle() == hitTarget || hitTarget.getVehicle() == projectile)) {
			return false;
		}
		if (hitTarget instanceof Player targetPlayer && (targetPlayer.isCreative() || targetPlayer.isSpectator())) {
			return false;
		}
		UUID lastTarget = nbt.hasUUID(IMBUE_LAST_TARGET) ? nbt.getUUID(IMBUE_LAST_TARGET) : null;
		int lastTargetTicks = nbt.getInt(IMBUE_LAST_TARGET_TICKS);
		int cooldown = Math.max(1, IMBUE_HIT_COOLDOWN_TICKS);
		if (lastTarget != null && lastTarget.equals(hitTarget.getUUID()) && lastTargetTicks > 0) {
			return false;
		}
		float progress = Math.min(1.0F, (float) uses / maxUses);
		boolean applied = ShineriteAttackHandler.applyImbuedEffects(effectsTag, ownerEntity, hitTarget, progress, MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get(), false);
		if (applied) {
			nbt.putUUID(IMBUE_LAST_TARGET, hitTarget.getUUID());
			nbt.putInt(IMBUE_LAST_TARGET_TICKS, cooldown);
			ShineriteAttackHandler.spawnImbuedCritBurst(projectile.level(), hitTarget, weaponItem, 0.15f, 6);
		}
		return applied;
	}

	@SubscribeEvent
	public static void onProjectileWaterImpact(ProjectileImpactEvent event) {
		if (!InteractionsConfig.PROJECTILE_INTERACTIONS.get() || !InteractionsConfig.PROJECTILE_IMPACT_WASHING.get() || !MechanicsConfig.WASHING.get()) {
			return;
		}
		if (!(event.getProjectile() instanceof ThrownPotion potion)) {
			return;
		}
		ItemStack potionStack = potion.getItem();
		if (!ModUtils.isValidWaterPotion(potionStack)) {
			return;
		}
		HitResult hitResult = event.getRayTraceResult();
		Level level = potion.level();
		if (hitResult instanceof BlockHitResult blockHit && InteractionsConfig.PROJECTILE_IMPACT_WASHING_BLOCKS.get()) {
			handleBlockHit(level, blockHit, potion);
		}
		if (hitResult instanceof EntityHitResult entityHit) {
			Entity hitEntity = entityHit.getEntity();
			if (hitEntity instanceof LivingEntity livingEntity && InteractionsConfig.PROJECTILE_IMPACT_WASHING_LIVING_ENTITIES.get()) {
				handleLivingEntityHit(level, livingEntity);
			}
		}
		if (level instanceof ServerLevel serverLevel) {
			if (InteractionsConfig.PROJECTILE_IMPACT_WASHING_DROPPED_ITEMS.get()) {
				handleItemEntitiesInArea(serverLevel, potion);
			}
			if (InteractionsConfig.PROJECTILE_IMPACT_WASHING_PROJECTILES.get()) {
				handleProjectilesInArea(serverLevel, potion);
			}
		}
	}

	@SubscribeEvent
	public static void onProjectileBackItemImpact(ProjectileImpactEvent event) {
		if (!InteractionsConfig.PROJECTILE_INTERACTIONS.get() || !InteractionsConfig.PROJECTILE_IMPACT_WASHING.get() || !MechanicsConfig.WASHING.get() || !InteractionsConfig.PROJECTILE_IMPACT_WASHING_BACK_ITEM.get()) {
			return;
		}
		if (!(event.getProjectile() instanceof ThrownPotion potion))
			return;
		if (!(event.getRayTraceResult() instanceof EntityHitResult hitResult))
			return;
		ItemStack potionStack = potion.getItem();
		if (!ModUtils.isValidWaterPotion(potionStack))
			return;
		Entity hitEntity = hitResult.getEntity();
		if (hitEntity instanceof LivingEntity livingEntity) {
			processBackItemWaterPotion(livingEntity, livingEntity.level());
		}
	}

	@SubscribeEvent
	public static void onProjectileImbueImpact(ProjectileImpactEvent event) {
		if (!InteractionsConfig.PROJECTILE_INTERACTIONS.get() || !InteractionsConfig.PROJECTILE_IMPACT_IMBUING.get() || !MechanicsConfig.IMBUING.get()) {
			return;
		}
		if (!(event.getProjectile() instanceof ThrownPotion potion))
			return;
		ItemStack potionStack = potion.getItem();
		Potion potionType = PotionUtils.getPotion(potionStack);
		if (potionType == Potions.WATER || potionType == Potions.AWKWARD || potionType == Potions.THICK || potionType == Potions.MUNDANE || potionType.getEffects().isEmpty()) {
			return;
		}
		Level level = potion.level();
		if (!(level instanceof ServerLevel serverLevel))
			return;
		double radius = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_RADIUS.get();
		double distanceFactor = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_DISTANCE_FACTOR.get();
		AABB aabb = potion.getBoundingBox().inflate(radius);
		Vec3 pos = potion.position();
		List<ItemEntity> items = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_DROPPED_ITEMS.get() ? serverLevel.getEntitiesOfClass(ItemEntity.class, aabb, e -> e.position().distanceTo(pos) <= distanceFactor) : List.of();
		List<AbstractArrow> arrows = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_PROJECTILES.get() ? serverLevel.getEntitiesOfClass(AbstractArrow.class, aabb, e -> e.position().distanceTo(pos) <= distanceFactor) : List.of();
		int totalAffected = 0;
		for (ItemEntity ie : items) {
			ItemStack stack = ie.getItem();
			if (ModUtils.isImbueableItem(stack) && !ModUtils.isImbued(stack)) {
				totalAffected++;
			}
		}
		for (AbstractArrow arrow : arrows) {
			CompoundTag nbt = arrow.getPersistentData();
			if (nbt.contains("WeaponItem", 10)) {
				ItemStack weapon = ItemStack.of(nbt.getCompound("WeaponItem"));
				if (ModUtils.isImbueableItem(weapon) && !ModUtils.isImbued(weapon)) {
					totalAffected++;
				}
			}
		}
		if (InteractionsConfig.PROJECTILE_IMPACT_IMBUING_DROPPED_ITEMS.get()) {
			handleItemEntitiesInArea(serverLevel, potion, potionStack, pos, totalAffected);
		}
		if (InteractionsConfig.PROJECTILE_IMPACT_IMBUING_PROJECTILES.get()) {
			handleProjectilesInArea(serverLevel, potion, potionStack, pos, totalAffected);
		}
	}

	private static void handleLivingEntityHit(Level level, LivingEntity livingEntity) {
		if (!(level instanceof ServerLevel serverLevel))
			return;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.HAND && slot.getType() != EquipmentSlot.Type.ARMOR)
				continue;
			ItemStack stack = livingEntity.getItemBySlot(slot);
			if (!ModUtils.isWashableItem(stack))
				continue;
			CompoundTag tag = stack.getTag();
			if (tag != null && tag.contains("Washable") && !tag.getBoolean("Washable")) {
				continue;
			}
			if (MechanicsConfig.COATING.get() && ModUtils.isCoated(stack))
				continue;
			boolean modified = false;
			if (ModUtils.isImbued(stack)) {
				if (tag != null && tag.contains("WashableImbued") && !tag.getBoolean("WashableImbued")) {
				} else {
					ModUtils.removeImbuement(stack);
					modified = true;
				}
			}
			if (MechanicsConfig.DYEING.get() && ModUtils.isDyed(stack)) {
				ModUtils.removeColor(stack);
				modified = true;
			}
			if (modified) {
				livingEntity.setItemSlot(slot, stack);
				serverLevel.sendParticles(ParticleTypes.SPLASH, livingEntity.getX(), livingEntity.getY() + livingEntity.getEyeHeight() / 2, livingEntity.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
			}
		}
	}

	private static void handleItemEntitiesInArea(ServerLevel serverLevel, ThrownPotion potion) {
		AABB aabb = potion.getBoundingBox().inflate(0.3);
		Vec3 potionPos = potion.position();
		for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(ItemEntity.class, aabb, e -> e.position().distanceTo(potionPos) <= 1.0)) {
			ItemStack stack = itemEntity.getItem();
			if (!ModUtils.isWashableItem(stack))
				continue;
			CompoundTag tag = stack.getTag();
			if (tag != null && tag.contains("Washable") && !tag.getBoolean("Washable")) {
				continue;
			}
			if (MechanicsConfig.COATING.get() && ModUtils.isCoated(stack))
				continue;
			boolean modified = false;
			if (ModUtils.isImbued(stack)) {
				if (tag != null && tag.contains("WashableImbued") && !tag.getBoolean("WashableImbued")) {
				} else {
					ModUtils.removeImbuement(stack);
					modified = true;
				}
			}
			if (MechanicsConfig.DYEING.get() && ModUtils.isDyed(stack)) {
				ModUtils.removeColor(stack);
				modified = true;
			}
			if (modified) {
				itemEntity.setItem(stack);
				ColorMechanicsHandler.sendUpdateToClient(itemEntity);
			}
			serverLevel.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
		}
	}

	private static void handleProjectilesInArea(ServerLevel serverLevel, ThrownPotion potion) {
		AABB aabb = potion.getBoundingBox().inflate(0.3);
		Vec3 potionPos = potion.position();
		for (AbstractArrow projectile : serverLevel.getEntitiesOfClass(AbstractArrow.class, aabb, e -> e.position().distanceTo(potionPos) <= 1.0)) {
			CompoundTag nbt = projectile.getPersistentData();
			if (!nbt.contains("WeaponItem", 10))
				continue;
			ItemStack weaponItem = ItemStack.of(nbt.getCompound("WeaponItem"));
			if (!ModUtils.isWashableItem(weaponItem))
				continue;
			CompoundTag tag = weaponItem.getTag();
if (tag != null && tag.contains("Washable") && !tag.getBoolean("Washable")) {
				continue;
			}
			if (MechanicsConfig.COATING.get() && ModUtils.isCoated(weaponItem))
				continue;
			boolean modified = false;
			if (ModUtils.isImbued(weaponItem)) {
				if (tag != null && tag.contains("WashableImbued") && !tag.getBoolean("WashableImbued")) {
				} else {
					ModUtils.removeImbuement(weaponItem);
					modified = true;
				}
			}
			if (MechanicsConfig.DYEING.get() && ModUtils.isDyed(weaponItem)) {
				ModUtils.removeColor(weaponItem);
				modified = true;
			}
			if (modified) {
				nbt.put("WeaponItem", weaponItem.save(new CompoundTag()));
			}
			serverLevel.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
		}
	}

	private static void processBackItemWaterPotion(LivingEntity entity, Level level) {
		if (!BackItemCompat.isAvailable())
			return;
		ItemStack backItem = BackItemCompat.getBackItem(entity);
		if (backItem.isEmpty())
			return;
		if (!ModUtils.isWashableItem(backItem))
			return;
		CompoundTag tag = backItem.getTag();
		if (tag != null && tag.contains("Washable") && !tag.getBoolean("Washable")) {
			return;
		}
		if (MechanicsConfig.COATING.get() && ModUtils.isCoated(backItem))
			return;
		boolean modified = false;
		if (ModUtils.isImbued(backItem)) {
			if (tag != null && tag.contains("WashableImbued") && !tag.getBoolean("WashableImbued")) {
			} else {
				ModUtils.removeImbuement(backItem);
				modified = true;
			}
		}
		if (MechanicsConfig.DYEING.get() && ModUtils.isDyed(backItem)) {
			ModUtils.removeColor(backItem);
			modified = true;
		}
		if (modified) {
			BackItemCompat.setBackItem(entity, backItem);
		}
	}

	private static void updateImbueColor(ItemStack itemStack, CompoundTag tag, int uses, int maxUses) {
		int imbuedColor = tag.getInt("ImbuedColor");
		float progress = uses / (float) maxUses;
		int newColor = lightenImbuedColor(imbuedColor, 1.0f - progress);
		ModUtils.applyDyeColor(itemStack, newColor);
		tag.putInt("ImbuedColor", newColor);
	}

	private static void resetItemImbue(ItemStack itemStack, Level world, Entity projectile) {
		if (world instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0f, 1.0f, 1.0f), 1.0f), projectile.getX(), projectile.getY(), projectile.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
			serverLevel.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	private static int lightenImbuedColor(int color, float fraction) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int minR = r / 2, minG = g / 2, minB = b / 2;
		r = Math.max(minR, Math.min(255, (int) (r + (255 - r) * fraction * 0.15f)));
		g = Math.max(minG, Math.min(255, (int) (g + (255 - g) * fraction * 0.15f)));
		b = Math.max(minB, Math.min(255, (int) (b + (255 - b) * fraction * 0.15f)));
		return (r << 16) | (g << 8) | b;
	}

	private static void handleItemEntitiesInArea(ServerLevel level, ThrownPotion potion, ItemStack potionStack, Vec3 pos, int totalAffected) {
		double radius = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_RADIUS.get();
		double distanceFactor = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_DISTANCE_FACTOR.get();
		AABB aabb = potion.getBoundingBox().inflate(radius);
		for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, aabb, e -> e.position().distanceTo(pos) <= distanceFactor)) {
			ItemStack stack = ie.getItem();
			if (!ModUtils.isImbueableItem(stack) || ModUtils.isImbued(stack) || InteractionHandler.hasCustomColor(stack))
				continue;
			imbueItemStack(level, stack, potionStack, ie, pos, totalAffected);
			ie.setItem(stack);
			ColorMechanicsHandler.sendUpdateToClient(ie);
		}
	}

	private static void handleProjectilesInArea(ServerLevel level, ThrownPotion potion, ItemStack potionStack, Vec3 pos, int totalAffected) {
		double radius = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_RADIUS.get();
		double distanceFactor = InteractionsConfig.PROJECTILE_IMPACT_IMBUING_DISTANCE_FACTOR.get();
		AABB aabb = potion.getBoundingBox().inflate(radius);
		for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, aabb, e -> e.position().distanceTo(pos) <= distanceFactor)) {
			CompoundTag nbt = arrow.getPersistentData();
			if (!nbt.contains("WeaponItem", 10))
				continue;
			ItemStack weapon = ItemStack.of(nbt.getCompound("WeaponItem"));
			if (!ModUtils.isImbueableItem(weapon) || ModUtils.isImbued(weapon) || InteractionHandler.hasCustomColor(weapon))
				continue;
			imbueItemStack(level, weapon, potionStack, arrow, pos, totalAffected);
			nbt.put("WeaponItem", weapon.save(new CompoundTag()));
		}
	}

	private static void imbueItemStack(ServerLevel level, ItemStack target, ItemStack potionStack, Entity entity, Vec3 pos, int totalAffected) {
		List<MobEffectInstance> effects = new ArrayList<>();
		Potion potionType = PotionUtils.getPotion(potionStack);
		if (potionType != Potions.EMPTY && potionType != Potions.WATER) {
			effects.addAll(potionType.getEffects());
		}
		effects.addAll(PotionUtils.getCustomEffects(potionStack));
		if (effects.isEmpty())
			return;
		boolean isInstant = effects.stream().allMatch(e -> e.getDuration() <= 20);
		int baseUses = isInstant ? 1 : InteractionHandler.calculateImbuedUses(target, effects);
		double distance = entity.position().distanceTo(pos);
		float proximityFactor = (float) Math.max(0.1, 1.0 - (distance / InteractionsConfig.PROJECTILE_IMPACT_IMBUING_DISTANCE_FACTOR.get()));
		float randomFactor = level.random.nextFloat() * 0.4f + 0.8f;
		float countPenalty = 1.0f / (1.0f + (totalAffected - 1) * 0.1f);
		float overallFactor = proximityFactor * randomFactor * countPenalty;
		int imbuedUses = Math.max(1, (int) (baseUses * overallFactor));
		int potionColor = PotionUtils.getColor(potionStack);
		CompoundTag tag = target.getOrCreateTag();
		ListTag effectsTag = new ListTag();
		for (MobEffectInstance effect : effects) {
			int adjustedDuration = Math.max(1, (int) (effect.getDuration() * 0.5f * overallFactor));
			MobEffectInstance saved = new MobEffectInstance(effect.getEffect(), adjustedDuration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
			effectsTag.add(saved.save(new CompoundTag()));
		}
		tag.put("ImbuedEffects", effectsTag);
		tag.putInt("ImbuedUses", imbuedUses);
		tag.putInt("MaxImbuedUses", imbuedUses);
		tag.putInt("ImbuedColor", potionColor);
		ModUtils.applyDyeColor(target, potionColor);
		level.sendParticles(ParticleTypes.SPLASH, entity.getX(), entity.getY() + entity.getEyeHeight() / 2, entity.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
		level.playSound(null, entity.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.15F);
	}

	private static void handleBlockHit(Level level, BlockHitResult hit, ThrownPotion potion) {
		BlockPos pos = hit.getBlockPos();
		BlockPos targetPos = null;
		BlockState targetState = null;
		BlockEntity blockEntity = null;
		if (hit.getDirection() == Direction.UP) {
			BlockPos above = pos.above();
			blockEntity = level.getBlockEntity(above);
			if (blockEntity instanceof BaseShineriteBlockEntity shineriteBE && (!MechanicsConfig.COATING.get() || !shineriteBE.isCoated()) && level.getBlockState(above).getBlock() instanceof ShineritiaBlock) {
				targetPos = above;
				targetState = level.getBlockState(targetPos);
			}
		} else if (hit.getDirection() == Direction.DOWN) {
			BlockPos below = pos.below();
			blockEntity = level.getBlockEntity(below);
			if (blockEntity instanceof BaseShineriteBlockEntity shineriteBE && (!MechanicsConfig.COATING.get() || !shineriteBE.isCoated()) && level.getBlockState(below).getBlock() instanceof ShineriteRootsBlock) {
				targetPos = below;
				targetState = level.getBlockState(targetPos);
			}
		}
		if (targetPos != null && targetState != null && blockEntity instanceof BaseShineriteBlockEntity shineriteBE) {
			boolean colorRemoved = false;
			boolean imbuementRemoved = false;
			if (MechanicsConfig.DYEING.get() && shineriteBE.getColor() != -1) {
				ModUtils.applyDye(level, targetPos, targetState, -1);
				colorRemoved = true;
			}
			if (shineriteBE.isImbued()) {
				shineriteBE.clearImbuedState();
				imbuementRemoved = true;
			}
			if (colorRemoved || imbuementRemoved) {
				blockEntity.setChanged();
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.getChunkSource().blockChanged(targetPos);
					level.sendBlockUpdated(targetPos, targetState, targetState, 3);
					serverLevel.sendParticles(ParticleTypes.SPLASH, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 10, 0.2, 0.2, 0.2, 0.0);
				}
			}
		}
	}
}
