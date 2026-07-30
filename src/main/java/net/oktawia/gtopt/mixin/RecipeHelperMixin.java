package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(value = RecipeHelper.class, remap = false)
public abstract class RecipeHelperMixin {

    @Inject(
            method = "handleRecipe",
            at = @At("HEAD"),
            cancellable = true)
    private static void gtopt$skipEmptyContents(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                                Map<RecipeCapability<?>, List<Content>> contents,
                                                Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                                                boolean isTick, boolean simulated,
                                                CallbackInfoReturnable<ActionResult> cir) {
        if (contents.isEmpty()) {
            cir.setReturnValue(ActionResult.PASS_NO_CONTENTS);
        }
    }
}
