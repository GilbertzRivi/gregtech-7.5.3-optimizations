package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;

import net.oktawia.gtopt.cover.IMachineControllerPoll;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CoverBehavior.class, remap = false)
public abstract class CoverBehaviorMixin {

    @Inject(
            method = "onLoad",
            at = @At("RETURN")
    )
    private void gtopt$initControllerPolling(CallbackInfo ci) {
        if ((Object) this instanceof IMachineControllerPoll poll) {
            poll.gtopt$initPolling();
        }
    }
}
