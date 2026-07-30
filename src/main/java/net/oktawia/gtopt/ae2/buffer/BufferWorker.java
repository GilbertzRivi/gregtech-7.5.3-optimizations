package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine.InternalSlot;

import appeng.api.crafting.IPatternDetails;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
public class BufferWorker {

    private final InternalSlot slot;
    private final SlotItemRecipeHandler itemHandler;
    private final SlotFluidRecipeHandler fluidHandler;

    @Setter
    private @Nullable IPatternDetails pattern;

    public BufferWorker(InternalSlot slot) {
        this.slot = slot;
        this.itemHandler = new SlotItemRecipeHandler(slot);
        this.fluidHandler = new SlotFluidRecipeHandler(slot);
    }
}
