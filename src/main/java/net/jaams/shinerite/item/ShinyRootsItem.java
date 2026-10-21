
package net.jaams.shinerite.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.init.ModSounds;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.JaamsShineriteMod;

public class ShinyRootsItem extends Item implements IDyeableItem {
	public static final int GLOWING_DURATION = 40;
	public static final int POISON_DURATION = 20;
	public static final int EXPERIENCE_MIN = 1;
	public static final int EXPERIENCE_MAX = 6;

	public ShinyRootsItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON).food(new FoodProperties.Builder().nutrition(1).saturationMod(0.3f).alwaysEat().build()));
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		int glowColor = ModUtils.getCurrentColor(stack);
		boolean hadColor = hasColor(stack);
		boolean imbued = ModUtils.isImbued(stack);
		int poisonColor = getColor(stack);
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (!level.isClientSide && entity instanceof Player player) {
			JaamsShineriteMod.queueServerWork(5, () -> {
				level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.EATS_GLOW_ROOTS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			});
			int xp = level.random.nextInt(EXPERIENCE_MAX - EXPERIENCE_MIN + 1) + EXPERIENCE_MIN;
			player.giveExperiencePoints(xp);
			player.giveExperiencePoints(xp);
			ModUtils.applyGlowingColorAndEffect(player, hadColor ? glowColor : -1, level, GLOWING_DURATION);
			if (imbued) {
				ModUtils.applyImbuedEffect(stack, level, player);
			} else if (hadColor) {
				ModUtils.applyPoisonWithParticles(level, player, poisonColor, POISON_DURATION);
			}
		}
		return result;
	}
}
