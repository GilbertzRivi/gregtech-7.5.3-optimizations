package net.oktawia.gtopt.ae2.buffer;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.Set;

public interface IBufferSlotOpt {

    boolean gtopt$isEmpty();

    Set<Item> gtopt$getItemTypes();

    Set<Fluid> gtopt$getFluidTypes();
}
