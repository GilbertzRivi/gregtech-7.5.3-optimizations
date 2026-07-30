package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;

import net.oktawia.gtopt.ae2.IStockingPartOpt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidHatchPartMachine.class, remap = false)
public abstract class FluidHatchPaintingMixin {

    @Inject(
            method = "onPaintingColorChanged",
            at = @At("RETURN"))
    private void gtopt$validateStockingConfig(int color, CallbackInfo ci) {
        if (this instanceof IStockingPartOpt part && !part.self().isRemote()) {
            part.validateConfig();
        }
    }
}
