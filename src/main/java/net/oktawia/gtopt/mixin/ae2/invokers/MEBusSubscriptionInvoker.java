package net.oktawia.gtopt.mixin.ae2.invokers;

import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MEBusPartMachine.class, remap = false)
public interface MEBusSubscriptionInvoker {

    @Invoker("updateInventorySubscription")
    void gtopt$updateInventorySubscription();
}
