package net.jaams.shinerite.handler.mechanic;

import org.joml.Vector3f;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.MechanicsConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@Mod.EventBusSubscriber
public class ColorMechanicsHandler {
	private static final Map<UUID, Integer> rainTickCounterMapMainHand = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapOffHand = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapHelmet = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapChestplate = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapLeggings = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapBoots = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapItemEntity = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapProjectile = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> rainTickCounterMapBackItem = new ConcurrentHashMap<>();
	private static final double PROJECTILE_SEARCH_RADIUS = 32.0;
	private static final int PARTICLE_COUNT = 5;
	private static final double PARTICLE_OFFSET = 0.3;
	private static final double PARTICLE_SPEED = 0.05;

	private static boolean isWashingEnabled() {
		return MechanicsConfig.WASHING.get();
	}

	private static int getTicksPerStep() {
		return MechanicsConfig.WASHING_TICKS_PER_STEP.get();
	}

	private static boolean isWaxingEnabled() {
		return MechanicsConfig.WAXING.get();
	}

	private static boolean isWaxedProtected(ItemStack stack) {
		return ModUtils.isWaxed(stack);
	}

	@SubscribeEvent
	public static void onProjectileColorTick(TickEvent.LevelTickEvent event) {
		if (!isWashingEnabled() || event.level.isClientSide || event.phase != TickEvent.Phase.END)
			return;
		if (!MechanicsConfig.WASHING_AFFECT_PROJECTILES.get())
			return;
		for (Player player : event.level.players()) {
			if (player instanceof ServerPlayer serverPlayer) {
				AABB searchBox = serverPlayer.getBoundingBox().inflate(PROJECTILE_SEARCH_RADIUS);
				for (AbstractArrow projectile : event.level.getEntitiesOfClass(AbstractArrow.class, searchBox)) {
					processProjectileItem(projectile, event.level);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onColorLivingTick(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		if (!isWashingEnabled() || entity.level().isClientSide)
			return;
		if (MechanicsConfig.WASHING_AFFECT_HELD_ITEMS.get()) {
			processItem(entity.getMainHandItem(), entity.level(), entity, rainTickCounterMapMainHand, EquipmentSlot.MAINHAND);
			processItem(entity.getOffhandItem(), entity.level(), entity, rainTickCounterMapOffHand, EquipmentSlot.OFFHAND);
		}
		if (MechanicsConfig.WASHING_AFFECT_ARMOR.get()) {
			processItem(entity.getItemBySlot(EquipmentSlot.HEAD), entity.level(), entity, rainTickCounterMapHelmet, EquipmentSlot.HEAD);
			processItem(entity.getItemBySlot(EquipmentSlot.CHEST), entity.level(), entity, rainTickCounterMapChestplate, EquipmentSlot.CHEST);
			processItem(entity.getItemBySlot(EquipmentSlot.LEGS), entity.level(), entity, rainTickCounterMapLeggings, EquipmentSlot.LEGS);
			processItem(entity.getItemBySlot(EquipmentSlot.FEET), entity.level(), entity, rainTickCounterMapBoots, EquipmentSlot.FEET);
		}
		if (MechanicsConfig.WASHING_AFFECT_BACK_ITEM.get()) {
			processBackItem(entity, entity.level());
		}
	}

	@SubscribeEvent
	public static void onColorWorldTick(TickEvent.LevelTickEvent event) {
		if (!isWashingEnabled() || event.level.isClientSide || event.phase != TickEvent.Phase.END)
			return;
		if (!MechanicsConfig.WASHING_AFFECT_DROPPED_ITEMS.get())
			return;
		event.level.players().forEach(player -> {
			AABB searchBox = player.getBoundingBox().inflate(16.0);
			for (ItemEntity itemEntity : event.level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
				processItemEntity(itemEntity, event.level);
			}
		});
	}

	private static void processBackItem(LivingEntity entity, Level level) {
		CompoundTag entityNBT = entity.serializeNBT();
		CompoundTag forgeCaps = entityNBT.getCompound("ForgeCaps");
		if (!forgeCaps.contains("jaams_mechanics:back_item"))
			return;
		CompoundTag backItemNBT = forgeCaps.getCompound("jaams_mechanics:back_item");
		if (!backItemNBT.contains("backItem", 10))
			return;
		ItemStack backItem = ItemStack.of(backItemNBT.getCompound("backItem"));
		if (backItem.isEmpty() || !ModUtils.isWashableItem(backItem))
			return;
		if (!ModUtils.isDyed(backItem) && !ModUtils.isImbued(backItem)) {
			rainTickCounterMapBackItem.put(entity.getUUID(), 0);
			return;
		}
		UUID entityId = entity.getUUID();
		int counter = rainTickCounterMapBackItem.getOrDefault(entityId, 0);
		if (isExposedToDecay(backItem, level, entity)) {
			if (++counter >= getTicksPerStep()) {
				int newColor = applyColorLightening(backItem, level, entity);
				if (newColor != 0xFFFFFF && newColor != -1) {
					generateDustParticles(level, entity, null, newColor, PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
				}
				backItemNBT.put("backItem", backItem.save(new CompoundTag()));
				forgeCaps.put("jaams_mechanics:back_item", backItemNBT);
				entityNBT.put("ForgeCaps", forgeCaps);
				entity.deserializeNBT(entityNBT);
				sendBackItemUpdateToClient(entity);
				counter = 0;
			}
		} else {
			counter = 0;
		}
		rainTickCounterMapBackItem.put(entityId, counter);
	}

	private static void processProjectileItem(AbstractArrow projectile, Level level) {
		CompoundTag nbt = projectile.getPersistentData();
		if (!nbt.contains("WeaponItem", 10))
			return;
		ItemStack weaponItem = ItemStack.of(nbt.getCompound("WeaponItem"));
		if (weaponItem.isEmpty() || !ModUtils.isWashableItem(weaponItem))
			return;
		if (!ModUtils.isDyed(weaponItem) && !ModUtils.isImbued(weaponItem)) {
			rainTickCounterMapProjectile.put(projectile.getUUID(), 0);
			return;
		}
		UUID projectileId = projectile.getUUID();
		int counter = rainTickCounterMapProjectile.getOrDefault(projectileId, 0);
		if (isExposedToDecay(weaponItem, level, projectile)) {
			if (++counter >= getTicksPerStep()) {
				int newColor = applyColorLightening(weaponItem, level, projectile);
				if (newColor != 0xFFFFFF && newColor != -1) {
					generateDustParticles(level, projectile, null, newColor, PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
				}
				nbt.put("WeaponItem", weaponItem.save(new CompoundTag()));
				counter = 0;
			}
		} else {
			counter = 0;
		}
		rainTickCounterMapProjectile.put(projectileId, counter);
	}

	private static void processItem(ItemStack stack, Level level, LivingEntity entity, Map<UUID, Integer> counterMap, EquipmentSlot slot) {
		if (stack.isEmpty() || !ModUtils.isWashableItem(stack))
			return;
		if (!ModUtils.isDyed(stack) && !ModUtils.isImbued(stack)) {
			UUID entityId = entity.getUUID();
			counterMap.put(entityId, 0);
			return;
		}
		UUID entityId = entity.getUUID();
		int counter = counterMap.getOrDefault(entityId, 0);
		boolean exposed = (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) ? isExposedToDecay(stack, level, entity) : isArmorExposedToDecay(stack, level, entity, slot);
		if (exposed) {
			if (++counter >= getTicksPerStep()) {
				int newColor = applyColorLightening(stack, level, entity);
				if (newColor != 0xFFFFFF && newColor != -1) {
					generateDustParticles(level, entity, slot, newColor, PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
				}
				counter = 0;
			}
		} else {
			counter = 0;
		}
		counterMap.put(entityId, counter);
	}

	private static void processItemEntity(ItemEntity itemEntity, Level level) {
		ItemStack stack = itemEntity.getItem();
		if (stack.isEmpty() || !ModUtils.isWashableItem(stack))
			return;
		if (!ModUtils.isDyed(stack) && !ModUtils.isImbued(stack)) {
			rainTickCounterMapItemEntity.put(itemEntity.getUUID(), 0);
			return;
		}
		UUID entityId = itemEntity.getUUID();
		int counter = rainTickCounterMapItemEntity.getOrDefault(entityId, 0);
		if (isExposedToDecay(stack, level, itemEntity)) {
			if (++counter >= getTicksPerStep()) {
				int newColor = applyColorLightening(stack, level, itemEntity);
				if (newColor != 0xFFFFFF && newColor != -1) {
					generateDustParticles(level, itemEntity, null, newColor, PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
				}
				itemEntity.setItem(stack);
				sendUpdateToClient(itemEntity);
				counter = 0;
			}
		} else {
			counter = 0;
		}
		rainTickCounterMapItemEntity.put(entityId, counter);
	}

	private static boolean isExposedToDecay(ItemStack stack, Level level, Entity entity) {
		CompoundTag tag = stack.getTag();
		if (tag != null) {
			if (tag.contains("Washable") && !tag.getBoolean("Washable")) {
				return false;
			}
			if (ModUtils.isImbued(stack) && tag.contains("WashableImbued") && !tag.getBoolean("WashableImbued")) {
				return false;
			}
		}
		if (isWaxedProtected(stack))
			return false;
		boolean rainEnabled = MechanicsConfig.WASHING_RAIN.get();
		boolean waterEnabled = MechanicsConfig.WASHING_WATER.get();
		boolean thunderBoost = MechanicsConfig.WASHING_THUNDERSTORM_BOOST.get();
		BlockPos above = entity.blockPosition().above();
		boolean raining = level.isRainingAt(above) && level.canSeeSky(above);
		boolean thundering = level.isThundering() && level.canSeeSky(above);
		boolean inWater = entity.isInWaterOrBubble();
		boolean byRain = rainEnabled && (raining || (thunderBoost && thundering));
		boolean byWater = waterEnabled && inWater;
		return byRain || byWater;
	}

	private static boolean isArmorExposedToDecay(ItemStack stack, Level level, LivingEntity entity, EquipmentSlot slot) {
		if (isWaxedProtected(stack))
			return false;
		boolean rainEnabled = MechanicsConfig.WASHING_RAIN.get();
		boolean waterEnabled = MechanicsConfig.WASHING_WATER.get();
		boolean thunderBoost = MechanicsConfig.WASHING_THUNDERSTORM_BOOST.get();
		BlockPos above = entity.blockPosition().above();
		boolean raining = level.isRainingAt(above) && level.canSeeSky(above);
		boolean thundering = level.isThundering() && level.canSeeSky(above);
		if ((rainEnabled && raining) || (thunderBoost && thundering)) {
			return true;
		}
		if (!waterEnabled || !entity.isInWaterOrBubble()) {
			return false;
		}
		double waterHeight = getWaterHeight(level, entity);
		double entityHeight = entity.getY();
		double headHeight = entityHeight + entity.getBbHeight();
		double chestHeight = entityHeight + entity.getBbHeight() * 0.6;
		double legsHeight = entityHeight + entity.getBbHeight() * 0.3;
		double feetHeight = entityHeight + entity.getBbHeight() * 0.1;
		return switch (slot) {
			case HEAD -> waterHeight >= headHeight;
			case CHEST -> waterHeight >= chestHeight || (entity.isCrouching() && waterHeight >= legsHeight);
			case LEGS -> waterHeight >= legsHeight;
			case FEET -> waterHeight >= feetHeight;
			default -> false;
		};
	}

	private static double getWaterHeight(Level level, LivingEntity entity) {
		BlockPos pos = entity.blockPosition();
		FluidState fluidState = level.getFluidState(pos);
		double waterHeight = pos.getY();
		if (fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER) {
			waterHeight += fluidState.getHeight(level, pos);
		}
		BlockPos abovePos = pos.above();
		FluidState aboveFluidState = level.getFluidState(abovePos);
		if (aboveFluidState.getType() == Fluids.WATER || aboveFluidState.getType() == Fluids.FLOWING_WATER) {
			waterHeight = abovePos.getY() + aboveFluidState.getHeight(level, abovePos);
		}
		return waterHeight;
	}

	private static int applyColorLightening(ItemStack stack, Level level, Entity entity) {
		int currentColor = ModUtils.getCurrentColor(stack);
		if (currentColor == -1) {
			ModUtils.removeColor(stack);
			return -1;
		}
		CompoundTag tag = stack.getOrCreateTag();
		boolean isImbued = ModUtils.isImbued(stack);
		if (isImbued) {
			if (tag.contains("WashableImbued") && !tag.getBoolean("WashableImbued")) {
				return currentColor;
			}
			if (!tag.contains("Waxed")) {
				int uses = tag.getInt("ImbuedUses");
				int maxUses = tag.getInt("MaxImbuedUses");
				uses--;
				tag.putInt("ImbuedUses", uses);
				if (uses > 0) {
					float progress = uses / (float) maxUses;
					int newColor = lightenImbuedColor(currentColor, 1.0f - progress);
					ModUtils.applyDyeColor(stack, newColor);
					tag.putInt("ImbuedColor", newColor);
					return newColor;
				} else {
					ModUtils.removeImbuement(stack);
					ModUtils.removeColor(stack);
					resetItemImbue(stack, level, entity);
					return -1;
				}
			}
		}
		if (!tag.contains("Waxed")) {
			int stepsToWhite = calculateStepsToWhite(currentColor);
			int newColor = lightenColor(currentColor, 1.0f / stepsToWhite);
			if (newColor >= 0xFFFFFF || newColor == currentColor) {
				ModUtils.removeColor(stack);
				return -1;
			} else {
				ModUtils.applyDyeColor(stack, newColor);
				return newColor;
			}
		}
		return currentColor;
	}

	private static void resetItemImbue(ItemStack stack, Level level, Entity entity) {
		if (!level.isClientSide) {
			ModUtils.removeColor(stack);
			ModUtils.removeImbuement(stack);
			generateDustParticles(level, entity, null, 0xFFFFFF, PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
			level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	private static int lightenColor(int color, float fraction) {
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		red = Math.min(255, (int) (red + (255 - red) * fraction));
		green = Math.min(255, (int) (green + (255 - green) * fraction));
		blue = Math.min(255, (int) (blue + (255 - blue) * fraction));
		return (red << 16) | (green << 8) | blue;
	}

	private static int lightenImbuedColor(int color, float fraction) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int minR = r / 2;
		int minG = g / 2;
		int minB = b / 2;
		r = (int) (r + (255 - r) * fraction * 0.15f);
		g = (int) (g + (255 - g) * fraction * 0.15f);
		b = (int) (b + (255 - b) * fraction * 0.15f);
		r = Math.max(minR, Math.min(255, r));
		g = Math.max(minG, Math.min(255, g));
		b = Math.max(minB, Math.min(255, b));
		return (r << 16) | (g << 8) | b;
	}

	private static int calculateStepsToWhite(int color) {
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		int maxDiff = Math.max(Math.max(255 - red, 255 - green), 255 - blue);
		return maxDiff > 0 ? (int) Math.ceil(maxDiff / 25.5) : 1;
	}

	public static void generateDustParticles(Level level, Entity entity, EquipmentSlot slot, int color, int count, double offsetX, double offsetY, double offsetZ, double speed) {
		if (!(level instanceof ServerLevel serverLevel))
			return;
		Vec3 pos;
		if (entity instanceof LivingEntity livingEntity) {
			if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
				pos = ModUtils.getHandPosition(livingEntity, slot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
			} else if (slot != null) {
				pos = getArmorPosition(livingEntity, slot);
			} else {
				pos = livingEntity.position().add(0, livingEntity.getBbHeight() * 0.6, 0);
			}
		} else {
			pos = entity.position().add(0, entity.getBbHeight() / 2, 0);
		}
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		serverLevel.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.0F), pos.x, pos.y, pos.z, count, offsetX, offsetY, offsetZ, speed);
	}

	private static Vec3 getArmorPosition(LivingEntity entity, EquipmentSlot slot) {
		double yOffset = switch (slot) {
			case HEAD -> entity.getBbHeight() * 0.9;
			case CHEST -> entity.getBbHeight() * 0.6;
			case LEGS -> entity.getBbHeight() * 0.3;
			case FEET -> entity.getBbHeight() * 0.1;
			default -> entity.getBbHeight() / 2;
		};
		return entity.position().add(0, yOffset, 0);
	}

	public static void sendUpdateToClient(ItemEntity itemEntity) {
		if (!itemEntity.level().isClientSide()) {
			List<SynchedEntityData.DataValue<?>> entityData = itemEntity.getEntityData().getNonDefaultValues();
			if (entityData != null) {
				ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(itemEntity.getId(), entityData);
				((ServerLevel) itemEntity.level()).getChunkSource().broadcast(itemEntity, packet);
			}
		}
	}

	public static void sendBackItemUpdateToClient(LivingEntity entity) {
		if (!entity.level().isClientSide()) {
			List<SynchedEntityData.DataValue<?>> entityData = entity.getEntityData().getNonDefaultValues();
			if (entityData != null) {
				ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entity.getId(), entityData);
				((ServerLevel) entity.level()).getChunkSource().broadcast(entity, packet);
			}
		}
	}
}
