package net.jaams.shinerite.mixins.trait;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import org.joml.Vector3f;
import org.joml.Quaternionf;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.nbt.Tag;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.util.ModTags;
import net.jaams.shinerite.util.ModEnums;
import net.jaams.shinerite.util.ModTraits;
import net.jaams.shinerite.init.ModSounds;
import net.jaams.shinerite.entity.ShineriteStarProjectileEntity;
import net.jaams.shinerite.entity.ShinerangProjectileEntity;
import net.jaams.shinerite.data.ThrowableItemData;
import net.jaams.shinerite.configuration.common.TraitsConfig;
import net.jaams.shinerite.configuration.common.ThrowableConfig;
import net.jaams.shinerite.configuration.common.ProjectileCommonConfig;
import net.jaams.shinerite.component.projectile.BaseWeaponProjectileEntity;
import net.jaams.shinerite.component.projectile.BaseReturningProjectileEntity;

@Mixin(ItemStack.class)
public class TraitThrowableMixin {
	@Unique
	private boolean isShinerang(ItemStack stack) {
		return stack.is(ModTags.SHINERANGS) || (stack.hasTag() && stack.getTag().getBoolean("ForceShinerangThrowable"));
	}

	@Unique
	private boolean isShineriteStar(ItemStack stack) {
		return stack.is(ModTags.SHINERITE_STARS) || (stack.hasTag() && stack.getTag().getBoolean("ForceShineriteStarThrowable"));
	}

	@Unique
	private boolean isThrowableType(ItemStack stack) {
		return isShinerang(stack) || isShineriteStar(stack) || getJsonThrowableEntry(stack) != null;
	}

