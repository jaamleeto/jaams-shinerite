package net.jaams.shinerite.block;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.init.ModBlockEntities;
import net.jaams.shinerite.init.ModItems;
import net.jaams.shinerite.component.block.BaseShineriteBlock;

import java.util.Map;
import java.util.List;
import java.util.Collections;

public class ShineriteOreBlock extends BaseShineriteBlock {
	public ShineriteOreBlock() {
		super(BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(3f).requiresCorrectToolForDrops(), ModBlockEntities.SHINERITE_ORE);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		super.playerWillDestroy(level, pos, state, player);
		if (!level.isClientSide && !player.isCreative() && state.canHarvestBlock(level, pos, player)) {
			RandomSource random = RandomSource.create();
			int experienceOrbs = random.nextInt(3) + 0;
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
		boolean hasSmelterEnchantment = false;
		boolean hasPickaxeHeater = false;
		boolean hasSmeltingEnchantment = false;
		boolean isCrouching = false;
		Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
		if (entity instanceof Player) {
			isCrouching = ((Player) entity).isCrouching();
			hasPickaxeHeater = ModUtils.isItemInCuriosSlot(entity, "artifacts:pickaxe_heater", "hands");
		}
		if (tool != null) {
			for (Map.Entry<Enchantment, Integer> enchantmentEntry : EnchantmentHelper.getEnchantments(tool).entrySet()) {
				Enchantment enchantment = enchantmentEntry.getKey();
				ResourceLocation enchantmentId = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
				if (enchantmentId != null) {
					String enchantmentIdStr = enchantmentId.toString().toLowerCase();
					if (enchantmentIdStr.equals("majruszsenchantments:smelter")) {
						hasSmelterEnchantment = true;
					} else if (ModUtils.hasSmeltingEnchantment(enchantmentIdStr)) {
						hasSmeltingEnchantment = true;
					}
				}
			}
		}
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
		if (hasSilkTouch) {
			ItemStack blockStack = new ItemStack(this);
			if (color != -1) {
				CompoundTag tag = blockStack.getOrCreateTag();
				tag.putInt("color", color);
			}
			if (isWaxed) {
				blockStack.getOrCreateTag().putBoolean("Waxed", true);
			}
			return Collections.singletonList(blockStack);
		} else {
			ItemStack dropStack = (hasSmeltingEnchantment || hasPickaxeHeater || (hasSmelterEnchantment && !isCrouching)) ? new ItemStack(ModItems.SHINERITE_INGOT.get()) : new ItemStack(ModItems.RAW_SHINERITE.get());
			if (color != -1) {
				CompoundTag tag = dropStack.getOrCreateTag();
				tag.putInt("color", color);
			}
			if (isWaxed) {
				dropStack.getOrCreateTag().putBoolean("Waxed", true);
			}
			int fortuneLevel = tool != null ? EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool) : 0;
			int dropCount = calculateDropCount(fortuneLevel, random);
			dropStack.setCount(dropCount);
			return Collections.singletonList(dropStack);
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
