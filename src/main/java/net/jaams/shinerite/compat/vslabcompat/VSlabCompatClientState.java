package net.jaams.shinerite.compat.vslabcompat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VSlabCompatClientState {
	public static class Entry {
		public final int color;
		public final int imbuedColor;
		public final boolean imbued;
		public Entry(int color, int imbuedColor, boolean imbued) {
			this.color = color;
			this.imbuedColor = imbuedColor;
			this.imbued = imbued;
		}
	}

	private static final Map<Long, Entry> MAP = new ConcurrentHashMap<>();

	public static void put(BlockPos pos, int color, int imbuedColor, boolean imbued) {
		MAP.put(pos.asLong(), new Entry(color, imbuedColor, imbued));
	}

	public static void remove(BlockPos pos) {
		MAP.remove(pos.asLong());
	}

	public static Entry get(BlockPos pos) {
		return pos == null ? null : MAP.get(pos.asLong());
	}

	public static int getTint(BlockPos pos) {
		Entry e = get(pos);
		if (e == null) return -1;
		if (e.imbued && e.imbuedColor != -1) return e.imbuedColor;
		return e.color;
	}

	public static void clear() {
		MAP.clear();
	}
}
