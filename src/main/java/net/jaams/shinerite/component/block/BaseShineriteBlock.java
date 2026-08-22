package net.jaams.shinerite.component.block;

import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.registries.RegistryObject;

import org.joml.Vector3f;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.particle.CustomEffectParticleData;
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
import java.util.ArrayList;
import java.util.Collections;

public abstract class BaseShineriteBlock extends Block implements EntityBlock {
	private final Map<BlockPos, CompoundTag> stateMap = new HashMap<>();
	private final Supplier<BlockEntityType<?>> blockEntityType;

	public BaseShineriteBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<?>> blockEntityType) {
		super(properties);
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

	@OnlyIn(Dist.CLIENT)
	public static void registerBlockColor(RegisterColorHandlersEvent.Block event, RegistryObject<Block> block) {
		event.getBlockColors().register((state, world, pos, index) -> {
			if (world != null && pos != null) {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
					if (MechanicsConfig.IMBUING.get() && shineriteBaseBlockEntity.isImbued() && shineriteBaseBlockEntity.getImbuedColor() != -1) {
						return shineriteBaseBlockEntity.getImbuedColor();
					}
					return shineriteBaseBlockEntity.getColor();
				}
			}
			return -1;
		}, block.get());
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerItemColor(RegisterColorHandlersEvent.Item event, RegistryObject<Block> block) {
		event.getItemColors().register((stack, index) -> {
			if (index > 0 || !(stack.getItem() instanceof IDyeableItem dyeableItem)) {
				return -1;
			}
			return dyeableItem.getColor(stack);
		}, block.get().asItem());
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 15;
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
			if (projectile instanceof ThrownPotion potion) {
				ItemStack potionStack = potion.getItem();
				Item potionItem = potionStack.getItem();
				if (potionItem == Items.SPLASH_POTION || potionItem == Items.LINGERING_POTION) {
					Potion thrownPotion = PotionUtils.getPotion(potionStack);
					boolean isWaterPotion = thrownPotion == Potions.WATER || thrownPotion == Potions.AWKWARD || thrownPotion == Potions.THICK || thrownPotion == Potions.MUNDANE || thrownPotion.getEffects().isEmpty();
					if (!isWaterPotion) {
						ModUtils.imbueBlockWithThrownPotion(level, pos, shineriteBaseBlockEntity, potionStack);
					} else if (!shineriteBaseBlockEntity.isCoated()) {
						boolean modified = false;
						if (shineriteBaseBlockEntity.getColor() != -1) {
							ModUtils.applyDye(level, pos, state, -1);
							modified = true;
						}
						if (shineriteBaseBlockEntity.isImbued()) {
							shineriteBaseBlockEntity.clearImbuedState();
							modified = true;
						}
						if (modified) {
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
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		super.fallOn(level, state, pos, entity, fallDistance);
		if (!level.isClientSide && MechanicsConfig.IMBUING.get() && entity instanceof LivingEntity livingEntity) {
			if (level.getBlockEntity(pos) instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
				shineriteBaseBlockEntity.registerDirectContact(livingEntity, true);
			}
		}
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
		if (blockEntity.isCoated()) {
			if (MechanicsConfig.COATING.get() && InteractionsConfig.BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem) {
				blockEntity.setCoated(false);
				world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!player.isCreative()) {
					ModUtils.reduceItemStack(player, itemstack);
				}
				ModUtils.generateCoatedRemovalParticles(world, pos, state, world.random);
				return InteractionResult.SUCCESS;
			}
			if (item instanceof ShinyPaintBrushItem || item instanceof DyeItem || item == Items.HONEYCOMB || item == Items.INK_SAC || item == Items.GLOW_INK_SAC) {
				long currentTime = world.getGameTime();
				if (currentTime - blockEntity.getLastCoatedParticleTime() >= 30) {
					ModUtils.generateCoatedParticles(world, pos, state, world.random);
					blockEntity.setLastCoatedParticleTime(currentTime);
				}
				return InteractionResult.CONSUME;
			}
			return InteractionResult.PASS;
		}
		if (MechanicsConfig.IMBUING.get() && itemstack.isEmpty() && player.isCrouching() && blockEntity.isImbued()) {
			return blockEntity.registerDirectContact(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		if ((MechanicsConfig.IMBUED_BLOCK_COATED_CAN_IMBUE.get() || !blockEntity.isCoated()) && MechanicsConfig.IMBUING.get() && InteractionsConfig.BLOCK_IMBUING.get() && item == Items.POTION && !blockEntity.isImbued() && blockColor == -1) {
			Potion potion = PotionUtils.getPotion(itemstack);
			if (potion != Potions.WATER && potion != Potions.AWKWARD && potion != Potions.THICK && potion != Potions.MUNDANE && !potion.getEffects().isEmpty()) {
				List<MobEffectInstance> effects = new ArrayList<>();
				if (potion != Potions.EMPTY) {
					effects.addAll(potion.getEffects());
				}
				effects.addAll(PotionUtils.getCustomEffects(itemstack));
				if (!effects.isEmpty()) {
					int potionColor = PotionUtils.getColor(itemstack);
					ListTag effectsTag = new ListTag();
					for (MobEffectInstance effect : effects) {
						MobEffectInstance savedEffect = new MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 2), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
						effectsTag.add(savedEffect.save(new CompoundTag()));
					}
					blockEntity.setImbuedState(effectsTag, 1, 1, potionColor);
					world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.15F);
					if (!player.isCreative()) {
						itemstack.shrink(1);
						player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
					}
					if (world instanceof ServerLevel serverLevel) {
						serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
						serverLevel.sendParticles(new CustomEffectParticleData(((potionColor >> 16) & 255) / 255.0F, ((potionColor >> 8) & 255) / 255.0F, (potionColor & 255) / 255.0F, 0.3F),
							pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.12);
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		if (MechanicsConfig.WASHING.get() && InteractionsConfig.BLOCK_WASHING.get() && item == Items.POTION && (blockColor != -1 || blockEntity.isImbued())) {
			Potion potion = PotionUtils.getPotion(itemstack);
			if (potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion.getEffects().isEmpty()) {
				if (blockColor != -1) {
					ModUtils.applyDye(world, pos, state, -1);
				}
				if (blockEntity.isImbued()) {
					blockEntity.clearImbuedState();
				}
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
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_DYEING.get() && !blockEntity.isImbued()) {
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
		if (InteractionsConfig.BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem) {
			if (MechanicsConfig.IMBUING.get() && blockEntity.isImbued()) {
				blockEntity.clearImbuedState();
				if (blockColor != -1) {
					ModUtils.applyDye(world, pos, state, -1);
				}
				if (!player.isCreative()) {
					ModUtils.reduceItemStack(player, itemstack);
				}
				world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
				if (world instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F),
						pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.05);
				}
				return InteractionResult.SUCCESS;
			}
			if (MechanicsConfig.DYEING.get() && blockColor != -1) {
				ModUtils.applyDye(world, pos, state, -1);
				if (!player.isCreative()) {
					ModUtils.reduceItemStack(player, itemstack);
				}
				return InteractionResult.SUCCESS;
			}
		}
		if (MechanicsConfig.COATING.get() && InteractionsConfig.BLOCK_COATING.get() && item == Items.HONEYCOMB) {
			blockEntity.setCoated(true);
			if (!player.isCreative()) {
				ModUtils.reduceItemStack(player, itemstack);
			}
			ModUtils.generateCoatedParticles(world, pos, state, world.random);
			world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_LIGHTENING.get() && item == Items.GLOW_INK_SAC && blockColor != -1 && !blockEntity.isImbued()) {
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
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_DARKENING.get() && item == Items.INK_SAC && !blockEntity.isImbued()) {
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
		boolean isCoated = false;
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity != null) {
			CompoundTag tag = blockEntity.saveWithoutMetadata();
			if (tag.contains("Color", Tag.TAG_INT)) {
				color = tag.getInt("Color");
			}
			if (tag.contains("Coated", Tag.TAG_BYTE)) {
				isCoated = tag.getBoolean("Coated");
			}
		}
		ItemStack stack = new ItemStack(this);
		CompoundTag stackTag = stack.getOrCreateTag();
		if (color != -1) {
			stackTag.putInt("color", color);
		}
		if (isCoated) {
			stackTag.putBoolean("Coated", true);
		}
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBE && shineriteBE.isImbued()) {
			stackTag.put("ImbuedEffects", shineriteBE.getImbuedEffects());
			stackTag.putInt("ImbuedUses", shineriteBE.getImbuedUses());
			stackTag.putInt("MaxImbuedUses", shineriteBE.getMaxImbuedUses());
			stackTag.putInt("ImbuedColor", shineriteBE.getImbuedColor());
			ModUtils.applyDyeColor(stack, shineriteBE.getImbuedColor());
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
			if (shineriteBaseBlockEntity.isCoated()) {
				stack.getOrCreateTag().putBoolean("Coated", true);
			}
			if (shineriteBaseBlockEntity.isImbued()) {
				CompoundTag stackTag = stack.getOrCreateTag();
				stackTag.put("ImbuedEffects", shineriteBaseBlockEntity.getImbuedEffects());
				stackTag.putInt("ImbuedUses", shineriteBaseBlockEntity.getImbuedUses());
				stackTag.putInt("MaxImbuedUses", shineriteBaseBlockEntity.getMaxImbuedUses());
				stackTag.putInt("ImbuedColor", shineriteBaseBlockEntity.getImbuedColor());
				ModUtils.applyDyeColor(stack, shineriteBaseBlockEntity.getImbuedColor());
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
				if (shineriteBaseBlockEntity.isImbued()) {
					ModUtils.generateImbuedBurstParticles(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, shineriteBaseBlockEntity.getImbuedColor(), 40);
				}
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
if (shineriteBaseBlockEntity.isCoated()) {
					ModUtils.generateCoatedParticles(world, pos, state, world.random);
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
						if (currentTime - shineriteBaseBlockEntity.getLastCoatedParticleTime() >= 45 && shineriteBaseBlockEntity.isCoated()) {
							ModUtils.generateClientCoatedParticles(level, pos, state, random);
							shineriteBaseBlockEntity.setLastCoatedParticleTime(currentTime);
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
			if (shineriteBaseBlockEntity.isCoated()) {
				ModUtils.generateCoatedParticles(world, pos, state, world.random);
			}
			if (stack.getItem() instanceof IDyeableItem dyeableItem && dyeableItem.hasColor(stack)) {
				int color = dyeableItem.getColor(stack);
				shineriteBaseBlockEntity.setColor(color);
			}
			if (stack.hasTag()) {
				CompoundTag tag = stack.getTag();
				if (tag.contains("Coated")) {
					boolean coated = tag.getBoolean("Coated");
					shineriteBaseBlockEntity.setCoated(coated);
				}
				if (tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
					ListTag effects = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
					int uses = tag.getInt("ImbuedUses");
					int maxUses = tag.getInt("MaxImbuedUses");
					int imbuedColor = tag.getInt("ImbuedColor");
					shineriteBaseBlockEntity.setImbuedState(effects, uses, maxUses, imbuedColor);
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
