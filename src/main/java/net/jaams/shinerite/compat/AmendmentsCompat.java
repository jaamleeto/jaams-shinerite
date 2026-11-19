package net.jaams.shinerite.compat;

import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;

import net.jaams.shinerite.handler.mechanic.InteractionHandler;
import net.jaams.shinerite.util.ModUtils;

/**
 * Shinerite compatibility with Amendments. Only referenced from the compat
 * mixins, so this class is never loaded unless Amendments is installed.
 */
public class AmendmentsCompat {

	/**
	 * Attempts to imbue the held item with the potion currently stored in an
	 * Amendments liquid cauldron. Uses the same validations and effects as
	 * {@link net.jaams.shinerite.recipe.ImbueItemRecipe}.
	 *
	 * @return true if the imbuement was applied and the interaction should be consumed
	 */
	public static boolean attemptImbuement(LiquidCauldronBlockTile tile, Player player, InteractionHand hand) {
		ItemStack target = player.getItemInHand(hand);
		if (target.isEmpty() || !ModUtils.isImbueableItem(target)) {
			return false;
		}
		SoftFluidStack fluid = tile.getSoftFluidTank().getFluid();
		if (!fluid.is(BuiltInSoftFluids.POTION) || !fluid.hasTag()) {
			return false;
		}
		List<MobEffectInstance> effects = PotionUtils.getAllEffects(fluid.getTag());
		if (effects.isEmpty()) {
			return false;
		}
		// Same validation as ImbueItemRecipe#isValidImbueableStack
		if (ModUtils.isCoated(target)) {
			return false;
		}
		boolean upgrading = ModUtils.isImbued(target);
		List<MobEffectInstance> upgrades = upgrading ? ModUtils.getImbuementUpgrades(target, effects) : List.of();
		if (upgrading && upgrades.isEmpty()) {
			return false;
		}
		if (!upgrading && ModUtils.getCurrentColor(target) != -1) {
			return false;
		}

		int potionColor = fluid.getTag().contains("CustomPotionColor", Tag.TAG_INT)
				? fluid.getTag().getInt("CustomPotionColor")
				: PotionUtils.getColor(effects);
		CompoundTag tag = target.getOrCreateTag();
		ListTag effectsTag = new ListTag();
		if (upgrading) {
			for (MobEffectInstance merged : ModUtils.mergeImbuementUpgrades(target, upgrades)) {
				effectsTag.add(merged.save(new CompoundTag()));
			}
		} else {
			int uses = InteractionHandler.calculateImbuedUses(target, effects);
			for (MobEffectInstance effect : effects) {
				int reducedDuration = Math.max(1, effect.getDuration() / 2);
				MobEffectInstance reduced = new MobEffectInstance(effect.getEffect(), reducedDuration,
						effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
				effectsTag.add(reduced.save(new CompoundTag()));
			}
			tag.putInt("ImbuedUses", uses);
			tag.putInt("MaxImbuedUses", uses);
		}
		tag.put("ImbuedEffects", effectsTag);
		tag.putInt("ImbuedColor", potionColor);
		ModUtils.applyDyeColor(target, potionColor);
		player.setItemInHand(hand, target);

		Level level = tile.getLevel();
		if (level != null && !level.isClientSide) {
			tile.consumeOneLayer();
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, tile.getBlockPos());
			level.playSound(null, tile.getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
			level.playSound(null, tile.getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0F, 1.15F);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.SPLASH, tile.getBlockPos().getX() + 0.5D,
						tile.getBlockPos().getY() + 1.0D, tile.getBlockPos().getZ() + 0.5D, 10, 0.2D, 0.2D, 0.2D, 0.0D);
			}
		}
		return true;
	}
}
