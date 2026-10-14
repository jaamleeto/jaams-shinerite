package net.jaams.shinerite.handler.behavior.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.jaams.shinerite.component.block.BaseShineriteBlock;
import net.jaams.shinerite.component.block.BaseShineriteBlockEntity;
import org.joml.Vector3f;

import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.configuration.common.MobBehaviorConfig;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.util.ModUtils;

/**
 * Lets mobs holding a Shiny Paint Brush or a Shiny Scraper use those tools on nearby Shinerite
 * blocks, the same way a player would by right clicking them: the brush pushes a block towards the
 * brush's own color, the scraper pulls it back and strips imbuements.
 *
 * <p>This is flavour only. It never consumes the tool and it never hands the block's imbuement to
 * the mob; it only reshapes the block, which is what makes a shinerite-carrying mob feel like it
 * belongs in a shinerite world. Everything is gated behind {@link MechanicsConfig#DYEING} plus the
 * interaction switches so it can be disabled without touching code.
 */
public class MobToolBehaviorHandler {

	

	private static final Map<UUID, State> STATES = new HashMap<>();

	private MobToolBehaviorHandler() {
	}

	private static final int APPROACH_REPATH_TICKS = 10;
	// Candidate stand spots around a target block: same level, one step down, one step up and two/three down.
	private static final BlockPos[] NEIGHBORS = new BlockPos[]{
		new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
		new BlockPos(1, -1, 0), new BlockPos(-1, -1, 0), new BlockPos(0, -1, 1), new BlockPos(0, -1, -1),
		new BlockPos(0, 1, 0), new BlockPos(1, 1, 0), new BlockPos(-1, 1, 0), new BlockPos(0, 1, 1), new BlockPos(0, 1, -1),
		new BlockPos(0, -2, 0), new BlockPos(0, -3, 0)
	};

	public static void removeState(UUID uuid) {
		STATES.remove(uuid);
	}

	public static boolean tryExecute(Mob mob, long tick) {
		if (mob.level().isClientSide || !InteractionsConfig.BLOCK_INTERACTIONS.get()
				|| !MobBehaviorConfig.MOB_TOOL_BEHAVIOR.get()) {
			return false;
		}
		ItemStack tool = findTool(mob);
		if (tool.isEmpty()) {
			STATES.remove(mob.getUUID());
			return false;
		}
		State state = STATES.computeIfAbsent(mob.getUUID(), k -> new State());

		if (state.target != null && !isStillTarget(mob, state.target)) {
			clearTarget(mob, state);
			state.nextScanTick = tick + MobBehaviorConfig.MOB_TOOL_SCAN_INTERVAL.get();
		}

		if (state.target == null) {
			if (tick < state.nextScanTick) {
				return false;
			}
			state.target = findShineriteBlock(mob);
			state.nextScanTick = tick + MobBehaviorConfig.MOB_TOOL_SCAN_INTERVAL.get();
			if (state.target == null) {
				return false;
			}
		}

		if (!isWithinReach(mob, state.target)) {
			approach(mob, state, tick);
			return false;
		}

		// In reach: stop dead, look straight at the block and swing as the tool touches it.
		stopAndFace(mob, state.target);
		BaseShineriteBlockEntity blockEntity = getBlockEntity(mob.level(), state.target);
		if (blockEntity == null) {
			clearTarget(mob, state);
			state.nextScanTick = tick + MobBehaviorConfig.MOB_TOOL_SCAN_INTERVAL.get();
			return false;
		}
		BlockPos pos = state.target;
		boolean worked;
		if (tool.getItem() instanceof ShinyPaintBrushItem) {
			worked = applyBrush(mob, pos, blockEntity, tool);
		} else {
			worked = applyScraper(mob, pos, blockEntity, tool);
		}
		clearTarget(mob, state);
		// Always consume the cooldown, worked or not, so a mob does not act every single tick.
		state.nextActionTick = tick + MobBehaviorConfig.MOB_TOOL_COOLDOWN_TICKS.get();
		state.nextScanTick = state.nextActionTick;
		return worked;
	}

