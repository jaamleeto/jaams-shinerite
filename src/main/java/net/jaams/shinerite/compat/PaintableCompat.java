package net.jaams.shinerite.compat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.im_maker.paintable.common.block.block_values.ModBlockStateProperties;
import net.im_maker.paintable.common.entity.custom.PrimedPnt;
import net.im_maker.paintable.common.item.PItems;
import net.im_maker.paintable.common.item.custom.DippedPaintBrushItem;
import net.im_maker.paintable.config.PaintableConfig;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.component.projectile.BaseWeaponProjectileEntity;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.util.ModUtils;

/**
 * Optional Paintable compatibility, following the same bridge pattern as the
 * other mods' compat classes.
 *
 * <p>No field or method of the outer class mentions a Paintable type. All direct
 * API calls live inside {@link PaintableImpl}, which is only loaded after
 * {@link #isAvailable()} succeeds, and every public entry point is wrapped in a
 * {@code Throwable} catch. The Forge listeners are registered from common setup,
 * also only when the mod is present.</p>
 */
@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PaintableCompat {

	public static final String MOD_ID = "paintable";

	private static final EquipmentSlot[] EQUIPPED_SLOTS = {
			EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private static Boolean loaded;

	private PaintableCompat() {
	}

	/** Cheap presence check that never touches an optional class. */
	public static boolean isAvailable() {
		if (loaded == null) {
			try {
				loaded = ModList.get() != null && ModList.get().isLoaded(MOD_ID);
			} catch (Throwable t) {
				loaded = false;
			}
		}
		return loaded;
	}

	/** Cheap check used by the always-loaded interaction handlers. */
	public static boolean isPaintBrush(@Nullable ItemStack stack) {
		if (!isAvailable() || stack == null || stack.isEmpty()) {
			return false;
		}
		try {
			return PaintableImpl.isPaintBrush(stack);
		} catch (Throwable t) {
			return false;
		}
	}

	@Nullable
	public static DyeColor getBrushColor(@Nullable ItemStack stack) {
		if (!isAvailable() || stack == null || stack.isEmpty()) {
			return null;
		}
		try {
			return PaintableImpl.getBrushColor(stack);
		} catch (Throwable t) {
			return null;
		}
	}

	/** Consumes one paint use, mirroring Paintable's own brush behaviour. */
	public static void consumePaint(Level level, Player player, ItemStack stack, InteractionHand hand) {
		if (!isAvailable()) {
			return;
		}
		try {
			PaintableImpl.consumePaint(level, player, stack, hand);
		} catch (Throwable t) {
			// Optional compat: brush bookkeeping only.
		}
	}

	/** True when this position holds a Shinerite block that can still be dyed. */
	public static boolean canDyeBlock(@Nullable Level level, @Nullable BlockPos pos) {
		if (!isAvailable()) {
			return false;
		}
		try {
			return PaintableImpl.canDyeBlock(level, pos);
		} catch (Throwable t) {
			return false;
		}
	}

	/** Applies a Paintable colour to a Shinerite block. */
	public static boolean dyeBlock(@Nullable Level level, @Nullable BlockPos pos, @Nullable DyeColor color) {
		if (!isAvailable()) {
			return false;
		}
		try {
			return PaintableImpl.dyeBlock(level, pos, color);
		} catch (Throwable t) {
			return false;
		}
	}

	@SubscribeEvent
	public static void registerIntegration(FMLCommonSetupEvent event) {
		if (!isAvailable()) {
			return;
		}
		try {
			PaintableImpl.registerListeners();
		} catch (Throwable t) {
			JaamsShineriteMod.LOGGER.error("[Jaam's Shinerite] Failed to register Paintable listeners", t);
		}
	}

	/** All direct Paintable references live here and are loaded lazily. */
	private static final class PaintableImpl {

		static void registerListeners() {
			MinecraftForge.EVENT_BUS.addListener(PaintableImpl::onRightClickBlock);
			MinecraftForge.EVENT_BUS.addListener(PaintableImpl::onPntExplosion);
		}

		static boolean isPaintBrush(ItemStack stack) {
			return stack.getItem() instanceof DippedPaintBrushItem;
		}

		@Nullable
		static DyeColor getBrushColor(ItemStack stack) {
			Item item = stack.getItem();
			return item instanceof DippedPaintBrushItem brush ? brush.getColor() : null;
		}

		/** Consumes one paint use, mirroring Paintable's own brush behaviour. */
		static void consumePaint(Level level, Player player, ItemStack stack, InteractionHand hand) {
			if (level == null || player == null || hand == null || player.isCreative()
					|| !(stack.getItem() instanceof DippedPaintBrushItem brush)) {
				return;
			}
			int left = brush.getPaintLeft(stack);
			if (left > 1) {
				brush.setPaintCount(stack, left - 1);
				return;
			}
			Item undippedBrush = PItems.PAINT_BRUSH.get();
			if (undippedBrush == null) {
				return;
			}
			player.setItemInHand(hand, new ItemStack(undippedBrush));
			level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
		}

		/** True when this position holds a Shinerite block that can still be dyed. */
		static boolean canDyeBlock(@Nullable Level level, @Nullable BlockPos pos) {
			if (level == null || pos == null || !MechanicsConfig.DYEING.get()) {
				return false;
			}
			BlockEntity blockEntity = level.getBlockEntity(pos);
			return blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity && !shineriteBlockEntity.isImbued();
		}

		/** Applies a Paintable colour to a Shinerite block. */
		static boolean dyeBlock(@Nullable Level level, @Nullable BlockPos pos, @Nullable DyeColor color) {
			if (color == null || !canDyeBlock(level, pos)) {
				return false;
			}
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity)) {
				return false;
			}
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) {
				return false;
			}
			int currentColor = shineriteBlockEntity.getColor();
			int newColor = ModUtils.calculateDyeColor(currentColor, color);
			if (newColor == currentColor) {
				return false;
			}
			ModUtils.applyDye(level, pos, state, newColor);
			return true;
		}

		private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
			if (!isAvailable() || !MechanicsConfig.DYEING.get()) {
				return;
			}
			Level level = event.getLevel();
			BlockPos pos = event.getPos();
			ItemStack heldStack = event.getEntity().getItemInHand(event.getHand());

			// Dip our own paint brush in a Paintable filled paint bucket placed in the world.
			if (heldStack.getItem() instanceof ShinyPaintBrushItem) {
				DyeColor bucketColor = getBucketColor(level.getBlockState(pos));
				if (bucketColor == null) {
					return;
				}
				event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
				event.setCanceled(true);
				if (!level.isClientSide()) {
					ModUtils.applyDyeColor(heldStack,
							ModUtils.calculateDyeColor(ModUtils.getCurrentColor(heldStack), bucketColor));
					drainPaintBucket(level, pos, level.getBlockState(pos));
				}
				return;
			}

			if (!InteractionsConfig.BLOCK_DYEING.get()) {
				return;
			}
			if (!(heldStack.getItem() instanceof DippedPaintBrushItem brush)) {
				return;
			}
			DyeColor dyeColor = brush.getColor();
			if (dyeColor == null || !canDyeBlock(level, pos)) {
				return;
			}

			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity)) {
				return;
			}
			int currentColor = shineriteBlockEntity.getColor();
			int newColor = ModUtils.calculateDyeColor(currentColor, dyeColor);
			if (newColor == currentColor) {
				return;
			}

			event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
			event.setCanceled(true);
			if (!level.isClientSide()) {
				ModUtils.applyDye(level, pos, level.getBlockState(pos), newColor);
				consumePaint(level, event.getEntity(), heldStack, event.getHand());
			}
		}

		/**
		 * Paintable names its filled buckets {@code paintable:<dye>_paint_bucket}.
		 * Those blocks carry a {@code paint} level that is consumed when a brush is
		 * dipped, exactly like Paintable's own brush does.
		 */
		@Nullable
		private static DyeColor getBucketColor(BlockState state) {
			if (state == null || state.isAir() || !state.hasProperty(ModBlockStateProperties.LEVEL_PAINT)
					|| state.getValue(ModBlockStateProperties.LEVEL_PAINT) <= 0) {
				return null;
			}
			for (DyeColor color : DyeColor.values()) {
				ResourceLocation id = ResourceLocation.fromNamespaceAndPath("paintable", color.getName() + "_paint_bucket");
				Block bucket = ForgeRegistries.BLOCKS.getValue(id);
				if (bucket != null && state.is(bucket)) {
					return color;
				}
			}
			return null;
		}

		private static void drainPaintBucket(Level level, BlockPos pos, BlockState state) {
			int paint = state.getValue(ModBlockStateProperties.LEVEL_PAINT);
			level.setBlockAndUpdate(pos, state.setValue(ModBlockStateProperties.LEVEL_PAINT, Math.max(0, paint - 1)));
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		/**
		 * Paintable's coloured TNT paints with a fake brush instead of a real
		 * explosion, and therefore never touches Shinerite block entities or
		 * entities. {@code PrimedPnt.tick()} calls {@code explode()} before
		 * {@code discard()}, so the exploding PNT is still in the world while this
		 * handler runs and we can read its colour directly.
		 */
		private static void onPntExplosion(ExplosionEvent.Detonate event) {
			if (!isAvailable() || !MechanicsConfig.DYEING.get() || event.getLevel().isClientSide()) {
				return;
			}
			Level level = event.getLevel();
			Vec3 center = event.getExplosion().getPosition();
			Entity source = findPnt(level, center);
			if (!(source instanceof PrimedPnt pnt)) {
				return;
			}
			DyeColor color = pnt.getColor();
			if (color == null) {
				return;
			}

			int radius = Math.max(1, Math.min(20, PaintableConfig.PNT_EXPLOSION_RADIUS.get()));
			BlockPos origin = new BlockPos((int) Math.floor(center.x), (int) Math.floor(center.y), (int) Math.floor(center.z));
			double radiusSqr = (double) radius * radius;
			for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
				if (pos.distSqr(origin) > radiusSqr) {
					continue;
				}
				dyeBlock(level, pos, color);
			}

			AABB area = new AABB(center.x - radius, center.y - radius, center.z - radius,
					center.x + radius, center.y + radius, center.z + radius);
			Set<UUID> alreadyDyed = new HashSet<>();
			for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
				if (dyeEntity(entity, color)) {
					alreadyDyed.add(entity.getUUID());
				}
			}

			// Items dropped by the explosion itself do not exist while Detonate runs.
			// Give the level a couple of ticks to add them, then dye those too.
			JaamsShineriteMod.queueServerWork(2, () -> {
				for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
					if (entity instanceof ItemEntity && !alreadyDyed.contains(entity.getUUID())) {
						dyeEntity(entity, color);
					}
				}
			});
		}

		@Nullable
		private static Entity findPnt(Level level, Vec3 center) {
			AABB search = new AABB(center.x - 2.0D, center.y - 2.0D, center.z - 2.0D,
					center.x + 2.0D, center.y + 2.0D, center.z + 2.0D);
			List<PrimedPnt> candidates = level.getEntitiesOfClass(PrimedPnt.class, search);
			Entity best = null;
			double bestDistance = 1.0D;
			for (PrimedPnt pnt : candidates) {
				double distance = pnt.distanceToSqr(center);
				if (distance < bestDistance) {
					best = pnt;
					bestDistance = distance;
				}
			}
			return best;
		}

		/**
		 * Dyes dyeable Shinerite stacks carried by projectiles, dropped as items,
		 * held in either hand, worn as armour, or stored in the back item slot.
		 * Returns true when at least one stack actually changed.
		 */
		private static boolean dyeEntity(Entity entity, DyeColor color) {
			if (entity instanceof BaseWeaponProjectileEntity projectile) {
				ItemStack weapon = projectile.getWeaponItem();
				if (dyeStack(weapon, color)) {
					projectile.setWeaponItem(weapon);
					return true;
				}
			} else if (entity instanceof ItemEntity itemEntity) {
				ItemStack stack = itemEntity.getItem().copy();
				if (dyeStack(stack, color)) {
					itemEntity.setItem(stack);
					return true;
				}
			} else if (entity instanceof LivingEntity living) {
				boolean changed = false;
				for (EquipmentSlot slot : EQUIPPED_SLOTS) {
					ItemStack stack = living.getItemBySlot(slot).copy();
					if (dyeStack(stack, color)) {
						living.setItemSlot(slot, stack);
						changed = true;
					}
				}
				if (BackItemCompat.isAvailable()) {
					ItemStack backItem = BackItemCompat.getBackItem(living).copy();
					if (dyeStack(backItem, color)) {
						BackItemCompat.setBackItem(living, backItem);
						changed = true;
					}
				}
				return changed;
			}
			return false;
		}

		/** Applies the PNT colour to a single Shinerite stack. */
		private static boolean dyeStack(ItemStack stack, DyeColor color) {
			if (stack.isEmpty() || !ModUtils.isDyeableItem(stack) || ModUtils.isImbued(stack) || ModUtils.isCoated(stack)) {
				return false;
			}
			int currentColor = ModUtils.getCurrentColor(stack);
			int newColor = ModUtils.calculateDyeColor(currentColor, color);
			if (newColor == currentColor) {
				return false;
			}
			ModUtils.applyDyeColor(stack, newColor);
			return true;
		}
	}
}
