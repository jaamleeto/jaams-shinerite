package net.jaams.shinerite.compat.vslabcompat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class VSlabCompatSavedData extends SavedData {
	private static final String DATA_NAME = "jaams_shinerite_vslab_compat";
	private final Map<Long, VSlabCompatState> states = new HashMap<>();

	public static VSlabCompatSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(VSlabCompatSavedData::load, VSlabCompatSavedData::new, DATA_NAME);
	}

	public VSlabCompatState get(BlockPos pos) {
		return states.get(pos.asLong());
	}

	public VSlabCompatState getOrCreate(BlockPos pos) {
		return states.computeIfAbsent(pos.asLong(), k -> {
			setDirty();
			return new VSlabCompatState();
		});
	}

	public void put(BlockPos pos, VSlabCompatState state) {
		if (state == null) {
			states.remove(pos.asLong());
		} else {
			states.put(pos.asLong(), state);
		}
		setDirty();
	}

	public void remove(BlockPos pos) {
		if (states.remove(pos.asLong()) != null) {
			setDirty();
		}
	}

	public void move(BlockPos from, BlockPos to) {
		VSlabCompatState s = states.remove(from.asLong());
		if (s != null) {
			states.put(to.asLong(), s);
			setDirty();
		}
	}

	public Map<Long, VSlabCompatState> entries() {
		return states;
	}

	public static VSlabCompatSavedData load(CompoundTag tag) {
		VSlabCompatSavedData data = new VSlabCompatSavedData();
		ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag e = list.getCompound(i);
			long pos = e.getLong("Pos");
			VSlabCompatState s = VSlabCompatState.load(e.getCompound("State"));
			data.states.put(pos, s);
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();
		for (Map.Entry<Long, VSlabCompatState> entry : states.entrySet()) {
			CompoundTag e = new CompoundTag();
			e.putLong("Pos", entry.getKey());
			e.put("State", entry.getValue().save());
			list.add(e);
		}
		tag.put("Entries", list);
		return tag;
	}

	public void cleanup(ServerLevel level) {
		boolean changed = false;
		Iterator<Map.Entry<Long, VSlabCompatState>> it = states.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Long, VSlabCompatState> entry = it.next();
			BlockPos pos = BlockPos.of(entry.getKey());
			if (!VSlabCompat.isCompatBlock(level.getBlockState(pos).getBlock())) {
				it.remove();
				changed = true;
			}
		}
		if (changed) setDirty();
	}
}