	/**
	 * Walks the mob towards a spot right next to its target block. The path is only rebuilt when the
	 * current one is gone (finished, blocked or interrupted), so the mob never starts and stops every few
	 * ticks while closing the distance.
	 */
	private static void approach(Mob mob, State state, long tick) {
		faceBlock(mob, state.target);
		BlockPos standPos = state.standPos;
		if (standPos == null) {
			standPos = findStandPos(mob, state.target);
			state.standPos = standPos;
			state.nextRepathTick = tick;
		}
		// Re-path when there is no path left, when the mob got stuck, or periodically to recover from stalls.
		boolean needsPath = mob.getNavigation().isDone() || tick >= state.nextRepathTick;
		if (!needsPath) {
			return;
		}
		state.nextRepathTick = tick + APPROACH_REPATH_TICKS;
		if (standPos == null) {
			// No reachable spot nearby: fall back to a plain walk and retry the search later.
			state.target = null;
			mob.getNavigation().stop();
			state.nextScanTick = tick + MobBehaviorConfig.MOB_TOOL_SCAN_INTERVAL.get();
			return;
		}
		mob.getNavigation().moveTo(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5,
			MobBehaviorConfig.MOB_TOOL_APPROACH_SPEED.get());
	}

	private static void clearTarget(Mob mob, State state) {
		state.target = null;
		state.standPos = null;
		state.nextRepathTick = 0;
		mob.getNavigation().stop();
	}

	/**
	 * Looks for a safe spot (solid ground, two free blocks for the body) as close as possible to the
	 * target block, so the mob can actually reach it instead of shuffling around it.
	 */
	private static BlockPos findStandPos(Mob mob, BlockPos target) {
		Level level = mob.level();
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (BlockPos candidate : NEIGHBORS) {
			BlockPos pos = target.offset(candidate.getX(), candidate.getY(), candidate.getZ());
			if (!isSafeStand(level, pos)) {
				continue;
			}
			double distance = mob.distanceToSqr(Vec3.atCenterOf(pos));
			if (distance < bestDistance) {
				bestDistance = distance;
				best = pos;
			}
		}
		return best;
	}

	private static boolean isSafeStand(Level level, BlockPos pos) {
		BlockPos feet = pos.above();
		BlockPos head = pos.above(2);
		return level.getBlockState(pos).isSolidRender(level, pos)
			&& level.getBlockState(feet).isAir()
			&& level.getBlockState(head).isAir();
	}

	private static boolean isWithinReach(Mob mob, BlockPos pos) {
		if (pos == null) {
			return false;
		}
		double reach = MobBehaviorConfig.MOB_TOOL_INTERACT_DISTANCE.get();
		double dx = pos.getX() + 0.5 - mob.getX();
		double dy = pos.getY() + 0.5 - mob.getEyeY();
		double dz = pos.getZ() + 0.5 - mob.getZ();
		return dx * dx + dy * dy + dz * dz <= reach * reach;
	}

	private static boolean isStillTarget(Mob mob, BlockPos pos) {
		return mob.level().getBlockState(pos).getBlock() instanceof BaseShineriteBlock
			&& mob.distanceToSqr(Vec3.atCenterOf(pos)) <= MobBehaviorConfig.MOB_TOOL_SEARCH_RADIUS.get()
				* MobBehaviorConfig.MOB_TOOL_SEARCH_RADIUS.get();
	}

	private static void stopAndFace(Mob mob, BlockPos pos) {
		mob.getNavigation().stop();
		mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
		faceBlock(mob, pos);
	}

	private static void faceBlock(Mob mob, BlockPos pos) {
		if (pos == null) {
			return;
		}
		double dx = pos.getX() + 0.5 - mob.getX();
		double dy = pos.getY() + 0.5 - mob.getEyeY();
		double dz = pos.getZ() + 0.5 - mob.getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		mob.setYRot((float) (Mth.atan2(dz, dx) * 180.0F / Math.PI) - 90.0F);
		mob.setYHeadRot(mob.getYRot());
		mob.setYBodyRot(mob.getYRot());
		mob.setXRot((float) (-(Mth.atan2(dy, horizontal) * 180.0F / Math.PI)));
		mob.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}

