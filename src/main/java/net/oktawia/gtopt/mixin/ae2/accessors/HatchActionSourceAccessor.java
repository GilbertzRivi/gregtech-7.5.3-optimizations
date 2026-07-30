package net.oktawia.gtopt.mixin.ae2.accessors;

import com.gregtechceu.gtceu.integration.ae2.machine.MEHatchPartMachine;

import appeng.api.networking.security.IActionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MEHatchPartMachine.class, remap = false)
public interface HatchActionSourceAccessor {

    @Accessor("actionSource")
    IActionSource gtopt$accessActionSource();
}
