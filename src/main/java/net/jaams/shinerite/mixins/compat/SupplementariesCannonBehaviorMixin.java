package net.jaams.shinerite.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.FireBehaviorsManager;
import net.minecraft.core.RegistryAccess;

import net.jaams.shinerite.compat.SupplementariesCompat;

/**
 * Re-registers the Shinerite star behavior after Supplementaries reloads its
 * cannon/present fire behaviors (which clears the previous registrations).
 */
@Mixin(value = FireBehaviorsManager.class, remap = false)
public abstract class SupplementariesCannonBehaviorMixin {

	@Inject(method = "registerBehaviors", at = @At("RETURN"), remap = false)
	private static void jaams$registerShineriteCannonBehavior(RegistryAccess access, CallbackInfo ci) {
		SupplementariesCompat.registerCannonBehaviors();
	}
}