	private static boolean applyBrush(Mob mob, BlockPos pos, BaseShineriteBlockEntity blockEntity, ItemStack tool) {
		if (!MobBehaviorConfig.MOB_TOOL_BRUSH_DYEING.get() || !InteractionsConfig.BLOCK_DYEING.get()) {
			return false;
		}
		if (!MechanicsConfig.DYEING.get() || blockEntity.isImbued()) {
			return false;
		}
		if (!(tool.getItem() instanceof ShinyPaintBrushItem brush) || !brush.hasColor(tool)) {
			return false;
		}
		int color = brush.getColor(tool);
		int blockColor = blockEntity.getColor();
		if (color == blockColor || (color == DyeColor.BLACK.getTextColor() && blockColor == ModUtils.lightenColor(DyeColor.BLACK.getTextColor()))) {
			return false;
		}
		if (color == DyeColor.BLACK.getTextColor()) {
			color = ModUtils.lightenColor(color);
		}
		Level level = mob.level();
		// applyDye already plays the dye sound and spawns the color dust particles.
		ModUtils.applyDye(level, pos, level.getBlockState(pos), color);
		performSwing(mob, tool, pos);
		consumeTool(mob, tool);
		return true;
	}

	private static boolean applyScraper(Mob mob, BlockPos pos, BaseShineriteBlockEntity blockEntity, ItemStack tool) {
		if (!MobBehaviorConfig.MOB_TOOL_SCRAPER_SCRAPING.get() || !InteractionsConfig.BLOCK_SCRAPING.get()) {
			return false;
		}
		Level level = mob.level();
		boolean stripped = false;
		if (MechanicsConfig.IMBUING.get() && blockEntity.isImbued()) {
			blockEntity.clearImbuedState();
			blockEntity.setChanged();
			if (blockEntity.getColor() != -1) {
				ModUtils.applyDye(level, pos, level.getBlockState(pos), -1);
			}
			level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
			spawnWhiteDust(level, pos);
			stripped = true;
		} else if (MechanicsConfig.DYEING.get() && blockEntity.getColor() != -1) {
			ModUtils.applyDye(level, pos, level.getBlockState(pos), -1);
			level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			stripped = true;
		}
		if (stripped) {
			performSwing(mob, tool, pos);
			consumeTool(mob, tool);
		}
		return stripped;
	}

	private static void spawnWhiteDust(Level level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F),
				pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.05);
		}
	}

	private static void performSwing(Mob mob, ItemStack tool, BlockPos pos) {
		faceBlock(mob, pos);
		InteractionHand hand = tool == mob.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		mob.swing(hand, true);
	}

	private static void consumeTool(Mob mob, ItemStack tool) {
		if (MobBehaviorConfig.MOB_TOOL_CONSUME_DURABILITY.get()) {
			tool.hurtAndBreak(1, mob, entity -> {
			});
		}
	}

	private static ItemStack findTool(Mob mob) {
		ItemStack main = mob.getMainHandItem();
		if (isTool(main)) {
			return main;
		}
		ItemStack off = mob.getOffhandItem();
		return isTool(off) ? off : ItemStack.EMPTY;
	}

	private static boolean isTool(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return stack.getItem() instanceof ShinyPaintBrushItem || stack.getItem() instanceof ShinyScraperItem;
	}

	private static BlockPos findShineriteBlock(Mob mob) {
		AABB box = mob.getBoundingBox().inflate(MobBehaviorConfig.MOB_TOOL_SEARCH_RADIUS.get());
		BlockPos min = new BlockPos((int) Math.floor(box.minX), (int) Math.floor(box.minY), (int) Math.floor(box.minZ));
		BlockPos max = new BlockPos((int) Math.floor(box.maxX), (int) Math.floor(box.maxY), (int) Math.floor(box.maxZ));
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (mob.level().getBlockState(pos).getBlock() instanceof BaseShineriteBlock) {
				cursor.set(pos);
				return cursor.immutable();
			}
		}
		return null;
	}

	private static BaseShineriteBlockEntity getBlockEntity(Level level, BlockPos pos) {
		if (!(level.getBlockState(pos).getBlock() instanceof BaseShineriteBlock)) {
			return null;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		return blockEntity instanceof BaseShineriteBlockEntity shineriteBlockEntity ? shineriteBlockEntity : null;
	}

	private static class State {
		private long nextActionTick;
		private long nextScanTick;
		private long nextRepathTick;
		private BlockPos target;
		private BlockPos standPos;
	}
}
