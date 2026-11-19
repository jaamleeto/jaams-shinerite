package net.jaams.shinerite.compat.vslabcompat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.configuration.common.InteractionsConfig;
import net.jaams.shinerite.configuration.common.MechanicsConfig;
import net.jaams.shinerite.item.ShinyPaintBrushItem;
import net.jaams.shinerite.item.ShinyScraperItem;
import net.jaams.shinerite.packet.VSlabCompatStatePacket;
import net.jaams.shinerite.particle.CustomEffectParticleData;
import net.jaams.shinerite.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID)
public class VSlabCompat {
	public static final String VSC_NAMESPACE = "v_slab_compat";

	public static boolean isEnabled() {
		return ModList.get().isLoaded(VSC_NAMESPACE);
	}

	public static boolean isCompatBlock(Block block) {
		if (block == null) return false;
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
		if (id == null || !VSC_NAMESPACE.equals(id.getNamespace())) return false;
		String path = id.getPath();
		return "jaams_shinerite/cut_shinerite_vertical_slab".equals(path)
			|| "jaams_shinerite/shinerite_tile_vertical_slab".equals(path);
	}

	public static boolean isCompatBlock(Level level, BlockPos pos) {
		return level != null && isCompatBlock(level.getBlockState(pos).getBlock());
	}

