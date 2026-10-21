
package net.jaams.shinerite.item;

import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.init.ModSounds;
import net.jaams.shinerite.handler.event.AdvancementsHandler;
import net.jaams.shinerite.dyeable.IDyeableItem;
import net.jaams.shinerite.JaamsShineriteMod;

public class GlowSaladItem extends Item implements IDyeableItem {
	public static final int NIGHT_VISION_DURATION = 300;
	public static final int GLOWING_DURATION = 300;
	public static final int POISON_DURATION = 30;
	public static final int EXPERIENCE_MIN = 1;
	public static final int EXPERIENCE_MAX = 16;

	public GlowSaladItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON).food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3f).alwaysEat().build()));
	}

	@Override
	public int getDefaultColor() {
		return -1;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof ComposterBlock) {
			return handleComposter(context);
		}
		return super.useOn(context);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack bowl = new ItemStack(Items.BOWL);
		int glowColor = ModUtils.getCurrentColor(stack);
		boolean hadColor = hasColor(stack);
		boolean imbued = ModUtils.isImbued(stack);
		int poisonColor = getColor(stack);
		super.finishUsingItem(stack, level, entity);
		if (!level.isClientSide && entity instanceof Player player) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0));
			JaamsShineriteMod.queueServerWork(5, () -> {
				level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.EATS_GLOW_SALAD.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			});
			int xp = level.random.nextInt(EXPERIENCE_MAX - EXPERIENCE_MIN + 1) + EXPERIENCE_MIN;
			player.giveExperiencePoints(xp);
			ModUtils.applyGlowingColorAndEffect(player, hadColor ? glowColor : -1, level, GLOWING_DURATION);
			if (level instanceof ServerLevel) {
				AdvancementsHandler.grantAdvancement((ServerPlayer) player, "glow_like_dat");
			}
			if (imbued) {
				ModUtils.applyImbuedEffect(stack, level, player);
			} else if (hadColor) {
				ModUtils.applyPoisonWithParticles(level, player, poisonColor, POISON_DURATION);
			}
		}
		return stack.isEmpty() ? bowl : stack;
	}

	private InteractionResult handleComposter(UseOnContext ctx) {
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos pos = ctx.getClickedPos();
		if (level.isClientSide || !(level.getBlockState(pos).getBlock() instanceof ComposterBlock)) {
			return InteractionResult.PASS;
		}
		int levelProp = level.getBlockState(pos).getValue(ComposterBlock.LEVEL);
		if (levelProp < 7) {
			level.setBlock(pos, level.getBlockState(pos).setValue(ComposterBlock.LEVEL, levelProp + 1), 3);
			level.playSound(null, pos, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
				ItemStack bowl = new ItemStack(Items.BOWL);
				if (!player.getInventory().add(bowl)) {
					player.drop(bowl, false);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
