package net.jaams.shinerite.component.projectile;

import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModProjectiles;
import net.jaams.shinerite.handler.mechanic.ProjectileImpactHandler;

import javax.annotation.Nullable;

import java.util.UUID;

public abstract class BaseWeaponProjectileEntity extends AbstractArrow implements IEntityAdditionalSpawnData {
	public static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.BYTE);
	public static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Integer> ID_SPIN_TICKS = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Float> ID_LAST_SPIN_ROTATION = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Boolean> ID_IMPACTED = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<ItemStack> ID_WEAPON_ITEM = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
	public static final EntityDataAccessor<Boolean> ID_CRITICAL = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Integer> ID_ORIGINAL_SLOT = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> ID_IGNORE_HIT_TICKS = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> ID_PIERCING = SynchedEntityData.defineId(BaseWeaponProjectileEntity.class, EntityDataSerializers.INT);
	public ItemStack weaponItem;
	protected float weaponDamage;
	protected float weaponKnockback;
	private float lastSpinRotation = 0.0F;
	protected int life = 0;
	protected int ignoreHitTicks = 0;
	protected int pierceLevel = 0;
	protected int piercedCount = 0;
	protected int originalSlotIndex = -1;
	protected int clientSideReturnTridentTickCount = 0;
	protected int multishotGroundTicks = 0;
	protected boolean isStuck = false;
	@Nullable
	protected UUID lastHitEntityId;

	protected BaseWeaponProjectileEntity(EntityType<? extends BaseWeaponProjectileEntity> type, Level level) {
		super(type, level);
		initializeWeaponItem(null);
	}

	protected BaseWeaponProjectileEntity(EntityType<? extends BaseWeaponProjectileEntity> type, double x, double y, double z, Level level) {
		super(type, x, y, z, level);
		initializeWeaponItem(null);
	}

	protected BaseWeaponProjectileEntity(EntityType<? extends BaseWeaponProjectileEntity> type, LivingEntity shooter, Level level) {
		super(type, shooter, level);
		initializeWeaponItem(null);
	}

	protected BaseWeaponProjectileEntity(EntityType<? extends BaseWeaponProjectileEntity> type, LivingEntity shooter, Level level, ItemStack weaponItem) {
		super(type, shooter, level);
		this.weaponItem = weaponItem != null && !weaponItem.isEmpty() ? weaponItem.copy() : getDefaultWeaponItem();
		this.entityData.set(ID_WEAPON_ITEM, this.weaponItem);
		this.getPersistentData().put("WeaponItem", this.weaponItem.save(new CompoundTag()));
		this.weaponDamage = 0.0F;
		this.weaponKnockback = 0.0F;
		this.entityData.set(ID_CRITICAL, isCritical());
		this.entityData.set(ID_ORIGINAL_SLOT, -1);
		this.entityData.set(ID_IGNORE_HIT_TICKS, 0);
		this.entityData.set(ID_PIERCING, 0);
		this.originalSlotIndex = -1;
		updateWeaponEnchantmentData();
		updateCustomNameFromWeapon();
	}

	public abstract ItemStack getDefaultWeaponItem();

	protected abstract SoundEvent getHitSound();

	protected abstract SoundEvent getGroundSound();

	protected abstract SoundEvent getLoyaltySound();

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ID_LOYALTY, (byte) 0);
		this.entityData.define(ID_FOIL, false);
		this.entityData.define(ID_SPIN_TICKS, 0);
		this.entityData.define(ID_LAST_SPIN_ROTATION, 0.0F);
		this.entityData.define(ID_IMPACTED, false);
		this.entityData.define(ID_WEAPON_ITEM, ItemStack.EMPTY);
		this.entityData.define(ID_CRITICAL, false);
		this.entityData.define(ID_ORIGINAL_SLOT, -1);
		this.entityData.define(ID_IGNORE_HIT_TICKS, 0);
		this.entityData.define(ID_PIERCING, 0);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("WeaponItem", 10)) {
			this.weaponItem = ItemStack.of(tag.getCompound("WeaponItem"));
			if (this.weaponItem.isEmpty()) {
				this.weaponItem = getDefaultWeaponItem();
			}
		} else {
			this.weaponItem = getDefaultWeaponItem();
		}
		this.entityData.set(ID_WEAPON_ITEM, this.weaponItem);
		this.getPersistentData().put("WeaponItem", this.weaponItem.save(new CompoundTag()));
		this.entityData.set(ID_IMPACTED, tag.getBoolean("HasImpacted"));
		this.entityData.set(ID_SPIN_TICKS, tag.getInt("SpinTicks"));
		this.lastSpinRotation = tag.getFloat("LastSpinRotation");
		this.entityData.set(ID_LAST_SPIN_ROTATION, this.lastSpinRotation);
		this.entityData.set(ID_CRITICAL, tag.getBoolean("IsCritical"));
		this.originalSlotIndex = tag.getInt("OriginalSlotIndex");
		this.entityData.set(ID_ORIGINAL_SLOT, this.originalSlotIndex);
		this.weaponDamage = tag.getFloat("WeaponDamage");
		this.weaponKnockback = tag.getFloat("WeaponKnockback");
		this.ignoreHitTicks = tag.getInt("IgnoreHitTicks");
		this.pierceLevel = tag.getInt("PierceLevel");
		this.piercedCount = tag.getInt("PiercedCount");
		this.entityData.set(ID_PIERCING, this.pierceLevel);
		this.isStuck = tag.getBoolean("IsStuck");
		if (tag.contains("LastHitEntityId")) {
			this.lastHitEntityId = tag.getUUID("LastHitEntityId");
		} else {
			this.lastHitEntityId = null;
		}
		updateWeaponEnchantmentData();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (!this.weaponItem.isEmpty()) {
			tag.put("WeaponItem", this.weaponItem.save(new CompoundTag()));
		}
		tag.putBoolean("IsCritical", isCritical());
		tag.putBoolean("HasImpacted", this.entityData.get(ID_IMPACTED));
		tag.putInt("SpinTicks", this.entityData.get(ID_SPIN_TICKS));
		tag.putFloat("LastSpinRotation", this.lastSpinRotation);
		tag.putInt("OriginalSlotIndex", this.originalSlotIndex);
		tag.putFloat("WeaponDamage", this.weaponDamage);
		tag.putFloat("WeaponKnockback", this.weaponKnockback);
		tag.putInt("IgnoreHitTicks", this.ignoreHitTicks);
		tag.putInt("PierceLevel", this.pierceLevel);
		tag.putInt("PiercedCount", this.piercedCount);
		tag.putBoolean("IsStuck", this.isStuck);
		if (this.lastHitEntityId != null) {
			tag.putUUID("LastHitEntityId", this.lastHitEntityId);
		}
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeItem(this.weaponItem);
		buffer.writeFloat(this.weaponDamage);
		buffer.writeBoolean(isCritical());
		buffer.writeInt(this.originalSlotIndex);
		buffer.writeFloat(this.lastSpinRotation);
		buffer.writeInt(this.ignoreHitTicks);
		buffer.writeInt(this.pierceLevel);
		buffer.writeInt(this.piercedCount);
		buffer.writeBoolean(this.lastHitEntityId != null);
		if (this.lastHitEntityId != null) {
			buffer.writeUUID(this.lastHitEntityId);
		}
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buf) {
		this.weaponItem = buf.readItem();
		this.entityData.set(ID_WEAPON_ITEM, this.weaponItem);
		this.getPersistentData().put("WeaponItem", this.weaponItem.save(new CompoundTag()));
		this.weaponDamage = buf.readFloat();
		this.setCritical(buf.readBoolean());
		this.originalSlotIndex = buf.readInt();
		this.entityData.set(ID_ORIGINAL_SLOT, this.originalSlotIndex);
		this.lastSpinRotation = buf.readFloat();
		this.entityData.set(ID_LAST_SPIN_ROTATION, this.lastSpinRotation);
		this.ignoreHitTicks = buf.readInt();
		this.entityData.set(ID_IGNORE_HIT_TICKS, this.ignoreHitTicks);
		this.pierceLevel = buf.readInt();
		this.piercedCount = buf.readInt();
		this.entityData.set(ID_PIERCING, this.pierceLevel);
		boolean hasLastHitEntityId = buf.readBoolean();
		this.lastHitEntityId = hasLastHitEntityId ? buf.readUUID() : null;
		updateWeaponEnchantmentData();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (ID_WEAPON_ITEM.equals(key)) {
			ItemStack newWeaponItem = this.entityData.get(ID_WEAPON_ITEM);
			if (!ItemStack.matches(this.weaponItem, newWeaponItem)) {
				this.weaponItem = newWeaponItem.isEmpty() ? getDefaultWeaponItem() : newWeaponItem.copy();
				updateWeaponEnchantmentData();
				this.getPersistentData().put("WeaponItem", this.weaponItem.save(new CompoundTag()));
			}
		} else if (ID_ORIGINAL_SLOT.equals(key)) {
			this.originalSlotIndex = this.entityData.get(ID_ORIGINAL_SLOT);
		} else if (ID_LAST_SPIN_ROTATION.equals(key)) {
			this.lastSpinRotation = this.entityData.get(ID_LAST_SPIN_ROTATION);
		} else if (ID_IGNORE_HIT_TICKS.equals(key)) {
			this.ignoreHitTicks = this.entityData.get(ID_IGNORE_HIT_TICKS);
		} else if (ID_PIERCING.equals(key)) {
			this.pierceLevel = this.entityData.get(ID_PIERCING);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide) {
			tickImbuementCooldown();
			if (handleCurseOfVanishingDisappear()) {
				return;
			}
			syncPersistentDataToEntityData();
			if (!this.weaponItem.isEmpty() && this.weaponItem.isDamageableItem() && this.weaponItem.getDamageValue() >= this.weaponItem.getMaxDamage()) {
				if (this.level() instanceof ServerLevel serverLevel) {
					ModProjectiles.handleItemBreak(serverLevel, this.weaponItem, this.getX(), this.getY(), this.getZ(), this);
				}
			}
		}
		if (this.inGroundTime > 1 && !this.entityData.get(ID_IMPACTED)) {
			this.entityData.set(ID_IMPACTED, true);
		}
		if (this.inGround) {
			this.entityData.set(ID_SPIN_TICKS, 0);
			if (this.getPersistentData().getBoolean("IsMultishotClone")) {
				this.multishotGroundTicks++;
				if (this.multishotGroundTicks >= 15 && this.level() instanceof ServerLevel serverLevel) {
					ModProjectiles.handleMultishotCloneBreak(serverLevel, this.weaponItem, this.getX(), this.getY(), this.getZ(), this);
				}
			}
		} else if (this.isNoPhysics()) {
			this.entityData.set(ID_SPIN_TICKS, 0);
			this.multishotGroundTicks = 0;
		} else {
			this.entityData.set(ID_SPIN_TICKS, this.entityData.get(ID_SPIN_TICKS) + 1);
			this.multishotGroundTicks = 0;
		}
		if (this.ignoreHitTicks > 0) {
			this.ignoreHitTicks--;
			this.entityData.set(ID_IGNORE_HIT_TICKS, this.ignoreHitTicks);
			if (this.ignoreHitTicks <= 0) {
				this.lastHitEntityId = null;
			}
		}
		handleLoyaltyReturn();
		resetLoyaltyIfNoOwner();
		handleMobOwnerPickup();
		handleMobPickup();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity target = result.getEntity();
		if (this.lastHitEntityId != null && target.getUUID().equals(this.lastHitEntityId) && this.ignoreHitTicks > 0) {
			return;
		}
		boolean shouldStop = this.piercedCount >= this.pierceLevel;
		DamageSource source = this.damageSources().trident(this, this.getOwner() == null ? this : this.getOwner());
		float damage = ModProjectiles.calculateProjectileDamage(this.weaponItem, this.weaponDamage, this.isMultishotClone(), this.isCritical(), this.level(), this.position(), target);
		if (target.getType() == EntityType.ENDERMAN && target instanceof LivingEntity living) {
			living.hurt(source, damage);
			return;
		}
		if (target instanceof LivingEntity living && living.isBlocking()) {
			handleShieldBlock(living, source, damage, this.getOwner());
			activateIgnoreHits(target.getUUID());
			return;
		}
		boolean damageApplied = target.hurt(source, damage);
		this.playSound(getHitSound(), 1.0F, 1.0F);
		if (!damageApplied) {
			this.setCritical(false);
			this.isStuck = true;
			ModProjectiles.reverseMovementAndRotation(this);
			activateIgnoreHits(target.getUUID());
			return;
		}
		if (target instanceof LivingEntity hitLiving) {
			if (this.isCritical() && hitLiving.getType() != EntityType.ENDERMAN) {
				if (this.level() instanceof ServerLevel server) {
					ModProjectiles.spawnCriticalEffects(server, hitLiving);
				}
			}
			if (hitLiving.getType() != EntityType.ENDERMAN) {
				this.doPostHurtEffects(hitLiving);
			}
			if (!hitLiving.isAlive()) {
				this.lastHitEntityId = hitLiving.getUUID();
			}
			applyKnockback(hitLiving);
		}
		this.piercedCount++;
		activateIgnoreHits(target.getUUID());
		if (shouldStop) {
			if (this.level() instanceof ServerLevel serverLevel) {
				ModProjectiles.handleProjectileTagBreak(serverLevel, this, this.getX(), this.getY(), this.getZ());
			}
			this.setCritical(false);
			this.entityData.set(ID_IMPACTED, true);
			stopAfterPiercing();
		}
	}

	@Override
	protected void doPostHurtEffects(LivingEntity entity) {
		super.doPostHurtEffects(entity);
		ModProjectiles.applyWeaponEnchantmentEffects(entity, this.getOwner(), this.weaponItem);
		if (this.isOnFire()) {
			entity.setSecondsOnFire(5);
		}
	}

	@Override
	public void onHitBlock(BlockHitResult result) {
		BlockPos pos = result.getBlockPos();
		BlockState state = this.level().getBlockState(pos);
		Direction face = result.getDirection();
		Vec3 vec = result.getLocation().subtract(this.position());
		Vec3 pushback = vec.normalize().scale(0.05F);
		this.setDeltaMovement(vec);
		this.setPosRaw(this.getX() - pushback.x, this.getY() - pushback.y, this.getZ() - pushback.z);
		this.inGround = true;
		this.shakeTime = 3;
		this.hurtMarked = true;
		this.setCritArrow(false);
		this.setShotFromCrossbow(false);
		this.setCritical(false);
		this.entityData.set(ID_IMPACTED, true);
		this.lastHitEntityId = null;
		this.ignoreHitTicks = 0;
		this.piercedCount = 0;
		if (!this.level().isClientSide) {
			if (result.getType() == BlockHitResult.Type.BLOCK && this.level() instanceof ServerLevel server) {
				ModProjectiles.spawnBlockParticles(server, pos, state, this.getX(), this.getY(), this.getZ());
			}
			if (this.level() instanceof ServerLevel serverLevel) {
				ModProjectiles.handleProjectileTagBreak(serverLevel, this, this.getX(), this.getY(), this.getZ());
			}
			ModProjectiles.tryBreakBlock(this.level(), pos, state, face);
			float finalSpin = this.entityData.get(ID_SPIN_TICKS) * 60.0F;
			this.entityData.set(ID_LAST_SPIN_ROTATION, finalSpin);
			this.getPersistentData().putFloat("LastSpinRotation", finalSpin);
		}
		this.playSound(getGroundSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void playerTouch(Player player) {
		if (!this.level().isClientSide && (this.inGround || this.isNoPhysics())) {
			if (this.tryPickup(player)) {
				player.take(this, 1);
				this.discard();
			}
		}
	}

	@Override
	protected boolean tryPickup(Player player) {
		Entity owner = this.getOwner();
		if (owner != null && owner != player)
			return false;
		ItemStack pickup = this.getPickupItem().copy();
		pickup.setCount(1);
		Inventory inv = player.getInventory();
		if (player.getAbilities().instabuild && this.pickup == Pickup.CREATIVE_ONLY)
			return true;
		if (this.pickup != Pickup.ALLOWED && !(this.isNoPhysics() && this.ownedBy(player)))
			return false;
		if (this.originalSlotIndex >= 0 && (this.originalSlotIndex <= 8 || this.originalSlotIndex == 40)) {
			ItemStack slot = inv.getItem(this.originalSlotIndex);
			if (slot.isEmpty()) {
				inv.setItem(this.originalSlotIndex, pickup);
				ModProjectiles.playPickupSoundPlayer(this.level(), player);
				return true;
			}
			if (ItemStack.isSameItemSameTags(slot, pickup) && slot.getCount() < slot.getMaxStackSize()) {
				slot.grow(1);
				ModProjectiles.playPickupSoundPlayer(this.level(), player);
				return true;
			}
		}
		for (int i = 0; i <= 8; i++) {
			if (i == this.originalSlotIndex)
				continue;
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				inv.setItem(i, pickup);
				ModProjectiles.playPickupSoundPlayer(this.level(), player);
				return true;
			}
			if (ItemStack.isSameItemSameTags(stack, pickup) && stack.getCount() < stack.getMaxStackSize()) {
				stack.grow(1);
				ModProjectiles.playPickupSoundPlayer(this.level(), player);
				return true;
			}
		}
		if (inv.add(pickup)) {
			ModProjectiles.playPickupSoundPlayer(this.level(), player);
			return true;
		}
		return false;
	}

	@Override
	protected ItemStack getPickupItem() {
		ItemStack stack = this.weaponItem.copy();
		stack.setCount(1);
		return stack;
	}

	@Override
	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.EMPTY;
	}

	public void initializeWeaponItem(ItemStack weaponItem) {
		this.weaponItem = weaponItem != null && !weaponItem.isEmpty() ? weaponItem.copy() : getDefaultWeaponItem();
		this.entityData.set(ID_WEAPON_ITEM, this.weaponItem);
		this.getPersistentData().put("WeaponItem", this.weaponItem.save(new CompoundTag()));
	}

	public void setWeaponItem(ItemStack newWeaponItem) {
		this.weaponItem = newWeaponItem != null && !newWeaponItem.isEmpty() ? newWeaponItem.copy() : getDefaultWeaponItem();
		if (!ItemStack.matches(this.entityData.get(ID_WEAPON_ITEM), this.weaponItem)) {
			this.entityData.set(ID_WEAPON_ITEM, this.weaponItem);
			this.getPersistentData().put("WeaponItem", this.weaponItem.save(new CompoundTag()));
			updateWeaponEnchantmentData();
			updateCustomNameFromWeapon();
		}
	}

	public void updateWeaponEnchantmentData() {
		this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.weaponItem));
		this.entityData.set(ID_FOIL, this.weaponItem.hasFoil());
		int enchantPierce = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, this.weaponItem);
		this.pierceLevel = Math.max(this.pierceLevel, enchantPierce);
		this.entityData.set(ID_PIERCING, this.pierceLevel);
	}

	protected void syncPersistentDataToEntityData() {
		CompoundTag persistent = this.getPersistentData();
		if (persistent.contains("WeaponItem", 10)) {
			ItemStack persistentItem = ItemStack.of(persistent.getCompound("WeaponItem"));
			if (!ItemStack.matches(persistentItem, this.weaponItem) && !persistentItem.isEmpty()) {
				this.weaponItem = persistentItem.copy();
				this.entityData.set(ID_WEAPON_ITEM, this.weaponItem);
				updateWeaponEnchantmentData();
			}
		}
		if (persistent.contains("IsCritical")) {
			boolean nbtValue = persistent.getBoolean("IsCritical");
			if (nbtValue != this.entityData.get(ID_CRITICAL)) {
				this.entityData.set(ID_CRITICAL, nbtValue);
			}
		}
		if (persistent.contains("WeaponDamage")) {
			float nbtDmg = persistent.getFloat("WeaponDamage");
			if (nbtDmg != this.weaponDamage) {
				this.weaponDamage = nbtDmg;
			}
		}
		if (persistent.contains("WeaponKnockback")) {
			float nbtKb = persistent.getFloat("WeaponKnockback");
			if (nbtKb != this.weaponKnockback) {
				this.weaponKnockback = nbtKb;
			}
		}
		if (persistent.contains("IgnoreHitTicks")) {
			int nbtTicks = persistent.getInt("IgnoreHitTicks");
			if (nbtTicks != this.ignoreHitTicks) {
				this.ignoreHitTicks = nbtTicks;
				this.entityData.set(ID_IGNORE_HIT_TICKS, nbtTicks);
			}
		}
		if (persistent.contains("PierceLevel")) {
			int nbtPierce = persistent.getInt("PierceLevel");
			if (nbtPierce != this.pierceLevel) {
				this.pierceLevel = nbtPierce;
				this.entityData.set(ID_PIERCING, this.pierceLevel);
			}
		}
		if (persistent.contains("PiercedCount")) {
			int nbtPierced = persistent.getInt("PiercedCount");
			if (nbtPierced != this.piercedCount) {
				this.piercedCount = nbtPierced;
			}
		}
		if (persistent.contains("OriginalSlotIndex")) {
			int nbtSlot = persistent.getInt("OriginalSlotIndex");
			if (nbtSlot != this.originalSlotIndex) {
				this.originalSlotIndex = nbtSlot;
				this.entityData.set(ID_ORIGINAL_SLOT, nbtSlot);
			}
		}
	}

	protected void handleLoyaltyReturn() {
		if (this.getPersistentData().getBoolean("IsMultishotClone"))
			return;
		Entity owner = this.getOwner();
		int loyalty = this.entityData.get(ID_LOYALTY);
		if (loyalty <= 0 || owner == null || !this.entityData.get(ID_IMPACTED)) {
			return;
		}
		if (!ModProjectiles.isAcceptableLoyaltyReturnOwner(owner)) {
			if (!this.level().isClientSide && this.pickup == Pickup.ALLOWED) {
				ModProjectiles.dropAsItem(this.level(), this.getPickupItem(), this.getX(), this.getY(), this.getZ());
			}
			this.discard();
			return;
		}
		this.setNoPhysics(true);
		this.entityData.set(ID_SPIN_TICKS, 0);
		Vec3 toOwner = owner.getEyePosition().subtract(this.position());
		this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015D * loyalty, this.getZ());
		if (this.level().isClientSide) {
			this.yOld = this.getY();
		}
		double pullSpeed = 0.05D * loyalty;
		this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(toOwner.normalize().scale(pullSpeed)));
		if (this.clientSideReturnTridentTickCount == 0) {
			this.playSound(getLoyaltySound(), 10.0F, 1.0F);
		}
		this.clientSideReturnTridentTickCount++;
	}

	protected void resetLoyaltyIfNoOwner() {
		if (!this.level().isClientSide && this.entityData.get(ID_LOYALTY) > 0 && this.getOwner() == null) {
			this.setNoPhysics(false);
			this.clientSideReturnTridentTickCount = 0;
		}
	}

	private void stopAfterPiercing() {
		ModProjectiles.reverseMovementAndRotation(this);
		this.setCritical(false);
	}

	private void handleShieldBlock(LivingEntity target, DamageSource source, float damage, Entity owner) {
		boolean canDisable = owner instanceof LivingEntity livingOwner && this.weaponItem.getItem().canDisableShield(this.weaponItem, target.getUseItem(), target, livingOwner);
		target.hurt(source, damage);
		if (canDisable) {
			ModProjectiles.handleBlocking(target, owner, this.weaponItem, this.level(), this, getShieldDisableCooldownTicks());
		} else {
			this.setCritical(false);
			this.playSound(getHitSound(), 1.0F, 1.0F);
		}
	}

	protected void applyKnockback(LivingEntity target) {
		double knockbackResistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		double knockbackFactor = 1.0 - Math.min(knockbackResistance, 1.0);
		if (knockbackFactor <= 0) {
			return;
		}
		float baseKnockback = this.weaponKnockback;
		int knockbackLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, this.weaponItem);
		if (knockbackLevel > 0) {
			baseKnockback += knockbackLevel * 0.8F;
		}
		float knockbackStrength = baseKnockback * 0.5F;
		if (this.isCritical()) {
			knockbackStrength += 0.6F;
		}
		if (knockbackStrength > 0.0F) {
			target.knockback(knockbackStrength * (float) knockbackFactor, -this.getDeltaMovement().x, -this.getDeltaMovement().z);
		}
	}

	protected void tickImbuementCooldown() {
		CompoundTag persistent = this.getPersistentData();
		int ticks = persistent.getInt(ProjectileImpactHandler.IMBUE_LAST_TARGET_TICKS);
		if (ticks <= 0) {
			return;
		}
		persistent.putInt(ProjectileImpactHandler.IMBUE_LAST_TARGET_TICKS, ticks - 1);
	}

	protected boolean handleCurseOfVanishingDisappear() {
		if (this.level().isClientSide) {
			return false;
		}
		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, this.weaponItem) <= 0) {
			return false;
		}
		Entity owner = this.getOwner();
		if (!this.hasImpacted() || (owner != null && owner.isAlive())) {
			return false;
		}
		ServerLevel serverLevel = (ServerLevel) this.level();
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, this.weaponItem.copy()), x, y + 0.5, z, 5, 0.1D, 0.1D, 0.1D, 0.05D);
		this.discard();
		return true;
	}

	protected void handleMobPickup() {
		if (!this.inGround || !(this.getOwner() instanceof Mob mob))
			return;
		if (this.position().distanceTo(mob.position()) <= 1.5D) {
			ItemStack pickup = this.getPickupItem().copy();
			pickup.setCount(1);
			if (ModProjectiles.tryMobPickup(pickup, mob, this.level())) {
				this.discard();
			}
		}
	}

	protected void handleMobOwnerPickup() {
		if (this.level().isClientSide || this.entityData.get(ID_LOYALTY) <= 0)
			return;
		if (this.getOwner() instanceof Mob mob) {
			AABB box = mob.getBoundingBox().inflate(0.3D);
			if (box.contains(this.position())) {
				ItemStack pickup = this.getPickupItem().copy();
				pickup.setCount(1);
				if (ModProjectiles.tryMobPickup(pickup, mob, this.level())) {
					this.discard();
				}
			}
		}
	}

	public void setOriginalSlotIndex(int slot) {
		this.originalSlotIndex = Math.max(-1, slot);
		this.entityData.set(ID_ORIGINAL_SLOT, this.originalSlotIndex);
	}

	public void setCritical(boolean critical) {
		this.entityData.set(ID_CRITICAL, critical);
		this.getPersistentData().putBoolean("IsCritical", critical);
	}

	public void updateCustomNameFromWeapon() {
		if (this.weaponItem.isEmpty()) {
			this.setCustomName(null);
			this.setCustomNameVisible(false);
			return;
		}
		Component name = this.weaponItem.getHoverName();
		ChatFormatting color = switch (this.weaponItem.getRarity()) {
			case COMMON -> ChatFormatting.WHITE;
			case UNCOMMON -> ChatFormatting.YELLOW;
			case RARE -> ChatFormatting.AQUA;
			case EPIC -> ChatFormatting.LIGHT_PURPLE;
		};
		this.setCustomName(name.copy().withStyle(color));
		this.setCustomNameVisible(false);
	}

	public void activateIgnoreHits(UUID hitEntityId) {
		if (hitEntityId == null)
			return;
		this.lastHitEntityId = hitEntityId;
		this.ignoreHitTicks = getIgnoreHitTicks();
		this.entityData.set(ID_IGNORE_HIT_TICKS, this.ignoreHitTicks);
	}

	public int getPiercingLevel() {
		return this.pierceLevel;
	}

	public int getPiercedCount() {
		return this.piercedCount;
	}

	public void setPiercedCount(int count) {
		this.piercedCount = Math.max(0, count);
		this.getPersistentData().putInt("PiercedCount", this.piercedCount);
	}

	public void resetPiercedCount() {
		this.piercedCount = 0;
		this.getPersistentData().putInt("PiercedCount", 0);
	}

	public void setPierceLevel(int level) {
		this.pierceLevel = Math.max(0, level);
		this.getPersistentData().putInt("PierceLevel", this.pierceLevel);
		this.entityData.set(ID_PIERCING, this.pierceLevel);
	}

	public int getShieldDisableCooldownTicks() {
		return 0;
	}

	public boolean isCritical() {
		return this.entityData.get(ID_CRITICAL);
	}

	public boolean isMultishotClone() {
		return this.getPersistentData().getBoolean("IsMultishotClone");
	}

	public ItemStack getWeaponItem() {
		return this.weaponItem.copy();
	}

	public boolean isFoil() {
		return this.entityData.get(ID_FOIL);
	}

	public boolean hasImpacted() {
		return this.entityData.get(ID_IMPACTED);
	}

	public int getSpinTicks() {
		return this.entityData.get(ID_SPIN_TICKS);
	}

	public float getLastSpinRotation() {
		return this.entityData.get(ID_LAST_SPIN_ROTATION);
	}

	public int getIgnoreHitTicks() {
		return 10;
	}

	public void setWeaponBaseDamage(float damage) {
		this.weaponDamage = damage;
		this.getPersistentData().putFloat("WeaponDamage", damage);
	}

	public void setWeaponBaseKnockback(float knockback) {
		this.weaponKnockback = knockback;
		this.getPersistentData().putFloat("WeaponKnockback", knockback);
	}

	public float getWeaponDamage() {
		return this.weaponDamage;
	}

	public float getWeaponKnockback() {
		return this.weaponKnockback;
	}
}
