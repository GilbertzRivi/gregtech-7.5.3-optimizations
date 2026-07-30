package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;

import net.oktawia.gtopt.ae2.IStockSilent;
import net.oktawia.gtopt.ae2.IStockingSlotOwner;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = ExportOnlyAESlot.class, remap = false)
public abstract class ExportOnlyAESlotMixin implements IStockSilent {

    @Shadow
    @Nullable
    protected GenericStack stock;

    @Override
    @Unique
    public boolean gtopt$setStockSilent(@Nullable GenericStack val) {
        if (this.stock == null && val == null) {
            return false;
        }
        if (val == null) {
            this.stock = null;
            return true;
        }
        if (val.equals(this.stock)) {
            return false;
        }
        this.stock = val;
        return true;
    }

    @Shadow
    @Nullable
    protected GenericStack config;

    @Unique
    @Nullable
    private GenericStack gtopt$previousConfig;

    @Inject(
            method = "setConfig",
            at = @At("HEAD"),
            cancellable = true)
    private void gtopt$rejectDuplicateConfig(GenericStack config, CallbackInfo ci) {
        if (this instanceof IStockingSlotOwner owner) {
            if (!owner.gtopt$isConfigAllowed(config)) {
                ci.cancel();
                return;
            }
            gtopt$previousConfig = this.config;
        }
    }

    @Inject(
            method = "setConfig",
            at = @At("RETURN"))
    private void gtopt$onConfigChanged(GenericStack config, CallbackInfo ci) {
        if (this instanceof IStockingSlotOwner owner && !Objects.equals(gtopt$previousConfig, this.config)) {
            owner.gtopt$notifyConfigChanged();
        }
    }
}
