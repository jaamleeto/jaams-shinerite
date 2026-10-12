package net.jaams.shinerite.sync;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.jaams.shinerite.loader.EquipmentModifierLoader;
import net.jaams.shinerite.loader.ItemModifierLoader;
import net.jaams.shinerite.loader.LootModifierLoader;
import net.jaams.shinerite.loader.TabModifierLoader;
import net.jaams.shinerite.loader.ThrowableModifierLoader;
import net.jaams.shinerite.loader.TradeModifierLoader;
import net.jaams.shinerite.loader.TraitModifierLoader;

public final class ModLoaderSync {
    private static final Map<String, NetworkSyncable> SYNCABLES = new LinkedHashMap<>();

    static {
        register(ItemModifierLoader.INSTANCE);
        register(ThrowableModifierLoader.INSTANCE);
        register(TraitModifierLoader.INSTANCE);
        register(TabModifierLoader.INSTANCE);
        register(EquipmentModifierLoader.INSTANCE);
        register(LootModifierLoader.INSTANCE);
        register(TradeModifierLoader.INSTANCE);
    }

    private ModLoaderSync() {
    }

    public static void register(NetworkSyncable syncable) {
        SYNCABLES.put(syncable.getSyncId(), syncable);
    }

    public static NetworkSyncable get(String syncId) {
        return SYNCABLES.get(syncId);
    }

    public static Collection<NetworkSyncable> all() {
        return SYNCABLES.values();
    }
}