	@Unique
	private boolean isThrowableEnabled(ItemStack stack) {
		if (!TraitsConfig.THROWABLE.get()) {
			return false;
		}
		if (TraitModifierDataDisabled(stack)) {
			return false;
		}
		if (stack.hasTag() && stack.getTag().contains("ThrowableTrait")) {
			return stack.getTag().getBoolean("ThrowableTrait");
		}
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.throw_enabled != null) {
			return json.throw_enabled;
		}
		if (!isShinerang(stack) && !isShineriteStar(stack)) {
			return false;
		}
		if (isShinerang(stack) && !ThrowableConfig.THROWABLE_SHINERANG.get()) {
			return false;
		}
		if (isShineriteStar(stack) && !ThrowableConfig.THROWABLE_SHINERITE_STAR.get()) {
			return false;
		}
		return true;
	}

	@Unique
	private boolean TraitModifierDataDisabled(ItemStack stack) {
		return net.jaams.shinerite.data.TraitModifierData.isTraitDisabled(stack, "throwable");
	}

	@Unique
	private ThrowableItemData.ThrowableEntry getJsonThrowableEntry(ItemStack stack) {
		return ThrowableItemData.getThrowableData(stack);
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void onThrowableUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand == InteractionHand.MAIN_HAND && player.isCrouching() && ModUtils.isBlockableItem(player.getItemInHand(InteractionHand.OFF_HAND))) {
			return;
		}
		if (!isThrowableType(stack) || !isThrowableEnabled(stack)) {
			return;
		}
		if (!canThrowWithDurabilityCheck(stack)) {
			cir.setReturnValue(InteractionResultHolder.fail(stack));
			return;
		}
		ModEnums.ThrowMode mode = getThrowMode(stack);
		if (mode == ModEnums.ThrowMode.INSTANT_ON_RIGHT_CLICK) {
			throwInstant(level, player, hand, stack);
			if (!level.isClientSide) {
				player.swing(hand, true);
			}
			cir.setReturnValue(InteractionResultHolder.consume(stack));
			return;
		}
		player.startUsingItem(hand);
		cir.setReturnValue(InteractionResultHolder.consume(stack));
	}

	@Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
	private void modifyThrowableUseDuration(CallbackInfoReturnable<Integer> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		if (!isThrowableType(stack) || !isThrowableEnabled(stack)) {
			return;
		}
		cir.setReturnValue(getUseDurationTicks(stack));
	}

	@Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
	private void changeThrowableUseAnimation(CallbackInfoReturnable<UseAnim> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		if (!isThrowableType(stack) || !isThrowableEnabled(stack)) {
			return;
		}
		cir.setReturnValue(getUseAnimation(stack));
	}

	@Inject(method = "releaseUsing", at = @At("HEAD"))
	private void onReleaseThrowable(Level level, LivingEntity entity, int durationUsed, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		if (!(entity instanceof Player player))
			return;
		if (!isThrowableType(stack) || !isThrowableEnabled(stack))
			return;
		ModEnums.ThrowMode mode = getThrowMode(stack);
		if (mode != ModEnums.ThrowMode.CHARGE_AND_RELEASE && mode != ModEnums.ThrowMode.CHARGE_RELEASE_AND_FINISH) {
			return;
		}
		int remaining = stack.getUseDuration() - durationUsed;
		int minCharge = getMinChargeTicks(stack);
		int maxCharge = getMaxChargeTicks(stack);
		if (remaining < minCharge)
			return;
		float power = ModUtils.getPowerForTime(remaining, minCharge, maxCharge);
		throwCharged(level, player, player.getUsedItemHand(), stack, power);
	}

	@Inject(method = "finishUsingItem", at = @At("HEAD"))
	private void onFinishUsing(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
		if (!(entity instanceof Player player))
			return;
		ItemStack stack = player.getItemInHand(player.getUsedItemHand());
		if (!isThrowableType(stack) || !isThrowableEnabled(stack))
			return;
		ModEnums.ThrowMode mode = getThrowMode(stack);
		if (mode != ModEnums.ThrowMode.CHARGE_AND_FINISH_USING && mode != ModEnums.ThrowMode.CHARGE_RELEASE_AND_FINISH) {
			return;
		}
		throwCharged(level, player, player.getUsedItemHand(), stack, 1.0f);
	}

	@Unique
	private ModEnums.ThrowMode getThrowMode(ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains("ThrowMode", Tag.TAG_STRING)) {
			try {
				return ModEnums.ThrowMode.valueOf(stack.getTag().getString("ThrowMode").toUpperCase(java.util.Locale.ROOT));
			} catch (Exception ignored) {
			}
		}
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.throw_mode != null && !json.throw_mode.isEmpty()) {
			try {
				return ModEnums.ThrowMode.valueOf(json.throw_mode.toUpperCase(java.util.Locale.ROOT));
			} catch (Exception ignored) {
			}
		}
		if (isShinerang(stack)) {
			return ModUtils.getConfigOrNbtEnum(stack, "ThrowMode", () -> ThrowableConfig.THROWABLE_SHINERANG_THROW_MODE.get());
		}
		return ModUtils.getConfigOrNbtEnum(stack, "ThrowMode", () -> ThrowableConfig.THROWABLE_SHINERITE_STAR_THROW_MODE.get());
	}

	@Unique
	private int getUseDurationTicks(ItemStack stack) {
		Integer nbt = getIntNBT(stack, "UseDurationTicks");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.use_duration_ticks != null && json.use_duration_ticks >= 0)
			return json.use_duration_ticks;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_USE_DURATION_TICKS.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_USE_DURATION_TICKS.get();
	}

	@Unique
	private UseAnim getUseAnimation(ItemStack stack) {
		String nbtValue = getStringNBT(stack, "UseAnimation");
		if (nbtValue != null && !nbtValue.isEmpty()) {
			return parseUseAnimation(nbtValue);
		}
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.use_animation != null && !json.use_animation.isEmpty()) {
			return parseUseAnimation(json.use_animation);
		}
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_USE_ANIMATION.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_USE_ANIMATION.get();
	}

	@Unique
	private UseAnim parseUseAnimation(String animationStr) {
		if (animationStr == null || animationStr.isEmpty()) {
			return UseAnim.SPEAR;
		}
		String upper = animationStr.toUpperCase(java.util.Locale.ROOT).trim();
		return switch (upper) {
			case "BOW" -> UseAnim.BOW;
			case "CROSSBOW" -> UseAnim.CROSSBOW;
			case "SPEAR" -> UseAnim.SPEAR;
			case "NONE" -> UseAnim.NONE;
			case "EAT" -> UseAnim.EAT;
			case "DRINK" -> UseAnim.DRINK;
			case "BLOCK" -> UseAnim.BLOCK;
			case "SPYGLASS" -> UseAnim.SPYGLASS;
			default -> UseAnim.SPEAR;
		};
	}

	@Unique
	private int getMinChargeTicks(ItemStack stack) {
		Integer nbt = getIntNBT(stack, "MinChargeTicks");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.min_charge_ticks != null && json.min_charge_ticks >= 0)
			return json.min_charge_ticks;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_MIN_CHARGE.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_MIN_CHARGE.get();
	}

	@Unique
	private int getMaxChargeTicks(ItemStack stack) {
		Integer nbt = getIntNBT(stack, "MaxChargeTicks");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.max_charge_ticks != null && json.max_charge_ticks >= 0)
			return json.max_charge_ticks;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_MAX_CHARGE.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_MAX_CHARGE.get();
	}

	@Unique
	private int getInstantCooldownTicks(ItemStack stack) {
		Integer nbt = getIntNBT(stack, "InstantCooldownTicks");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.instant_cooldown_ticks != null && json.instant_cooldown_ticks >= 0)
			return json.instant_cooldown_ticks;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_COOLDOWN_TICKS_INSTANT.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_COOLDOWN_TICKS_INSTANT.get();
	}

	@Unique
	private float getMinSpeed(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "MinSpeed");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.min_speed != null && json.min_speed >= 0)
			return json.min_speed;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_MIN_SPEED.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_MIN_SPEED.get().floatValue();
	}

	@Unique
	private float getMaxSpeed(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "MaxSpeed");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.max_speed != null && json.max_speed >= 0)
			return json.max_speed;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_MAX_SPEED.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_MAX_SPEED.get().floatValue();
	}

	@Unique
	private float getInaccuracy(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "InaccuracyDegrees");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.inaccuracy != null && json.inaccuracy >= 0)
			return json.inaccuracy;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_INACCURACY.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_INACCURACY.get().floatValue();
	}

	@Unique
	private float getCriticalPowerThreshold(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "CriticalPowerThreshold");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.critical_power_threshold != null && json.critical_power_threshold >= 0)
			return json.critical_power_threshold;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_CRITICAL_POWER_THRESHOLD.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_CRITICAL_POWER_THRESHOLD.get().floatValue();
	}

	@Unique
	private int getDamageOnThrow(ItemStack stack) {
		Integer nbt = getIntNBT(stack, "DamageOnThrow");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.damage_on_throw != null && json.damage_on_throw >= 0)
			return json.damage_on_throw;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_DAMAGE_AMOUNT_ON_THROW.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_DAMAGE_AMOUNT_ON_THROW.get();
	}

	@Unique
	private int getMinRemainingDurability(ItemStack stack) {
		Integer nbt = getIntNBT(stack, "MinRemainingDurability");
		if (nbt != null)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.min_remaining_durability != null)
			return json.min_remaining_durability;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_MIN_REMAINING_DURABILITY.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_MIN_REMAINING_DURABILITY.get();
	}

	@Unique
	private boolean getAllowMultishot(ItemStack stack) {
		Boolean nbt = getBooleanNBT(stack, "MultishotAllowed");
		if (nbt != null)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.allow_multishot != null)
			return json.allow_multishot;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_ALLOW_MULTISHOT.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_ALLOW_MULTISHOT.get();
	}

	@Unique
	private float getMultishotSpreadAngle(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "MultishotSpreadAngle");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.multishot_spread_angle != null && json.multishot_spread_angle >= 0)
			return json.multishot_spread_angle;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_MULTISHOT_SPREAD_ANGLE.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_MULTISHOT_SPREAD_ANGLE.get().floatValue();
	}

	@Unique
	private boolean getRememberSlot(ItemStack stack) {
		Boolean nbt = getBooleanNBT(stack, "RememberSlot");
		if (nbt != null)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.remember_slot != null)
			return json.remember_slot;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_REMEMBER_SLOT.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_REMEMBER_SLOT.get();
	}

	@Unique
	private float getRecoil(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "RecoilDistance");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.recoil != null && json.recoil >= 0)
			return json.recoil;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_RECOIL.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_RECOIL.get().floatValue();
	}

	@Unique
	private boolean getRecoilOnlyFullyCharged(ItemStack stack) {
		Boolean nbt = getBooleanNBT(stack, "RecoilOnlyFullCharge");
		if (nbt != null)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.recoil_only_fully_charged != null)
			return json.recoil_only_fully_charged;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_RECOIL_ONLY_FULLY_CHARGED.get()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_RECOIL_ONLY_FULLY_CHARGED.get();
	}

	@Unique
	private float getRecoilCrouchReduction(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "RecoilCrouchReduction");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.recoil_crouch_reduction != null && json.recoil_crouch_reduction >= 0)
			return json.recoil_crouch_reduction;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_RECOIL_CROUCH_REDUCTION.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_RECOIL_CROUCH_REDUCTION.get().floatValue();
	}

	@Unique
	private float getRecoilVerticalMultiplier(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "RecoilVerticalMultiplier");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.recoil_vertical_multiplier != null && json.recoil_vertical_multiplier >= 0)
			return json.recoil_vertical_multiplier;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_RECOIL_VERTICAL_MULTIPLIER.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_RECOIL_VERTICAL_MULTIPLIER.get().floatValue();
	}

	@Unique
	private float getRecoilPitchKick(ItemStack stack) {
		Float nbt = getFloatNBT(stack, "RecoilPitchKick");
		if (nbt != null && nbt >= 0)
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.recoil_pitch_kick != null && json.recoil_pitch_kick >= 0)
			return json.recoil_pitch_kick;
		return isShinerang(stack)
				? ThrowableConfig.THROWABLE_SHINERANG_RECOIL_PITCH_KICK.get().floatValue()
				: ThrowableConfig.THROWABLE_SHINERITE_STAR_RECOIL_PITCH_KICK.get().floatValue();
	}

	@Unique
	private String getProjectileType(ItemStack stack) {
		String nbt = getStringNBT(stack, "ProjectileType");
		if (nbt != null && !nbt.isEmpty())
			return nbt;
		ThrowableItemData.ThrowableEntry json = getJsonThrowableEntry(stack);
		if (json != null && json.projectile != null && !json.projectile.isEmpty())
			return json.projectile;
		return isShinerang(stack) ? "jaams_shinerite:shinerang" : "jaams_shinerite:shinerite_star";
	}

	@Unique
	private boolean isJsonProjectileShinerang(ItemStack stack) {
		String type = getProjectileType(stack);
		return type != null && (type.contains("shinerang") || type.equals("jaams_shinerite:shinerang"));
	}

	@Unique
	private void throwInstant(Level level, Player player, InteractionHand hand, ItemStack stack) {
		boolean isCreative = player.getAbilities().instabuild;
		int cooldown = getInstantCooldownTicks(stack);
		if (!level.isClientSide) {
			player.getCooldowns().addCooldown(stack.getItem(), cooldown);
		}
		boolean success = performThrowWithDamageCheck(level, player, hand, stack, 1.0f);
		if (success && !level.isClientSide && !isCreative) {
			stack.shrink(1);
			if (stack.isEmpty())
				player.setItemInHand(hand, ItemStack.EMPTY);
			player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		}
	}

	@Unique
	private void throwCharged(Level level, Player player, InteractionHand hand, ItemStack stack, float power) {
		boolean isCreative = player.getAbilities().instabuild;
		boolean success = performThrowWithDamageCheck(level, player, hand, stack, power);
		if (success && !level.isClientSide && !isCreative) {
			stack.shrink(1);
			if (stack.isEmpty())
				player.setItemInHand(hand, ItemStack.EMPTY);
			player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		}
	}

	@Unique
	private boolean performThrowWithDamageCheck(Level level, Player player, InteractionHand hand, ItemStack stack, float power) {
		boolean isCreative = player.getAbilities().instabuild;
		if (!isCreative && !level.isClientSide && ModUtils.hasDurability(stack)) {
			int damageAmount = getDamageOnThrow(stack);
			if (stack.getDamageValue() + damageAmount >= stack.getMaxDamage()) {
				stack.hurtAndBreak(damageAmount, player, p -> p.broadcastBreakEvent(hand));
				return false;
			}
			stack.hurtAndBreak(damageAmount, player, p -> p.broadcastBreakEvent(hand));
		}
		performThrow(level, player, hand, stack, power);
		return true;
	}

	@Unique
	private boolean canThrowWithDurabilityCheck(ItemStack stack) {
		if (!ModUtils.hasDurability(stack))
			return true;
		int minRemaining = getMinRemainingDurability(stack);
		int currentRemaining = stack.getMaxDamage() - stack.getDamageValue();
		if (minRemaining == 0)
			return true;
		if (minRemaining == -1)
			return currentRemaining >= 2;
		return currentRemaining >= minRemaining;
	}

	@Unique
	private void performThrow(Level level, Player player, InteractionHand hand, ItemStack stack, float power) {
		boolean isShinerang = isJsonProjectileShinerang(stack);
		float minSpeed = getMinSpeed(stack);
		float maxSpeed = getMaxSpeed(stack);
		float speed = minSpeed + (maxSpeed - minSpeed) * power * power;
		boolean isCreative = player.getAbilities().instabuild;
		boolean allowMultishot = getAllowMultishot(stack);
		int multishotLevel = allowMultishot ? EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack) : 0;
		int projectileCount = multishotLevel > 0 ? 3 : 1;
		float spreadAngle = getMultishotSpreadAngle(stack);
		float[] shotPitches = ModUtils.generateShotPitches(level.getRandom(), projectileCount);
		float inaccuracy = getInaccuracy(stack);
		boolean shouldBeCritical;
		ModEnums.ThrowMode mode = getThrowMode(stack);
		boolean isInstant = mode == ModEnums.ThrowMode.INSTANT_ON_RIGHT_CLICK;
		if (!level.isClientSide) {
			int flameLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack);
			boolean isFlaming = flameLevel > 0;
			for (int i = 0; i < projectileCount; i++) {
				ItemStack projectileStack = stack.copy();
				BaseWeaponProjectileEntity projectile;
				if (isShinerang) {
					projectile = new ShinerangProjectileEntity(level, player, projectileStack);
				} else {
					projectile = new ShineriteStarProjectileEntity(level, player, projectileStack);
				}
				if (isFlaming) {
					projectile.setSecondsOnFire(100);
				}
				if (getRememberSlot(stack)) {
					int slotIndex = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
					projectile.setOriginalSlotIndex(slotIndex);
				}
				if (isInstant) {
					shouldBeCritical = ModUtils.isProjectileCritical(player, power);
				} else {
					float threshold = getCriticalPowerThreshold(stack);
					shouldBeCritical = power >= threshold && ModUtils.isProjectileCritical(player, power);
				}
				if (shouldBeCritical) {
					projectile.setCritical(true);
				}
				int pierceLevel = stack.getTag() != null && stack.getTag().contains("PiercingLevel", Tag.TAG_INT)
						? stack.getTag().getInt("PiercingLevel")
						: (isShinerang ? ProjectileCommonConfig.SHINERANG_PROJECTILE_PIERCING_LEVEL.get() : ProjectileCommonConfig.SHINERITE_STAR_PROJECTILE_PIERCING_LEVEL.get());
				pierceLevel += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, stack);
				projectile.setPierceLevel(Math.max(0, pierceLevel));
				if (multishotLevel > 0) {
					projectile.pickup = isCreative ? AbstractArrow.Pickup.CREATIVE_ONLY : (i == 0 ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED);
					if (i != 0) {
						projectile.getPersistentData().putBoolean("IsMultishotClone", true);
					}
					float rotation = (i == 0) ? 0.0F : (i == 1) ? -spreadAngle : spreadAngle;
					Vec3 upVector = player.getUpVector(1.0F);
					Quaternionf rotationQuaternion = new Quaternionf().setAngleAxis(rotation * (float) Math.PI / 180F, (float) upVector.x, (float) upVector.y, (float) upVector.z);
					Vec3 viewVector = player.getViewVector(1.0F);
					Vector3f rotatedVector = new Vector3f((float) viewVector.x, (float) viewVector.y, (float) viewVector.z).rotate(rotationQuaternion);
					if (i == 0) {
						float forwardOffset = 1.0F;
						Vec3 offset = viewVector.scale(forwardOffset);
						projectile.setPos(player.getX() + offset.x, player.getEyeY() + offset.y, player.getZ() + offset.z);
					}
					projectile.shoot(rotatedVector.x(), rotatedVector.y(), rotatedVector.z(), speed, inaccuracy);
				} else {
					projectile.pickup = isCreative ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
					projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, inaccuracy);
				}
				if (projectile instanceof BaseReturningProjectileEntity returningProjectile) {
					double minRangeBase = ModUtils.getConfigOrNbtFloat(stack, "MinRangeBase", TraitsConfig.THROWBACK_SHINERANG_PROJECTILE_MIN_RANGE::get);
					float maxRange = ModUtils.getConfigOrNbtFloat(stack, "MaxRange", TraitsConfig.THROWBACK_SHINERANG_PROJECTILE_MAX_RANGE::get);
					double weaponRangeCalc = minRangeBase + (power * 20.0);
					float finalRange = (float) Math.min(weaponRangeCalc, maxRange);
					returningProjectile.setWeaponRange(finalRange);
				}
				projectile.setWeaponItem(stack.copy());
				level.addFreshEntity(projectile);
				SoundEvent shootSound;
				if (stack.hasTag() && stack.getTag().getBoolean("GenericSoundSet")) {
					shootSound = ModSounds.PROJECTILE_THROW.get();
				} else if (isShinerang) {
					shootSound = ModSounds.SHINERANG_SHOOT.get();
				} else {
					shootSound = ModSounds.SHINERITE_STAR_SHOOT.get();
				}
				level.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), shootSound, SoundSource.PLAYERS, 1.0F, shotPitches[i]);
			}
			boolean fullChargeOnlyRecoil = getRecoilOnlyFullyCharged(stack);
			boolean shouldApplyRecoil = !fullChargeOnlyRecoil || power >= 0.99f;
			if (shouldApplyRecoil) {
				float recoilDistance = getRecoil(stack) * power;
				float crouchReduction = getRecoilCrouchReduction(stack);
				float verticalMult = getRecoilVerticalMultiplier(stack);
				ModUtils.applyRecoil(player, recoilDistance, crouchReduction, verticalMult);
			}
		} else {
			float recoilPitchKick = getRecoilPitchKick(stack);
			float pitchChange = recoilPitchKick * power;
			player.setXRot(player.getXRot() - pitchChange);
		}
	}

	@Unique
	private Integer getIntNBT(ItemStack stack, String key) {
		net.minecraft.nbt.CompoundTag tag = stack.getTag();
		return (tag != null && tag.contains(key, Tag.TAG_INT)) ? tag.getInt(key) : null;
	}

	@Unique
	private Float getFloatNBT(ItemStack stack, String key) {
		net.minecraft.nbt.CompoundTag tag = stack.getTag();
		return (tag != null && tag.contains(key, Tag.TAG_FLOAT)) ? tag.getFloat(key) : null;
	}

	@Unique
	private Boolean getBooleanNBT(ItemStack stack, String key) {
		net.minecraft.nbt.CompoundTag tag = stack.getTag();
		return (tag != null && tag.contains(key, Tag.TAG_BYTE)) ? tag.getBoolean(key) : null;
	}

	@Unique
	private String getStringNBT(ItemStack stack, String key) {
		net.minecraft.nbt.CompoundTag tag = stack.getTag();
		return (tag != null && tag.contains(key, Tag.TAG_STRING)) ? tag.getString(key) : null;
	}
}