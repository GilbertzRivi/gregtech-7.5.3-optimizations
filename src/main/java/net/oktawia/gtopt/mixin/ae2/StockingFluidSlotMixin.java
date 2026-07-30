package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;

import net.oktawia.gtopt.ae2.IStockingPartOpt;
import net.oktawia.gtopt.ae2.IStockingSlotOwner;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
       targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine$ExportOnlyAEStockingFluidSlot",
       remap = false)
public abstract class StockingFluidSlotMixin implements IStockingSlotOwner {

    @Shadow
    @Final
    MEStockingHatchPartMachine this$0;

    @Override
    public void gtopt$notifyConfigChanged() {
        ((IStockingPartOpt) this$0).gtopt$markForRefresh();
    }

    @Override
    public boolean gtopt$isConfigAllowed(@Nullable GenericStack stack) {
        IStockingPartOpt part = (IStockingPartOpt) this$0;
        if (part.gtopt$isApplyingAutoPull()) {
            return true;
        }
        if (stack == null || stack.amount() <= 0) {
            return true;
        }
        return !part.getSlotList().hasStackInConfig(stack, true);
    }
}
