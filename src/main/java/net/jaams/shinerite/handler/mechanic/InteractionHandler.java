package net.jaams.shinerite.handler.mechanic;

import org.joml.Vector3f;

import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.NonNullList;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.particle.CustomEffectParticleData;
import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;

import java.util.List;
import java.util.ArrayList;

@Mod.EventBusSubscriber
public class InteractionHandler {
	public static final int DEFAULT_COLOR = -1;
	public static final int BASE_IMBUED_USES = 5;

	private static boolean isCoatableItemExtended(ItemStack stack) {
		return ModUtils.isCoatableItem(stack);
	}

	private static boolean isScrapableItem(ItemStack stack) {
		return ModUtils.isCoatableItem(stack);
	}

	@SubscribeEvent
	public static void onInteractionRightClickItem(PlayerInteractEvent.RightClickItem event) {
		Level level = event.getLevel();
		Player player = event.getEntity();
		if (!InteractionsConfig.HAND_INTERACTIONS.get()) {
			return;
		}
		ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);
		if (mainHandStack.getItem() instanceof ShinyScraperItem && offHandStack.getItem() instanceof ShinyScraperItem) {
			if (InteractionsConfig.HAND_DUAL_SCRAPING.get()) {
				handleDualScraperInteraction(level, player, mainHandStack, offHandStack, event);
			}
			return;
		}
		if (MechanicsConfig.COATING.get() && InteractionsConfig.HAND_COATING.get()) {
			if (isCoatableItemExtended(mainHandStack) && offHandStack.is(Items.HONEYCOMB)) {
				handleCoatedInteraction(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, event);
				return;
			} else if (isCoatableItemExtended(offHandStack) && mainHandStack.is(Items.HONEYCOMB)) {
				handleCoatedInteraction(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, event);
				return;
			}
		}
		if (InteractionsConfig.HAND_SCRAPING.get()) {
			if ((isScrapableItem(mainHandStack) || (MechanicsConfig.IMBUING.get() && ModUtils.isImbueableItem(mainHandStack) && ModUtils.isImbued(mainHandStack))
					|| (MechanicsConfig.DYEING.get() && ModUtils.isDyeableItem(mainHandStack) && ModUtils.getCurrentColor(mainHandStack) != DEFAULT_COLOR)) && offHandStack.getItem() instanceof ShinyScraperItem) {
				handleScrapingInteraction(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, event);
				return;
			} else if ((isScrapableItem(offHandStack) || (MechanicsConfig.IMBUING.get() && ModUtils.isImbueableItem(offHandStack) && ModUtils.isImbued(offHandStack))
					|| (MechanicsConfig.DYEING.get() && ModUtils.isDyeableItem(offHandStack) && ModUtils.getCurrentColor(offHandStack) != DEFAULT_COLOR)) && mainHandStack.getItem() instanceof ShinyScraperItem) {
				handleScrapingInteraction(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, event);
				return;
			}
		}
		if (MechanicsConfig.DYEING.get()) {
			boolean dyeWithDyeItem = InteractionsConfig.HAND_DYEING_DYE.get();
			boolean dyeWithBrush = InteractionsConfig.HAND_DYEING_BRUSH.get();
			if ((dyeWithDyeItem || dyeWithBrush)) {
				if (ModUtils.isDyeableItem(mainHandStack) && !ModUtils.isImbued(mainHandStack) && !ModUtils.isCoated(mainHandStack)) {
					boolean validSecondary = (dyeWithDyeItem && offHandStack.getItem() instanceof DyeItem) || (dyeWithBrush && offHandStack.getItem() instanceof ShinyPaintBrushItem);
					if (validSecondary) {
						handleDyeingInteraction(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, event);
						return;
					}
				}
				if (ModUtils.isDyeableItem(offHandStack) && !ModUtils.isImbued(offHandStack) && !ModUtils.isCoated(offHandStack)) {
					boolean validSecondary = (dyeWithDyeItem && mainHandStack.getItem() instanceof DyeItem) || (dyeWithBrush && mainHandStack.getItem() instanceof ShinyPaintBrushItem);
					if (validSecondary) {
						handleDyeingInteraction(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, event);
						return;
					}
				}
			}
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.HAND_LIGHTENING.get()) {
			if (ModUtils.isDyeableItem(mainHandStack) && !ModUtils.isImbued(mainHandStack) && !ModUtils.isCoated(mainHandStack) && offHandStack.is(Items.GLOW_INK_SAC)) {
				handleLighteningInteraction(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, event);
				return;
			}
			if (ModUtils.isDyeableItem(offHandStack) && !ModUtils.isImbued(offHandStack) && !ModUtils.isCoated(offHandStack) && mainHandStack.is(Items.GLOW_INK_SAC)) {
				handleLighteningInteraction(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, event);
				return;
			}
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.HAND_DARKENING.get()) {
			if (ModUtils.isDyeableItem(mainHandStack) && !ModUtils.isImbued(mainHandStack) && !ModUtils.isCoated(mainHandStack) && offHandStack.is(Items.INK_SAC)) {
				handleDarkeningInteraction(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, event);
				return;
			}
			if (ModUtils.isDyeableItem(offHandStack) && !ModUtils.isImbued(offHandStack) && !ModUtils.isCoated(offHandStack) && mainHandStack.is(Items.INK_SAC)) {
				handleDarkeningInteraction(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, event);
				return;
			}
		}
		if (MechanicsConfig.IMBUING.get() && InteractionsConfig.HAND_IMBUING.get()) {
			if (ModUtils.isImbueableItem(mainHandStack) && !ModUtils.isCoated(mainHandStack) && !hasCustomColor(mainHandStack) && offHandStack.is(Items.POTION) && PotionUtils.getPotion(offHandStack) != Potions.WATER) {
				handleImbuingInteraction(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, event);
				return;
			} else if (ModUtils.isImbueableItem(offHandStack) && !ModUtils.isCoated(offHandStack) && !hasCustomColor(offHandStack) && mainHandStack.is(Items.POTION) && PotionUtils.getPotion(mainHandStack) != Potions.WATER) {
				handleImbuingInteraction(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, event);
				return;
			}
		}
		if (MechanicsConfig.WASHING.get() && InteractionsConfig.HAND_WASHING_WATER_BOTTLE.get()) {
			boolean mainIsWashable = ModUtils.isImbueableItem(mainHandStack) || (MechanicsConfig.DYEING.get() && ModUtils.isDyeableItem(mainHandStack));
			if (mainIsWashable && offHandStack.is(Items.POTION)) {
				Potion potion = PotionUtils.getPotion(offHandStack);
				if ((potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion.getEffects().isEmpty()) && !ModUtils.isCoated(mainHandStack)) {
					handleWaterBottleInteraction(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, event);
				}
			}
			boolean offIsWashable = ModUtils.isImbueableItem(offHandStack) || (MechanicsConfig.DYEING.get() && ModUtils.isDyeableItem(offHandStack));
			if (offIsWashable && mainHandStack.is(Items.POTION)) {
				Potion potion = PotionUtils.getPotion(mainHandStack);
				if ((potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion.getEffects().isEmpty()) && !ModUtils.isCoated(offHandStack)) {
					handleWaterBottleInteraction(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, event);
				}
			}
		}
	}

	private static void handleCoatedInteraction(Level level, Player player, ItemStack shineriteStack, ItemStack honeycombStack, InteractionHand hand, PlayerInteractEvent.RightClickItem event) {
		if (ModUtils.isCoated(shineriteStack)) {
			return;
		}
		cancelEvent(event, level);
		handleItemInteraction(level, player, shineriteStack, honeycombStack, hand, (targetItem) -> {
			targetItem.getOrCreateTag().putBoolean("Coated", true);
			level.playSound(null, player.blockPosition(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
			generateParticles(level, player, hand, ParticleTypes.WAX_ON, 5, 0.3, 0.3, 0.3, 0.05);
		});
	}

	private static void handleScrapingInteraction(Level level, Player player, ItemStack targetStack, ItemStack scraperStack, InteractionHand hand, PlayerInteractEvent.RightClickItem event) {
		boolean isCoated = ModUtils.isCoated(targetStack);
		boolean isImbued = ModUtils.isImbued(targetStack);
		boolean hasColor = ModUtils.getCurrentColor(targetStack) != DEFAULT_COLOR;
		if (!isCoated && !isImbued && !hasColor) {
			return;
		}
		cancelEvent(event, level);
		if (!level.isClientSide()) {
			int originalSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
			boolean isSingleItem = targetStack.getCount() == 1;
			ItemStack resultStack = processScraping(level, player, targetStack, scraperStack, hand, isCoated, isImbued, hasColor);
			handleResultStack(player, targetStack, resultStack, originalSlot, isSingleItem);
		}
	}

	private static void handleDyeingInteraction(Level level, Player player, ItemStack shineriteStack, ItemStack dyeStack, InteractionHand hand, PlayerInteractEvent.RightClickItem event) {
		int dyeColor;
		if (dyeStack.getItem() instanceof ShinyPaintBrushItem brush) {
			if (!brush.hasColor(dyeStack)) {
				return;
			}
			dyeColor = brush.getColor(dyeStack);
			if (ModUtils.getCurrentColor(shineriteStack) == dyeColor) {
				return;
			}
		} else if (dyeStack.getItem() instanceof DyeItem dyeItem) {
			dyeColor = calculateDyeColor(shineriteStack, dyeItem);
			if (ModUtils.getCurrentColor(shineriteStack) == dyeColor) {
				return;
			}
		} else {
			return;
		}
		cancelEvent(event, level);
		handleItemInteraction(level, player, shineriteStack, dyeStack, hand, (targetItem) -> {
			ModUtils.applyDyeColor(targetItem, dyeColor);
			level.playSound(null, player.blockPosition(), SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
			generateDustParticles(level, player, hand, dyeColor, 5, 0.3, 0.3, 0.3, 0.05);
		});
		if (!player.isCreative()) {
			if (dyeStack.getItem() instanceof ShinyPaintBrushItem) {
				dyeStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
			} else {
				dyeStack.shrink(1);
			}
		}
	}

	public static void handleLighteningInteraction(Level level, Player player, ItemStack shineriteStack, ItemStack glowInkStack, InteractionHand hand, PlayerInteractEvent.RightClickItem event) {
		int currentColor = ModUtils.getCurrentColor(shineriteStack);
		if (currentColor == DEFAULT_COLOR || currentColor == 0xFFFFFF) {
			return;
		}
		int lighterColor = lightenColor(currentColor);
		if (lighterColor == currentColor) {
			return;
		}
		cancelEvent(event, level);
		handleItemInteraction(level, player, shineriteStack, glowInkStack, hand, (targetItem) -> {
			ModUtils.applyDyeColor(targetItem, lighterColor);
			level.playSound(null, player.blockPosition(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
			generateDustParticles(level, player, hand, lighterColor, 5, 0.3, 0.3, 0.3, 0.05);
		});
	}

	public static void handleDarkeningInteraction(Level level, Player player, ItemStack shineriteStack, ItemStack inkStack, InteractionHand hand, PlayerInteractEvent.RightClickItem event) {
		int currentColor = ModUtils.getCurrentColor(shineriteStack);
		if (currentColor == 0x000000) {
			return;
		}
		int darkerColor = darkenColor(currentColor);
		if (darkerColor == currentColor) {
			return;
		}
		cancelEvent(event, level);
		handleItemInteraction(level, player, shineriteStack, inkStack, hand, (targetItem) -> {
			ModUtils.applyDyeColor(targetItem, darkerColor);
			level.playSound(null, player.blockPosition(), SoundEvents.INK_SAC_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
			generateDustParticles(level, player, hand, darkerColor, 5, 0.3, 0.3, 0.3, 0.05);
		});
	}

	private static void handleImbuingInteraction(Level level, Player player, ItemStack targetStack, ItemStack potionStack, InteractionHand hand, PlayerInteractEvent.RightClickItem event) {
		if (ModUtils.isImbued(targetStack)) {
			return;
		}
		List<MobEffectInstance> effects = new ArrayList<>();
		Potion potionType = PotionUtils.getPotion(potionStack);
		if (potionType != Potions.EMPTY && potionType != Potions.WATER) {
			effects.addAll(potionType.getEffects());
		}
		effects.addAll(PotionUtils.getCustomEffects(potionStack));
		if (effects.isEmpty()) {
			return;
		}
		if (potionType == Potions.WATER || potionType == Potions.AWKWARD || potionType == Potions.THICK || potionType == Potions.MUNDANE) {
			return;
		}
		int potionColor = PotionUtils.getColor(potionStack);
		boolean isInstant = effects.stream().allMatch(e -> e.getDuration() <= 20);
		int imbuedUses = Math.max(1, isInstant ? 1 : calculateImbuedUses(targetStack, effects));
		cancelEvent(event, level);
		handleItemInteraction(level, player, targetStack, potionStack, hand, targetItem -> {
			CompoundTag tag = targetItem.getOrCreateTag();
			ListTag effectsTag = new ListTag();
			for (MobEffectInstance effect : effects) {
				MobEffectInstance savedEffect = new MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 2), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
				effectsTag.add(savedEffect.save(new CompoundTag()));
			}
			tag.put("ImbuedEffects", effectsTag);
			tag.putInt("ImbuedUses", imbuedUses);
			tag.putInt("MaxImbuedUses", imbuedUses);
			tag.putInt("ImbuedColor", potionColor);
			ModUtils.applyDyeColor(targetItem, potionColor);
			level.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.15F);
			if (!player.getAbilities().instabuild) {
				potionStack.shrink(1);
				addOrDropItem(player, new ItemStack(Items.GLASS_BOTTLE));
			}
		});
	}

	private static void handleDualScraperInteraction(Level level, Player player, ItemStack mainHandStack, ItemStack offHandStack, PlayerInteractEvent.RightClickItem event) {
		boolean hasColorMain = ModUtils.getCurrentColor(mainHandStack) != DEFAULT_COLOR;
		boolean hasColorOff = ModUtils.getCurrentColor(offHandStack) != DEFAULT_COLOR;
		boolean isCoatedMain = ModUtils.isCoated(mainHandStack);
		boolean isCoatedOff = ModUtils.isCoated(offHandStack);
		boolean isImbuedMain = ModUtils.isImbued(mainHandStack);
		boolean isImbuedOff = ModUtils.isImbued(offHandStack);
		if (!hasColorMain && !hasColorOff && !isCoatedMain && !isCoatedOff && !isImbuedMain && !isImbuedOff) {
			return;
		}
		cancelEvent(event, level);
		if (!level.isClientSide()) {
			if (hasColorMain || isCoatedMain || isImbuedMain) {
				int mainHandSlot = player.getInventory().selected;
				boolean isSingleItemMain = mainHandStack.getCount() == 1;
				ItemStack resultStack = processScraping(level, player, mainHandStack, offHandStack, InteractionHand.MAIN_HAND, isCoatedMain, isImbuedMain, hasColorMain);
				handleResultStack(player, mainHandStack, resultStack, mainHandSlot, isSingleItemMain);
			}
			if (hasColorOff || isCoatedOff || isImbuedOff) {
				int offHandSlot = 40;
				boolean isSingleItemOff = offHandStack.getCount() == 1;
				ItemStack resultStack = processScraping(level, player, offHandStack, mainHandStack, InteractionHand.OFF_HAND, isCoatedOff, isImbuedOff, hasColorOff);
				handleResultStack(player, offHandStack, resultStack, offHandSlot, isSingleItemOff);
			}
		}
	}

	private static void handleWaterBottleInteraction(Level level, Player player, ItemStack targetStack, ItemStack waterBottleStack, InteractionHand hand, PlayerInteractEvent.RightClickItem event) {
		boolean isImbued = ModUtils.isImbued(targetStack);
		boolean hasColor = ModUtils.getCurrentColor(targetStack) != DEFAULT_COLOR;
		if (!isImbued && !hasColor) {
			return;
		}
		cancelEvent(event, level);
		handleItemInteraction(level, player, targetStack, waterBottleStack, hand, (targetItem) -> {
			if (isImbued) {
				ModUtils.removeImbuement(targetItem);
			}
			if (ModUtils.getCurrentColor(targetItem) != DEFAULT_COLOR) {
				ModUtils.removeColor(targetItem);
			}
			level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
			generateParticles(level, player, hand, ParticleTypes.SPLASH, 5, 0.3, 0.3, 0.3, 0.05);
			if (!player.isCreative()) {
				waterBottleStack.shrink(1);
				addOrDropItem(player, new ItemStack(Items.GLASS_BOTTLE));
			}
		});
	}

	private static void handleItemInteraction(Level level, Player player, ItemStack targetStack, ItemStack secondaryStack, InteractionHand hand, ItemProcessor processor) {
		if (!level.isClientSide()) {
			int originalSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
			ItemStack resultStack;
			boolean isSingleItem = targetStack.getCount() == 1;
			if (isSingleItem) {
				resultStack = targetStack;
			} else {
				resultStack = targetStack.split(1);
			}
			processor.process(resultStack);
			if (!player.isCreative() && !(secondaryStack.getItem() instanceof ShinyScraperItem) && !(secondaryStack.getItem() instanceof ShinyPaintBrushItem)) {
				secondaryStack.shrink(1);
			}
			handleResultStack(player, targetStack, resultStack, originalSlot, isSingleItem);
		}
	}

	private static void handleResultStack(Player player, ItemStack originalStack, ItemStack resultStack, int originalSlot, boolean isSingleItem) {
		if (resultStack.isEmpty()) {
			return;
		}
		if (isSingleItem) {
			if (originalSlot == 40) {
				player.getInventory().offhand.set(0, ItemStack.EMPTY);
			} else if (originalSlot >= 0 && originalSlot < player.getInventory().items.size()) {
				player.getInventory().items.set(originalSlot, ItemStack.EMPTY);
			}
		}
		if (addToExistingStack(player, resultStack)) {
			return;
		}
		if (isSingleItem) {
			if (originalSlot == 40) {
				ItemStack offHand = player.getInventory().offhand.get(0);
				if (offHand.isEmpty()) {
					player.getInventory().offhand.set(0, resultStack);
					return;
				}
			} else if (originalSlot >= 0 && originalSlot < player.getInventory().items.size()) {
				ItemStack slotStack = player.getInventory().items.get(originalSlot);
				if (slotStack.isEmpty()) {
					player.getInventory().items.set(originalSlot, resultStack);
					return;
				}
			}
		}
		if (!addToExistingStack(player, resultStack)) {
			addOrDropItem(player, resultStack);
		}
	}

	public static ItemStack processScraping(Level level, Player player, ItemStack targetStack, ItemStack scraperStack, InteractionHand hand, boolean isCoated, boolean isImbued, boolean hasColor) {
		ItemStack resultStack = targetStack.getCount() > 1 ? targetStack.split(1) : targetStack;
		if (isCoated) {
			resultStack.getOrCreateTag().remove("Coated");
			level.playSound(null, player.blockPosition(), SoundEvents.AXE_WAX_OFF, SoundSource.PLAYERS, 1.0F, 1.0F);
			generateParticles(level, player, hand, ParticleTypes.WAX_OFF, 5, 0.3, 0.3, 0.3, 0.05);
		} else if (isImbued) {
			ModUtils.removeImbuement(resultStack);
			ModUtils.removeColor(resultStack);
			level.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 0.8F);
			generateDustParticles(level, player, hand, 0xFFFFFF, 8, 0.3, 0.3, 0.3, 0.05);
		} else if (hasColor) {
			ModUtils.removeColor(resultStack);
			level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
			generateDustParticles(level, player, hand, DEFAULT_COLOR, 5, 0.3, 0.3, 0.3, 0.05);
		}
		if (!player.isCreative()) {
			scraperStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
		}
		return resultStack;
	}

	private static boolean addToExistingStack(Player player, ItemStack targetItem) {
		NonNullList<ItemStack> inventory = player.getInventory().items;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stackInSlot = inventory.get(i);
			if (ItemStack.isSameItemSameTags(stackInSlot, targetItem) && stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
				int transferAmount = Math.min(targetItem.getCount(), stackInSlot.getMaxStackSize() - stackInSlot.getCount());
				stackInSlot.grow(transferAmount);
				targetItem.shrink(transferAmount);
				if (targetItem.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	private static void addOrDropItem(Player player, ItemStack itemStack) {
		if (!player.getInventory().add(itemStack)) {
			player.drop(itemStack, false);
		}
	}

	public static int calculateImbuedUses(ItemStack targetStack, List<MobEffectInstance> effects) {
		if (targetStack.getMaxStackSize() > 1 || targetStack.isEdible()) {
			return 1;
		}
		return calculateBaseImbuedUses(effects);
	}

	public static int calculateBaseImbuedUses(List<MobEffectInstance> effects) {
		return effects.isEmpty() ? BASE_IMBUED_USES : BASE_IMBUED_USES + (effects.get(0).getAmplifier() + 1) * 2;
	}

	public static void generateParticles(Level level, Player player, InteractionHand hand, ParticleOptions particleType, int count, double offsetX, double offsetY, double offsetZ, double speed) {
		if (level instanceof ServerLevel serverLevel) {
			Vec3 handPos = ModUtils.getHandPosition(player, hand);
			serverLevel.sendParticles(particleType, handPos.x, handPos.y, handPos.z, count, offsetX, offsetY, offsetZ, speed);
		}
	}

	public static void generateDustParticles(Level level, Player player, InteractionHand hand, int color, int count, double offsetX, double offsetY, double offsetZ, double speed) {
		if (level instanceof ServerLevel serverLevel) {
			Vec3 handPos = ModUtils.getHandPosition(player, hand);
			float r = ((color >> 16) & 255) / 255.0F;
			float g = ((color >> 8) & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.0F), handPos.x, handPos.y, handPos.z, count, offsetX, offsetY, offsetZ, speed);
		}
	}

	public static void generateCustomEffectParticles(Level level, Player player, InteractionHand hand, int color, int count, double offsetX, double offsetY, double offsetZ, double speed) {
		if (level instanceof ServerLevel serverLevel) {
			Vec3 handPos = ModUtils.getHandPosition(player, hand);
			float r = ((color >> 16) & 255) / 255.0F;
			float g = ((color >> 8) & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			serverLevel.sendParticles(new CustomEffectParticleData(r, g, b, 0.3F), handPos.x, handPos.y, handPos.z, count, offsetX, offsetY, offsetZ, speed);
		}
	}

	public static void cancelEvent(PlayerInteractEvent.RightClickItem event, Level level) {
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
	}

	public static boolean hasCustomColor(ItemStack stack) {
		return ModUtils.getCurrentColor(stack) != DEFAULT_COLOR;
	}

	public static int calculateDyeColor(ItemStack shineriteStack, DyeItem dyeItem) {
		int[] combinedColors = new int[3];
		int maxColor = 0;
		int colorCount = 0;
		int currentColor = ModUtils.getCurrentColor(shineriteStack);
		if (currentColor != DEFAULT_COLOR) {
			float r = (currentColor >> 16 & 255) / 255f;
			float g = (currentColor >> 8 & 255) / 255f;
			float b = (currentColor & 255) / 255f;
			maxColor += (int) (Math.max(r, Math.max(g, b)) * 255f);
			combinedColors[0] += (int) (r * 255f);
			combinedColors[1] += (int) (g * 255f);
			combinedColors[2] += (int) (b * 255f);
			colorCount++;
		}
		float[] dyeComponents = dyeItem.getDyeColor().getTextureDiffuseColors();
		int r = (int) (dyeComponents[0] * 255f);
		int g = (int) (dyeComponents[1] * 255f);
		int b = (int) (dyeComponents[2] * 255f);
		maxColor += Math.max(r, Math.max(g, b));
		combinedColors[0] += r;
		combinedColors[1] += g;
		combinedColors[2] += b;
		colorCount++;
		r = combinedColors[0] / colorCount;
		g = combinedColors[1] / colorCount;
		b = combinedColors[2] / colorCount;
		float avgColor = (float) maxColor / colorCount;
		float maxValue = Math.max(r, Math.max(g, b));
		r = (int) (r * avgColor / maxValue);
		g = (int) (g * avgColor / maxValue);
		b = (int) (b * avgColor / maxValue);
		return (r << 16) | (g << 8) | b;
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

	@FunctionalInterface
	private interface ItemProcessor {
		void process(ItemStack targetItem);
	}
}
