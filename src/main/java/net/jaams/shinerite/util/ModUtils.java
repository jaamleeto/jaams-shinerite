package net.jaams.shinerite.util;

import org.joml.Vector3f;

import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.g;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.common.ToolActions;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ResourceLocationException;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.packet.GlowColorPacket;
import net.jaams.shinerite.handler.mechanic.InteractionHandler;
import net.jaams.shinerite.handler.mechanic.ShineriteAttackHandler;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.client.TooltipsConfig;
import net.jaams.shinerite.capability.glowingcolor.GlowingColorProvider;
import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.particle.CustomEffectParticleData;

import java.util.function.Supplier;
import java.util.function.Predicate;
import java.util.function.IntSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.BooleanSupplier;
import java.util.Set;
import java.util.Optional;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

public class ModUtils {
	public static final Logger LOGGER = LogManager.getLogger();

	/** Fallback RGB for imbuement feedback when the item has no usable color. */
	public static final int IMBUE_FALLBACK_COLOR = 0xFFFFFF;

	public static final MobEffect[] NATURAL_IMBUE_EFFECTS = {
		MobEffects.REGENERATION, MobEffects.LUCK, MobEffects.DIG_SPEED,
		MobEffects.JUMP, MobEffects.POISON, MobEffects.HEAL,
		MobEffects.BLINDNESS, MobEffects.CONFUSION, MobEffects.DAMAGE_BOOST
	};

	public static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	public static String getConfigOrNbtString(ItemStack stack, String nbtKey, Supplier<String> configValue) {
		if (stack.hasTag() && stack.getTag().contains(nbtKey, Tag.TAG_STRING)) {
			return stack.getTag().getString(nbtKey);
		}
		return configValue.get();
	}

	public static double getConfigOrNbtDouble(ItemStack stack, String nbtKey, DoubleSupplier configValue) {
		if (stack.hasTag() && stack.getTag().contains(nbtKey, Tag.TAG_DOUBLE)) {
			return stack.getTag().getDouble(nbtKey);
		}
		return configValue.getAsDouble();
	}

	public static float getConfigOrNbtFloat(ItemStack stack, String nbtKey, DoubleSupplier configValue) {
		if (stack.hasTag() && stack.getTag().contains(nbtKey, Tag.TAG_FLOAT)) {
			return stack.getTag().getFloat(nbtKey);
		}
		return (float) configValue.getAsDouble();
	}

	public static int getConfigOrNbtInt(ItemStack stack, String nbtKey, IntSupplier configValue) {
		if (stack.hasTag() && stack.getTag().contains(nbtKey, Tag.TAG_INT)) {
			return stack.getTag().getInt(nbtKey);
		}
		return configValue.getAsInt();
	}

	public static boolean getConfigOrNbtBoolean(ItemStack stack, String nbtKey, BooleanSupplier configValue) {
		if (stack.hasTag() && stack.getTag().contains(nbtKey, Tag.TAG_BYTE)) {
			return stack.getTag().getBoolean(nbtKey);
		}
		return configValue.getAsBoolean();
	}

	public static <T extends Enum<T>> T getConfigOrNbtEnum(ItemStack stack, String nbtKey, Supplier<T> configValue) {
		if (stack.hasTag() && stack.getTag().contains(nbtKey, Tag.TAG_STRING)) {
			String enumName = stack.getTag().getString(nbtKey);
			try {
				return Enum.valueOf(configValue.get().getDeclaringClass(), enumName);
			} catch (IllegalArgumentException e) {
			}
		}
		return configValue.get();
	}

	public static boolean hasDurability(ItemStack stack) {
		return stack.getMaxDamage() > 1;
	}

	public static String toRoman(int n) {
		return switch (n) {
			case 1 -> "I";
			case 2 -> "II";
			case 3 -> "III";
			case 4 -> "IV";
			case 5 -> "V";
			case 6 -> "VI";
			case 7 -> "VII";
			case 8 -> "VIII";
			case 9 -> "IX";
			case 10 -> "X";
			default -> Integer.toString(n);
		};
	}

	public static boolean isHeldByPlayer(Player player, ItemStack stack) {
		if (player == null || stack == null || stack.isEmpty()) {
			return false;
		}
		return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
	}

	public static float getPowerForTime(int durationRemaining, int minChargeTicks, int maxChargeTicks) {
		if (durationRemaining <= minChargeTicks) {
			return 0.0f;
		}
		float effectiveDuration = durationRemaining - minChargeTicks;
		float maxEffective = maxChargeTicks - minChargeTicks;
		if (maxEffective <= 0) {
			return 1.0f;
		}
		float f = effectiveDuration / maxEffective;
		f = (f * f + f * 2.0f) / 3.0f;
		return Math.min(f, 1.0f);
	}

	public static float[] generateShotPitches(RandomSource random, int projectileCount) {
		float[] pitches = new float[projectileCount];
		for (int i = 0; i < projectileCount; i++) {
			boolean flag = random.nextBoolean();
			pitches[i] = 1.0F / (random.nextFloat() * 0.5F + 1.8F) + (flag ? 0.63F : 0.43F);
		}
		return pitches;
	}

