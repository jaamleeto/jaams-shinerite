package net.jaams.shinerite.dispenser;

import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.Position;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockSource;

import net.jaams.shinerite.init.ModEntities;
import net.jaams.shinerite.entity.ShineriteStarProjectileEntity;
import net.jaams.shinerite.configuration.common.InteractionsConfig;

public class ShineriteStarDispenser extends AbstractProjectileDispenseBehavior {
	@Override
	protected Projectile getProjectile(Level level, Position pos, ItemStack stack) {
		ShineriteStarProjectileEntity entity = new ShineriteStarProjectileEntity(ModEntities.SHINERITE_STAR_PROJECTILE.get(), level);
		entity.setWeaponItem(stack);
		entity.setPos(pos.x(), pos.y(), pos.z());
		entity.pickup = AbstractArrow.Pickup.ALLOWED;
		return entity;
	}

	@Override
	public ItemStack execute(BlockSource source, ItemStack stack) {
		Level level = source.getLevel();
		if (!InteractionsConfig.DISPENSER_INTERACTIONS.get() || !InteractionsConfig.DISPENSER_PROJECTILE_SHINERITE_STAR.get()) {
			Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
			Position pos = DispenserBlock.getDispensePosition(source);
			ItemStack singleItem = stack.split(1);
			DefaultDispenseItemBehavior.spawnItem(level, singleItem, 6, facing, pos);
			return stack;
		}
		return super.execute(source, stack);
	}
}
