package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = RecipeHandlerList.class, remap = false)
public abstract class RecipeHandlerListMixin {

    @Redirect(
              method = "handleRecipe",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/capability/recipe/IRecipeHandler;handleRecipe(Lcom/gregtechceu/gtceu/api/capability/recipe/IO;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Ljava/util/List;Z)Ljava/util/List;"
              )
    )
    private List<?> gtopt$skipEmptyInputHandler(IRecipeHandler<?> handler, IO io, GTRecipe recipe, List<?> left,
                                                boolean simulate) {
        if (io == IO.IN && handler.getTotalContentAmount() == 0 &&
                handler.getCapability() != CWURecipeCapability.CAP) {
            return left;
        }
        return handler.handleRecipe(io, recipe, left, simulate);
    }
}
