package net.oktawia.gtopt.ae2;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.oktawia.gtopt.ae2.gridservice.IStockingService;

import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface IStockingPartOpt extends IMEStockingPart {

    IManagedGridNode getMainNode();

    boolean isOnline();

    boolean isWorkingEnabled();

    boolean shouldSyncME();

    boolean updateMEStatus();

    boolean isFormed();

    int getPaintingColor();

    SortedSet<IMultiController> getControllers();

    IActionSource gtopt$getActionSource();

    boolean gtopt$isAutoPullValid(AEKey what, long amount);

    void gtopt$setApplyingAutoPull(boolean applying);

    boolean gtopt$isApplyingAutoPull();

    default Set<AEKey> gtopt$getStockingKeys() {
        Set<AEKey> keys = new HashSet<>();
        IConfigurableSlotList slots = getSlotList();
        for (int i = 0; i < slots.getConfigurableSlots(); i++) {
            GenericStack config = slots.getConfigurableSlot(i).getConfig();
            if (config != null) {
                keys.add(config.what());
            }
        }
        return keys;
    }

    default boolean gtopt$applyStockSilent(Object2LongMap<AEKey> changed) {
        IConfigurableSlotList slots = getSlotList();
        int min = getMinStackSize();
        boolean anyChanged = false;
        for (int i = 0; i < slots.getConfigurableSlots(); i++) {
            IConfigurableSlot slot = slots.getConfigurableSlot(i);
            GenericStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            AEKey key = config.what();
            if (!changed.containsKey(key)) {
                continue;
            }
            long amount = changed.getLong(key);
            GenericStack newStock = amount >= min ? new GenericStack(key, amount) : null;
            if (((IStockSilent) slot).gtopt$setStockSilent(newStock)) {
                anyChanged = true;
            }
        }
        return anyChanged;
    }

    default void gtopt$notifyStockChanged() {
        gtopt$notifySlotList(getSlotList());
    }

    default boolean gtopt$isCycleDue() {
        int interval = getTicksPerCycle();
        if (interval <= 0) {
            interval = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;
        }
        return self().getOffsetTimer() % interval == 0;
    }

    default void gtopt$applyAutoPull(List<GenericStack> selection) {
        IConfigurableSlotList slots = getSlotList();
        int slotCount = slots.getConfigurableSlots();
        int filled = 0;
        boolean changed = false;
        gtopt$setApplyingAutoPull(true);
        try {
            for (GenericStack stack : selection) {
                if (filled >= slotCount) {
                    break;
                }
                IConfigurableSlot slot = slots.getConfigurableSlot(filled++);
                GenericStack newConfig = new GenericStack(stack.what(), 1);
                if (!newConfig.equals(slot.getConfig())) {
                    changed = true;
                }
                slot.setConfig(newConfig);
                if (((IStockSilent) slot).gtopt$setStockSilent(stack)) {
                    changed = true;
                }
            }
            slots.clearInventory(filled);
        } finally {
            gtopt$setApplyingAutoPull(false);
        }
        if (changed) {
            gtopt$notifySlotList(slots);
        }
    }

    default void gtopt$markForRefresh() {
        if (self().isRemote()) {
            return;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid != null) {
            grid.getService(IStockingService.class).markForRefresh(this);
        }
    }

    default void gtopt$markForAutoPull() {
        if (self().isRemote()) {
            return;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid != null) {
            grid.getService(IStockingService.class).markForAutoPull(this);
        }
    }

    private static void gtopt$notifySlotList(IConfigurableSlotList slots) {
        if (slots instanceof NotifiableItemStackHandler items) {
            items.onContentsChanged();
        } else if (slots instanceof NotifiableFluidTank tank) {
            tank.onContentsChanged();
        }
    }
}