	public static void sync(ServerLevel level, BlockPos pos, @Nullable VSlabCompatState state) {
		VSlabCompatStatePacket pkt;
		if (state == null) {
			pkt = new VSlabCompatStatePacket(pos, -1, -1, false, true);
		} else {
			pkt = new VSlabCompatStatePacket(pos, state.getColor(), state.getImbuedColor(), state.isImbued(), false);
		}
		JaamsShineriteMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), pkt);
	}

	public static void markChanged(ServerLevel level, BlockPos pos, VSlabCompatState state) {
		VSlabCompatSavedData.get(level).setDirty();
		sync(level, pos, state);
		BlockState bs = level.getBlockState(pos);
		level.sendBlockUpdated(pos, bs, bs, 3);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!isEnabled()) return;
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		if (!isCompatBlock(state.getBlock())) return;
		InteractionResult result = handleUse(state, level, pos, event.getEntity(), event.getHand(), event.getHitVec());
		if (result.consumesAction()) {
			event.setCanceled(true);
			event.setCancellationResult(result);
		}
	}

	public static InteractionResult handleUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!InteractionsConfig.BLOCK_INTERACTIONS.get()) {
			return InteractionResult.PASS;
		}
		ItemStack itemstack = player.getItemInHand(hand);
		Item item = itemstack.getItem();
		if (!(world instanceof ServerLevel serverLevel)) {
			// client: optimistically consume for some interactions so hand swings feel right; server is authoritative
			// but easier to let server handle and return PASS on client except simple cases
			// we'll run logic on server side
			return InteractionResult.PASS;
		}
		VSlabCompatSavedData data = VSlabCompatSavedData.get(serverLevel);
		VSlabCompatState st = data.get(pos);
		int blockColor = st != null ? st.getColor() : -1;
		boolean coated = st != null && st.isCoated();
		boolean imbued = st != null && st.isImbued();

		if (coated) {
			if (MechanicsConfig.COATING.get() && InteractionsConfig.BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem) {
				st.setCoated(false);
				world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
				ModUtils.generateCoatedRemovalParticles(world, pos, state, world.random);
				markChanged(serverLevel, pos, st);
				return InteractionResult.SUCCESS;
			}
			if (item instanceof ShinyPaintBrushItem || item instanceof DyeItem || item == Items.HONEYCOMB || item == Items.INK_SAC || item == Items.GLOW_INK_SAC) {
				long currentTime = world.getGameTime();
				if (currentTime - st.getLastCoatedParticleTime() >= 30) {
					ModUtils.generateCoatedParticles(world, pos, state, world.random);
					st.setLastCoatedParticleTime(currentTime);
					data.setDirty();
				}
				return InteractionResult.CONSUME;
			}
			return InteractionResult.PASS;
		}

		if (MechanicsConfig.IMBUING.get() && itemstack.isEmpty() && player.isCrouching() && imbued) {
			if (registerDirectContact(serverLevel, pos, st, player)) {
				// registerDirectContact consumes/applies as per BE logic
				markChanged(serverLevel, pos, st);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		if ((MechanicsConfig.IMBUED_BLOCK_COATED_CAN_IMBUE.get() || !coated) && MechanicsConfig.IMBUING.get() && InteractionsConfig.BLOCK_IMBUING.get() && item == Items.POTION && !imbued && blockColor == -1) {
			Potion potion = PotionUtils.getPotion(itemstack);
			if (potion != Potions.WATER && potion != Potions.AWKWARD && potion != Potions.THICK && potion != Potions.MUNDANE && !potion.getEffects().isEmpty()) {
				List<MobEffectInstance> effects = new ArrayList<>();
				if (potion != Potions.EMPTY) effects.addAll(potion.getEffects());
				effects.addAll(PotionUtils.getCustomEffects(itemstack));
				if (!effects.isEmpty()) {
					st = data.getOrCreate(pos);
					int potionColor = PotionUtils.getColor(itemstack);
					ListTag effectsTag = new ListTag();
					for (MobEffectInstance effect : effects) {
						MobEffectInstance savedEffect = new MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 2), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
						effectsTag.add(savedEffect.save(new CompoundTag()));
					}
					st.setImbuedState(effectsTag, 1, 1, potionColor);
					world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.15F);
					if (!player.isCreative()) {
						itemstack.shrink(1);
						player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
					}
					serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
					serverLevel.sendParticles(new CustomEffectParticleData(((potionColor >> 16) & 255) / 255.0F, ((potionColor >> 8) & 255) / 255.0F, (potionColor & 255) / 255.0F, 0.3F),
						pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.12);
					markChanged(serverLevel, pos, st);
					return InteractionResult.SUCCESS;
				}
			}
		}

		if (MechanicsConfig.WASHING.get() && InteractionsConfig.BLOCK_WASHING.get() && item == Items.POTION && (blockColor != -1 || imbued)) {
			Potion potion = PotionUtils.getPotion(itemstack);
			if (potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion.getEffects().isEmpty()) {
				st = data.getOrCreate(pos);
				if (st.getColor() != -1) st.setColor(-1);
				if (st.isImbued()) st.clearImbuedState();
				if (!player.isCreative()) {
					itemstack.shrink(1);
					player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
				}
				world.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
				serverLevel.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
				cleanupIfDefault(serverLevel, pos, st);
				markChanged(serverLevel, pos, st != null && !isDefault(st) ? st : null);
				return InteractionResult.SUCCESS;
			}
		}

		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_DYEING.get() && !imbued) {
			if (item instanceof ShinyPaintBrushItem brush && brush.hasColor(itemstack)) {
				st = data.getOrCreate(pos);
				int color = brush.getColor(itemstack);
				int cur = st.getColor();
				if (color == cur || (color == DyeColor.BLACK.getTextColor() && cur == ModUtils.lightenColor(DyeColor.BLACK.getTextColor()))) {
					return InteractionResult.PASS;
				}
				if (color == DyeColor.BLACK.getTextColor()) color = ModUtils.lightenColor(color);
				applyDye(serverLevel, pos, state, st, color);
				if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
				markChanged(serverLevel, pos, st);
				return InteractionResult.SUCCESS;
			}
			if (item instanceof DyeItem dyeItem) {
				st = data.getOrCreate(pos);
				int expectedColor = ModUtils.calculateDyeColor(st.getColor(), dyeItem);
				if (st.getColor() == expectedColor) return InteractionResult.PASS;
				applyDye(serverLevel, pos, state, st, expectedColor);
				if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
				markChanged(serverLevel, pos, st);
				return InteractionResult.SUCCESS;
			}
		}

		if (InteractionsConfig.BLOCK_SCRAPING.get() && item instanceof ShinyScraperItem) {
			if (MechanicsConfig.IMBUING.get() && imbued) {
				st = data.getOrCreate(pos);
				st.clearImbuedState();
				if (st.getColor() != -1) st.setColor(-1);
				if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
				world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
				serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F),
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.05);
				cleanupIfDefault(serverLevel, pos, st);
				markChanged(serverLevel, pos, st != null && !isDefault(st) ? st : null);
				return InteractionResult.SUCCESS;
			}
			if (MechanicsConfig.DYEING.get() && blockColor != -1) {
				st = data.getOrCreate(pos);
				st.setColor(-1);
				if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
				world.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
				cleanupIfDefault(serverLevel, pos, st);
				markChanged(serverLevel, pos, st != null && !isDefault(st) ? st : null);
				return InteractionResult.SUCCESS;
			}
		}

		if (MechanicsConfig.COATING.get() && InteractionsConfig.BLOCK_COATING.get() && item == Items.HONEYCOMB) {
			st = data.getOrCreate(pos);
			st.setCoated(true);
			if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
			ModUtils.generateCoatedParticles(world, pos, state, world.random);
			world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
			markChanged(serverLevel, pos, st);
			return InteractionResult.SUCCESS;
		}

		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_LIGHTENING.get() && item == Items.GLOW_INK_SAC && blockColor != -1 && !imbued) {
			st = data.getOrCreate(pos);
			int newColor = ModUtils.lightenColor(st.getColor());
			if (newColor == st.getColor()) return InteractionResult.PASS;
			applyDye(serverLevel, pos, state, st, newColor);
			if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
			world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			ModUtils.generateDustParticles(world, pos, newColor);
			markChanged(serverLevel, pos, st);
			return InteractionResult.SUCCESS;
		}

		if (MechanicsConfig.DYEING.get() && InteractionsConfig.BLOCK_DARKENING.get() && item == Items.INK_SAC && !imbued) {
			st = data.getOrCreate(pos);
			int cur = st.getColor();
			int newColor = cur == -1 ? 0xCDCDCD : ModUtils.darkenColor(cur);
			if (newColor == cur) return InteractionResult.PASS;
			applyDye(serverLevel, pos, state, st, newColor);
			if (!player.isCreative()) ModUtils.reduceItemStack(player, itemstack);
			world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			ModUtils.generateDustParticles(world, pos, newColor);
			markChanged(serverLevel, pos, st);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	public static boolean isDefault(VSlabCompatState st) {
		return st == null || (st.getColor() == -1 && !st.isCoated() && !st.isImbued() && st.getTicksUnderRain() == 0);
	}

	public static void cleanupIfDefault(ServerLevel level, BlockPos pos, VSlabCompatState st) {
		if (st != null && isDefault(st)) {
			VSlabCompatSavedData.get(level).remove(pos);
			sync(level, pos, null);
		}
	}

	private static void applyDye(ServerLevel level, BlockPos pos, BlockState state, VSlabCompatState st, int color) {
		st.setColor(color);
		level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
		// particles similar to ModUtils.applyDye but without requiring BE
		net.minecraft.world.phys.shapes.VoxelShape shape = state.getShape(level, pos);
		net.minecraft.world.phys.AABB aabb = shape.bounds().inflate(0.25);
		float[] colorComponents = new float[]{(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F};
		for (int i = 0; i < 50; i++) {
			double x = pos.getX() + aabb.minX + level.random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + level.random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + level.random.nextDouble() * (aabb.maxZ - aabb.minZ);
			level.sendParticles(new DustParticleOptions(new Vector3f(colorComponents[0], colorComponents[1], colorComponents[2]), 1.0F), x, y, z, 1, 0.0, 0.1, 0.0, 0.0);
		}
	}

	// Imbued contact/apply logic (simplified mirror of BaseShineriteBlockEntity)
	public static boolean registerDirectContact(ServerLevel level, BlockPos pos, VSlabCompatState st, LivingEntity target) {
		if (st == null || !st.isImbued()) return false;
		applyImbuedEffectsTo(st, target);
		spawnApplyFeedback(level, pos, st);
		st.clearImbuedState();
		st.setColor(-1);
		return true;
	}

	private static void applyImbuedEffectsTo(VSlabCompatState st, LivingEntity target) {
		ListTag effects = st.getImbuedEffects();
		if (effects == null) return;
		int minTicks = MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get();
		for (int i = 0; i < effects.size(); i++) {
			CompoundTag effectData = effects.getCompound(i);
			MobEffectInstance savedEffect = ModUtils.loadImbuedEffect(effectData);
			if (savedEffect == null) continue;
			net.minecraft.world.effect.MobEffect effect = savedEffect.getEffect();
			int finalDuration = Math.max(minTicks, savedEffect.getDuration());
			if (effect.isInstantenous()) {
				effect.applyInstantenousEffect(null, null, target, savedEffect.getAmplifier(), 1.0);
			} else {
				target.addEffect(new MobEffectInstance(effect, finalDuration, savedEffect.getAmplifier(), savedEffect.isAmbient(), savedEffect.isVisible(), savedEffect.showIcon()));
			}
		}
	}

	private static void spawnApplyFeedback(ServerLevel level, BlockPos pos, VSlabCompatState st) {
		level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 1.25F);
		int imbuedColor = st.getImbuedColor();
		if (imbuedColor == -1) return;
		float r = ((imbuedColor >> 16) & 255) / 255.0F;
		float g = ((imbuedColor >> 8) & 255) / 255.0F;
		float b = (imbuedColor & 255) / 255.0F;
		level.sendParticles(new CustomEffectParticleData(r, g, b, 0.3F),
			pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 10, 0.35, 0.25, 0.35, 0.12);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
		if (!isEnabled()) return;
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		BlockPos pos = event.getPos();
		BlockState state = event.getPlacedBlock();
		if (!isCompatBlock(state.getBlock())) return;
		if (!(event.getEntity() instanceof LivingEntity placer)) return;
		ItemStack stack = placer.getItemInHand(placer.getUsedItemHand());
		if (stack.isEmpty() || !isCompatBlock(Block.byItem(stack.getItem()))) {
			// try main/offhand
			if (placer instanceof Player p) {
				if (isCompatBlock(Block.byItem(p.getMainHandItem().getItem()))) stack = p.getMainHandItem();
				else if (isCompatBlock(Block.byItem(p.getOffhandItem().getItem()))) stack = p.getOffhandItem();
				else return;
			} else return;
		}
		VSlabCompatSavedData data = VSlabCompatSavedData.get(level);
		VSlabCompatState st = data.getOrCreate(pos);
		boolean any = false;
		int itemColor = ModUtils.getCurrentColor(stack);
		if (itemColor != -1) {
			st.setColor(itemColor);
			any = true;
		}
		if (stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if (tag != null) {
				if (tag.contains("Coated")) { st.setCoated(tag.getBoolean("Coated")); any = true; }
				if (tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
					ListTag effects = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
					int uses = tag.getInt("ImbuedUses");
					int maxUses = tag.getInt("MaxImbuedUses");
					int imbuedColor = ModUtils.getImbuedColor(stack);
					st.setImbuedState(effects, uses, maxUses, imbuedColor);
					any = true;
				}
			}
		}
		if (any) {
			markChanged(level, pos, st);
		} else {
			// keep default (empty) state out to save space
			if (isDefault(st)) {
				data.remove(pos);
				sync(level, pos, null);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!isEnabled()) return;
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		BlockPos pos = event.getPos();
		BlockState state = event.getState();
		if (!isCompatBlock(state.getBlock())) return;
		VSlabCompatSavedData data = VSlabCompatSavedData.get(level);
		VSlabCompatState st = data.get(pos);

		Player player = event.getPlayer();
		// imbuement transfer to breaker
		if (st != null && st.isImbued()) {
			transferImbuementToBreaker(level, pos, st, player);
		}

		if (!player.isCreative()) {
			ItemStack drop = createDrop(state, st);
			Block.popResource(level, pos, drop);
		}
		// remove state and prevent default drops by canceling and manually breaking
		data.remove(pos);
		sync(level, pos, null);
		// play break effects like vanilla
		level.levelEvent(null, 2001, pos, Block.getId(state));
		level.removeBlock(pos, false);
		event.setCanceled(true);
	}

	private static ItemStack createDrop(BlockState state, @Nullable VSlabCompatState st) {
		ItemStack stack = new ItemStack(state.getBlock().asItem());
		if (st == null) return stack;
		int color = st.getColor();
		if (st.isImbued()) color = -1; // mirror BaseShineriteBlock.getDrops
		CompoundTag tag = stack.getOrCreateTag();
		if (color != -1) tag.putInt("color", color);
		if (st.isCoated()) tag.putBoolean("Coated", true);
		// imbued state is not preserved in drops for normal shinerite blocks? getDrops sets color -1 if imbued but doesn't copy effects - matches behavior seen
		return stack;
	}

	private static void transferImbuementToBreaker(ServerLevel level, BlockPos pos, VSlabCompatState st, Player player) {
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.BLOCK_BREAK_TRANSFER_IMBUEMENT.get()) return;
		if (ModUtils.hasRestrictedEffect(player)) return;
		ListTag effects = st.getImbuedEffects();
		if (effects == null || effects.isEmpty()) return;
		float factor = MechanicsConfig.IMBUED_BLOCK_BREAK_DURATION_FACTOR.get().floatValue();
		int minTicks = MechanicsConfig.IMBUED_MIN_DURATION_TICKS.get();
		for (int i = 0; i < effects.size(); i++) {
			CompoundTag effectData = effects.getCompound(i);
			MobEffectInstance savedEffect = ModUtils.loadImbuedEffect(effectData);
			if (savedEffect == null) continue;
			net.minecraft.world.effect.MobEffect effect = savedEffect.getEffect();
			if (effect.isInstantenous()) {
				effect.applyInstantenousEffect(null, null, player, savedEffect.getAmplifier(), 1.0D);
				continue;
			}
			int duration = Math.max(minTicks, (int) (savedEffect.getDuration() * factor));
			player.addEffect(new MobEffectInstance(effect, duration, savedEffect.getAmplifier(), savedEffect.isAmbient(), savedEffect.isVisible(), savedEffect.showIcon()));
		}
		level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.9F);
		if (st.getImbuedColor() != -1) {
			ModUtils.generateImbuedBurstParticles(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, st.getImbuedColor(), 30);
		}
	}

	@SubscribeEvent
	public static void onProjectileImpact(net.minecraftforge.event.entity.ProjectileImpactEvent event) {
		if (!isEnabled()) return;
		if (!(event.getRayTraceResult() instanceof BlockHitResult hit)) return;
		Projectile projectile = event.getProjectile();
		Level level = projectile.level();
		if (!(level instanceof ServerLevel serverLevel)) return;
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!isCompatBlock(state.getBlock())) return;

		VSlabCompatSavedData data = VSlabCompatSavedData.get(serverLevel);
		VSlabCompatState st = data.get(pos);
		if (st == null) st = new VSlabCompatState();
		boolean tracked = data.get(pos) != null;
		int color = st.getColor();
		Vec3 hitLocation = hit.getLocation();

		boolean isPotionProjectile = projectile instanceof ThrownPotion potion
			&& (potion.getItem().getItem() == Items.SPLASH_POTION || potion.getItem().getItem() == Items.LINGERING_POTION);
		if (!isPotionProjectile) {
			ModUtils.generateProjectileHitParticles(serverLevel, hitLocation, color);
		}

		if (projectile instanceof ThrownPotion potion) {
			ItemStack potionStack = potion.getItem();
			Item potionItem = potionStack.getItem();
			if (potionItem == Items.SPLASH_POTION || potionItem == Items.LINGERING_POTION) {
				Potion thrownPotion = PotionUtils.getPotion(potionStack);
				boolean isWaterPotion = thrownPotion == Potions.WATER || thrownPotion == Potions.AWKWARD || thrownPotion == Potions.THICK || thrownPotion == Potions.MUNDANE || thrownPotion.getEffects().isEmpty();
				if (!isWaterPotion) {
					if (tryImbueFromThrownPotion(serverLevel, pos, st, potionStack)) {
						data.put(pos, st);
						sync(serverLevel, pos, st);
					}
				} else if (!st.isCoated()) {
					boolean modified = false;
					if (st.getColor() != -1) { st.setColor(-1); modified = true; }
					if (st.isImbued()) { st.clearImbuedState(); modified = true; }
					if (modified) {
						serverLevel.sendParticles(ParticleTypes.SPLASH, hitLocation.x, hitLocation.y, hitLocation.z, 10, 0.2, 0.2, 0.2, 0.0);
						if (isDefault(st)) {
							data.remove(pos);
							sync(serverLevel, pos, null);
						} else {
							data.put(pos, st);
							sync(serverLevel, pos, st);
						}
					}
				}
			}
		}

		if (!tracked && isDefault(st)) {
			// nothing to store
		} else if (tracked && isDefault(st)) {
			data.remove(pos);
			sync(serverLevel, pos, null);
		}
	}

	private static boolean tryImbueFromThrownPotion(ServerLevel level, BlockPos pos, VSlabCompatState st, ItemStack potionStack) {
		if (!MechanicsConfig.IMBUING.get() || !InteractionsConfig.BLOCK_IMBUING.get()) return false;
		if (st.isImbued() || st.getColor() != -1 || (st.isCoated() && !MechanicsConfig.IMBUED_BLOCK_COATED_CAN_IMBUE.get())) return false;
		Potion potion = PotionUtils.getPotion(potionStack);
		if (potion == Potions.WATER || potion == Potions.AWKWARD || potion == Potions.THICK || potion == Potions.MUNDANE || potion.getEffects().isEmpty()) return false;
		List<MobEffectInstance> effects = new ArrayList<>(potion.getEffects());
		effects.addAll(PotionUtils.getCustomEffects(potionStack));
		if (effects.isEmpty()) return false;
		int potionColor = PotionUtils.getColor(potionStack);
		ListTag effectsTag = new ListTag();
		for (MobEffectInstance effect : effects) {
			MobEffectInstance savedEffect = new MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 2), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
			effectsTag.add(savedEffect.save(new CompoundTag()));
		}
		st.setImbuedState(effectsTag, 1, 1, potionColor);
		level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.15F);
		level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
		level.sendParticles(new CustomEffectParticleData(((potionColor >> 16) & 255) / 255.0F, ((potionColor >> 8) & 255) / 255.0F, (potionColor & 255) / 255.0F, 0.3F),
			pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 15, 0.35, 0.25, 0.35, 0.12);
		return true;
	}

	@SubscribeEvent
	public static void onLivingFall(net.minecraftforge.event.entity.living.LivingFallEvent event) {
		if (!isEnabled()) return;
		LivingEntity entity = event.getEntity();
		if (!(entity.level() instanceof ServerLevel level)) return;
		if (!MechanicsConfig.IMBUING.get()) return;
		// check block at/below feet
		BlockPos pos = BlockPos.containing(entity.getX(), entity.getY() - 0.2D, entity.getZ());
		if (!isCompatBlock(level.getBlockState(pos).getBlock())) {
			pos = entity.blockPosition().below();
			if (!isCompatBlock(level.getBlockState(pos).getBlock())) return;
		}
		VSlabCompatSavedData data = VSlabCompatSavedData.get(level);
		VSlabCompatState st = data.get(pos);
		if (st == null || !st.isImbued()) return;
		int imbuedColor = st.getImbuedColor();
		if (registerDirectContact(level, pos, st, entity)) {
			if (imbuedColor != -1) {
				ModUtils.generateImbuedBurstParticles(level, entity.getX(), entity.getY(), entity.getZ(), imbuedColor, 50);
			}
			if (isDefault(st)) {
				data.remove(pos);
				sync(level, pos, null);
			} else {
				markChanged(level, pos, st);
			}
		}
	}

	@SubscribeEvent
	public static void onLevelTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
		if (!isEnabled() || event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel level)) return;
		if (level.getGameTime() % 10 != 0) return; // throttle
		VSlabCompatSavedData data = VSlabCompatSavedData.get(level);
		if (data.entries().isEmpty()) return;
		java.util.List<Long> toRemove = null;
		for (java.util.Map.Entry<Long, VSlabCompatState> e : data.entries().entrySet()) {
			BlockPos pos = BlockPos.of(e.getKey());
			BlockState bs = level.getBlockState(pos);
			if (!isCompatBlock(bs.getBlock())) {
				if (toRemove == null) toRemove = new java.util.ArrayList<>();
				toRemove.add(e.getKey());
				sync(level, pos, null);
				continue;
			}
			boolean changed = tickState(level, pos, bs, e.getValue());
			if (isDefault(e.getValue())) {
				if (toRemove == null) toRemove = new java.util.ArrayList<>();
				toRemove.add(e.getKey());
				sync(level, pos, null);
				changed = false;
			}
			if (changed) {
				data.setDirty();
				sync(level, pos, e.getValue());
			}
		}
		if (toRemove != null) {
			for (Long k : toRemove) data.entries().remove(k);
			data.setDirty();
		}
	}

	private static boolean tickState(ServerLevel level, BlockPos pos, BlockState state, VSlabCompatState st) {
		boolean changed = false;
		// imbued ambient particles
		if (MechanicsConfig.IMBUING.get() && st.isImbued() && st.getImbuedColor() != -1) {
			int particleInterval = MechanicsConfig.IMBUED_BLOCK_PARTICLE_INTERVAL.get();
			if (particleInterval > 0 && level.getGameTime() % particleInterval == 0) {
				int c = st.getImbuedColor();
				level.sendParticles(new CustomEffectParticleData(((c >> 16) & 255) / 255.0F, ((c >> 8) & 255) / 255.0F, (c & 255) / 255.0F, 0.3F),
					pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 1, 0.3, 0.25, 0.3, 0.01);
			}
		}
		// washing / rain decay (simplified, runs every 10 ticks so scale)
		boolean hasColor = st.getColor() != -1;
		boolean imbued = MechanicsConfig.IMBUING.get() && st.isImbued();
		if (!MechanicsConfig.WASHING.get() || !MechanicsConfig.WASHING_AFFECT_BLOCKS.get() || st.isCoated() || (!hasColor && !imbued)) {
			if (st.getTicksUnderRain() != 0) { st.setTicksUnderRain(0); changed = true; }
			return changed;
		}
		boolean shouldDecay = false;
		if (MechanicsConfig.WASHING_RAIN.get() && level.isRaining()) {
			if (level.isRainingAt(pos.above())) shouldDecay = true;
			if (!shouldDecay && MechanicsConfig.WASHING_RAIN_SIDES.get()) {
				BlockPos[] sidePositions = new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()};
				for (BlockPos sidePos : sidePositions) {
					BlockState sideState = level.getBlockState(sidePos);
					if (!sideState.isSolidRender(level, sidePos) && level.isRainingAt(sidePos)) { shouldDecay = true; break; }
				}
			}
		}
		if (!shouldDecay && MechanicsConfig.WASHING_WATER.get()) {
			BlockPos[] adjacentPositions = new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()};
			for (BlockPos adjacentPos : adjacentPositions) {
				if (level.getFluidState(adjacentPos).is(FluidTags.WATER)) { shouldDecay = true; break; }
			}
		}
		if (!shouldDecay) {
			if (st.getTicksUnderRain() != 0) { st.setTicksUnderRain(0); changed = true; }
			return changed;
		}
		st.setTicksUnderRain(st.getTicksUnderRain() + 10);
		int ticksPerStep = MechanicsConfig.WASHING_TICKS_PER_STEP.get();
		if (st.getTicksUnderRain() >= ticksPerStep) {
			if (imbued) {
				int uses = st.getImbuedUses() - 1;
				if (uses <= 0) {
					st.clearImbuedState();
					level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
					level.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 10, 0.3, 0.25, 0.3, 0.05);
				} else {
					int newImbuedColor = st.getImbuedColor();
					if (newImbuedColor != -1) {
						float progress = uses / (float) Math.max(1, st.getMaxImbuedUses());
						newImbuedColor = ModUtils.lightenImbuedColor(newImbuedColor, 1.0F - progress);
					}
					st.setImbuedState(st.getImbuedEffects(), uses, st.getMaxImbuedUses(), newImbuedColor);
				}
				changed = true;
			}
			if (st.getColor() != -1) {
				lightenColorStep(level, pos, state, st);
				if (MechanicsConfig.WASHING_THUNDERSTORM_BOOST.get() && level.isThundering()) {
					lightenColorStep(level, pos, state, st);
				}
				changed = true;
			}
			st.setTicksUnderRain(level.random.nextInt(Math.max(0, ticksPerStep / 5)));
		} else {
			changed = true; // tick counter advanced
		}
		return changed;
	}

	private static void lightenColorStep(ServerLevel level, BlockPos pos, BlockState state, VSlabCompatState st) {
		int color = st.getColor();
		if (color == -1) return;
		int red = Math.min(255, ((color >> 16) & 0xFF) + 10);
		int green = Math.min(255, ((color >> 8) & 0xFF) + 10);
		int blue = Math.min(255, (color & 0xFF) + 10);
		int newColor = (red << 16) | (green << 8) | blue;
		if (newColor == 0xFFFFFF) {
			st.setColor(-1);
		} else {
			st.setColor(newColor);
		}
		// dust particles like BE
		net.minecraft.world.phys.shapes.VoxelShape shape = state.getShape(level, pos);
		net.minecraft.world.phys.AABB aabb = shape.bounds().inflate(0.2);
		float r = ((color >> 16) & 255) / 255.0F;
		float g = ((color >> 8) & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		Vector3f colorVec = new Vector3f(r, g, b);
		int particleCount = level.random.nextInt(20, 41);
		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + aabb.minX + level.random.nextDouble() * (aabb.maxX - aabb.minX);
			double y = pos.getY() + aabb.minY + level.random.nextDouble() * (aabb.maxY - aabb.minY);
			double z = pos.getZ() + aabb.minZ + level.random.nextDouble() * (aabb.maxZ - aabb.minZ);
			double motionY = 0.08 + level.random.nextDouble() * 0.12;
			double motionXZ = level.random.nextGaussian() * 0.02;
			level.sendParticles(new DustParticleOptions(colorVec, 1.0F), x, y, z, 1, motionXZ, motionY, motionXZ, 0.0);
		}
	}
}
