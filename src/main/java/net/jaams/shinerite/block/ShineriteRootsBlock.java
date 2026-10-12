package net.jaams.shinerite.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import net.jaams.shinerite.component.block.BaseShineriteBlock;
import net.jaams.shinerite.util.ModUtils;

import java.util.List;
import java.util.Collections;

public class ShineriteRootsBlock extends BaseShineriteBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public ShineriteRootsBlock() {
		super(BlockBehaviour.Properties.of().ignitedByLava().sound(SoundType.ROOTS).strength(0.0f).noOcclusion().pushReaction(PushReaction.DESTROY).hasPostProcess((bs, br, bp) -> true).isRedstoneConductor((bs, br, bp) -> false)
				.lightLevel(state -> 3), ModBlockEntities.SHINERITE_ROOTS);
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return state.getFluidState().isEmpty();
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return box(2, 10, 2, 14, 16, 14);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getClickedFace() == Direction.DOWN) {
			boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
			return this.defaultBlockState().setValue(WATERLOGGED, flag);
		}
		return null;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean canSurvive(BlockState p_153347_, LevelReader p_153348_, BlockPos p_153349_) {
		BlockPos blockpos = p_153349_.above();
		BlockState blockstate = p_153348_.getBlockState(blockpos);
		return blockstate.is(this) || blockstate.isFaceSturdy(p_153348_, blockpos, Direction.DOWN);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.UP && !facingState.is(this) && !facingState.isFaceSturdy(world, facingPos, Direction.DOWN)) {
			return Blocks.AIR.defaultBlockState();
		}
		if (state.getValue(WATERLOGGED)) {
			world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return context.getItemInHand().getItem() != this.asItem();
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity instanceof BaseShineriteBlockEntity shineriteBaseBlockEntity) {
			ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
			RandomSource random = RandomSource.create();
			boolean hasSilkTouch = tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
			boolean isShears = tool != null && tool.getItem() == Items.SHEARS;
			if (hasSilkTouch || isShears) {
				ItemStack blockStack = new ItemStack(this);
				applyStateToStack(shineriteBaseBlockEntity, blockStack);
				return Collections.singletonList(blockStack);
			} else {
				ItemStack rawStack = new ItemStack(ModItems.SHINY_ROOTS.get());
				applyStateToStack(shineriteBaseBlockEntity, rawStack);
				int fortuneLevel = tool != null ? EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool) : 0;
				int dropCount = calculateDropCount(fortuneLevel, random);
				rawStack.setCount(dropCount);
				return Collections.singletonList(rawStack);
			}
		}
		return super.getDrops(state, builder);
	}

	private static void applyStateToStack(BaseShineriteBlockEntity blockEntity, ItemStack stack) {
		int color = blockEntity.getColor();
		if (color != -1 && stack.getItem() instanceof IDyeableItem dyeableItem) {
			dyeableItem.setColor(stack, color);
		}
		if (blockEntity.isCoated()) {
			stack.getOrCreateTag().putBoolean("Coated", true);
		}
		if (blockEntity.isImbued()) {
			CompoundTag stackTag = stack.getOrCreateTag();
			stackTag.put("ImbuedEffects", blockEntity.getImbuedEffects());
			stackTag.putInt("ImbuedUses", blockEntity.getImbuedUses());
			stackTag.putInt("MaxImbuedUses", blockEntity.getMaxImbuedUses());
			stackTag.putInt("ImbuedColor", blockEntity.getImbuedColor());
			ModUtils.applyDyeColor(stack, blockEntity.getImbuedColor());
		}
	}

	private int calculateDropCount(int fortuneLevel, RandomSource random) {
		int baseDrops = random.nextInt(2) + 1;
		if (fortuneLevel > 0) {
			int maxDrops = baseDrops + random.nextInt(fortuneLevel + 1);
			return Math.min(maxDrops, 2 + fortuneLevel);
		}
		return baseDrops;
	}

}
