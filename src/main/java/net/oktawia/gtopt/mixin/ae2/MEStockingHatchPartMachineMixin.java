package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;

import net.oktawia.gtopt.ae2.IStockingPartOpt;
import net.oktawia.gtopt.mixin.ae2.accessors.AeFluidHandlerAccessor;
import net.oktawia.gtopt.mixin.ae2.accessors.HatchActionSourceAccessor;
import net.oktawia.gtopt.mixin.ae2.invokers.MEHatchSubscriptionInvoker;

import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Predicate;

@Mixin(value = MEStockingHatchPartMachine.class, remap = false)
public abstract class MEStockingHatchPartMachineMixin implements IStockingPartOpt {

    @Shadow
    private boolean autoPull;

    @Shadow
    private int ticksPerCycle;

    @Shadow
    private Predicate<GenericStack> autoPullTest;

    @Unique
    private boolean gtopt$applyingAutoPull;

    @Override
    public boolean gtopt$isApplyingAutoPull() {
        return gtopt$applyingAutoPull;
    }

    @Override
    public void gtopt$setApplyingAutoPull(boolean applying) {
        gtopt$applyingAutoPull = applying;
    }

    @Override
    public IActionSource gtopt$getActionSource() {
        return ((HatchActionSourceAccessor) (Object) this).gtopt$accessActionSource();
    }

    @Override
    public boolean gtopt$isAutoPullValid(AEKey what, long amount) {
        return what instanceof AEFluidKey && autoPullTest.test(new GenericStack(what, amount));
    }

    @Overwrite
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        if (!isFormed()) return false;
        int myColor = getPaintingColor();
        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof MEStockingHatchPartMachine hatch) {
                    if ((Object) hatch == this || hatch.getPaintingColor() != myColor) continue;
                    if (hatch.getSlotList().hasStackInConfig(config, false)) return true;
                }
            }
        }
        return false;
    }

    @Overwrite
    public void autoIO() {
        if (!isWorkingEnabled()) {
            return;
        }
        if (!shouldSyncME()) {
            return;
        }
        if (ticksPerCycle == 0) {
            ticksPerCycle = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;
        }
        if (updateMEStatus()) {
            ((MEHatchSubscriptionInvoker) this).gtopt$updateTankSubscription();
        }
    }

    @Overwrite
    public void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        if (!self().isRemote()) {
            if (!this.autoPull) {
                ((AeFluidHandlerAccessor) (Object) this).gtopt$ExportOnlyAEFluidList().clearInventory(0);
            } else if (updateMEStatus()) {
                gtopt$markForAutoPull();
                ((MEHatchSubscriptionInvoker) this).gtopt$updateTankSubscription();
            }
        }
    }
}
