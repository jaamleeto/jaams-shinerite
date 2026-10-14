package net.jaams.shinerite.handler.event;

import net.jaams.shinerite.JaamsShineriteMod;
import net.jaams.shinerite.handler.behavior.item.MobThrowableBehaviorHandler;
import net.jaams.shinerite.handler.behavior.item.MobToolBehaviorHandler;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JaamsShineriteMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobBehaviorHandler {

	public static final TagKey<EntityType<?>> TAG_THROWABLE =
			TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(JaamsShineriteMod.MODID, "behavior/throwable"));

	@SubscribeEvent
	public static void onMobBehaviorTick(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		if (!(entity instanceof Mob mob)) return;
		if (mob.isRemoved() || !mob.isAlive()) {
			MobThrowableBehaviorHandler.removeState(mob.getUUID());
			MobToolBehaviorHandler.removeState(mob.getUUID());
			return;
		}
		long gameTime = mob.level().getGameTime();
		if (mob.getType().is(TAG_THROWABLE)) {
			MobThrowableBehaviorHandler.tryExecute(mob, gameTime);
		}
		MobToolBehaviorHandler.tryExecute(mob, gameTime);
	}
}
