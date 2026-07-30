package net.oktawia.gtopt.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeRunner;

import net.oktawia.gtopt.recipe.IRecipeVersionHolder;
import net.oktawia.gtopt.recipe.RecipeCapabilityVersions;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(value = RecipeRunner.class, remap = false)
public abstract class RecipeRunnerMixin {

    @Shadow
    @Final
    private IO io;

    @Unique
    private RecipeCapabilityVersions gtopt$versions;

    @Inject(
            method = "<init>",
            at = @At("RETURN"))
    private void gtopt$captureVersions(GTRecipe recipe, IO io, boolean isTick, IRecipeCapabilityHolder holder,
                                       Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches, boolean simulated,
                                       CallbackInfo ci) {
        if (holder instanceof IRecipeVersionHolder versionHolder) {
            gtopt$versions = versionHolder.gtopt$getRecipeVersions();
        }
    }

    @Redirect(
              method = "handleContents",
              at = @At(
                       value = "INVOKE",
                       target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                       ordinal = 0))
    private Object gtopt$skipHandlerRebuild(Map<Object, Object> proxies, Object key, Object defaultValue) {
        if (gtopt$hasValidCache()) {
            return Collections.emptyList();
        }
        return proxies.getOrDefault(key, defaultValue);
    }

    @ModifyVariable(
                    method = "handleContents",
                    at = @At("STORE"),
                    name = "handlerGroups")
    private Map<RecipeHandlerGroup, List<RecipeHandlerList>> gtopt$cacheHandlerGroups(Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups) {
        if (gtopt$versions == null || io != IO.IN) {
            return handlerGroups;
        }
        if (gtopt$hasValidCache()) {
            return gtopt$versions.cachedGroupsIn;
        }
        gtopt$versions.cachedGroupsIn = handlerGroups;
        gtopt$versions.cachedGroupsVersionIn = gtopt$versions.topologyVersion;
        return handlerGroups;
    }

    @Unique
    private boolean gtopt$hasValidCache() {
        return gtopt$versions != null && io == IO.IN &&
                gtopt$versions.cachedGroupsIn != null &&
                gtopt$versions.cachedGroupsVersionIn == gtopt$versions.topologyVersion;
    }
}
