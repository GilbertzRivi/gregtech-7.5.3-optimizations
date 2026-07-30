package net.oktawia.gtopt.mixin.ae2.accessors;

import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MEInputHatchPartMachine.class, remap = false)
public interface AeFluidHandlerAccessor {

    @Accessor("aeFluidHandler")
    ExportOnlyAEFluidList gtopt$ExportOnlyAEFluidList();
}
