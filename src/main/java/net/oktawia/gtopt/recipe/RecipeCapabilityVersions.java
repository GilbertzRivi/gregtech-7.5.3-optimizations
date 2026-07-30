package net.oktawia.gtopt.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import java.util.List;
import java.util.Map;

public final class RecipeCapabilityVersions {

    public int inputVersion;
    public int topologyVersion;

    public int cachedGroupsVersionIn = -1;
    public Map<RecipeHandlerGroup, List<RecipeHandlerList>> cachedGroupsIn;
}
