package net.jaams.shinerite.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;

import net.minecraftforge.registries.ForgeRegistries;

import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.init.ModBlocks;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.util.ModUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PottedShineritiaBlock extends FlowerPotBlock implements EntityBlock {
	private final Supplier<BlockEntityType<?>> blockEntityType;

	public PottedShineritiaBlock(Supplier<BlockEntityType<?>> blockEntityType) {
		super(() -> (FlowerPotBlock) Blocks.FLOWER_POT, () -> ModBlocks.SHINERITIA.get(), BlockBehaviour.Properties.copy(Blocks.FLOWER_POT).lightLevel(state -> 3));
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
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof BaseShineriteBlockEntity blockEntity)) {
			return super.use(state, world, pos, player, hand, hit);
		}
		ItemStack itemstack = player.getItemInHand(hand);
		Item item = itemstack.getItem();
		if (InteractionsConfig.BLOCK_INTERACTIONS.get()) {
			InteractionResult result = handleShineriteInteractions(state, world, pos, player, itemstack, item, blockEntity);
			if (result != InteractionResult.PASS) {
				return result;
			}
			if (blockEntity.isCoated()) {
				return InteractionResult.PASS;
			}
		}
		if (isPottablePlant(itemstack)) {
			return super.use(state, world, pos, player, hand, hit);
		}
		return removePlant(state, world, pos, player, hand, blockEntity);
	}

	private InteractionResult handleShineriteInteractions(BlockState state, Level world, BlockPos pos, Player player, ItemStack itemstack, Item item, BaseShineriteBlockEntity blockEntity) {
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

	private static boolean isPottablePlant(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem blockItem)) {
			return false;
		}
		Map<ResourceLocation, Supplier<? extends Block>> fullPots = ((FlowerPotBlock) Blocks.FLOWER_POT).getFullPotsView();
		return fullPots.containsKey(ForgeRegistries.BLOCKS.getKey(blockItem.getBlock()));
	}

	private InteractionResult removePlant(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BaseShineriteBlockEntity blockEntity) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		SoundEvent removeSound = null;
		if (blockEntity.isImbued()) {
			removeSound = SoundEvents.BREWING_STAND_BREW;
		} else if (blockEntity.isCoated()) {
			removeSound = SoundEvents.HONEYCOMB_WAX_ON;
		} else if (blockEntity.getColor() != -1) {
			removeSound = SoundEvents.DYE_USE;
		}
		ItemStack plant = createPlantItem(blockEntity);
		if (player.getItemInHand(hand).isEmpty()) {
			player.setItemInHand(hand, plant);
		} else if (!player.getInventory().add(plant)) {
			player.drop(plant, false);
		}
		world.setBlock(pos, Blocks.FLOWER_POT.defaultBlockState(), 3);
		world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		if (removeSound != null) {
			world.playSound(null, pos, removeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
		if (world instanceof ServerLevel serverLevel) {
			if (!blockEntity.isImbued() && blockEntity.getColor() != -1) {
				ModUtils.generateBlockTransitionParticles(serverLevel, pos, state, blockEntity.getColor(), 40, 1.0F);
			}
			if (blockEntity.isImbued()) {
				ModUtils.generateImbuedBurstParticles(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, blockEntity.getImbuedColor(), 40);
			}
		}
		return InteractionResult.CONSUME;
	}

	private static ItemStack createPlantItem(BaseShineriteBlockEntity blockEntity) {
		ItemStack plant = new ItemStack(ModItems.SHINERITIA.get());
		int color = blockEntity.isImbued() ? -1 : blockEntity.getColor();
		if (color != -1) {
			if (plant.getItem() instanceof IDyeableItem dyeableItem) {
				dyeableItem.setColor(plant, color);
			} else {
				plant.getOrCreateTag().putInt("color", color);
			}
		}
		if (blockEntity.isCoated()) {
			plant.getOrCreateTag().putBoolean("Coated", true);
		}
		return plant;
	}

	private static ItemStack createCloneItem(BaseShineriteBlockEntity blockEntity) {
		ItemStack plant = createPlantItem(blockEntity);
		if (blockEntity.isImbued()) {
			plant.getOrCreateTag().put("ImbuedEffects", blockEntity.getImbuedEffects());
			plant.getOrCreateTag().putInt("ImbuedUses", blockEntity.getImbuedUses());
			plant.getOrCreateTag().putInt("MaxImbuedUses", blockEntity.getMaxImbuedUses());
			plant.getOrCreateTag().putInt("ImbuedColor", blockEntity.getImbuedColor());
			ModUtils.applyDyeColor(plant, blockEntity.getImbuedColor());
		}
		return plant;
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		super.playerWillDestroy(level, pos, state, player);
		if (level.getBlockEntity(pos) instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			shineriteBlockEntity.transferImbuementToBreaker(level, pos, player);
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		ItemStack plant = new ItemStack(ModItems.SHINERITIA.get());
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			plant = createPlantItem(shineriteBlockEntity);
		}
		return List.of(new ItemStack(Items.FLOWER_POT), plant);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity) {
			return createCloneItem(shineriteBlockEntity);
		}
		return super.getCloneItemStack(world, pos, state);
	}
}
