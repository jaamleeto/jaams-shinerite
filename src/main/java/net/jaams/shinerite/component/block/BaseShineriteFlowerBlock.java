package net.jaams.shinerite.component.block;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;

import javax.annotation.Nullable;

import java.util.function.Supplier;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;

public class BaseShineriteFlowerBlock extends FlowerBlock implements EntityBlock {
	private final Map<BlockPos, CompoundTag> stateMap = new HashMap<>();
	private final Supplier<BlockEntityType<?>> blockEntityType;

	public BaseShineriteFlowerBlock(Supplier<MobEffect> effect, int effectDuration, BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
		super(effect, effectDuration, properties);
		this.blockEntityType = blockEntityType;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BaseShineriteBlockEntity(blockEntityType.get(), pos, state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return type == blockEntityType.get() ? (BlockEntityTicker<T>) (level1, pos, state1, blockEntity) -> {
			if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
				shineriteBlockEntity.tick(level1, pos, state1, shineriteBlockEntity);
			}
		} : null;
	}

	@Override
	public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
		super.onProjectileHit(level, state, hit, projectile);
		BlockPos pos = hit.getBlockPos();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
			int color = shineriteBaseBlockEntity.getColor();
			Vec3 hitLocation = hit.getLocation();
			if (level instanceof ServerLevel serverLevel && !(projectile instanceof ThrownPotion potion && (potion.getItem().getItem() == Items.SPLASH_POTION || potion.getItem().getItem() == Items.LINGERING_POTION))) {
				ModUtils.generateProjectileHitParticles(serverLevel, hitLocation, color);
			}
			if (!shineriteBaseBlockEntity.isWaxed() && projectile instanceof ThrownPotion potion) {
				ItemStack potionStack = potion.getItem();
				Item potionItem = potionStack.getItem();
				if (potionItem == Items.SPLASH_POTION || potionItem == Items.LINGERING_POTION) {
					Potion thrownPotion = PotionUtils.getPotion(potionStack);
					if (thrownPotion == Potions.WATER || thrownPotion == Potions.AWKWARD || thrownPotion == Potions.THICK || thrownPotion == Potions.MUNDANE || thrownPotion.getEffects().isEmpty()) {
						ModUtils.applyDye(level, pos, state, -1);
						blockEntity.setChanged();
						if (level instanceof ServerLevel serverLevel) {
							serverLevel.getChunkSource().blockChanged(pos);
							level.sendBlockUpdated(pos, state, state, 3);
							serverLevel.sendParticles(ParticleTypes.SPLASH, hitLocation.x, hitLocation.y, hitLocation.z, 10, 0.2, 0.2, 0.2, 0.0);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
			int color = shineriteBaseBlockEntity.getColor();
			ModUtils.generateRunningEffectParticles(serverLevel, entity, color);
			return color != -1;
		}
		return false;
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity && !level.isClientSide()) {
			int color = shineriteBaseBlockEntity.getColor();
			ModUtils.generateLandingEffectParticles(level, entity, color, numberOfParticles);
			return color != -1;
		}
		return false;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!InteractionsConfig.BLOCK_INTERACTIONS.get()) {
			return InteractionResult.PASS;
		}
		ItemStack itemstack = player.getItemInHand(hand);
		Item item = itemstack.getItem();
		if (!(world.getBlockEntity(pos) instanceof BaseShineriteBlockEntity blockEntity)) {
			return InteractionResult.PASS;
		}
		int blockColor = blockEntity.getColor();
		if (blockEntity.isWaxed()) {
			if (MechanicsConfig.WAXING.get() && InteractionsConfig.BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem) {
				blockEntity.setWaxed(false);
				world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!player.isCreative()) {
					ModUtils.reduceItemStack(player, itemstack);
				}
				ModUtils.generateWaxRemovalParticles(world, pos, state, world.random);
				return InteractionResult.SUCCESS;
			}
			if (item instanceof ShinyPaintBrushItem || item instanceof DyeItem || item == Items.HONEYCOMB || item == Items.INK_SAC || item == Items.GLOW_INK_SAC) {
				long currentTime = world.getGameTime();
				if (currentTime - blockEntity.getLastWaxParticleTime() >= 30) {
					ModUtils.generateWaxParticles(world, pos, state, world.random);
					blockEntity.setLastWaxParticleTime(currentTime);
				}
				return InteractionResult.CONSUME;
			}
			return InteractionResult.PASS;
		}
		if (MechanicsConfig.WASHING.get() && InteractionsConfig.BLOCK_WASHING.get() && item == Items.POTION && blockColor != -1) {
			Potion potion = PotionUtils.getPotion(itemstack);
			if (potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion.getEffects().isEmpty()) {
				ModUtils.applyDye(world, pos, state, -1);
				if (!player.isCreative()) {
					itemstack.shrink(1);
					player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
				}
				world.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (world instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
				}
				return InteractionResult.SUCCESS;
			}
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_DYEING.get()) {
			if (item instanceof ShinyPaintBrushItem brush && brush.hasColor(itemstack)) {
				int color = brush.getColor(itemstack);
				if (color == blockColor || (color == DyeColor.BLACK.getTextColor() && blockColor == ModUtils.lightenColor(DyeColor.BLACK.getTextColor()))) {
					return InteractionResult.PASS;
				}
				if (color == DyeColor.BLACK.getTextColor()) {
					color = ModUtils.lightenColor(color);
				}
				ModUtils.applyDye(world, pos, state, color);
				if (!player.isCreative()) {
					ModUtils.reduceItemStack(player, itemstack);
				}
				return InteractionResult.SUCCESS;
			}
			if (item instanceof DyeItem dyeItem) {
				int expectedColor = ModUtils.calculateDyeColor(blockColor, dyeItem);
				if (blockColor == expectedColor) {
					return InteractionResult.PASS;
				}
				ModUtils.applyDye(world, pos, state, expectedColor);
				if (!player.isCreative()) {
					ModUtils.reduceItemStack(player, itemstack);
				}
				return InteractionResult.SUCCESS;
			}
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem && blockColor != -1) {
			ModUtils.applyDye(world, pos, state, -1);
			if (!player.isCreative()) {
				ModUtils.reduceItemStack(player, itemstack);
			}
			return InteractionResult.SUCCESS;
		}
		if (MechanicsConfig.WAXING.get() && InteractionsConfig.BLOCK_WAXING.get() && item == Items.HONEYCOMB) {
			blockEntity.setWaxed(true);
			if (!player.isCreative()) {
				ModUtils.reduceItemStack(player, itemstack);
			}
			ModUtils.generateWaxParticles(world, pos, state, world.random);
			world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_LIGHTENING.get() && item == Items.GLOW_INK_SAC && blockColor != -1) {
			int newColor = ModUtils.lightenColor(blockColor);
			if (newColor == blockColor) {
				return InteractionResult.PASS;
			}
			ModUtils.applyDye(world, pos, state, newColor);
			if (!player.isCreative()) {
				ModUtils.reduceItemStack(player, itemstack);
			}
			world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			ModUtils.generateDustParticles(world, pos, newColor);
			return InteractionResult.SUCCESS;
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_DARKENING.get() && item == Items.INK_SAC) {
			int newColor = blockColor == -1 ? 0xCDCDCD : ModUtils.darkenColor(blockColor);
			if (newColor == blockColor) {
				return InteractionResult.PASS;
			}
			ModUtils.applyDye(world, pos, state, newColor);
			if (!player.isCreative()) {
				ModUtils.reduceItemStack(player, itemstack);
			}
			world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			ModUtils.generateDustParticles(world, pos, newColor);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
			int color = shineriteBaseBlockEntity.getColor();
			return new float[]{(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F};
		}
		return new float[]{1f, 1f, 1f};
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
		int color = -1;
		boolean isWaxed = false;
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity != null) {
			CompoundTag tag = blockEntity.saveWithoutMetadata();
			if (tag.contains("Color", Tag.TAG_INT)) {
				color = tag.getInt("Color");
			}
			if (tag.contains("Waxed", Tag.TAG_BYTE)) {
				isWaxed = tag.getBoolean("Waxed");
			}
		}
		ItemStack stack = new ItemStack(this);
		CompoundTag stackTag = stack.getOrCreateTag();
		if (color != -1) {
			stackTag.putInt("color", color);
		}
		if (isWaxed) {
			stackTag.putBoolean("Waxed", true);
		}
		return Collections.singletonList(stack);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
			ItemStack stack = new ItemStack(this);
			int color = shineriteBaseBlockEntity.getColor();
			if (color != -1 && stack.getItem() instanceof IDyeableItem dyeableItem) {
				dyeableItem.setColor(stack, color);
			}
			if (shineriteBaseBlockEntity.isWaxed()) {
				stack.getOrCreateTag().putBoolean("Waxed", true);
			}
			return stack;
		}
		return super.getCloneItemStack(world, pos, state);
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
		super.triggerEvent(state, world, pos, eventID, eventParam);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity == null ? false : blockEntity.triggerEvent(eventID, eventParam);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!world.isClientSide && isMoving) {
			if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
				stateMap.put(pos, shineriteBaseBlockEntity.saveWithFullMetadata());
			}
		}
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity && !world.isClientSide) {
			int color = shineriteBaseBlockEntity.getColor();
			if (world instanceof ServerLevel serverLevel) {
				ModUtils.generateBlockTransitionParticles(serverLevel, pos, state, color, 40, 1.0F);
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!world.isClientSide) {
			BlockPos originalPos = findOriginalPosition(pos);
			if (originalPos != null && stateMap.containsKey(originalPos)) {
				if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
					CompoundTag savedData = stateMap.remove(originalPos);
					if (savedData != null) {
						shineriteBaseBlockEntity.load(savedData);
						shineriteBaseBlockEntity.setChanged();
						shineriteBaseBlockEntity.sync();
						world.setBlock(pos, state, 3);
					}
					world.sendBlockUpdated(pos, state, state, 3);
				}
			}
			if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
				if (shineriteBaseBlockEntity.isWaxed()) {
					ModUtils.generateWaxParticles(world, pos, state, world.random);
				}
				int color = shineriteBaseBlockEntity.getColor();
				if (world instanceof ServerLevel serverLevel && isMoving) {
					ModUtils.generateBlockTransitionParticles(serverLevel, pos, state, color, 40, 1.5f);
				}
			}
		}
		super.onPlace(state, world, pos, oldState, isMoving);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
			int color = shineriteBaseBlockEntity.getColor();
			level.sendBlockUpdated(pos, state, state, 3);
		}
		if (level.isClientSide) {
			List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(5));
			for (Player player : players) {
				ItemStack mainHandItem = player.getMainHandItem();
				ItemStack offHandItem = player.getOffhandItem();
				if (player.isCrouching() && (mainHandItem.getItem() == Items.HONEYCOMB || offHandItem.getItem() == Items.HONEYCOMB)) {
					if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
						long currentTime = level.getGameTime();
						if (currentTime - shineriteBaseBlockEntity.getLastWaxParticleTime() >= 45 && shineriteBaseBlockEntity.isWaxed()) {
							ModUtils.generateClientWaxParticles(level, pos, state, random);
							shineriteBaseBlockEntity.setLastWaxParticleTime(currentTime);
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(world, pos, state, placer, stack);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
			if (shineriteBaseBlockEntity.isWaxed()) {
				ModUtils.generateWaxParticles(world, pos, state, world.random);
			}
			if (stack.getItem() instanceof IDyeableItem dyeableItem && dyeableItem.hasColor(stack)) {
				int color = dyeableItem.getColor(stack);
				shineriteBaseBlockEntity.setColor(color);
			}
			if (stack.hasTag()) {
				CompoundTag tag = stack.getTag();
				if (tag.contains("Waxed")) {
					boolean waxed = tag.getBoolean("Waxed");
					shineriteBaseBlockEntity.setWaxed(waxed);
				}
			}
			shineriteBaseBlockEntity.setChanged();
			world.sendBlockUpdated(pos, state, state, 3);
		}
	}

	public BlockPos findOriginalPosition(BlockPos newPos) {
		for (Map.Entry<BlockPos, CompoundTag> entry : stateMap.entrySet()) {
			BlockPos originalPos = entry.getKey();
			if (originalPos.distSqr(newPos) <= 1) {
				return originalPos;
			}
		}
		return null;
	}
}
