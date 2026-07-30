package net.oktawia.gtopt.mixin.ae2.accessors;

import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;

import appeng.api.networking.security.IActionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MEBusPartMachine.class, remap = false)
public interface BusActionSourceAccessor {

    @Accessor("actionSource")
    IActionSource gtopt$accessActionSource();
}
