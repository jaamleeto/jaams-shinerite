
package net.jaams.shinerite.entity;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
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
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.init.ModEntities;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.configuration.client.ProjectileClientConfig;
import net.jaams.shinerite.component.projectile.BaseWeaponProjectileEntity;

public class ShineriteStarProjectileEntity extends BaseWeaponProjectileEntity {
	public ShineriteStarProjectileEntity(PlayMessages.SpawnEntity packet, Level world) {
		super(ModEntities.SHINERITE_STAR_PROJECTILE.get(), world);
		initializeProperties();
	}

	public ShineriteStarProjectileEntity(EntityType<? extends ShineriteStarProjectileEntity> type, Level world) {
		super(type, world);
		initializeProperties();
	}

	public ShineriteStarProjectileEntity(EntityType<? extends ShineriteStarProjectileEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world);
		initializeProperties();
	}

	public ShineriteStarProjectileEntity(EntityType<? extends ShineriteStarProjectileEntity> type, LivingEntity entity, Level world) {
		super(type, entity, world);
		initializeProperties();
	}

	public ShineriteStarProjectileEntity(Level world, LivingEntity entity, ItemStack weaponItem) {
		super(ModEntities.SHINERITE_STAR_PROJECTILE.get(), entity, world, weaponItem);
		initializeProperties();
	}

	private void initializeProperties() {
		this.setWeaponBaseDamage(getBaseDamageFromWeapon());
		this.setWeaponBaseKnockback(getBaseKnockbackFromWeapon());
		this.setPierceLevel(ModProjectiles.getPiercingLevel(this.getWeaponItem(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL.get()));
		refreshDimensions();
	}

	private float getBaseDamageFromWeapon() {
		return ModProjectiles.getBaseDamage(this.getWeaponItem(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_DAMAGE.get());
	}

	private float getBaseKnockbackFromWeapon() {
		return ModProjectiles.getBaseKnockback(this.getWeaponItem(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_KNOCKBACK.get());
	}

	private int getDespawnTicks() {
		return ModProjectiles.getDespawnTicks(this.getWeaponItem(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_DESPAWN_TIME.get());
	}

	private float getWaterInertiaFromWeapon() {
		return ModProjectiles.getWaterInertia(this.getWeaponItem(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_WATER_INERTIA.get());
	}

	private boolean getAllowCriticals() {
		return ModProjectiles.getAllowCriticals(this.getWeaponItem(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_ALLOW_CRITICALS.get());
	}

	private int getIgnoreHitTicksFromWeapon() {
		return ModProjectiles.getIgnoreHitTicks(this.getWeaponItem(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_IGNORE_TICKS.get());
	}

	private boolean getHasTrailParticles() {
		ItemStack weapon = this.getWeaponItem();
		if (weapon.hasTag() && weapon.getTag().contains("ProjectileTrail")) {
			return weapon.getTag().getBoolean("ProjectileTrail");
		}
		return ProjectileClientConfig.SHINERITE_STAR_PROJECTILE_TRAIL.get();
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
		return ModProjectiles.getDimensions(weapon, ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_HITBOX_WIDTH.get().floatValue(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_HITBOX_HEIGHT.get().floatValue());
	}

	@Override
	public ItemStack getDefaultWeaponItem() {
		return new ItemStack(ModItems.SHINERITE_STAR.get());
	}

	@Override
	protected SoundEvent getHitSound() {
		return getSound("projectile_hit", "shinerite_star_hit", SoundEvents.TRIDENT_HIT);
	}

	@Override
	protected SoundEvent getGroundSound() {
		return getSound("projectile_ground", "shinerite_star_ground", SoundEvents.TRIDENT_HIT_GROUND);
	}

	@Override
	protected SoundEvent getLoyaltySound() {
		return getSound("projectile_return", "shinerite_star_return", SoundEvents.TRIDENT_RETURN);
	}

	@Override
	public void tick() {
		super.tick();
		spawnTrailParticles();
		if (!this.level().isClientSide && !this.inGround && !this.isNoPhysics()) {
			if (this.tickCount % 6 == 0) {
				ModUtils.playSound(this, "jaams_shinerite:thin_swoosh_air", SoundSource.AMBIENT, 0.3F, 1.0F);
			}
		}
	}

	@Override
	protected void tickDespawn() {
		life++;
		int despawnTime = getDespawnTicks();
		if (life >= despawnTime) {
			if (ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_DESPAWN_AS_ITEM.get() && this.pickup != AbstractArrow.Pickup.CREATIVE_ONLY) {
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
	public int getIgnoreHitTicks() {
		return getIgnoreHitTicksFromWeapon();
	}

	private void spawnTrailParticles() {
		if (!this.level().isClientSide)
			return;
		if (this.isInWater() || this.inGround)
			return;
		if (!getHasTrailParticles())
			return;
		int spawnRate = ProjectileClientConfig.SHINERITE_STAR_PROJECTILE_TRAIL_SPAWN_RATE.get();
		if (this.random.nextInt(spawnRate) != 0)
			return;
		float r = 1.0F, g = 1.0F, b = 1.0F;
		int color = -1;
		if (this.weaponItem.getItem() instanceof IDyeableItem dyeableItem && dyeableItem.hasColor(this.weaponItem)) {
			color = dyeableItem.getColor(this.weaponItem);
			r = ((color >> 16) & 255) / 255.0F;
			g = ((color >> 8) & 255) / 255.0F;
			b = (color & 255) / 255.0F;
		}
		this.level().addParticle(new CustomFireworkParticleData(r, g, b, 0.14F), this.getX(), this.getY() + 0.1 + this.random.nextGaussian() * 0.05, this.getZ(), this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02,
				this.random.nextGaussian() * 0.02);
	}
}
