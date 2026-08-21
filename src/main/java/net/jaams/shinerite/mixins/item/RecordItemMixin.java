package net.jaams.shinerite.mixins.item;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.jaams.shinerite.item.MusicDiscShinyItem;

import javax.annotation.Nullable;

import java.util.List;

@Mixin(RecordItem.class)
public abstract class RecordItemMixin {
	@Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
	private void customShinyDiscTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltipLines, TooltipFlag flag, CallbackInfo ci) {
		if (stack.getItem() instanceof MusicDiscShinyItem shinyDisc) {
			ci.cancel();
			int color = shinyDisc.getColor(stack);
			String colorCategory = getColorCategory(color);
			String songName = switch (colorCategory) {
				case "WHITE" -> "Shiny";
				case "ORANGE" -> "California";
				case "MAGENTA" -> "Lithium";
				case "LIGHT_BLUE" -> "Sadness";
				case "YELLOW" -> "Special";
				case "LIME" -> "Spy";
				case "PINK" -> "Family";
				case "GRAY" -> "Blade";
				case "LIGHT_GRAY" -> "Hunter";
				case "CYAN" -> "Holy";
				case "PURPLE" -> "Gurenge";
				case "BLUE" -> "Sadness";
				case "BROWN" -> "War";
				case "GREEN" -> "Toxicity";
				case "RED" -> "Bloody";
				case "BLACK" -> "Revenge";
				default -> "???";
			};
			tooltipLines.add(Component.literal("jaamleeto - " + songName).withStyle(ChatFormatting.GRAY));
		}
	}

	private String getColorCategory(int color) {
		int[][] preciseColors = {{0xF9FFFE, /* WHITE */}, {0xF9801D, /* ORANGE */}, {0xC74EBD, /* MAGENTA */}, {0x3AB3DA, /* LIGHT_BLUE */}, {0xFED83D, /* YELLOW */}, {0x80C71F, /* LIME */}, {0xF38BAA, /* PINK */}, {0x474F52, /* GRAY */},
				{0x9D9D97, /* LIGHT_GRAY */}, {0x169C9C, /* CYAN */}, {0x8932B8, /* PURPLE */}, {0x3C44AA, /* BLUE */}, {0x835432, /* BROWN */}, {0x5E7C16, /* GREEN */}, {0xB02E26, /* RED */}, {0x1D1D21, /* BLACK */}};
		String[] names = {"WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"};
		for (int i = 0; i < preciseColors.length; i++) {
			int target = preciseColors[i][0];
			if (colorMatches(color, target, 25)) {
				return names[i];
			}
		}
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		if (r > 220 && g > 220 && b > 220)
			return "WHITE";
		if (r > g + 50 && r > b + 50 && r > 120)
			return "RED";
		if (g > r + 50 && g > b + 50 && g > 120)
			return "LIME";
		if (b > r + 50 && b > g + 50 && b > 120)
			return "BLUE";
		if (r < 100 && g < 100 && b < 100)
			return "BLACK";
		return "NEVER";
	}

	private boolean colorMatches(int color1, int color2, int tolerance) {
		int r1 = (color1 >> 16) & 0xFF;
		int g1 = (color1 >> 8) & 0xFF;
		int b1 = color1 & 0xFF;
		int r2 = (color2 >> 16) & 0xFF;
		int g2 = (color2 >> 8) & 0xFF;
		int b2 = color2 & 0xFF;
		return Math.abs(r1 - r2) <= tolerance && Math.abs(g1 - g2) <= tolerance && Math.abs(b1 - b2) <= tolerance;
	}
}
