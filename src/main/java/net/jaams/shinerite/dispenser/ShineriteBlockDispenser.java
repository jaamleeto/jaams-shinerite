package net.jaams.shinerite.dispenser;

import org.joml.Vector3f;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;

public class ShineriteBlockDispenser extends DefaultDispenseItemBehavior {
	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		if (!InteractionsConfig.DISPENSER_INTERACTIONS.get()) {
			return super.execute(source, stack);
		}
		Level world = source.getLevel();
		BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
		BlockState state = world.getBlockState(pos);
		if (!(world.getBlockEntity(pos) instanceof BaseShineriteBlockEntity blockEntity)) {
			return super.execute(source, stack);
		}
		Item item = stack.getItem();
		int blockColor = blockEntity.getColor();
		if (blockEntity.isCoated()) {
			if (MechanicsConfig.COATING.get() && InteractionsConfig.DISPENSER_BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem) {
				blockEntity.setCoated(false);
				world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
				ModUtils.generateCoatedRemovalParticles(world, pos, state, world.random);
				blockEntity.setChanged();
				world.setBlock(pos, state, 3);
				return ModUtils.reduceDispenserItemStack(source, stack);
			}
			ModUtils.generateCoatedParticles(world, pos, state, world.random);
			return stack;
		}
		if (MechanicsConfig.WASHING.get() && InteractionsConfig.DISPENSER_BLOCK_WASHING.get() && item == Items.POTION && (blockColor != -1 || blockEntity.isImbued())) {
			Potion potion = PotionUtils.getPotion(stack);
			if (potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion.getEffects().isEmpty()) {
				if (blockColor != -1) {
					applyDye(world, pos, state, -1);
				}
				if (blockEntity.isImbued()) {
					blockEntity.clearImbuedState();
				}
				stack.shrink(1);
				world.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (world instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
					world.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
					serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GLASS_BOTTLE)), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.1);
				}
				blockEntity.setChanged();
				world.setBlock(pos, state, 3);
				return stack;
			}
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.DISPENSER_BLOCK_DYEING.get() && !blockEntity.isImbued()) {
			if (item instanceof ShinyPaintBrushItem brush && brush.hasColor(stack)) {
				int color = brush.getColor(stack);
				if (color == blockColor || (color == DyeColor.BLACK.getTextColor() && blockColor == ModUtils.lightenColor(DyeColor.BLACK.getTextColor()))) {
					return stack;
				}
				if (color == DyeColor.BLACK.getTextColor()) {
					color = ModUtils.lightenColor(color);
				}
				applyDye(world, pos, state, color);
				blockEntity.setChanged();
				world.setBlock(pos, state, 3);
				return ModUtils.reduceDispenserItemStack(source, stack);
			}
			if (item instanceof DyeItem dyeItem) {
				int expectedColor = ModUtils.calculateDyeColor(blockColor, dyeItem);
				if (blockColor == expectedColor) {
					return stack;
				}
				applyDye(world, pos, state, expectedColor);
				stack.shrink(1);
				blockEntity.setChanged();
				world.setBlock(pos, state, 3);
				return stack;
			}
		}
		if (InteractionsConfig.DISPENSER_BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem) {
			if (MechanicsConfig.IMBUING.get() && blockEntity.isImbued()) {
				blockEntity.clearImbuedState();
				if (blockColor != -1) {
					applyDye(world, pos, state, -1);
				} else {
					blockEntity.setChanged();
					world.setBlock(pos, state, 3);
				}
				world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
				ModUtils.generateDispenserDustParticles(world, pos, state, new Vector3f(1.0F, 1.0F, 1.0F));
				return ModUtils.reduceDispenserItemStack(source, stack);
			}
			if (MechanicsConfig.DYEING.get() && blockColor != -1) {
				applyDye(world, pos, state, -1);
				blockEntity.setChanged();
				world.setBlock(pos, state, 3);
				return ModUtils.reduceDispenserItemStack(source, stack);
			}
		}
		if (MechanicsConfig.COATING.get() && InteractionsConfig.DISPENSER_BLOCK_COATING.get() && item == Items.HONEYCOMB) {
			blockEntity.setCoated(true);
			stack.shrink(1);
			blockEntity.setChanged();
			world.setBlock(pos, state, 3);
			world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
			ModUtils.generateCoatedParticles(world, pos, state, world.random);
			return stack;
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.DISPENSER_BLOCK_LIGHTENING.get() && item == Items.GLOW_INK_SAC && blockColor != -1 && !blockEntity.isImbued()) {
			int newColor = ModUtils.lightenColor(blockColor);
			if (newColor == blockColor) {
				return stack;
			}
			applyDye(world, pos, state, newColor);
			stack.shrink(1);
			blockEntity.setChanged();
			world.setBlock(pos, state, 3);
			world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			ModUtils.generateDispenserDustParticles(world, pos, state, new Vector3f((newColor >> 16 & 255) / 255.0F, (newColor >> 8 & 255) / 255.0F, (newColor & 255) / 255.0F));
			return stack;
		}
		if (MechanicsConfig.DYEING.get() && InteractionsConfig.DISPENSER_BLOCK_DARKENING.get() && item == Items.INK_SAC && !blockEntity.isImbued()) {
			int newColor = blockColor == -1 ? 0x404040 : ModUtils.darkenColor(blockColor);
			if (newColor == blockColor) {
				return stack;
			}
			applyDye(world, pos, state, newColor);
			stack.shrink(1);
			blockEntity.setChanged();
			world.setBlock(pos, state, 3);
			world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			ModUtils.generateDispenserDustParticles(world, pos, state, new Vector3f((newColor >> 16 & 255) / 255.0F, (newColor >> 8 & 255) / 255.0F, (newColor & 255) / 255.0F));
			return stack;
		}
		return super.execute(source, stack);
	}

	private void applyDye(Level world, BlockPos pos, BlockState state, int color) {
		if (world.getBlockEntity(pos) instanceof BaseShineriteBlockEntity blockEntity) {
			blockEntity.setColor(color);
			blockEntity.setChanged();
			world.setBlock(pos, state, 3);
			world.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			Vector3f colorVector = new Vector3f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
			ModUtils.generateDispenserDustParticles(world, pos, state, colorVector);
		}
	}
}
