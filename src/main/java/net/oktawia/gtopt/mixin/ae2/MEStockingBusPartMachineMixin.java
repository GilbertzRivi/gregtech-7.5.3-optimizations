package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;

import net.oktawia.gtopt.ae2.IStockingPartOpt;
import net.oktawia.gtopt.mixin.ae2.accessors.AeItemHandlerAccessor;
import net.oktawia.gtopt.mixin.ae2.accessors.BusActionSourceAccessor;
import net.oktawia.gtopt.mixin.ae2.invokers.MEBusSubscriptionInvoker;

import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Predicate;

@Mixin(value = MEStockingBusPartMachine.class, remap = false)
public abstract class MEStockingBusPartMachineMixin implements IStockingPartOpt {

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
        return ((BusActionSourceAccessor) (Object) this).gtopt$accessActionSource();
    }

    @Override
    public boolean gtopt$isAutoPullValid(AEKey what, long amount) {
        return what instanceof AEItemKey && autoPullTest.test(new GenericStack(what, amount));
    }

    @Overwrite
    public boolean testConfiguredInOtherPart(@Nullable GenericStack config) {
        if (config == null) return false;
        if (!isFormed() || ((MEStockingBusPartMachine) (Object) this).isDistinct()) return false;
        int myColor = getPaintingColor();
        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof MEStockingBusPartMachine bus) {
                    if ((Object) bus == this || bus.isDistinct() || bus.getPaintingColor() != myColor) continue;
                    if (bus.getSlotList().hasStackInConfig(config, false)) return true;
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
            ((MEBusSubscriptionInvoker) this).gtopt$updateInventorySubscription();
        }
    }

    @Overwrite
    public void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        if (!self().isRemote()) {
            if (!this.autoPull) {
                ((AeItemHandlerAccessor) (Object) this).gtopt$getExportOnlyAEItemList().clearInventory(0);
            } else if (updateMEStatus()) {
                gtopt$markForAutoPull();
                ((MEBusSubscriptionInvoker) this).gtopt$updateInventorySubscription();
            }
        }
    }
}
