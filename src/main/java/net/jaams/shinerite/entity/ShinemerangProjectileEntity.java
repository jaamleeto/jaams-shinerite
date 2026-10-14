
package net.jaams.shinerite.entity;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.PacketDistributor;

import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModProjectiles;
import net.jaams.shinerite.particle.CustomFireworkParticleData;
import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.init.ModEntities;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.configuration.client.ProjectileClientConfig;
import net.jaams.shinerite.component.projectile.BaseReturningProjectileEntity;
import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;
import net.jaams.shinerite.JaamsShineriteMod;

public class ShinemerangProjectileEntity extends BaseReturningProjectileEntity {
	public ShinemerangProjectileEntity(PlayMessages.SpawnEntity packet, Level world) {
		super(ModEntities.SHINEMERANG_PROJECTILE.get(), world);
		initializeProperties();
	}

	public ShinemerangProjectileEntity(EntityType<? extends ShinemerangProjectileEntity> type, Level world) {
		super(type, world);
		initializeProperties();
	}

	public ShinemerangProjectileEntity(EntityType<? extends ShinemerangProjectileEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world);
		initializeProperties();
	}

	public ShinemerangProjectileEntity(EntityType<? extends ShinemerangProjectileEntity> type, LivingEntity entity, Level world) {
		super(type, entity, world);
		initializeProperties();
	}

	public ShinemerangProjectileEntity(Level world, LivingEntity entity, ItemStack weaponItem) {
		super(ModEntities.SHINEMERANG_PROJECTILE.get(), entity, world, weaponItem);
		initializeProperties();
	}

	private void initializeProperties() {
		this.setWeaponBaseDamage(getBaseDamageFromWeapon());
		this.setWeaponBaseKnockback(getBaseKnockbackFromWeapon());
		this.setPierceLevel(ModProjectiles.getPiercingLevel(this.getWeaponItem(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_PIERCING_LEVEL.get()));
		refreshDimensions();
	}

	private float getBaseDamageFromWeapon() {
		return ModProjectiles.getBaseDamage(this.getWeaponItem(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_BASE_DAMAGE.get());
	}

	private float getBaseKnockbackFromWeapon() {
		return ModProjectiles.getBaseKnockback(this.getWeaponItem(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_BASE_KNOCKBACK.get());
	}

	private boolean getThrowbackEnabled() {
		return ModProjectiles.getThrowbackEnabled(this.getWeaponItem(), TraitsConfig.THROWBACK.get());
	}

	private boolean getRadiantCollectorEnabled() {
		return ModProjectiles.getRadiantCollectorEnabled(this.getWeaponItem(), TraitsConfig.RADIANT_COLLECTOR.get());
	}

	private boolean getReturnOnBlockHit() {
		return ModProjectiles.getReturnOnBlockHit(this.getWeaponItem(), TraitsConfig.THROWBACK_SHINEMERANG_PROJECTILE_ON_BLOCK.get());
	}

	private boolean getReturnOnEntityHit() {
		return ModProjectiles.getReturnOnEntityHit(this.getWeaponItem(), TraitsConfig.THROWBACK_SHINEMERANG_PROJECTILE_ON_HIT.get());
	}

	private boolean getReturnOnMaxRange() {
		return ModProjectiles.getReturnOnMaxRange(this.getWeaponItem(), TraitsConfig.THROWBACK_SHINEMERANG_PROJECTILE_ON_MAX_RANGE.get());
	}

	private boolean getRadiantGlowEnabled() {
		if (!getRadiantCollectorEnabled()) {
			return false;
		}
		return ModProjectiles.getRadiantCollectorGlow(this.getWeaponItem(), TraitsConfig.RADIANT_COLLECTOR_SHINEMERANG_PROJECTILE_GLOW.get());
	}

	private int getMaxMountedEntitiesFromWeapon() {
		return ModProjectiles.getMaxMountedEntities(this.getWeaponItem(), getRadiantCollectorEnabled(), TraitsConfig.RADIANT_COLLECTOR_SHINEMERANG_PROJECTILE_MAX_ITEMS.get());
	}

	private int getDespawnTicks() {
		return ModProjectiles.getDespawnTicks(this.getWeaponItem(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_DESPAWN_TIME.get());
	}

	private float getWaterInertiaFromWeapon() {
		return ModProjectiles.getWaterInertia(this.getWeaponItem(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_WATER_INERTIA.get());
	}

	private boolean getAllowCriticals() {
		return ModProjectiles.getAllowCriticals(this.getWeaponItem(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_ALLOW_CRITICALS.get());
	}

	private int getIgnoreHitTicksFromWeapon() {
		return ModProjectiles.getIgnoreHitTicks(this.getWeaponItem(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_IGNORE_TICKS.get());
	}

	private boolean getHasTrailParticles() {
		ItemStack weapon = this.getWeaponItem();
		if (weapon.hasTag() && weapon.getTag().contains("ProjectileTrail")) {
			return weapon.getTag().getBoolean("ProjectileTrail");
		}
		return ProjectileClientConfig.SHINEMERANG_PROJECTILE_TRAIL.get();
	}

	private boolean useGenericProjectileSounds() {
		ItemStack weapon = this.getWeaponItem();
		return weapon.hasTag() && weapon.getTag().getBoolean("GenericSoundSet");
	}

	private SoundEvent getSound(String genericId, String specificId, SoundEvent fallback) {
		String soundId = useGenericProjectileSounds() ? genericId : specificId;
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("jaams_shinerite:" + soundId));
		return sound != null ? sound : fallback;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		ItemStack weapon = this.getWeaponItem();
		return ModProjectiles.getDimensions(weapon, ProjectileCommonConfig.SHINEMERANG_PROJECTILE_HITBOX_WIDTH.get().floatValue(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_HITBOX_HEIGHT.get().floatValue());
	}

	@Override
	public ItemStack getDefaultWeaponItem() {
		return new ItemStack(ModItems.SHINEMERANG.get());
	}

	@Override
	protected SoundEvent getHitSound() {
		return getSound("projectile_hit", "shinemerang_hit", SoundEvents.TRIDENT_HIT);
	}

	@Override
	protected SoundEvent getGroundSound() {
		return getSound("projectile_ground", "shinemerang_ground", SoundEvents.TRIDENT_HIT_GROUND);
	}

	@Override
	protected SoundEvent getLoyaltySound() {
		return getSound("projectile_return", "shinemerang_return", SoundEvents.TRIDENT_RETURN);
	}

	@Override
	protected boolean returnOnBlockHit(BlockHitResult blockHitResult) {
		return getThrowbackEnabled() && getReturnOnBlockHit();
	}

	@Override
	protected boolean returnOnEntityHit(EntityHitResult entityHitResult) {
		return getThrowbackEnabled() && getReturnOnEntityHit();
	}

	@Override
	protected boolean returnOnMaxRange() {
		return getThrowbackEnabled() && getReturnOnMaxRange();
	}

	@Override
	public int getMaxMountedEntities() {
		return getMaxMountedEntitiesFromWeapon();
	}

	@Override
	public int getIgnoreHitTicks() {
		return getIgnoreHitTicksFromWeapon();
	}

	@Override
	protected float getWaterInertia() {
		return getWaterInertiaFromWeapon();
	}

	@Override
	public boolean isCritical() {
		if (!getAllowCriticals()) {
			return false;
		}
		return super.isCritical();
	}

	@Override
	public void tick() {
		super.tick();
		spawnTrailParticles();
		if (!this.level().isClientSide && !this.inGround && !this.isNoPhysics() && getSwooshSoundEnabled()) {
			if (this.tickCount % getSwooshInterval() == 0) {
				float pitch = 1.0F + this.random.nextFloat() * 0.25F;
				ModUtils.playSound(this, "jaams_shinerite:swoosh_air", SoundSource.AMBIENT, 0.3F, pitch);
			}
		}
	}

	private int getSwooshInterval() {
		ItemStack weapon = this.getWeaponItem();
		if (weapon.hasTag() && weapon.getTag().contains("ProjectileSwooshInterval")) {
			return Math.max(1, weapon.getTag().getInt("ProjectileSwooshInterval"));
		}
		int interval = ProjectileCommonConfig.SHINEMERANG_PROJECTILE_SWOOSH_INTERVAL.get();
		return Math.max(1, interval);
	}

	private boolean getSwooshSoundEnabled() {
		ItemStack weapon = this.getWeaponItem();
		if (weapon.hasTag() && weapon.getTag().contains("ProjectileSwoosh")) {
			return weapon.getTag().getBoolean("ProjectileSwoosh");
		}
		return ProjectileCommonConfig.SHINEMERANG_PROJECTILE_SWOOSH_SOUND.get();
	}

	@Override
	protected void tickDespawn() {
		life++;
		int despawnTime = getDespawnTicks();
		if (life >= despawnTime) {
			if (ProjectileCommonConfig.SHINEMERANG_PROJECTILE_DESPAWN_AS_ITEM.get() && this.pickup != AbstractArrow.Pickup.CREATIVE_ONLY) {
				ModProjectiles.dropAsItem(this.level(), this.getPickupItem().copy(), this.getX(), this.getY(), this.getZ());
			} else if (!this.level().isClientSide) {
				ItemStack itemStack = this.weaponItem.copy();
				if (!itemStack.isEmpty()) {
					((ServerLevel) this.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack), getX(), getY() + 0.5, getZ(), 5, 0.1d, 0.1d, 0.1d, 0.05d);
				}
			}
			discard();
		}
	}

	@Override
	public void removePassenger(Entity passenger) {
		if (passenger != null) {
			passenger.setGlowingTag(false);
		}
		super.removePassenger(passenger);
	}

	@Override
	protected void handleCollectItemsWhileReturning() {
		if (!getRadiantCollectorEnabled()) {
			return;
		}
		super.handleCollectItemsWhileReturning();
		for (Entity passenger : this.getPassengers()) {
			applyGlowToPassenger(passenger);
		}
	}

	private void applyGlowToPassenger(Entity passenger) {
		if (!(passenger instanceof ItemEntity || passenger instanceof ExperienceOrb)) {
			return;
		}
		if (!getRadiantGlowEnabled()) {
			return;
		}
		passenger.setGlowingTag(true);
		int color = getGlowColorFromWeapon();
		final int finalColor = color;
		passenger.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(glow -> {
			glow.setColor(finalColor);
			JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY.with(() -> passenger), new GlowColorPacket(passenger.getId(), finalColor));
		});
	}

	private int getGlowColorFromWeapon() {
		ItemStack weaponItem = this.getWeaponItem();
		if (weaponItem.getItem() instanceof IDyeableItem dyeable && dyeable.hasColor(weaponItem)) {
			return dyeable.getColor(weaponItem);
		}
		return -1;
	}

	private void spawnTrailParticles() {
		if (!this.level().isClientSide) {
			return;
		}
		if (this.isInWater() || this.inGround) {
			return;
		}
		if (!getHasTrailParticles()) {
			return;
		}
		int spawnRate = ProjectileClientConfig.SHINEMERANG_PROJECTILE_TRAIL_SPAWN_RATE.get();
		if (this.random.nextInt(spawnRate) != 0) {
			return;
		}
		float r = 1.0F, g = 1.0F, b = 1.0F;
		int color = -1;
		if (this.weaponItem.getItem() instanceof IDyeableItem dyeableItem && dyeableItem.hasColor(this.weaponItem)) {
			color = dyeableItem.getColor(this.weaponItem);
			r = ((color >> 16) & 255) / 255.0F;
			g = ((color >> 8) & 255) / 255.0F;
			b = (color & 255) / 255.0F;
		}
		double offsetY = 0.15 + this.random.nextGaussian() * 0.04;
		this.level().addParticle(new CustomFireworkParticleData(r, g, b, 0.18F), this.getX() + this.random.nextGaussian() * 0.08, this.getY() + offsetY, this.getZ() + this.random.nextGaussian() * 0.08, 0.0, 0.0, 0.0);
	}
}
