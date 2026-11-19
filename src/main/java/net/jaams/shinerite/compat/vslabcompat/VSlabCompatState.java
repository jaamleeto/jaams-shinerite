package net.jaams.shinerite.compat.vslabcompat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class VSlabCompatState {
	private int color = -1;
	private boolean coated = false;
	private ListTag imbuedEffects = null;
	private int imbuedUses = 0;
	private int maxImbuedUses = 0;
	private int imbuedColor = -1;
	private int ticksUnderRain = 0;
	private long lastCoatedParticleTime = 0;

	public int getColor() { return color; }
	public void setColor(int color) { this.color = color; }

	public boolean isCoated() { return coated; }
	public void setCoated(boolean coated) { this.coated = coated; }

	public boolean isImbued() { return imbuedEffects != null && !imbuedEffects.isEmpty() && imbuedUses > 0; }

	public ListTag getImbuedEffects() { return imbuedEffects; }
	public int getImbuedUses() { return imbuedUses; }
	public int getMaxImbuedUses() { return maxImbuedUses; }
	public int getImbuedColor() { return imbuedColor; }

	public void setImbuedState(ListTag effects, int uses, int maxUses, int imbuedColor) {
		this.imbuedEffects = effects;
		this.imbuedUses = uses;
		this.maxImbuedUses = maxUses;
		this.imbuedColor = imbuedColor;
	}

	public void clearImbuedState() {
		this.imbuedEffects = null;
		this.imbuedUses = 0;
		this.maxImbuedUses = 0;
		this.imbuedColor = -1;
	}

	public int getTicksUnderRain() { return ticksUnderRain; }
	public void setTicksUnderRain(int ticksUnderRain) { this.ticksUnderRain = ticksUnderRain; }

	public long getLastCoatedParticleTime() { return lastCoatedParticleTime; }
	public void setLastCoatedParticleTime(long lastCoatedParticleTime) { this.lastCoatedParticleTime = lastCoatedParticleTime; }

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Color", color);
		tag.putBoolean("Coated", coated);
		tag.putInt("TicksUnderRain", ticksUnderRain);
		if (imbuedEffects != null && !imbuedEffects.isEmpty()) {
			tag.put("ImbuedEffects", imbuedEffects);
			tag.putInt("ImbuedUses", imbuedUses);
			tag.putInt("MaxImbuedUses", maxImbuedUses);
			tag.putInt("ImbuedColor", imbuedColor);
		}
		return tag;
	}

	public static VSlabCompatState load(CompoundTag tag) {
		VSlabCompatState s = new VSlabCompatState();
		if (tag.contains("Color", Tag.TAG_INT)) s.color = tag.getInt("Color");
		if (tag.contains("Coated", Tag.TAG_BYTE)) s.coated = tag.getBoolean("Coated");
		if (tag.contains("TicksUnderRain", Tag.TAG_INT)) s.ticksUnderRain = tag.getInt("TicksUnderRain");
		if (tag.contains("ImbuedEffects", Tag.TAG_LIST)) {
			s.imbuedEffects = tag.getList("ImbuedEffects", Tag.TAG_COMPOUND);
			s.imbuedUses = tag.getInt("ImbuedUses");
			s.maxImbuedUses = tag.getInt("MaxImbuedUses");
			s.imbuedColor = tag.getInt("ImbuedColor");
		}
		return s;
	}
}