	public static void applyRecoil(LivingEntity entity, float recoilDistance, float crouchRecoilReduction, float verticalRecoilMultiplier) {
		if (entity == null || recoilDistance <= 0.0F) {
			return;
		}
		double recoilMultiplier = recoilDistance;
		if (entity instanceof Player player && player.isCrouching() && !player.isCreative()) {
			recoilMultiplier *= crouchRecoilReduction;
			if (!player.isCreative()) {
				player.getFoodData().addExhaustion(0.2F);
			}
		} else if (entity instanceof Player player && !player.isCreative()) {
			player.getFoodData().addExhaustion(0.5F);
		}
		double knockbackResistance = Math.max(0.0, Math.min(1.0, entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
		double knockbackReduction = 1.0 - Math.pow(1.0 - knockbackResistance, 4.0);
		recoilMultiplier *= (1.0 - knockbackReduction);
		double movementSpeed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
		double baseMovementSpeed = 0.1;
		double movementSpeedMultiplier = Math.max(0.1, movementSpeed / baseMovementSpeed);
		recoilMultiplier *= movementSpeedMultiplier;
		double horizontalRecoil = 1.2 * recoilMultiplier;
		double verticalRecoil = 1.2 * verticalRecoilMultiplier * recoilMultiplier;
		horizontalRecoil = Math.max(0.0, horizontalRecoil);
		verticalRecoil = Math.max(0.0, verticalRecoil);
		if (horizontalRecoil < 0.01 && verticalRecoil < 0.01) {
			return;
		}
		float yaw = entity.getYHeadRot() * ((float) Math.PI / 180F);
		float pitch = (entity instanceof Player player) ? player.getXRot() * ((float) Math.PI / 180F) : 0.0F; // Default pitch for mobs
		double motionX = Math.sin(yaw) * horizontalRecoil * Math.cos(pitch);
		double motionZ = -Math.cos(yaw) * horizontalRecoil * Math.cos(pitch);
		double motionY = 0.0;
		final float MIN_PITCH_FOR_VERTICAL_DEGREES = 45.0F;
		float minPitchForVertical = (float) Math.toRadians(MIN_PITCH_FOR_VERTICAL_DEGREES);
		double horizontalScale = 1.0;
		if (pitch > minPitchForVertical) {
			float pitchFactor = (pitch - minPitchForVertical) / ((float) Math.PI / 2 - minPitchForVertical);
			pitchFactor = Math.min(1.0f, pitchFactor);
			double verticalScale = Math.cos(pitchFactor * Math.PI / 2);
			motionY = verticalRecoil * (1.0 - verticalScale);
			horizontalScale = Math.max(0.5, 1.0 - pitchFactor * 0.5);
			motionX *= horizontalScale;
			motionZ *= horizontalScale;
		}
		motionX = Mth.clamp(motionX, -1.5, 1.5);
		motionY = Mth.clamp(motionY, -1.5 * verticalRecoilMultiplier, 1.5 * verticalRecoilMultiplier);
		motionZ = Mth.clamp(motionZ, -1.5, 1.5);
		entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
		entity.hurtMarked = true;
		if (entity instanceof ServerPlayer serverPlayer) {
			serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(entity));
		} else if (!entity.level().isClientSide) {
			entity.level().getEntities(null, entity.getBoundingBox().inflate(10.0)).stream().filter(e -> e instanceof ServerPlayer).forEach(e -> ((ServerPlayer) e).connection.send(new ClientboundSetEntityMotionPacket(entity)));
		}
	}

	public static double calculateDamage(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 1.0;
		}
		double base = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().filter(m -> m.getOperation() == AttributeModifier.Operation.ADDITION).mapToDouble(AttributeModifier::getAmount).sum();
		if (base <= 0.0) {
			base = 1.0;
		}
		base += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
		return base + 1.0;
	}

	public static boolean isShineriteItem(ItemStack stack) {
		return stack.is(ModTags.SHINERITE);
	}

