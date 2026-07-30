package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ProxyBufferRecipeHandlerList extends RecipeHandlerList {

    private final ProxyRecipeHandler<Ingredient> circuit;
    private final ProxyRecipeHandler<Ingredient> sharedItem;
    private final ProxyRecipeHandler<Ingredient> slotItem;
    private final ProxyRecipeHandler<FluidIngredient> sharedFluid;
    private final ProxyRecipeHandler<FluidIngredient> slotFluid;

    private @Nullable MEPatternBufferPartMachine buffer;

    public ProxyBufferRecipeHandlerList(MetaMachine machine) {
        super(IO.IN);
        this.circuit = new ProxyRecipeHandler<>(machine, ItemRecipeCapability.CAP);
        this.sharedItem = new ProxyRecipeHandler<>(machine, ItemRecipeCapability.CAP);
        this.slotItem = new ProxyRecipeHandler<>(machine, ItemRecipeCapability.CAP);
        this.sharedFluid = new ProxyRecipeHandler<>(machine, FluidRecipeCapability.CAP);
        this.slotFluid = new ProxyRecipeHandler<>(machine, FluidRecipeCapability.CAP);
        addHandlers(circuit, sharedItem, slotItem, sharedFluid, slotFluid);
        setGroup(RecipeHandlerGroupDistinctness.BUS_DISTINCT);
    }

    public void setBuffer(MEPatternBufferPartMachine buffer) {
        if (this.buffer == buffer) {
            return;
        }
        this.buffer = buffer;
        IPatternBufferOpt opt = (IPatternBufferOpt) buffer;
        circuit.setProxy(buffer.getCircuitInventory());
        sharedItem.setProxy(buffer.getShareInventory());
        sharedFluid.setProxy(buffer.getShareTank());
        slotItem.setProxy(opt.gtopt$getAggregateItemHandler());
        slotFluid.setProxy(opt.gtopt$getAggregateFluidHandler());
    }

    public void clearBuffer() {
        if (this.buffer == null) {
            return;
        }
        this.buffer = null;
        circuit.setProxy(null);
        sharedItem.setProxy(null);
        sharedFluid.setProxy(null);
        slotItem.setProxy(null);
        slotFluid.setProxy(null);
    }

    @Override
    public Map<RecipeCapability<?>, List<Object>> handleRecipe(IO io, GTRecipe recipe,
                                                               Map<RecipeCapability<?>, List<Object>> contents,
                                                               boolean simulate) {
        MEPatternBufferPartMachine target = buffer;
        if (target == null) {
            return contents;
        }
        return ((IPatternBufferOpt) target).gtopt$getBufferRecipeHandler().handleRecipe(io, recipe, contents, simulate);
    }

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean ignored, boolean notify) {}
}
