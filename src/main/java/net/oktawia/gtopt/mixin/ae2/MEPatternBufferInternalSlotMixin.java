package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.oktawia.gtopt.ae2.buffer.IBufferSlotOpt;

import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = MEPatternBufferPartMachine.InternalSlot.class, remap = false)
public abstract class MEPatternBufferInternalSlotMixin implements IBufferSlotOpt {

    @Shadow
    @Final
    private Object2LongOpenCustomHashMap<ItemStack> itemInventory;

    @Shadow
    @Final
    private Object2LongOpenHashMap<FluidStack> fluidInventory;

    @Unique
    private Set<Item> gtopt$itemTypes;

    @Unique
    private Set<Fluid> gtopt$fluidTypes;

    @Inject(
            method = "onContentsChanged()V",
            at = @At("HEAD")
    )
    private void gtopt$invalidateTypeCache(CallbackInfo ci) {
        gtopt$itemTypes = null;
        gtopt$fluidTypes = null;
    }

    @Override
    public boolean gtopt$isEmpty() {
        return itemInventory.isEmpty() && fluidInventory.isEmpty();
    }

    @Override
    public Set<Item> gtopt$getItemTypes() {
        Set<Item> cached = gtopt$itemTypes;
        if (cached == null) {
            cached = new ReferenceOpenHashSet<>(itemInventory.size());
            for (ItemStack stack : itemInventory.keySet()) {
                cached.add(stack.getItem());
            }
            gtopt$itemTypes = cached;
        }
        return cached;
    }

    @Override
    public Set<Fluid> gtopt$getFluidTypes() {
        Set<Fluid> cached = gtopt$fluidTypes;
        if (cached == null) {
            cached = new ReferenceOpenHashSet<>(fluidInventory.size());
            for (FluidStack stack : fluidInventory.keySet()) {
                cached.add(stack.getFluid());
            }
            gtopt$fluidTypes = cached;
        }
        return cached;
    }
}