	public static boolean isBlockableItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (stack.hasTag() && stack.getTag().contains("Blockable")) {
			return stack.getTag().getBoolean("Blockable");
		}
		if (stack.getUseAnimation() == UseAnim.BLOCK) {
			return true;
		}
		Item item = stack.getItem();
		if (item instanceof ShieldItem || item.canPerformAction(stack, ToolActions.SHIELD_BLOCK)) {
			return true;
		}
		return false;
	}

	public static boolean isImbueableItem(ItemStack stack) {
		if (!MechanicsConfig.IMBUING.get()) {
			return false;
		}
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("Imbueable")) {
			return tag.getBoolean("Imbueable");
		}
		if (MechanicsConfig.IMBUABLE_WEAPONS.get() && stack.is(ModTags.SHINERITE_WEAPONS)) return true;
		if (MechanicsConfig.IMBUABLE_FOOD.get() && stack.is(ModTags.SHINERITE_FOOD)) return true;
		if (MechanicsConfig.IMBUABLE_ARMOR.get() && stack.is(ModTags.SHINERITE_ARMOR)) return true;
		if (MechanicsConfig.IMBUABLE_BLOCKS.get() && stack.is(ModTags.SHINERITE_BLOCKS)) return true;
		if (MechanicsConfig.IMBUABLE_MATERIALS.get() && stack.is(ModTags.SHINERITE_MATERIALS)) return true;
		if (MechanicsConfig.IMBUABLE_MUSIC_DISCS.get() && stack.is(ModTags.SHINERITE_MUSIC_DISCS)) return true;
		return false;
	}

	public static boolean isCoatableItem(ItemStack stack) {
		if (!MechanicsConfig.COATING.get()) {
			return false;
		}
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("coatable")) {
			return tag.getBoolean("coatable");
		}
		return stack.is(ModTags.COATABLES);
	}

	public static boolean isDyeableItem(ItemStack stack) {
		if (!MechanicsConfig.DYEING.get()) {
			return false;
		}
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("Dyeable")) {
			return tag.getBoolean("Dyeable");
		}
		return stack.is(ModTags.DYEABLES);
	}

	public static boolean isWashableItem(ItemStack stack) {
		if (!MechanicsConfig.WASHING.get()) {
			return false;
		}
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("Washable")) {
			return tag.getBoolean("Washable");
		}
		return stack.is(ModTags.WASHABLES);
	}

	public static boolean isImbued(ItemStack stack) {
		if (!MechanicsConfig.IMBUING.get()) {
			return false;
		}
		CompoundTag tag = stack.getTag();
		return tag != null && tag.contains("ImbuedEffects", Tag.TAG_LIST);
	}

	public static boolean isCoated(ItemStack stack) {
		if (!MechanicsConfig.COATING.get()) {
			return false;
		}
		return stack.getOrCreateTag().getBoolean("Coated");
	}

	public static Component formatDuration(int ticks, ChatFormatting color) {
		int totalSeconds = ticks / 20;
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		String formattedTime = minutes > 0 ? String.format("%02d:%02d", minutes, seconds) : String.format("00:%02d", seconds);
		return Component.literal(formattedTime).withStyle(color);
	}

	public static void applyGlowingColorAndEffect(Player player, ItemStack stack, Level level, int duration) {
		int color = ModUtils.getCurrentColor(stack);
		player.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0));
		player.getCapability(GlowingColorProvider.GLOWING_COLOR).ifPresent(cap -> {
			cap.setColor(color);
			if (level instanceof ServerLevel serverLevel) {
				JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new GlowColorPacket(player.getId(), color));
			}
		});
	}

	public static void applyPoisonWithParticles(Level level, Player player, int color, int durationTicks) {
		player.addEffect(new MobEffectInstance(MobEffects.POISON, durationTicks, 0));
		if (level instanceof ServerLevel serverLevel) {
			float r = ((color >> 16) & 255) / 255.0F;
			float g = ((color >> 8) & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			Vector3f look = player.getLookAngle().toVector3f().mul(0.5f);
			double x = player.getX() + look.x();
			double y = player.getEyeY() - 0.2;
			double z = player.getZ() + look.z();
			for (int i = 0; i < 5; i++) {
				serverLevel.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.3f), x, y, z, 1, serverLevel.random.nextGaussian() * 0.02, serverLevel.random.nextGaussian() * 0.02, serverLevel.random.nextGaussian() * 0.02, 0.0);
			}
		}
	}

	public static void applyImbuedEffect(ItemStack stack, Level level, LivingEntity entity) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
			return;
		}
		ListTag effectsTag = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
		if (effectsTag.isEmpty()) {
			return;
		}
		int uses = tag.getInt("ImbuedUses");
		int maxUses = tag.getInt("MaxImbuedUses");
		float fraction = (uses > 0 && maxUses > 0) ? Math.min(1.0F, (float) uses / maxUses) : 1.0F;
		int minDuration = MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get();
		boolean applied = ShineriteAttackHandler.applyImbuedEffects(effectsTag, null, entity, fraction, minDuration, false);
		if (!applied) {
			return;
		}
		int imbuedColor = tag.getInt("ImbuedColor");
		if (level instanceof ServerLevel serverLevel && imbuedColor != InteractionHandler.DEFAULT_COLOR) {
			float r = ((imbuedColor >> 16) & 255) / 255.0F;
			float g = ((imbuedColor >> 8) & 255) / 255.0F;
			float b = (imbuedColor & 255) / 255.0F;
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.3f), entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(), 12, 0.3, 0.3, 0.3, 0.0);
		}
	}

	public static void handleItemBreak(ItemStack itemStack, LivingEntity entity, float breakChance, int particleCount, String soundEvent) {
		if (entity == null || itemStack == null || entity.level().isClientSide() || !(entity.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (serverLevel.random.nextFloat() < breakChance && itemStack.getCount() > 0) {
			ItemStack particleStack = itemStack.copy();
			boolean isMainHand = ItemStack.isSameItem(itemStack, entity.getMainHandItem());
			entity.swing(isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
			Vec3 lookVec = entity.getLookAngle().normalize();
			Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
			float offset = isMainHand ? 0.15f : -0.15f;
			double startX = entity.getX() + rightVec.x * offset;
			double startY = entity.getEyeY();
			double startZ = entity.getZ() + rightVec.z * offset;
			Vec3 startPos = new Vec3(startX, startY, startZ);
			float particleDistance = 0.5f;
			Vec3 endPos = startPos.add(lookVec.scale(particleDistance));
			ClipContext clipContext = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
			BlockHitResult blockHit = entity.level().clip(clipContext);
			EntityHitResult entityHit = ModUtils.getEntityHitResult(serverLevel, entity, startPos, endPos);
			double adjustedDistance = particleDistance;
			if (blockHit.getType() != HitResult.Type.MISS) {
				adjustedDistance = Math.min(adjustedDistance, startPos.distanceTo(blockHit.getLocation()));
			}
			if (entityHit != null) {
				adjustedDistance = Math.min(adjustedDistance, startPos.distanceTo(entityHit.getLocation()));
			}
			adjustedDistance = Math.max(adjustedDistance - 0.1f, 0.1f);
			double particleX = startX + lookVec.x * adjustedDistance;
			double particleY = startY + lookVec.y * adjustedDistance;
			double particleZ = startZ + lookVec.z * adjustedDistance;
			ParticleOptions particle = new ItemParticleOption(ParticleTypes.ITEM, particleStack);
			serverLevel.sendParticles(particle, particleX, particleY, particleZ, particleCount, 0.1d, 0.1d, 0.1d, 0.05d);
			SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(soundEvent));
			if (sound != null) {
				serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.HOSTILE, 1.0F, 1.0F); // Cambiado a HOSTILE
			}
			itemStack.shrink(1);
		}
	}

	public static EntityHitResult getEntityHitResult(ServerLevel level, LivingEntity shooter, Vec3 startPos, Vec3 endPos) {
		AABB aabb = new AABB(startPos, endPos).inflate(1.0);
		for (Entity entity : level.getEntities(shooter, aabb, e -> e instanceof LivingEntity && e.isPickable())) {
			AABB entityBox = entity.getBoundingBox().inflate(0.3);
			Optional<Vec3> hit = entityBox.clip(startPos, endPos);
			if (hit.isPresent()) {
				return new EntityHitResult(entity, hit.get());
			}
		}
		return null;
	}

	public static boolean isProjectileCritical(Player player, float power) {
		boolean attackPowerFull = power >= 1.0F;
		boolean inAir = !player.onGround();
		boolean hasFallen = player.fallDistance > 0.0F;
		boolean notClimbing = !player.onClimbable();
		boolean notWet = !player.isInWater();
		boolean notBlind = !player.hasEffect(MobEffects.BLINDNESS);
		boolean notRiding = !player.isPassenger();
		boolean notSprinting = !player.isSprinting();
		return attackPowerFull && inAir && hasFallen && notClimbing && notWet && notBlind && notRiding && notSprinting;
	}

	public static boolean canAddTooltip(ItemStack stack, boolean requireCtrl, boolean requireAlt, List<? extends String> extraExclusions) {
		if (!TooltipsConfig.TOOLTIPS.get())
			return false;
		if (isItemExcluded(stack, TooltipsConfig.EXCLUDED_TOOLTIPS_ITEMS.get()))
			return false;
		if (requireCtrl && !TooltipsConfig.CONTROL_TOOLTIPS.get())
			return false;
		if (requireAlt && !TooltipsConfig.ALT_TOOLTIPS.get())
			return false;
		if (extraExclusions != null && !extraExclusions.isEmpty() && isItemExcluded(stack, extraExclusions)) {
			return false;
		}
		return true;
	}

	public static void addTrait(ItemStack stack, List<Component> tooltip, String traitKey, String descKey) {
		if (!canAddTooltip(stack, false, false, null))
			return;
		tooltip.add(Component.translatable(traitKey).withStyle(ChatFormatting.GOLD));
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.GRAY));
		}
	}

	public static void addNegativeTrait(ItemStack stack, List<Component> tooltip, String traitKey, String descKey) {
		if (!canAddTooltip(stack, false, false, null))
			return;
		tooltip.add(Component.translatable(traitKey).withStyle(ChatFormatting.RED));
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.GRAY));
		}
	}

	public static void addProjectileTrait(ItemStack stack, List<Component> tooltip, String traitKey, String descKey) {
		if (!canAddTooltip(stack, false, false, null))
			return;
		tooltip.add(Component.translatable(traitKey).withStyle(ChatFormatting.YELLOW));
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.GRAY));
		}
	}

	public static void addPropertyTrait(ItemStack stack, List<Component> tooltip, TooltipFlag flag, String propertyKey, String descKey) {
		if (!canAddTooltip(stack, false, false, null))
			return;
		if (!flag.isAdvanced())
			return;
		tooltip.add(Component.translatable(propertyKey).withStyle(ChatFormatting.AQUA));
		if (Screen.hasShiftDown()) {
			tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.GRAY));
		}
	}

	public static void addExtraInfo(ItemStack stack, List<Component> tooltip, String key, ChatFormatting... styles) {
		if (!canAddTooltip(stack, true, false, TooltipsConfig.EXCLUDED_CONTROL_TOOLTIPS_ITEMS.get()))
			return;
		tooltip.add(Component.translatable(key).withStyle(styles));
	}

	public static void addStat(ItemStack stack, List<Component> tooltip, String key, double value) {
		if (!canAddTooltip(stack, true, false, TooltipsConfig.EXCLUDED_CONTROL_TOOLTIPS_ITEMS.get()))
			return;
		tooltip.add(Component.translatable("tooltip.jaams_shinerite.properties." + key, value).withStyle(ChatFormatting.GRAY));
	}

	public static void addLongDescription(ItemStack stack, List<Component> tooltip, String key, ChatFormatting... styles) {
		if (!canAddTooltip(stack, false, true, TooltipsConfig.EXCLUDED_ALT_TOOLTIPS_ITEMS.get()))
			return;
		tooltip.add(Component.translatable(key).withStyle(styles));
	}

	public static void addLongDescriptionComponent(ItemStack stack, List<Component> tooltip, Component component) {
		if (!canAddTooltip(stack, false, true, TooltipsConfig.EXCLUDED_ALT_TOOLTIPS_ITEMS.get()))
			return;
		tooltip.add(component);
	}

	public static boolean isItemExcluded(ItemStack stack, List<? extends String> excludedList) {
		if (stack == null || stack.isEmpty() || excludedList == null || excludedList.isEmpty()) {
			return false;
		}
		ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (itemKey == null) {
			return false;
		}
		Set<String> excludedSet = new HashSet<>(excludedList);
		return ModUtils.matchesList(excludedSet, itemKey, stack.getItem(), false);
	}

	public static boolean isItemWeapon(ItemStack itemStack) {
		return itemStack.getItem().getDefaultAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE);
	}

	public static boolean hasRestrictedEffect(LivingEntity entity) {
		return entity.hasEffect(BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("jaams_weaponry", "knocked_up"))) || entity.hasEffect(BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.fromNamespaceAndPath("jaams_weaponry", "incapable")));
	}

	public static boolean hasSmeltingEnchantment(String enchantmentIdStr) {
		return enchantmentIdStr.equals("tonsofenchants:smelting") || enchantmentIdStr.equals("feur_magic:smelting") || enchantmentIdStr.equals("auto_smelting:auto_smelt");
	}

	public static boolean isProjectileBreakableBlock(BlockState blockState) {
		Block block = blockState.getBlock();
		return block == Blocks.POINTED_DRIPSTONE || block == Blocks.SCAFFOLDING;
	}

	public static void playSound(Entity entity, String soundEvent) {
		if (!(entity.level() instanceof ServerLevel level))
			return;
		BlockPos pos = entity.blockPosition();
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(soundEvent));
		if (sound != null) {
			level.playSound(null, pos, sound, SoundSource.PLAYERS, 1.0f, 1.0f);
		}
	}

	public static void playSound(Entity entity, String soundEvent, SoundSource source) {
		if (!(entity.level() instanceof ServerLevel level))
			return;
		BlockPos pos = entity.blockPosition();
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(soundEvent));
		if (sound != null) {
			level.playSound(null, pos, sound, source, 1.0f, 1.0f);
		}
	}

	public static void playSound(Entity entity, String soundEvent, SoundSource source, float volume, float pitch) {
		if (!(entity.level() instanceof ServerLevel level))
			return;
		BlockPos pos = entity.blockPosition();
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(soundEvent));
		if (sound != null) {
			level.playSound(null, pos, sound, source, volume, pitch);
		}
	}

	public static void playClientSound(Entity entity, String soundEvent) {
		if (!(entity.level() instanceof ClientLevel level))
			return;
		BlockPos pos = entity.blockPosition();
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(soundEvent));
		if (sound != null) {
			level.playLocalSound(pos, sound, SoundSource.PLAYERS, 1.0f, 1.0f, false);
		}
	}

	public static Vec3 getHandPosition(LivingEntity entity, InteractionHand hand) {
		boolean isMainHand = hand == InteractionHand.MAIN_HAND;
		Vec3 eyePosition = entity.getEyePosition(1.0F);
		Vec3 lookDirection = entity.getLookAngle();
		float handOffset = isMainHand ? 0.3F : -0.3F;
		return eyePosition.add(lookDirection.cross(new Vec3(0, 1, 0)).scale(handOffset)).add(lookDirection.scale(1.0)).subtract(0, 0.15, 0.12);
	}

	public static ItemStack getItemInMainHand(LivingEntity entity, Item item) {
		ItemStack mainHand = entity.getMainHandItem();
		return mainHand.is(item) ? mainHand : ItemStack.EMPTY;
	}

	public static ItemStack getItemInEitherHand(LivingEntity entity, Item item) {
		ItemStack mainHand = entity.getMainHandItem();
		ItemStack offHand = entity.getOffhandItem();
		return mainHand.is(item) ? mainHand : offHand.is(item) ? offHand : ItemStack.EMPTY;
	}

	public static ItemStack getItemInEitherHand(LivingEntity entity, Predicate<ItemStack> predicate) {
		ItemStack mainHand = entity.getMainHandItem();
		if (!mainHand.isEmpty() && predicate.test(mainHand)) {
			return mainHand;
		}
		ItemStack offHand = entity.getOffhandItem();
		if (!offHand.isEmpty() && predicate.test(offHand)) {
			return offHand;
		}
		return ItemStack.EMPTY;
	}

	public static Vec3 getDirectionToTarget(LivingEntity entity, LivingEntity target) {
		return new Vec3(target.getX() - entity.getX(), target.getEyeY() - entity.getEyeY(), target.getZ() - entity.getZ()).normalize();
	}

	public static boolean canThrowAtTarget(LivingEntity entity, LivingEntity target, double minDistance, double maxDistance) {
		double distance = target.distanceTo(entity);
		Vec3 direction = getDirectionToTarget(entity, target);
		Vec3 entityLook = entity.getLookAngle().normalize();
		double angle = direction.dot(entityLook);
		return distance <= maxDistance && distance >= minDistance && angle > 0.5;
	}

	public static boolean isItemInCuriosSlot(Entity entity, String itemId, String slotIdentifier) {
		if (entity == null || itemId == null || slotIdentifier == null) {
			return false;
		}
		CompoundTag entityData = new CompoundTag();
		entity.saveWithoutId(entityData);
		CompoundTag forgeCaps = entityData.getCompound("ForgeCaps");
		if (!forgeCaps.contains("curios:inventory")) {
			return false;
		}
		CompoundTag curiosInventory = forgeCaps.getCompound("curios:inventory");
		ListTag curiosList = curiosInventory.getList("Curios", CompoundTag.TAG_COMPOUND);
		for (int i = 0; i < curiosList.size(); i++) {
			CompoundTag curioSlot = curiosList.getCompound(i);
			String identifier = curioSlot.getString("Identifier");
			if (slotIdentifier.equals(identifier)) {
				CompoundTag stacksHandler = curioSlot.getCompound("StacksHandler");
				CompoundTag stacks = stacksHandler.getCompound("Stacks");
				ListTag items = stacks.getList("Items", CompoundTag.TAG_COMPOUND);
				for (int j = 0; j < items.size(); j++) {
					CompoundTag item = items.getCompound(j);
					String id = item.getString("id");
					if (itemId.equals(id)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isEntityInBattleMode(Entity entity) {
		if (entity == null) {
			return false;
		}
		CompoundTag entityData = new CompoundTag();
		entity.saveWithoutId(entityData);
		CompoundTag forgeCaps = entityData.getCompound("ForgeCaps");
		if (forgeCaps.contains("epicfight:skill_cap")) {
			CompoundTag skillCap = forgeCaps.getCompound("epicfight:skill_cap");
			String playerMode = skillCap.getString("playerMode");
			return "EPICFIGHT".equals(playerMode) || "BATTLE".equals(playerMode);
		}
		return false;
	}

	public static boolean isDyed(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			return false;
		}
		if (tag.contains("color", Tag.TAG_INT)) {
			return true;
		}
		if (tag.contains("display", Tag.TAG_COMPOUND)) {
			CompoundTag display = tag.getCompound("display");
			if (display.contains("color", Tag.TAG_INT)) {
				return true;
			}
		}
		return false;
	}

	public static void applyDyeColor(ItemStack itemStack, int color) {
		if (itemStack.isEmpty()) {
			return;
		}
		Item item = itemStack.getItem();
		if (item instanceof net.minecraft.world.item.DyeableLeatherItem dyeableItem) {
			dyeableItem.setColor(itemStack, color);
			return;
		}
		if (item instanceof IDyeableItem dyeableItem) {
			dyeableItem.setColor(itemStack, color);
			return;
		}
		CompoundTag tag = itemStack.getOrCreateTag();
		tag.putInt("color", color);
	}

	public static int getCurrentColor(ItemStack stack) {
		if (stack.isEmpty()) {
			return -1;
		}
		Item item = stack.getItem();
		if (item instanceof IDyeableItem dyeableItem) {
			return dyeableItem.getColor(stack);
		}
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			return -1;
		}
		if (tag.contains("color", Tag.TAG_INT)) {
			return tag.getInt("color");
		}
		if (tag.contains("display", Tag.TAG_COMPOUND)) {
			CompoundTag display = tag.getCompound("display");
			if (display.contains("color", Tag.TAG_INT)) {
				return display.getInt("color");
			}
		}
		return -1;
	}

	public static void removeColor(ItemStack itemStack) {
		CompoundTag tag = itemStack.getOrCreateTag();
		tag.remove("color");
		if (tag.contains("display", Tag.TAG_COMPOUND)) {
			CompoundTag display = tag.getCompound("display");
			display.remove("color");
			if (display.isEmpty()) {
				tag.remove("display");
			}
		}
		tag.remove("ImbuedColor");
	}

	public static int getImbuedColor(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("ImbuedColor", Tag.TAG_INT)) {
			return -1;
		}
		return tag.getInt("ImbuedColor");
	}

	/** Safe RGB color used for imbuement feedback particles; never white/transparent. */
	public static int getImbuedParticleColor(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return IMBUE_FALLBACK_COLOR;
		}
		int currentColor = getCurrentColor(stack);
		if (isValidParticleColor(currentColor)) {
			return currentColor & 0xFFFFFF;
		}
		int imbuedColor = getImbuedColor(stack);
		if (isValidParticleColor(imbuedColor)) {
			return imbuedColor & 0xFFFFFF;
		}
		return IMBUE_FALLBACK_COLOR;
	}

	private static boolean isValidParticleColor(int color) {
		return color != -1 && color >= 0 && color <= 0xFFFFFF;
	}

	public static void removeImbuement(ItemStack stack) {
		if (!stack.hasTag()) {
			return;
		}
		CompoundTag tag = stack.getTag();
		tag.remove("ImbuedEffects");
		tag.remove("ImbuedUses");
		tag.remove("MaxImbuedUses");
		tag.remove("ImbuedColor");
	}

	public static boolean isValidWaterPotion(ItemStack potionStack) {
		Item potionItem = potionStack.getItem();
		if (potionItem == Items.SPLASH_POTION || potionItem == Items.LINGERING_POTION) {
			Potion thrownPotion = PotionUtils.getPotion(potionStack);
			return thrownPotion == Potions.WATER || thrownPotion == Potions.AWKWARD || thrownPotion == Potions.THICK || thrownPotion == Potions.MUNDANE || thrownPotion.getEffects().isEmpty();
		}
		return false;
	}

	public static boolean matchesList(Set<String> set, ResourceLocation itemKey, Item item, boolean isWhitelist) {
		if (set == null || set.isEmpty()) {
			return false;
		}
		for (String entry : set) {
			entry = entry.trim();
			if (entry.isEmpty()) {
				continue;
			}
			if (entry.equals("all")) {
				if (ForgeRegistries.ITEMS.containsKey(itemKey)) {
					return true;
				}
				continue;
			}
			if (entry.startsWith("#")) {
				try {
					String tagName = entry.substring(1);
					TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagName));
					if (item.builtInRegistryHolder().is(tag)) {
						return true;
					}
				} catch (ResourceLocationException e) {
					LOGGER.warn("Invalid tag entry in list (ignoring): '{}'", entry);
				}
				continue;
			}
			if (entry.equals("bows:*")) {
				if (item instanceof BowItem) {
					return true;
				}
			} else if (entry.equals("crossbows:*")) {
				if (item instanceof CrossbowItem) {
					return true;
				}
			} else if (entry.equals("swords:*")) {
				if (item instanceof SwordItem) {
					return true;
				}
			} else if (entry.equals("pickaxes:*")) {
				if (item instanceof PickaxeItem) {
					return true;
				}
			} else if (entry.equals("axes:*")) {
				if (item instanceof AxeItem) {
					return true;
				}
			} else if (entry.equals("blocks:*")) {
				if (item instanceof BlockItem) {
					return true;
				}
			} else if (entry.equals("edibles:*")) {
				if (item.getFoodProperties() != null) {
					return true;
				}
			} else if (entry.equals("shovels:*")) {
				if (item instanceof ShovelItem) {
					return true;
				}
			} else if (entry.equals("hoes:*")) {
				if (item instanceof HoeItem) {
					return true;
				}
			} else if (entry.equals("armor:*")) {
				if (item instanceof ArmorItem) {
					return true;
				}
			} else if (entry.contains(":*")) {
				String[] parts = entry.split(":");
				if (parts.length == 2 && parts[1].equals("*")) {
					String namespace = parts[0];
					if (itemKey.getNamespace().equals(namespace)) {
						return true;
					}
				}
			} else if (entry.contains("*")) {
				String[] parts = entry.split(":");
				if (parts.length == 2) {
					String namespace = parts[0];
					String pattern = parts[1].replace("*", "");
					if (itemKey.getNamespace().equals(namespace) && itemKey.getPath().contains(pattern)) {
						return true;
					}
				} else if (parts.length == 1) {
					String pattern = parts[0].replace("*", "");
					if (itemKey.getPath().contains(pattern)) {
						return true;
					}
				} else {
					LOGGER.warn("Invalid wildcard pattern (ignoring): '{}'. Expected format: 'namespace:pattern' or 'pattern'", entry);
				}
			} else {
				try {
					ResourceLocation resourceLocation = ResourceLocation.parse(entry);
					if (ForgeRegistries.ITEMS.containsKey(resourceLocation) && itemKey.equals(resourceLocation)) {
						return true;
					}
				} catch (ResourceLocationException e) {
					LOGGER.warn("Invalid entry in list (ignoring): '{}'", entry);
				}
			}
		}
		return false;
	}

	/**
	 * The color names this mod understands, in the order used by {@code /jaams_shinerite color set}.
	 * Kept here so the command and the data driven loaders share a single palette.
	 */
	public static final String[] SUPPORTED_COLOR_NAMES = {"red", "green", "blue", "white", "black", "yellow", "purple",
			"cyan", "orange", "pink", "lime", "magenta", "gray", "light_gray", "brown", "gold", "aqua", "violet",
			"indigo", "teal"};

	/**
	 * Resolves a color name such as {@code "red"} or a hex value such as {@code "#8B4513"} into a packed
	 * RGB int. Throws {@link IllegalArgumentException} for anything the mod does not know.
	 */
	public static int parseColor(String input) {
		String s = input.toLowerCase(java.util.Locale.ROOT).trim();
		if (s.startsWith("#") || s.startsWith("0x")) {
			String hex = s.replaceFirst("^(#|0x)", "");
			if (hex.length() == 6 && hex.matches("[0-9a-fA-F]{6}")) {
				return Integer.parseUnsignedInt(hex, 16) & 0xFFFFFF;
			}
			throw new IllegalArgumentException();
		}
		return switch (s) {
			case "red" -> 0xFF0000;
			case "green" -> 0x00FF00;
			case "blue" -> 0x0000FF;
			case "white" -> 0xFFFFFF;
			case "black" -> 0x000000;
			case "yellow" -> 0xFFFF00;
			case "purple" -> 0x800080;
			case "cyan" -> 0x00FFFF;
			case "orange" -> 0xFFA500;
			case "pink" -> 0xFFC1CC;
			case "lime" -> 0x32CD32;
			case "magenta" -> 0xFF00FF;
			case "gray" -> 0x808080;
			case "light_gray" -> 0xD3D3D3;
			case "brown" -> 0x8B4513;
			case "gold" -> 0xFFD700;
			case "aqua" -> 0x00CED1;
			case "violet" -> 0xEE82EE;
			case "indigo" -> 0x4B0082;
			case "teal" -> 0x008080;
			default -> throw new IllegalArgumentException();
		};
	}

	/** Returns a uniformly random color from {@link #SUPPORTED_COLOR_NAMES}. */
	public static int randomSupportedColor(net.minecraft.util.RandomSource random) {
		return parseColor(SUPPORTED_COLOR_NAMES[random.nextInt(SUPPORTED_COLOR_NAMES.length)]);
	}

	public static int lightenColor(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		r = Math.min(255, r + 50);
		g = Math.min(255, g + 50);
		b = Math.min(255, b + 50);
		return (r << 16) | (g << 8) | b;
	}

	public static int darkenColor(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		r = Math.max(0, r - 50);
		g = Math.max(0, g - 50);
		b = Math.max(0, b - 50);
		return (r << 16) | (g << 8) | b;
	}

	public static void reduceItemStack(Player player, ItemStack itemstack) {
		if (itemstack.isDamageableItem() && itemstack.getMaxStackSize() == 1) {
			itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
		} else {
			itemstack.shrink(1);
		}
	}

	public static ItemStack reduceDispenserItemStack(BlockSource source, ItemStack itemstack) {
		if (itemstack.isDamageableItem() && itemstack.getMaxStackSize() == 1) {
			if (itemstack.hurt(1, source.getLevel().random, null)) {
				itemstack.setCount(0);
			}
		} else {
			itemstack.shrink(1);
		}
		return itemstack;
	}

	public static void generateDustParticles(Level world, BlockPos pos, int color) {
		if (world instanceof ServerLevel serverLevel) {
			float r = ((color >> 16) & 255) / 255.0F;
			float g = ((color >> 8) & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.0F), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
		}
	}

	public static void generateCoatedParticles(Level world, BlockPos pos, BlockState state, RandomSource random) {
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}
		VoxelShape shape = state.getShape(world, pos);
		AABB aabb = shape.bounds().inflate(0.2);
		for (int i = 0; i < 20; i++) {
			double x = pos.getX() + aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ);
			double xSpeed = (random.nextDouble() - 0.5) * 0.02;
			double ySpeed = (random.nextDouble() - 0.5) * 0.02;
			double zSpeed = (random.nextDouble() - 0.5) * 0.02;
			serverWorld.sendParticles(ParticleTypes.WAX_ON, x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.25);
		}
	}

	public static void generateCoatedRemovalParticles(Level world, BlockPos pos, BlockState state, RandomSource random) {
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}
		VoxelShape shape = state.getShape(world, pos);
		AABB aabb = shape.bounds().inflate(0.2);
		for (int i = 0; i < 20; i++) {
			double x = pos.getX() + aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ);
			double xSpeed = (random.nextDouble() - 0.5) * 0.02;
			double ySpeed = (random.nextDouble() - 0.5) * 0.02;
			double zSpeed = (random.nextDouble() - 0.5) * 0.02;
			serverWorld.sendParticles(ParticleTypes.WAX_OFF, x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.25);
		}
	}

	public static void applyDye(Level world, BlockPos pos, BlockState state, int color) {
		if (!(world.getBlockEntity(pos) instanceof BaseShineriteBlockEntity blockEntity)) {
			return;
		}
		blockEntity.setColor(color);
		blockEntity.setChanged();
		world.sendBlockUpdated(pos, state, state, 3);
		world.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}
		VoxelShape shape = state.getShape(world, pos);
		AABB aabb = shape.bounds().inflate(0.25);
		RandomSource random = serverWorld.random;
		float[] colorComponents = new float[]{(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F};
		for (int i = 0; i < 50; i++) {
			double x = pos.getX() + aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ);
			serverWorld.sendParticles(new DustParticleOptions(new Vector3f(colorComponents[0], colorComponents[1], colorComponents[2]), 1.0F), x, y, z, 1, 0.0, 0.1, 0.0, 0.0);
		}
	}

	public static boolean imbueBlockWithThrownPotion(Level world, BlockPos pos, BaseShineriteBlockEntity blockEntity, ItemStack potionStack) {
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.BLOCK_IMBUING.get())
			return false;
		if (blockEntity.isImbued() || blockEntity.getColor() != -1 || (blockEntity.isCoated() && !MechanicsConfig.IMBUED_BLOCK_COATED_CAN_IMBUE.get()))
			return false;
		Potion potion = PotionUtils.getPotion(potionStack);
		if (potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion == Potions.MUNDANE || potion.getEffects().isEmpty())
			return false;
		List<MobEffectInstance> effects = new ArrayList<>(potion.getEffects());
		effects.addAll(PotionUtils.getCustomEffects(potionStack));
		if (effects.isEmpty())
			return false;
		int potionColor = PotionUtils.getColor(potionStack);
		ListTag effectsTag = new ListTag();
		for (MobEffectInstance effect : effects) {
			MobEffectInstance savedEffect = new MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 2), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
			effectsTag.add(savedEffect.save(new CompoundTag()));
		}
		blockEntity.setImbuedState(effectsTag, 1, 1, potionColor);
		world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.15F);
		if (world instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
			serverLevel.sendParticles(new CustomEffectParticleData(((potionColor >> 16) & 255) / 255.0F, ((potionColor >> 8) & 255) / 255.0F, (potionColor & 255) / 255.0F, 0.3F),
				pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.12);
		}
		return true;
	}

	public static boolean applyRandomNaturalImbuement(BaseShineriteBlockEntity blockEntity, RandomSource random) {
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.BLOCK_IMBUING.get()) {
			return false;
		}
		int count = 1 + random.nextInt(2);
		java.util.List<MobEffect> chosen = new java.util.ArrayList<>();
		int attempts = 0;
		while (chosen.size() < count && attempts < 40) {
			MobEffect effect = NATURAL_IMBUE_EFFECTS[random.nextInt(NATURAL_IMBUE_EFFECTS.length)];
			if (!chosen.contains(effect)) {
				chosen.add(effect);
			}
			attempts++;
		}
		if (chosen.isEmpty()) {
			return false;
		}
		ListTag effectsTag = new ListTag();
		int r = 0, g = 0, b = 0, n = 0;
		for (MobEffect effect : chosen) {
			boolean instant = effect.isInstantenous();
			int duration = instant ? 1 : (100 + random.nextInt(200));
			int amplifier = random.nextInt(2);
			MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier, false, true, true);
			effectsTag.add(instance.save(new CompoundTag()));
			int c = effect.getColor();
			r += (c >> 16) & 0xFF;
			g += (c >> 8) & 0xFF;
			b += c & 0xFF;
			n++;
		}
		int color = ((r / n) << 16) | ((g / n) << 8) | (b / n);
		int uses = 1 + random.nextInt(3);
		blockEntity.setColor(color);
		blockEntity.setImbuedState(effectsTag, uses, uses, color);
		return true;
	}

	/**
	 * Imbues a block entity with exactly ONE effect picked from {@code pool}.
	 *
	 * <p>Worldgen deliberately never stacks several effects on the same block: each generated
	 * Shinerite flower or root carries a single, readable effect so a player can tell at a glance
	 * which one they stumbled upon. The pool is chosen per biome so the effect always matches where
	 * the block grew.</p>
	 *
	 * @return true when the block entity ended up imbued, false when the roll failed (in which case
	 * the caller should fall back to its plain colour palette)
	 */
	public static boolean applyNaturalImbuement(BaseShineriteBlockEntity blockEntity, RandomSource random, MobEffect[] pool, double chance) {
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.BLOCK_IMBUING.get()) {
			return false;
		}
		if (pool == null || pool.length == 0 || chance <= 0.0 || random.nextFloat() >= chance) {
			return false;
		}

		MobEffect effect = pool[random.nextInt(pool.length)];
		// Instantaneous effects have no sensible "touch the block and get a buff" behaviour.
		if (effect.isInstantenous()) {
			return false;
		}

		int duration = 100 + random.nextInt(200);
		int amplifier = random.nextInt(2);
		int color = effect.getColor();
		ListTag effectsTag = new ListTag();
		effectsTag.add(new MobEffectInstance(effect, duration, amplifier, false, true, true).save(new CompoundTag()));

		blockEntity.setColor(color);
		blockEntity.setImbuedState(effectsTag, 1, 1, color);
		return true;
	}

	public static void generateClientCoatedParticles(Level world, BlockPos pos, BlockState state, RandomSource random) {
		if (!(world instanceof ClientLevel clientLevel)) {
			return;
		}
		VoxelShape shape = state.getShape(clientLevel, pos);
		AABB aabb = shape.bounds().inflate(0.2);
		for (int i = 0; i < 15; i++) {
			double x = pos.getX() + aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ);
			double xSpeed = (random.nextDouble() - 0.5) * 0.05;
			double ySpeed = (random.nextDouble() - 0.5) * 0.05;
			double zSpeed = (random.nextDouble() - 0.5) * 0.05;
			clientLevel.addParticle(ParticleTypes.WAX_ON, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}

	public static void generateBlockTransitionParticles(ServerLevel serverLevel, BlockPos pos, BlockState state, int color, int particleCount, float particleSize) {
		if (color == -1) {
			return;
		}
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		VoxelShape shape = state.getShape(serverLevel, pos);
		AABB aabb = shape.bounds().inflate(0.2);
		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + aabb.minX + serverLevel.random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + serverLevel.random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + serverLevel.random.nextDouble() * (aabb.maxZ - aabb.minZ);
			double xSpeed = (serverLevel.random.nextDouble() - 0.5) * 0.1;
			double ySpeed = (serverLevel.random.nextDouble() - 0.5) * 0.1;
			double zSpeed = (serverLevel.random.nextDouble() - 0.5) * 0.1;
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(red, green, blue), particleSize), x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.0);
		}
	}

	public static void generateDispenserDustParticles(Level world, BlockPos pos, BlockState state, Vector3f color) {
		if (!(world instanceof ServerLevel serverLevel)) {
			return;
		}
		VoxelShape shape = state.getShape(serverLevel, pos);
		AABB aabb = shape.bounds().inflate(0.2);
		RandomSource random = serverLevel.random;
		int particleCount = 20;
		float particleSize = 1.5f;
		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ);
			double xSpeed = (random.nextDouble() - 0.5) * 0.1;
			double ySpeed = (random.nextDouble() - 0.5) * 0.1;
			double zSpeed = (random.nextDouble() - 0.5) * 0.1;
			serverLevel.sendParticles(new DustParticleOptions(color, particleSize), x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.0);
		}
	}

	public static void generateRunningEffectParticles(ServerLevel serverLevel, Entity entity, int color) {
		if (color == -1) {
			return;
		}
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		float particleSize = 1.2f;
		int particleCount = 3;
		for (int i = 0; i < particleCount; i++) {
			double velocityX = (serverLevel.random.nextDouble() - 0.5) * 0.1;
			double velocityY = serverLevel.random.nextDouble() * 0.1;
			double velocityZ = (serverLevel.random.nextDouble() - 0.5) * 0.1;
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(red, green, blue), particleSize), entity.getX() + (serverLevel.random.nextDouble() - 0.5), entity.getY() + 0.1, entity.getZ() + (serverLevel.random.nextDouble() - 0.5), 1,
					velocityX, velocityY, velocityZ, 0.0);
		}
	}

	public static void generateProjectileHitParticles(ServerLevel serverLevel, Vec3 hitLocation, int color) {
		float red, green, blue;
		if (color == -1) {
			red = 1.0F;
			green = 1.0F;
			blue = 1.0F;
		} else {
			red = (color >> 16 & 255) / 255.0F;
			green = (color >> 8 & 255) / 255.0F;
			blue = (color & 255) / 255.0F;
		}
		float particleSize = 1.5f;
		int particleCount = 20;
		for (int i = 0; i < particleCount; i++) {
			double angle = serverLevel.random.nextDouble() * 2 * Math.PI;
			double speed = serverLevel.random.nextDouble() * 0.15 + 0.05;
			double velocityX = Math.cos(angle) * speed;
			double velocityY = serverLevel.random.nextDouble() * 0.2 + 0.1;
			double velocityZ = Math.sin(angle) * speed;
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(red, green, blue), particleSize), hitLocation.x(), hitLocation.y(), hitLocation.z(), 1, velocityX, velocityY, velocityZ, 0.0);
		}
	}

	public static void generateLandingEffectParticles(ServerLevel serverLevel, LivingEntity entity, int color, int numberOfParticles) {
		if (color == -1) {
			return;
		}
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		float particleSize = 1.75f;
		int particleCount = Math.max(1, numberOfParticles / 2);
		for (int i = 0; i < particleCount; i++) {
			double velocityX = (serverLevel.random.nextDouble() - 0.5) * 0.2;
			double velocityY = serverLevel.random.nextDouble() * 0.2;
			double velocityZ = (serverLevel.random.nextDouble() - 0.5) * 0.2;
			double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.75;
			double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.75;
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(red, green, blue), particleSize), entity.getX() + offsetX, entity.getY(), entity.getZ() + offsetZ, 1, velocityX, velocityY, velocityZ, 0.0);
		}
	}

	public static void generateImbuedBurstParticles(ServerLevel serverLevel, double x, double y, double z, int color, int count) {
		float red, green, blue;
		if (color == -1) {
			red = 1.0F;
			green = 1.0F;
			blue = 1.0F;
		} else {
			red = ((color >> 16) & 255) / 255.0F;
			green = ((color >> 8) & 255) / 255.0F;
			blue = (color & 255) / 255.0F;
		}
		RandomSource random = serverLevel.random;
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double radius = random.nextDouble() * 0.45;
			double offsetX = Math.cos(angle) * radius;
			double offsetZ = Math.sin(angle) * radius;
			double offsetY = random.nextDouble() * 0.15;
			double speedY = random.nextDouble() * 0.18 + 0.1;
			double driftX = Math.cos(angle) * random.nextDouble() * 0.05 + (random.nextDouble() - 0.5) * 0.03;
			double driftZ = Math.sin(angle) * random.nextDouble() * 0.05 + (random.nextDouble() - 0.5) * 0.03;
			serverLevel.sendParticles(new CustomEffectParticleData(red, green, blue, 0.2F), x + offsetX, y + offsetY, z + offsetZ, 1,
				driftX, speedY, driftZ, 0.0);
		}
		serverLevel.sendParticles(ParticleTypes.SPLASH, x, y + 0.2, z, Math.max(6, count / 4), 0.45, 0.15, 0.45, 0.05);
	}

	public static int calculateDyeColor(int baseColor, DyeItem dyeItem) {
		int[] combinedColors = new int[3];
		int maxColor = 0;
		int colorCount = 0;
		if (baseColor != -1) {
			float r = (float) (baseColor >> 16 & 255) / 255f;
			float g = (float) (baseColor >> 8 & 255) / 255f;
			float b = (float) (baseColor & 255) / 255f;
			maxColor = (int) ((float) maxColor + Math.max(r, Math.max(g, b)) * 255f);
			combinedColors[0] = (int) ((float) combinedColors[0] + r * 255f);
			combinedColors[1] = (int) ((float) combinedColors[1] + g * 255f);
			combinedColors[2] = (int) ((float) combinedColors[2] + b * 255f);
			colorCount++;
		}
		float[] colorComponents = dyeItem.getDyeColor().getTextureDiffuseColors();
		int r = (int) (colorComponents[0] * 255f);
		int g = (int) (colorComponents[1] * 255f);
		int b = (int) (colorComponents[2] * 255f);
		maxColor += Math.max(r, Math.max(g, b));
		combinedColors[0] += r;
		combinedColors[1] += g;
		combinedColors[2] += b;
		colorCount++;
		r = combinedColors[0] / colorCount;
		g = combinedColors[1] / colorCount;
		b = combinedColors[2] / colorCount;
		float avgColor = (float) maxColor / (float) colorCount;
		float maxValue = (float) Math.max(r, Math.max(g, b));
		r = (int) ((float) r * avgColor / maxValue);
		g = (int) ((float) g * avgColor / maxValue);
		b = (int) ((float) b * avgColor / maxValue);
		return (r << 16) | (g << 8) | b;
	}
}
