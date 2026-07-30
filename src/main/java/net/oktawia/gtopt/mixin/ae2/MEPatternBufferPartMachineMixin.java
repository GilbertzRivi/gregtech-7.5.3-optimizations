package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine.InternalSlot;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.oktawia.gtopt.ae2.buffer.AggregateFluidHandler;
import net.oktawia.gtopt.ae2.buffer.AggregateItemHandler;
import net.oktawia.gtopt.ae2.buffer.BufferRecipeHandlerList;
import net.oktawia.gtopt.ae2.buffer.BufferWorker;
import net.oktawia.gtopt.ae2.buffer.IBufferSlotOpt;
import net.oktawia.gtopt.ae2.buffer.IPatternBufferOpt;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.KeyCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferPartMachineMixin implements IPatternBufferOpt {

    @Shadow
    @Final
    protected InternalSlot[] internalInventory;

    @Shadow
    @Final
    private CustomItemStackHandler patternInventory;

    @Shadow
    @Final
    private Set<BlockPos> proxies;

    @Shadow
    private boolean needPatternSync;

    @Shadow
    protected abstract boolean checkInput(KeyCounter[] inputHolder);

    @Unique
    private IPatternDetails[] gtopt$patternSlotDetails;

    @Unique
    private List<BufferWorker> gtopt$workers;

    @Unique
    private AggregateItemHandler gtopt$aggregateItemHandler;

    @Unique
    private AggregateFluidHandler gtopt$aggregateFluidHandler;

    @Unique
    private BufferRecipeHandlerList gtopt$bufferRecipeHandler;

    @Unique
    private List<RecipeHandlerList> gtopt$bufferHandlers;

    @Inject(
            method = "<init>",
            at = @At("TAIL"))
    private void gtopt$initWorkers(IMachineBlockEntity holder, Object[] args, CallbackInfo ci) {
        MEPatternBufferPartMachine self = (MEPatternBufferPartMachine) (Object) this;
        gtopt$patternSlotDetails = new IPatternDetails[internalInventory.length];
        gtopt$workers = new ArrayList<>();
        gtopt$aggregateItemHandler = new AggregateItemHandler(self, gtopt$workers);
        gtopt$aggregateFluidHandler = new AggregateFluidHandler(self, gtopt$workers);
        gtopt$bufferRecipeHandler = new BufferRecipeHandlerList(gtopt$workers, self.getCircuitInventory(),
                self.getShareInventory(), self.getShareTank(), gtopt$aggregateItemHandler,
                gtopt$aggregateFluidHandler);
        gtopt$bufferHandlers = List.of(gtopt$bufferRecipeHandler);
        gtopt$addWorker();
    }

    @Inject(
            method = "onLoad",
            at = @At("TAIL"))
    private void gtopt$decodePatternsOnLoad(CallbackInfo ci) {
        if (((MEPatternBufferPartMachine) (Object) this).getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, () -> {
                int slots = Math.min(patternInventory.getSlots(), gtopt$patternSlotDetails.length);
                for (int i = 0; i < slots; i++) {
                    gtopt$patternSlotDetails[i] = PatternDetailsHelper.decodePattern(
                            patternInventory.getStackInSlot(i), serverLevel);
                }
                gtopt$syncWorkerCount();
                gtopt$ensureWorkersForExistingContents();
            }));
        }
    }

    @Inject(
            method = "getRecipeHandlers",
            at = @At("HEAD"),
            cancellable = true)
    private void gtopt$useBufferHandler(CallbackInfoReturnable<List<RecipeHandlerList>> cir) {
        cir.setReturnValue(gtopt$bufferHandlers);
    }

    @Inject(
            method = "onPatternChange",
            at = @At("HEAD"),
            cancellable = true)
    private void gtopt$onPatternChange(int index, CallbackInfo ci) {
        ci.cancel();
        var self = (MEPatternBufferPartMachine) (Object) this;
        if (self.isRemote()) {
            return;
        }
        IPatternDetails oldPattern = gtopt$patternSlotDetails[index];
        IPatternDetails newPattern = PatternDetailsHelper.decodePattern(
                patternInventory.getStackInSlot(index), self.getLevel());
        gtopt$patternSlotDetails[index] = newPattern;
        if (oldPattern != null && !oldPattern.equals(newPattern)) {
            for (BufferWorker worker : gtopt$workers) {
                if (oldPattern.equals(worker.getPattern())) {
                    worker.getSlot().refund();
                    worker.setPattern(null);
                    break;
                }
            }
        }
        needPatternSync = true;
    }

    @Inject(
            method = "getAvailablePatterns",
            at = @At("HEAD"),
            cancellable = true)
    private void gtopt$getAvailablePatterns(CallbackInfoReturnable<List<IPatternDetails>> cir) {
        List<IPatternDetails> patterns = new ArrayList<>(gtopt$patternSlotDetails.length);
        for (IPatternDetails details : gtopt$patternSlotDetails) {
            if (details != null) {
                patterns.add(details);
            }
        }
        cir.setReturnValue(patterns);
    }

    @Inject(
            method = "pushPattern",
            at = @At("HEAD"),
            cancellable = true)
    private void gtopt$pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder,
                                   CallbackInfoReturnable<Boolean> cir) {
        MEPatternBufferPartMachine self = (MEPatternBufferPartMachine) (Object) this;
        if (!self.isFormed() || !self.getMainNode().isActive() || !checkInput(inputHolder)) {
            cir.setReturnValue(false);
            return;
        }

        boolean knownPattern = false;
        for (IPatternDetails details : gtopt$patternSlotDetails) {
            if (patternDetails.equals(details)) {
                knownPattern = true;
                break;
            }
        }
        if (!knownPattern) {
            cir.setReturnValue(false);
            return;
        }

        for (BufferWorker worker : gtopt$workers) {
            if (((IBufferSlotOpt) worker.getSlot()).gtopt$isEmpty()) {
                worker.setPattern(null);
            }
        }

        BufferWorker free = null;
        for (BufferWorker worker : gtopt$workers) {
            if (patternDetails.equals(worker.getPattern())) {
                worker.getSlot().pushPattern(patternDetails, inputHolder);
                cir.setReturnValue(true);
                return;
            }
            if (free == null && ((IBufferSlotOpt) worker.getSlot()).gtopt$isEmpty()) {
                free = worker;
            }
        }

        if (free != null) {
            free.setPattern(patternDetails);
            free.getSlot().pushPattern(patternDetails, inputHolder);
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(false);
    }

    @Inject(
            method = "update",
            at = @At("TAIL"))
    private void gtopt$trimOnUpdate(CallbackInfo ci) {
        gtopt$trimSurplusWorkers();
    }

    @Inject(
            method = "addProxy",
            at = @At("TAIL"))
    private void gtopt$onProxyAdded(MEPatternBufferProxyPartMachine proxy, CallbackInfo ci) {
        gtopt$syncWorkerCount();
    }

    @Inject(
            method = "removeProxy",
            at = @At("TAIL"))
    private void gtopt$onProxyRemoved(MEPatternBufferProxyPartMachine proxy, CallbackInfo ci) {
        gtopt$syncWorkerCount();
    }

    @Override
    public BufferRecipeHandlerList gtopt$getBufferRecipeHandler() {
        return gtopt$bufferRecipeHandler;
    }

    @Override
    public AggregateItemHandler gtopt$getAggregateItemHandler() {
        return gtopt$aggregateItemHandler;
    }

    @Override
    public AggregateFluidHandler gtopt$getAggregateFluidHandler() {
        return gtopt$aggregateFluidHandler;
    }

    @Unique
    private void gtopt$onSlotChanged() {
        gtopt$aggregateItemHandler.notifyListeners();
        gtopt$aggregateFluidHandler.notifyListeners();
    }

    @Unique
    private void gtopt$addWorker() {
        int index = gtopt$workers.size();
        if (index >= internalInventory.length) {
            return;
        }
        InternalSlot slot = internalInventory[index];
        slot.setOnContentsChanged(this::gtopt$onSlotChanged);
        gtopt$workers.add(new BufferWorker(slot));
    }

    @Unique
    private void gtopt$removeLastWorker() {
        int last = gtopt$workers.size() - 1;
        if (last < 0) {
            return;
        }
        BufferWorker worker = gtopt$workers.get(last);
        worker.getSlot().refund();
        worker.getSlot().setOnContentsChanged(() -> {});
        gtopt$workers.remove(last);
        gtopt$onSlotChanged();
    }

    @Unique
    private int gtopt$targetWorkerCount() {
        return Mth.clamp(proxies.size() + 1, 1, internalInventory.length);
    }

    @Unique
    private void gtopt$syncWorkerCount() {
        int target = gtopt$targetWorkerCount();
        while (gtopt$workers.size() < target) {
            gtopt$addWorker();
        }
        while (gtopt$workers.size() > target) {
            gtopt$removeLastWorker();
        }
    }

    @Unique
    private void gtopt$ensureWorkersForExistingContents() {
        int highestOccupied = -1;
        for (int i = 0; i < internalInventory.length; i++) {
            if (!((IBufferSlotOpt) internalInventory[i]).gtopt$isEmpty()) {
                highestOccupied = i;
            }
        }
        while (gtopt$workers.size() <= highestOccupied) {
            gtopt$addWorker();
        }
    }

    @Unique
    private void gtopt$trimSurplusWorkers() {
        int target = gtopt$targetWorkerCount();
        while (gtopt$workers.size() > target &&
                ((IBufferSlotOpt) gtopt$workers.get(gtopt$workers.size() - 1).getSlot()).gtopt$isEmpty()) {
            gtopt$removeLastWorker();
        }
    }
}
