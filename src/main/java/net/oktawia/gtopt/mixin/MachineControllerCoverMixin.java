package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.cover.MachineControllerCover;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.oktawia.gtopt.config.GTOptConfig;
import net.oktawia.gtopt.cover.IMachineControllerPoll;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MachineControllerCover.class, remap = false)
public abstract class MachineControllerCoverMixin implements IMachineControllerPoll {

    @Unique
    private static final int gtopt$POLL_INTERVAL = 40;

    @Shadow
    private boolean preventPowerFail;

    @Shadow
    protected abstract void updateInput();

    @Shadow
    protected abstract int getInputSignal();

    @Unique
    private TickableSubscription gtopt$pollSub;

    @Unique
    private int gtopt$tick = 0;

    @Unique
    private int gtopt$lastSignal = -1;

    @Inject(
            method = "onAttached",
            at = @At("RETURN"))
    private void gtopt$startPollingOnAttach(ItemStack itemStack, ServerPlayer player, CallbackInfo ci) {
        if (GTOptConfig.disablePowerFailingByDefault()) {
            preventPowerFail = true;
        }
        gtopt$initPolling();
    }

    @Inject(
            method = "onRemoved",
            at = @At("HEAD"))
    private void gtopt$stopPolling(CallbackInfo ci) {
        if (gtopt$pollSub != null) {
            gtopt$pollSub.unsubscribe();
            gtopt$pollSub = null;
        }
    }

    @Override
    public void gtopt$initPolling() {
        var holder = ((CoverBehavior) (Object) this).coverHolder;
        var level = holder.getLevel();
        if (level == null || level.isClientSide()) return;
        var machine = MetaMachine.getMachine(level, holder.getPos());
        if (machine != null) {
            gtopt$lastSignal = getInputSignal();
            updateInput();
            gtopt$pollSub = machine.subscribeServerTick(gtopt$pollSub, this::gtopt$poll);
        }
    }

    @Unique
    private void gtopt$poll() {
        if (++gtopt$tick >= gtopt$POLL_INTERVAL) {
            gtopt$tick = 0;
            int currentSignal = getInputSignal();
            if (currentSignal != gtopt$lastSignal) {
                gtopt$lastSignal = currentSignal;
                updateInput();
            }
        }
    }
}
