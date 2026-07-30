package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine.InternalSlot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SlotItemRecipeHandler implements IRecipeHandler<Ingredient> {

    private final InternalSlot slot;

    public SlotItemRecipeHandler(InternalSlot slot) {
        this.slot = slot;
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        if (io != IO.IN || slot.isItemEmpty()) {
            return left;
        }
        return slot.handleItemInternal(left, simulate);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return new ArrayList<>(slot.getItems());
    }

    @Override
    public double getTotalContentAmount() {
        long sum = 0;
        for (ItemStack stack : slot.getItems()) {
            sum += stack.getCount();
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
    public int getPriority() {
        return IFilteredHandler.HIGH;
    }
}
