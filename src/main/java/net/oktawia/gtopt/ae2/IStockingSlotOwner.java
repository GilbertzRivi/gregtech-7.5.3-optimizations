package net.oktawia.gtopt.ae2;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

public interface IStockingSlotOwner {

    void gtopt$notifyConfigChanged();

    boolean gtopt$isConfigAllowed(@Nullable GenericStack stack);
}
