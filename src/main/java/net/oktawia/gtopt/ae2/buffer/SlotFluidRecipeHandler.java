package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine.InternalSlot;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SlotFluidRecipeHandler implements IRecipeHandler<FluidIngredient> {

    private final InternalSlot slot;

    public SlotFluidRecipeHandler(InternalSlot slot) {
        this.slot = slot;
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                   boolean simulate) {
        if (io != IO.IN || slot.isFluidEmpty()) {
            return left;
        }
        return slot.handleFluidInternal(left, simulate);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return new ArrayList<>(slot.getFluids());
    }

    @Override
    public double getTotalContentAmount() {
        long sum = 0;
        for (FluidStack stack : slot.getFluids()) {
            sum += stack.getAmount();
        }
        return sum;
    }

    @Override
    public RecipeCapability<FluidIngredient> getCapability() {
        return FluidRecipeCapability.CAP;
    }

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public int getPriority() {
        return IFilteredHandler.HIGH;
    }
}
