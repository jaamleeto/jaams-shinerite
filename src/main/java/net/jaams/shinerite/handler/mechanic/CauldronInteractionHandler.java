package net.jaams.shinerite.handler.mechanic;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.jaams.shinerite.util.ModUtils;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.InteractionsConfig;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CauldronInteractionHandler {
	@SubscribeEvent
	public static void onBlockInteraction(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		Player player = event.getEntity();
		BlockPos pos = event.getPos();
		InteractionHand hand = event.getHand();
		ItemStack stack = player.getItemInHand(hand);
		BlockState state = level.getBlockState(pos);
		if (!InteractionsConfig.CAULDRON_INTERACTIONS.get()) {
			return;
		}
		if (!MechanicsConfig.WASHING.get()) {
			return;
		}
		if (!(state.getBlock() instanceof LayeredCauldronBlock cauldron) || state.getValue(LayeredCauldronBlock.LEVEL) <= 0) {
			return;
		}
		if (!ModUtils.isWashableItem(stack)) {
			return;
		}
		if (MechanicsConfig.WAXING.get() && ModUtils.isWaxed(stack)) {
			return;
		}
		boolean canWashColor = MechanicsConfig.DYEING.get() && InteractionsConfig.CAULDRON_WASH_COLOR.get();
		boolean canWashImbuement = MechanicsConfig.IMBUING.get() && InteractionsConfig.CAULDRON_WASH_IMBUEMENT.get();
		boolean hasColor = canWashColor && ModUtils.isDyed(stack);
		boolean hasImbuement = canWashImbuement && ModUtils.isImbued(stack);
		if (!hasColor && !hasImbuement) {
			return;
		}
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
		event.setCanceled(true);
		if (!level.isClientSide()) {
			handleCustomCauldronInteraction(level, pos, player, hand, stack, cauldron, hasColor, hasImbuement);
		}
	}

	private static void handleCustomCauldronInteraction(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, LayeredCauldronBlock cauldron, boolean washColor, boolean washImbuement) {
		ItemStack target = stack;
		boolean split = false;
		if (stack.getCount() > 1) {
			target = stack.split(1);
			split = true;
		}
		boolean colorRemoved = false;
		boolean imbuementRemoved = false;
		CompoundTag tag = target.getTag();
		if (washColor && ModUtils.isDyed(target)) {
			ModUtils.removeColor(target);
			colorRemoved = true;
		}
		if (washImbuement && ModUtils.isImbued(target)) {
			if (tag != null && tag.contains("WashableImbued") && !tag.getBoolean("WashableImbued")) {
			} else {
				ModUtils.removeImbuement(target);
				imbuementRemoved = true;
			}
		}
		boolean modified = colorRemoved || imbuementRemoved;
		if (modified) {
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 12, 0.2, 0.1, 0.2, 0.0);
			}
			cauldron.lowerFillLevel(level.getBlockState(pos), level, pos);
		} else {
			level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
		if (split && !target.isEmpty()) {
			if (!player.getInventory().add(target)) {
				player.drop(target, false);
			}
		}
		if (!player.getAbilities().instabuild && modified) {
			stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
		}
		player.swing(hand);
	}
}
