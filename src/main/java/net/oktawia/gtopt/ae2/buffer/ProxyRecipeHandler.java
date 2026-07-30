package net.oktawia.gtopt.ae2.buffer;

import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ProxyRecipeHandler<T> extends NotifiableRecipeHandlerTrait<T> {

    private final RecipeCapability<T> capability;

    private @Nullable IRecipeHandlerTrait<T> proxy;
    private @Nullable ISubscription proxySub;

    public ProxyRecipeHandler(MetaMachine machine, RecipeCapability<T> capability) {
        super(machine);
        this.capability = capability;
    }

    public void setProxy(@Nullable IRecipeHandlerTrait<T> proxy) {
        if (this.proxy == proxy) {
            return;
        }
        this.proxy = proxy;
        if (proxySub != null) {
            proxySub.unsubscribe();
            proxySub = null;
        }
        if (proxy != null) {
            proxySub = proxy.addChangedListener(this::notifyListeners);
        }
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    @Override
    public RecipeCapability<T> getCapability() {
        return capability;
    }

    @Override
    public List<T> handleRecipeInner(IO io, GTRecipe recipe, List<T> left, boolean simulate) {
        if (proxy == null) {
            return left;
        }
        return proxy.handleRecipeInner(io, recipe, left, simulate);
    }

    @Override
    public int getSize() {
        return proxy == null ? 0 : proxy.getSize();
    }

    @Override
    public @NotNull List<Object> getContents() {
        return proxy == null ? Collections.emptyList() : proxy.getContents();
    }

    @Override
    public double getTotalContentAmount() {
        return proxy == null ? 0 : proxy.getTotalContentAmount();
    }

    @Override
    public int getPriority() {
        return proxy == null ? IFilteredHandler.LOW : proxy.getPriority();
    }

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean ignored) {}
}
