package net.jaams.shinerite.handler.mechanic;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.particle.CustomEffectParticleData;

import java.util.stream.StreamSupport;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Mod.EventBusSubscriber
public class ImbuedItemHandler {
	private static final Map<UUID, Integer> particleTickCounterMainHand = new HashMap<>();
	private static final Map<UUID, Integer> particleTickCounterOffHand = new HashMap<>();
	private static final Map<UUID, Integer> particleTickCounterGround = new HashMap<>();
	private static final Map<UUID, Integer> particleTickCounterProjectile = new HashMap<>();
	private static final int PARTICLE_INTERVAL = 10;

	@SubscribeEvent
	public static void onEntityImbuedTick(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		Level world = entity.level();
		if (world.isClientSide())
			return;
		UUID entityId = entity.getUUID();
		processHand(entity, world, entityId, entity.getMainHandItem(), InteractionHand.MAIN_HAND, particleTickCounterMainHand);
		processHand(entity, world, entityId, entity.getOffhandItem(), InteractionHand.OFF_HAND, particleTickCounterOffHand);
		processArmor(entity, world, entityId);
	}

	@SubscribeEvent
	public static void onImbuedLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.level.isClientSide())
			return;
		ServerLevel serverLevel = (ServerLevel) event.level;
		StreamSupport.stream(serverLevel.getEntities().getAll().spliterator(), false).filter(entity -> entity instanceof ItemEntity).map(entity -> (ItemEntity) entity).forEach(itemEntity -> {
			ItemStack stack = itemEntity.getItem();
			if (ModUtils.isImbued(stack)) {
				processGroundItem(itemEntity, serverLevel, stack);
			} else {
				particleTickCounterGround.remove(itemEntity.getUUID());
			}
		});
		for (Player player : serverLevel.players()) {
			if (player instanceof ServerPlayer serverPlayer) {
				AABB searchBox = serverPlayer.getBoundingBox().inflate(56.0);
				serverLevel.getEntitiesOfClass(AbstractArrow.class, searchBox).forEach(projectile -> processProjectile(projectile, serverLevel));
			}
		}
	}

	private static void processHand(LivingEntity entity, Level world, UUID entityId, ItemStack stack, InteractionHand hand, Map<UUID, Integer> counterMap) {
		if (stack.isEmpty() || !ModUtils.isImbued(stack)) {
			counterMap.remove(entityId);
			return;
		}
		int tickCounter = counterMap.compute(entityId, (id, count) -> {
			if (count == null)
				return 0;
			return count + 1;
		});
		if (tickCounter >= PARTICLE_INTERVAL) {
			spawnImbuedParticles(stack, entity, world, hand);
			counterMap.put(entityId, 0);
		}
	}

	private static void processGroundItem(ItemEntity itemEntity, ServerLevel world, ItemStack stack) {
		UUID entityId = itemEntity.getUUID();
		int tickCounter = particleTickCounterGround.compute(entityId, (id, count) -> {
			if (count == null)
				return 1;
			return count + 1;
		});
		if (tickCounter >= PARTICLE_INTERVAL) {
			spawnGroundImbuedParticles(stack, itemEntity, world);
			particleTickCounterGround.put(entityId, 0);
		}
	}

	private static void processProjectile(AbstractArrow projectile, ServerLevel level) {
		CompoundTag nbt = projectile.getPersistentData();
		if (!nbt.contains("WeaponItem", 10)) {
			particleTickCounterProjectile.remove(projectile.getUUID());
			return;
		}
		ItemStack weaponItem = ItemStack.of(nbt.getCompound("WeaponItem"));
		if (!ModUtils.isImbued(weaponItem)) {
			particleTickCounterProjectile.remove(projectile.getUUID());
			return;
		}
		UUID projectileId = projectile.getUUID();
		int tickCounter = particleTickCounterProjectile.compute(projectileId, (id, count) -> {
			if (count == null)
				return 0;
			return count + 1;
		});
		if (tickCounter >= PARTICLE_INTERVAL) {
			spawnProjectileImbuedParticles(weaponItem, projectile, level);
			particleTickCounterProjectile.put(projectileId, 0);
		}
	}

	private static void spawnImbuedParticles(ItemStack stack, LivingEntity entity, Level world, InteractionHand hand) {
		int color = ModUtils.getImbuedColor(stack);
		if (color == -1)
			return;
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		Vec3 handPos = ModUtils.getHandPosition(entity, hand);
		((ServerLevel) world).sendParticles(new CustomEffectParticleData(r, g, b, 0.25F), handPos.x, handPos.y, handPos.z, 2, 0.2, 0.2, 0.2, 0.02);
	}

	private static void spawnGroundImbuedParticles(ItemStack stack, ItemEntity itemEntity, ServerLevel world) {
		int color = ModUtils.getImbuedColor(stack);
		if (color == -1)
			return;
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		Vec3 pos = itemEntity.position().add(0, 0.2, 0);
		world.sendParticles(new CustomEffectParticleData(r, g, b, 0.2F), pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0.01);
	}

	private static void spawnProjectileImbuedParticles(ItemStack weaponItem, AbstractArrow projectile, ServerLevel level) {
		int color = ModUtils.getImbuedColor(weaponItem);
		if (color == -1)
			return;
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		Vec3 pos = projectile.position().add(0, 0.1, 0);
		level.sendParticles(new CustomEffectParticleData(r, g, b, 0.2F), pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.02);
	}

	private static void processArmor(LivingEntity entity, Level world, UUID entityId) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.ARMOR)
				continue;
			ItemStack stack = entity.getItemBySlot(slot);
			if (stack.isEmpty() || !ModUtils.isImbued(stack))
				continue;
			String key = entityId.toString() + "_armor_" + slot.getName();
			Map<String, Integer> armorCounter = particleTickCounterArmor.computeIfAbsent(entityId, k -> new HashMap<>());
			int tickCounter = armorCounter.compute(slot.getName(), (k, count) -> {
				if (count == null)
					return 0;
				return count + 1;
			});
			if (tickCounter >= PARTICLE_INTERVAL) {
				spawnArmorImbuedParticles(stack, entity, world, slot);
				armorCounter.put(slot.getName(), 0);
			}
		}
	}

	private static final Map<UUID, Map<String, Integer>> particleTickCounterArmor = new HashMap<>();

	private static void spawnArmorImbuedParticles(ItemStack stack, LivingEntity entity, Level world, EquipmentSlot slot) {
		int color = ModUtils.getImbuedColor(stack);
		if (color == -1)
			return;
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		double offsetY = switch (slot) {
			case HEAD -> entity.getEyeY() + 0.2;
			case CHEST -> entity.getY() + entity.getBbHeight() * 0.6;
			case LEGS -> entity.getY() + entity.getBbHeight() * 0.35;
			case FEET -> entity.getY() + 0.1;
			default -> entity.getY() + entity.getBbHeight() * 0.5;
		};
		double offsetX = (world.random.nextDouble() - 0.5) * entity.getBbWidth();
		double offsetZ = (world.random.nextDouble() - 0.5) * entity.getBbWidth();
		((ServerLevel) world).sendParticles(new CustomEffectParticleData(r, g, b, 0.2F),
			entity.getX() + offsetX, offsetY, entity.getZ() + offsetZ, 1, 0.1, 0.1, 0.1, 0.02);
	}
}
