package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.oktawia.gtopt.ae2.IStockingPartOpt;

import appeng.api.networking.IGridNodeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MEBusPartMachine.class, remap = false)
public abstract class MEBusPartMachineMixin {

    @Unique
    private boolean gtopt$wasOnline;

    @Inject(
            method = "onMainNodeStateChanged",
            at = @At("HEAD")
    )
    private void gtopt$captureOnlineState(IGridNodeListener.State reason, CallbackInfo ci) {
        if (this instanceof IStockingPartOpt part) {
            gtopt$wasOnline = part.isOnline();
        }
    }

    @Inject(
            method = "onMainNodeStateChanged",
            at = @At("RETURN")
    )
    private void gtopt$refreshOnStateChange(IGridNodeListener.State reason, CallbackInfo ci) {
        if (!(this instanceof IStockingPartOpt part) || part.isOnline() == gtopt$wasOnline) {
            return;
        }
        if (part.isOnline()) {
            if (part.isAutoPull()) {
                part.gtopt$markForAutoPull();
            }
            part.gtopt$markForRefresh();
        } else {
            IConfigurableSlotList slots = part.getSlotList();
            if (part.isAutoPull()) {
                slots.clearInventory(0);
            } else {
                for (int i = 0; i < slots.getConfigurableSlots(); i++) {
                    IConfigurableSlot slot = slots.getConfigurableSlot(i);
                    if (slot == null) {
                        continue;
                    }
                    slot.setStock(null);
                }
            }
        }
    }
}
