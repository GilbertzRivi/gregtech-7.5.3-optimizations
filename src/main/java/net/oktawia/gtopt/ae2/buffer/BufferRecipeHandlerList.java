package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BufferRecipeHandlerList extends RecipeHandlerList {

    private final List<BufferWorker> workers;
    private final IRecipeHandler<?> circuitInventory;
    private final IRecipeHandler<?> shareInventory;
    private final IRecipeHandler<?> shareTank;
    private final List<IRecipeHandler<?>> sharedItemHandlers;
    private final List<IRecipeHandler<?>> sharedFluidHandlers;

    public BufferRecipeHandlerList(List<BufferWorker> workers, IRecipeHandler<?> circuitInventory,
                                   IRecipeHandler<?> shareInventory, IRecipeHandler<?> shareTank,
                                   AggregateItemHandler aggregateItems, AggregateFluidHandler aggregateFluids) {
        super(IO.IN);
        this.workers = workers;
        this.circuitInventory = circuitInventory;
        this.shareInventory = shareInventory;
        this.shareTank = shareTank;
        this.sharedItemHandlers = List.of(circuitInventory, shareInventory);
        this.sharedFluidHandlers = List.of(shareTank);
        addHandlers(circuitInventory, shareInventory, shareTank, aggregateItems, aggregateFluids);
        setGroup(RecipeHandlerGroupDistinctness.BUS_DISTINCT);
    }

    @Override
    public Map<RecipeCapability<?>, List<Object>> handleRecipe(IO io, GTRecipe recipe,
                                                               Map<RecipeCapability<?>, List<Object>> contents,
                                                               boolean simulate) {
        if (io != IO.IN || contents.isEmpty()) {
            return contents;
        }

        for (BufferWorker worker : workers) {
            IBufferSlotOpt slot = (IBufferSlotOpt) worker.getSlot();
            if (slot.gtopt$isEmpty() || !couldSlotMatchContents(slot, contents)) {
                continue;
            }
            Map<RecipeCapability<?>, List<Object>> left = consume(worker, io, recipe, contents, true);
            if (!left.isEmpty()) {
                continue;
            }
            return simulate ? left : consume(worker, io, recipe, contents, false);
        }

        Map<RecipeCapability<?>, List<Object>> shared = consume(null, io, recipe, contents, true);
        if (!shared.isEmpty()) {
            return contents;
        }
        return simulate ? shared : consume(null, io, recipe, contents, false);
    }

    private Map<RecipeCapability<?>, List<Object>> consume(@Nullable BufferWorker worker, IO io, GTRecipe recipe,
                                                           Map<RecipeCapability<?>, List<Object>> contents,
                                                           boolean simulate) {
        var copy = new Reference2ObjectOpenHashMap<>(contents);
        for (var it = copy.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            for (IRecipeHandler<?> handler : handlersFor(entry.getKey(), worker)) {
                if (handler.getTotalContentAmount() == 0) {
                    continue;
                }
                var left = handler.handleRecipe(io, recipe, entry.getValue(), simulate);
                if (left == null) {
                    it.remove();
                    break;
                }
                entry.setValue(new ArrayList<>(left));
            }
        }
        return copy;
    }

    private List<IRecipeHandler<?>> handlersFor(RecipeCapability<?> cap, @Nullable BufferWorker worker) {
        if (cap == ItemRecipeCapability.CAP) {
            return worker == null ? sharedItemHandlers :
                    List.of(circuitInventory, shareInventory, worker.getItemHandler());
        }
        if (cap == FluidRecipeCapability.CAP) {
            return worker == null ? sharedFluidHandlers : List.of(shareTank, worker.getFluidHandler());
        }
        return List.of();
    }

    private static boolean couldSlotMatchContents(IBufferSlotOpt slot,
                                                  Map<RecipeCapability<?>, List<Object>> contents) {
        List<Object> itemContents = contents.get(ItemRecipeCapability.CAP);
        if (itemContents != null) {
            Set<Item> itemTypes = slot.gtopt$getItemTypes();
            if (!itemTypes.isEmpty()) {
                for (Object obj : itemContents) {
                    if (!(obj instanceof Ingredient ingredient) || ingredient.isEmpty()) {
                        continue;
                    }
                    for (ItemStack stack : ingredient.getItems()) {
                        if (itemTypes.contains(stack.getItem())) {
                            return true;
                        }
                    }
                }
            }
        }
        List<Object> fluidContents = contents.get(FluidRecipeCapability.CAP);
        if (fluidContents != null) {
            Set<Fluid> fluidTypes = slot.gtopt$getFluidTypes();
            if (!fluidTypes.isEmpty()) {
                for (Object obj : fluidContents) {
                    if (!(obj instanceof FluidIngredient ingredient) || ingredient.isEmpty()) {
                        continue;
                    }
                    for (FluidStack stack : ingredient.getStacks()) {
                        if (fluidTypes.contains(stack.getFluid())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean ignored, boolean notify) {}
}
