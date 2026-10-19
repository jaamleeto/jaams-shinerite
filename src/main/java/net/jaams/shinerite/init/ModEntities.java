
package net.jaams.shinerite.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import net.jaams.shinerite.entity.ShineriteStarProjectileEntity;
import net.jaams.shinerite.entity.ShinerangProjectileEntity;
import net.jaams.shinerite.JaamsShineriteMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JaamsShineriteMod.MODID);
	public static final RegistryObject<EntityType<ShinerangProjectileEntity>> SHINERANG_PROJECTILE = register("shinerang_projectile", EntityType.Builder.<ShinerangProjectileEntity>of(ShinerangProjectileEntity::new, MobCategory.MISC)
			.setCustomClientFactory(ShinerangProjectileEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<ShineriteStarProjectileEntity>> SHINERITE_STAR_PROJECTILE = register("shinerite_star_projectile",
			EntityType.Builder.<ShineriteStarProjectileEntity>of(ShineriteStarProjectileEntity::new, MobCategory.MISC).setCustomClientFactory(ShineriteStarProjectileEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
					.setUpdateInterval(1).sized(0.5f, 0.5f));

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}
}
