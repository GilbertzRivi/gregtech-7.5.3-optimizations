package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.cover.MachineControllerCover;

import net.oktawia.gtopt.config.GTOptConfig;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class RecipeLogicPowerFailMixin {

    @Shadow
    @Final
    public IRecipeLogicMachine machine;

    @Shadow
    protected int runAttempt;

    @Shadow
    protected int runDelay;

    @Shadow
    public abstract boolean isSuspend();

    @Shadow
    public abstract void setStatus(RecipeLogic.Status status);

    @Shadow
    public abstract void updateTickSubscription();

    @Inject(
            method = "handleRecipeWorking",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic;setStatus(Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic$Status;)V",
                     ordinal = 1
            ),
            cancellable = true
    )
    private void gtopt$onPreventPowerFailDefault(CallbackInfo ci) {
        if (GTOptConfig.disablePowerFailingByDefault() && !gtopt$anyExplicitlyDisabled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "onMachineLoad",
            at = @At("RETURN")
    )
    private void gtopt$restartSuspendedOnLoad(CallbackInfo ci) {
        if (GTOptConfig.disablePowerFailingByDefault() && isSuspend() && !gtopt$anyExplicitlyDisabled() &&
                machine.isWorkingEnabled()) {
            runAttempt = 0;
            runDelay = 0;
            setStatus(RecipeLogic.Status.IDLE);
            updateTickSubscription();
        }
    }

    @Unique
    private boolean gtopt$anyExplicitlyDisabled() {
        return machine.self().getCoverContainer().getCovers().stream()
                .filter(cover -> cover instanceof MachineControllerCover)
                .anyMatch(cover -> !((MachineControllerCover) cover).preventPowerFail());
    }
}
