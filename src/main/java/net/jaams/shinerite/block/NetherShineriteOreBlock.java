package net.jaams.shinerite.block;

import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.component.block.BaseShineriteBlock;

import java.util.List;
import java.util.Collections;

public class NetherShineriteOreBlock extends BaseShineriteBlock implements EntityBlock {
	public NetherShineriteOreBlock() {
		super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.NETHERRACK).strength(3f).requiresCorrectToolForDrops(), ModBlockEntities.NETHER_SHINERITE_ORE);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		super.playerWillDestroy(level, pos, state, player);
		if (!level.isClientSide && !player.isCreative() && state.canHarvestBlock(level, pos, player)) {
			RandomSource random = RandomSource.create();
			int experienceOrbs = random.nextInt(2) + 1;
			if (level instanceof ServerLevel serverLevel) {
				popExperience(serverLevel, pos, experienceOrbs);
			}
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
		RandomSource random = RandomSource.create();
		boolean hasSilkTouch = tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
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
		if (hasSilkTouch) {
			ItemStack blockStack = new ItemStack(this);
			if (color != -1) {
				CompoundTag tag = blockStack.getOrCreateTag();
				tag.putInt("color", color);
			}
			if (isCoated) {
				blockStack.getOrCreateTag().putBoolean("Coated", true);
			}
			return Collections.singletonList(blockStack);
		} else {
			ItemStack rawStack = new ItemStack(ModItems.SHINERITE_NUGGET.get());
			if (color != -1) {
				CompoundTag tag = rawStack.getOrCreateTag();
				tag.putInt("color", color);
			}
			if (isCoated) {
				rawStack.getOrCreateTag().putBoolean("Coated", true);
			}
			int fortuneLevel = tool != null ? EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool) : 0;
			int dropCount = random.nextInt(5) + 2;
			if (fortuneLevel > 0) {
				dropCount *= (random.nextInt(fortuneLevel + 2) + 1);
			}
			rawStack.setCount(dropCount);
			return Collections.singletonList(rawStack);
		}
	}

	private int calculateDropCount(int fortuneLevel, RandomSource random) {
		if (fortuneLevel > 0) {
			int maxDrops = 1 << fortuneLevel;
			return random.nextInt(maxDrops) + 1;
		}
		return 1;
	}

}
