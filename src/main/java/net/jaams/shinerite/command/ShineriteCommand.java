package net.jaams.shinerite.command;

import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.g;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterCommandsEvent;

import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.util.ModUtils;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;

@Mod.EventBusSubscriber
public class ShineriteCommand {
	private static final int MIN_DURATION_SECONDS = 1;
	private static final int MAX_DURATION_SECONDS = 9999;
	private static final int MIN_AMPLIFIER = 0;
	private static final int MAX_AMPLIFIER = 255;
	private static final int MIN_USES = 1;
	private static final int MAX_USES = 9999;
	private static final String[] SUPPORTED_COLORS = {"red", "green", "blue", "white", "black", "yellow", "purple", "cyan", "orange", "pink", "lime", "magenta", "gray", "light_gray", "brown", "gold", "aqua", "violet", "indigo", "teal"};

	private static String getModVersion() {
		try {
			return ModList.get().getModContainerById("jaams_shinerite").map(c -> c.getModInfo().getVersion().toString()).orElse("Unknown");
		} catch (Exception e) {
			return "Unknown";
		}
	}

	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(Commands.literal("jaams").then(Commands.literal("shinerite").then(Commands.literal("version").executes(ctx -> {
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.jaams_shinerite.version", Component.literal(getModVersion()).withStyle(ChatFormatting.YELLOW)), false);
			return 1;
		}))
				// ── COLOR COMMANDS ───────────────────────────────────────────────
				.then(Commands.literal("color").requires(s -> s.hasPermission(2)).then(Commands.literal("set").then(Commands.argument("color", StringArgumentType.string()).suggests(ShineriteCommand::suggestMatchingColors).executes(ctx -> {
					ServerPlayer p = ctx.getSource().getPlayerOrException();
					ItemStack stack = p.getMainHandItem();
					if (!ModUtils.isDyeableItem(stack)) {
						ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.not_dyeable").withStyle(ChatFormatting.RED));
						return 0;
					}
					String input = StringArgumentType.getString(ctx, "color");
					int color = parseColor(input);
					ModUtils.applyDyeColor(stack, color);
					ctx.getSource().sendSuccess(() -> Component.translatable("commands.jaams_shinerite.color.success").withStyle(ChatFormatting.GREEN), false);
					return 1;
				}))).then(Commands.literal("rgb")
						.then(Commands.argument("r", IntegerArgumentType.integer(0, 255)).then(Commands.argument("g", IntegerArgumentType.integer(0, 255)).then(Commands.argument("b", IntegerArgumentType.integer(0, 255)).executes(ctx -> {
							ServerPlayer p = ctx.getSource().getPlayerOrException();
							ItemStack stack = p.getMainHandItem();
							if (!ModUtils.isDyeableItem(stack)) {
								ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.not_dyeable").withStyle(ChatFormatting.RED));
								return 0;
							}
							int r = IntegerArgumentType.getInteger(ctx, "r");
							int g = IntegerArgumentType.getInteger(ctx, "g");
							int b = IntegerArgumentType.getInteger(ctx, "b");
							int color = (r << 16) | (g << 8) | b;
							ModUtils.applyDyeColor(stack, color);
							ctx.getSource().sendSuccess(() -> Component.translatable("commands.jaams_shinerite.color.success").withStyle(ChatFormatting.GREEN), false);
							return 1;
						}))))).then(Commands.literal("clear").executes(ctx -> {
							ServerPlayer p = ctx.getSource().getPlayerOrException();
							ItemStack stack = p.getMainHandItem();
							if (!ModUtils.isDyeableItem(stack)) {
								ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.not_dyeable").withStyle(ChatFormatting.RED));
								return 0;
							}
							if (!ModUtils.isDyed(stack)) {
								ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.color.clear.no_color").withStyle(ChatFormatting.RED));
								return 0;
							}
							ModUtils.removeColor(stack);
							ctx.getSource().sendSuccess(() -> Component.translatable("commands.jaams_shinerite.color.clear.success").withStyle(ChatFormatting.GREEN), false);
							return 1;
						})))
				// ── IMBUE COMMANDS ───────────────────────────────────────────────
				.then(Commands.literal("imbue").requires(s -> s.hasPermission(2)).then(
						Commands.literal("set").then(Commands.argument("effect", ResourceLocationArgument.id()).suggests(ShineriteCommand::suggestMatchingEffects).then(Commands.argument("uses", IntegerArgumentType.integer(MIN_USES, MAX_USES)).then(
								Commands.argument("duration", IntegerArgumentType.integer(MIN_DURATION_SECONDS, MAX_DURATION_SECONDS)).then(Commands.argument("amplifier", IntegerArgumentType.integer(MIN_AMPLIFIER, MAX_AMPLIFIER)).executes(ctx -> {
									ServerPlayer p = ctx.getSource().getPlayerOrException();
									ItemStack stack = p.getMainHandItem();
									if (!ModUtils.isImbueableItem(stack)) {
										ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.not_imbuable").withStyle(ChatFormatting.RED));
										return 0;
									}
									ResourceLocation id = ResourceLocationArgument.getId(ctx, "effect");
									MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
									if (effect == null) {
										ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.imbue.invalid_effect", id).withStyle(ChatFormatting.RED));
										return 0;
									}
									int addedUses = IntegerArgumentType.getInteger(ctx, "uses");
									int durationSec = IntegerArgumentType.getInteger(ctx, "duration");
									int addedAmplifier = IntegerArgumentType.getInteger(ctx, "amplifier");
									int durationTicks = durationSec * 20;
									CompoundTag tag = stack.getOrCreateTag();
									ListTag effectsTag = tag.contains("ImbuedEffects", Tag.TAG_LIST) ? tag.getList("ImbuedEffects", Tag.TAG_COMPOUND) : new ListTag();
									MobEffectInstance newInstance = new MobEffectInstance(effect, durationTicks, addedAmplifier, false, true, true);
									boolean foundExisting = false;
									for (int i = 0; i < effectsTag.size(); i++) {
										CompoundTag existingTag = effectsTag.getCompound(i);
										MobEffectInstance existing = MobEffectInstance.load(existingTag);
										if (existing != null && existing.getEffect() == effect) {
											int combinedAmplifier = existing.getAmplifier() + addedAmplifier;
											MobEffectInstance combined = new MobEffectInstance(effect, Math.max(existing.getDuration(), durationTicks), combinedAmplifier, false, true, true);
											effectsTag.set(i, combined.save(new CompoundTag()));
											foundExisting = true;
											break;
										}
									}
									if (!foundExisting) {
										effectsTag.add(newInstance.save(new CompoundTag()));
									}
									tag.put("ImbuedEffects", effectsTag);
									int currentUses = tag.getInt("ImbuedUses");
									int currentMaxUses = tag.getInt("MaxImbuedUses");
									tag.putInt("ImbuedUses", currentUses + addedUses);
									tag.putInt("MaxImbuedUses", currentMaxUses + addedUses);
									List<MobEffectInstance> allEffects = new ArrayList<>();
									for (int i = 0; i < effectsTag.size(); i++) {
										allEffects.add(MobEffectInstance.load(effectsTag.getCompound(i)));
									}
									int newColor = PotionUtils.getColor(allEffects);
									tag.putInt("ImbuedColor", newColor);
									ModUtils.applyDyeColor(stack, newColor);
									if (tag.isEmpty()) {
										stack.setTag(null);
									}
									Component effectName = effect.getDisplayName();
									int displayedLevel = foundExisting ? allEffects.stream().filter(e -> e.getEffect() == effect).findFirst().map(e -> e.getAmplifier() + 1).orElse(addedAmplifier + 1) : addedAmplifier + 1;
									final Component successMessage = Component.translatable(foundExisting ? "commands.jaams_shinerite.imbue.upgraded" : "commands.jaams_shinerite.imbue.success", effectName, addedUses, displayedLevel, durationSec)
											.withStyle(ChatFormatting.GREEN);
									ctx.getSource().sendSuccess(() -> successMessage, false);
									return 1;
								}))))))
						.then(Commands.literal("clear").executes(ctx -> {
							ServerPlayer p = ctx.getSource().getPlayerOrException();
							ItemStack stack = p.getMainHandItem();
							if (!ModUtils.isImbueableItem(stack)) {
								ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.not_imbuable").withStyle(ChatFormatting.RED));
								return 0;
							}
							if (!ModUtils.isImbued(stack)) {
								ctx.getSource().sendFailure(Component.translatable("commands.jaams_shinerite.imbue.clear.no_imbuement").withStyle(ChatFormatting.RED));
								return 0;
							}
							CompoundTag tag = stack.getTag();
							if (tag != null) {
								tag.remove("ImbuedEffects");
								tag.remove("ImbuedUses");
								tag.remove("MaxImbuedUses");
								tag.remove("ImbuedColor");
								tag.remove("ImbuedDuration");
								tag.remove("ImbuedAmplifier");
								stack.setTag(tag.isEmpty() ? null : tag);
							}
							ModUtils.removeColor(stack);
							ctx.getSource().sendSuccess(() -> Component.translatable("commands.jaams_shinerite.imbue.clear.success").withStyle(ChatFormatting.GREEN), false);
							return 1;
						})))));
	}

	private static CompletableFuture<Suggestions> suggestMatchingColors(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().toLowerCase();
		for (String color : SUPPORTED_COLORS) {
			if (remaining.isEmpty() || color.startsWith(remaining))
				builder.suggest(color);
		}
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestMatchingEffects(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().toLowerCase();
		List<String> matches = new ArrayList<>();
		for (ResourceLocation id : ForgeRegistries.MOB_EFFECTS.getKeys()) {
			String full = id.toString().toLowerCase();
			String name = id.getPath().toLowerCase();
			if (full.contains(remaining) || name.contains(remaining))
				matches.add(id.toString());
		}
		matches.sort(Comparator.comparingInt((String s) -> s.equalsIgnoreCase(remaining) ? -1 : 0).thenComparing(s -> new ResourceLocation(s).getPath()));
		matches.forEach(builder::suggest);
		return builder.buildFuture();
	}

	private static int parseColor(String input) {
		String s = input.toLowerCase().trim();
		if (s.startsWith("#") || s.startsWith("0x")) {
			String hex = s.replaceFirst("^(#|0x)", "");
			if (hex.length() == 6 && hex.matches("[0-9a-fA-F]{6}")) {
				return Integer.parseUnsignedInt(hex, 16) & 0xFFFFFF;
			}
			throw new IllegalArgumentException();
		}
		return switch (s) {
			case "red" -> 0xFF0000;
			case "green" -> 0x00FF00;
			case "blue" -> 0x0000FF;
			case "white" -> 0xFFFFFF;
			case "black" -> 0x000000;
			case "yellow" -> 0xFFFF00;
			case "purple" -> 0x800080;
			case "cyan" -> 0x00FFFF;
			case "orange" -> 0xFFA500;
			case "pink" -> 0xFFC1CC;
			case "lime" -> 0x32CD32;
			case "magenta" -> 0xFF00FF;
			case "gray" -> 0x808080;
			case "light_gray" -> 0xD3D3D3;
			case "brown" -> 0x8B4513;
			case "gold" -> 0xFFD700;
			case "aqua" -> 0x00CED1;
			case "violet" -> 0xEE82EE;
			case "indigo" -> 0x4B0082;
			case "teal" -> 0x008080;
			default -> throw new IllegalArgumentException();
		};
	}
}
