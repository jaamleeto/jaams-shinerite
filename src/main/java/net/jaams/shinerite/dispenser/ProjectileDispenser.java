package net.jaams.shinerite.dispenser;

import net.jaams.shinerite.init.ModEntities;
import net.jaams.shinerite.init.ModSounds;
import net.jaams.shinerite.entity.ShineriteStarProjectileEntity;
import net.jaams.shinerite.entity.ShinemerangProjectileEntity;
import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.util.ModProjectiles;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ProjectileDispenser {

	private static final float PROJECTILE_SPEED = 1.5F;

	public static boolean canDispense(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (stack.is(ModTags.SHINEMERANGS)) {
			return InteractionsConfig.DISPENSER_INTERACTIONS.get();
		}
		if (stack.is(ModTags.SHINERITE_STARS)) {
			return InteractionsConfig.DISPENSER_INTERACTIONS.get() && InteractionsConfig.DISPENSER_PROJECTILE_SHINERITE_STAR.get();
		}
		return false;
	}

	public static ItemStack dispense(BlockSource source, ItemStack stack) {
		Level level = source.getLevel();
		if (level.isClientSide()) {
			return new DefaultDispenseItemBehavior().dispense(source, stack);
		}

		Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
		Position position = DispenserBlock.getDispensePosition(source);

		Entity entity = createProjectileEntity(stack, level, position.x(), position.y(), position.z());
		if (entity == null) {
			return new DefaultDispenseItemBehavior().dispense(source, stack);
		}

		if (entity instanceof AbstractArrow arrow) {
			arrow.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), PROJECTILE_SPEED, 0.0F);
		}

		level.addFreshEntity(entity);
		playShootSound(level, position, stack);
		stack.shrink(1);
		return stack;
	}

	private static Entity createProjectileEntity(ItemStack stack, Level level, double x, double y, double z) {
		if (stack.is(ModTags.SHINEMERANGS)) {
			ShinemerangProjectileEntity entity = new ShinemerangProjectileEntity(ModEntities.SHINEMERANG_PROJECTILE.get(), x, y, z, level);
			initializeProjectile(entity, stack, ProjectileCommonConfig.SHINEMERANG_PROJECTILE_BASE_DAMAGE.get(), ProjectileCommonConfig.SHINEMERANG_PROJECTILE_BASE_KNOCKBACK.get(),
					ProjectileCommonConfig.SHINEMERANG_PROJECTILE_PIERCING_LEVEL.get());
			return entity;
		}
		if (stack.is(ModTags.SHINERITE_STARS)) {
			ShineriteStarProjectileEntity entity = new ShineriteStarProjectileEntity(ModEntities.SHINERITE_STAR_PROJECTILE.get(), x, y, z, level);
			initializeProjectile(entity, stack, ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_DAMAGE.get(), ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_BASE_KNOCKBACK.get(),
					ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL.get());
			return entity;
		}
		return null;
	}

	private static void initializeProjectile(net.jaams.shinerite.component.projectile.BaseWeaponProjectileEntity entity, ItemStack stack, double baseDamage, double baseKnockback, int pierceLevel) {
		entity.setWeaponItem(stack);
		entity.setWeaponBaseDamage(ModProjectiles.getBaseDamage(stack, baseDamage));
		entity.setWeaponBaseKnockback(ModProjectiles.getBaseKnockback(stack, baseKnockback));
		entity.setPierceLevel(ModProjectiles.getPiercingLevel(stack, pierceLevel));
	}

	private static void playShootSound(Level level, Position position, ItemStack stack) {
		SoundEvent sound;
		if (stack.hasTag() && stack.getTag().getBoolean("GenericSoundSet")) {
			sound = ModSounds.PROJECTILE_THROW.get();
		} else if (stack.is(ModTags.SHINEMERANGS)) {
			sound = ModSounds.SHINEMERANG_SHOOT.get();
		} else {
			sound = ModSounds.SHINERITE_STAR_SHOOT.get();
		}
		level.playSound(null, position.x(), position.y(), position.z(), sound, SoundSource.PLAYERS, 1.0F, 1.0F + level.random.nextFloat() * 0.2F - 0.1F);
	}
}
