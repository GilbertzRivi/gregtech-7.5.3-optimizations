package net.oktawia.gtopt.mixin.ae2.invokers;

import com.gregtechceu.gtceu.integration.ae2.machine.MEHatchPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MEHatchPartMachine.class, remap = false)
public interface MEHatchSubscriptionInvoker {

    @Invoker("updateTankSubscription")
    void gtopt$updateTankSubscription();
}
