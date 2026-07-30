package net.oktawia.gtopt.mixin.ae2.accessors;

import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MEInputBusPartMachine.class, remap = false)
public interface AeItemHandlerAccessor {

    @Accessor("aeItemHandler")
    ExportOnlyAEItemList gtopt$getExportOnlyAEItemList();
}
