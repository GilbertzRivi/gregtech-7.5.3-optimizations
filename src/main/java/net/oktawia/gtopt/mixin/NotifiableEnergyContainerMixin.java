package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NotifiableEnergyContainer.class, remap = false)
public abstract class NotifiableEnergyContainerMixin {

    @Shadow
    public abstract long getEnergyStored();

    @Inject(
            method = "getTotalContentAmount",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gtopt$useEnergyGetter(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue((double) getEnergyStored());
    }
}
