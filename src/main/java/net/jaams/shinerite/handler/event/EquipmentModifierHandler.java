package net.jaams.shinerite.handler.event;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import net.jaams.shinerite.loader.EquipmentModifierLoader;

@Mod.EventBusSubscriber(modid = "jaams_shinerite", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EquipmentModifierHandler {
    private static final Logger LOGGER = LogManager.getLogger(EquipmentModifierHandler.class);

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Player) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        if (EquipmentModifierLoader.INSTANCE.getForEntityType(mob.getType()).isEmpty()) return;

        EquipmentModifierLoader.INSTANCE.applySpawnEquipment(mob, mob.getRandom());
    }
}
