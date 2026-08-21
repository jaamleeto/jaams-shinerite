package net.jaams.shinerite.loader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.AddReloadListenerEvent;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

import net.jaams.shinerite.data.LootModifierData;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootModifierLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final LootModifierLoader INSTANCE = new LootModifierLoader();
    private volatile Map<ResourceLocation, LootModifierData> modifiers = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(LootModifierLoader.class);

    private LootModifierLoader() {
        super(GSON, "jaams/loot_modifier");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager,
            ProfilerFiller profiler) {
        if (resources == null) {
            LOGGER.warn("LootModifierLoader apply called with null resources");
            return;
        }
        Map<ResourceLocation, LootModifierData> newModifiers = new ConcurrentHashMap<>();
        int count = 0;
        int errors = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            ResourceLocation fileId = entry.getKey();
            try {
                LootModifierData data = GSON.fromJson(entry.getValue(), LootModifierData.class);
                if (data == null) {
                    LOGGER.warn("Loot modifier file {} returned null data", fileId);
                    errors++;
                    continue;
                }
                if (data.entries == null || data.entries.isEmpty()) {
                    LOGGER.warn("Loot modifier file {} has no entries defined", fileId);
                    errors++;
                    continue;
                }
                if (data.enabled != null && !data.enabled) {
                    LOGGER.info("Loot modifier file {} is disabled, skipping", fileId);
                    continue;
                }
                newModifiers.put(fileId, data);
                count++;
            } catch (Exception e) {
                errors++;
                LOGGER.error("Failed to load loot modifier file: {}", fileId, e);
            }
        }
        this.modifiers = newModifiers;
        LOGGER.info("Loaded {} loot modifiers ({} errors)", count, errors);
    }

    public List<LootModifierData> getForLootTable(ResourceLocation lootTableId) {
        if (lootTableId == null) {
            return List.of();
        }
        List<LootModifierData> result = new ArrayList<>();
        String tableId = lootTableId.toString();
        for (LootModifierData data : modifiers.values()) {
            if (data == null) continue;
            if (matchesLootTable(data, tableId)) {
                result.add(data);
            }
        }
        return result;
    }

    public List<LootModifierData> getAll() {
        return new ArrayList<>(modifiers.values());
    }

    public boolean evaluateConditions(LootModifierData data, net.minecraft.world.level.storage.loot.LootContext context) {
        if (data == null) return false;
        if (data.conditions == null || data.conditions.isEmpty()) return true;
        return net.jaams.shinerite.condition.ConditionEvaluator.evaluateAll(data.conditions, data.condition_mode,
                resolveStack(context));
    }

    private net.minecraft.world.item.ItemStack resolveStack(net.minecraft.world.level.storage.loot.LootContext context) {
        if (context == null) return null;
        var param = context.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY);
        if (param instanceof net.minecraft.world.entity.player.Player player && !player.getMainHandItem().isEmpty()) {
            return player.getMainHandItem();
        }
        return null;
    }

    private boolean matchesLootTable(LootModifierData data, String tableId) {
        if (data.loot_tables.isEmpty()) {
            return !isExcluded(data, tableId);
        }
        boolean matches = false;
        for (String pattern : data.loot_tables) {
            if (evaluatePattern(pattern, tableId)) {
                matches = true;
                break;
            }
        }
        if (!matches) return false;
        return !isExcluded(data, tableId);
    }

    private boolean isExcluded(LootModifierData data, String tableId) {
        if (data.exclude_loot_tables == null || data.exclude_loot_tables.isEmpty()) {
            return false;
        }
        for (String excluded : data.exclude_loot_tables) {
            if (evaluatePattern(excluded, tableId)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluatePattern(String pattern, String value) {
        if (pattern.startsWith("regex:")) {
            return value.matches(pattern.substring(6));
        }
        if (pattern.contains("*")) {
            String regex = "^" + pattern.replace("*", ".*") + "$";
            return value.matches(regex);
        }
        return value.equals(pattern);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }
}