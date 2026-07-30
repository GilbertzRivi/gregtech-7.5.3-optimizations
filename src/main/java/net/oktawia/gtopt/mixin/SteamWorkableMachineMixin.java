package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.steam.SteamWorkableMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import net.oktawia.gtopt.recipe.IRecipeVersionHolder;
import net.oktawia.gtopt.recipe.RecipeCapabilityVersions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SteamWorkableMachine.class, remap = false)
public abstract class SteamWorkableMachineMixin implements IRecipeVersionHolder {

    @Unique
    private RecipeCapabilityVersions gtopt$recipeVersions;

    @Override
    public RecipeCapabilityVersions gtopt$getRecipeVersions() {
        if (gtopt$recipeVersions == null) {
            gtopt$recipeVersions = new RecipeCapabilityVersions();
        }
        return gtopt$recipeVersions;
    }

    @Redirect(
              method = "onLoad",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/machine/trait/RecipeHandlerList;subscribe(Ljava/lang/Runnable;)Lcom/lowdragmc/lowdraglib/syncdata/ISubscription;"))
    private ISubscription gtopt$trackVersions(RecipeHandlerList handlerList, Runnable listener) {
        RecipeCapabilityVersions versions = gtopt$getRecipeVersions();
        IO io = handlerList.getHandlerIO();
        versions.topologyVersion++;
        if (io.support(IO.IN)) versions.inputVersion++;
        return handlerList.subscribe(() -> {
            if (io.support(IO.IN)) versions.inputVersion++;
            listener.run();
        });
    }
}
