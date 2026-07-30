package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AggregateFluidHandler extends NotifiableRecipeHandlerTrait<FluidIngredient> {

    private final List<BufferWorker> workers;

    public AggregateFluidHandler(MetaMachine machine, List<BufferWorker> workers) {
        super(machine);
        this.workers = workers;
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                   boolean simulate) {
        return left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        List<Object> contents = new ArrayList<>();
        for (BufferWorker worker : workers) {
            contents.addAll(worker.getSlot().getFluids());
        }
        return contents;
    }

    @Override
    public double getTotalContentAmount() {
        long sum = 0;
        for (BufferWorker worker : workers) {
            for (FluidStack stack : worker.getSlot().getFluids()) {
                sum += stack.getAmount();
            }
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
    public void setDistinct(boolean ignored) {}

    @Override
    public int getPriority() {
        return IFilteredHandler.HIGH;
    }
}
