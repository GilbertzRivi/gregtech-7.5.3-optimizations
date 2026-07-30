package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AggregateItemHandler extends NotifiableRecipeHandlerTrait<Ingredient> {

    private final List<BufferWorker> workers;

    public AggregateItemHandler(MetaMachine machine, List<BufferWorker> workers) {
        super(machine);
        this.workers = workers;
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        return left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        List<Object> contents = new ArrayList<>();
        for (BufferWorker worker : workers) {
            contents.addAll(worker.getSlot().getItems());
        }
        return contents;
    }

    @Override
    public double getTotalContentAmount() {
        long sum = 0;
        for (BufferWorker worker : workers) {
            for (ItemStack stack : worker.getSlot().getItems()) {
                sum += stack.getCount();
            }
        }
        return sum;
    }

    @Override
    public RecipeCapability<Ingredient> getCapability() {
        return ItemRecipeCapability.CAP;
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
