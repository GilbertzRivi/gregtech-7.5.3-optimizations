package net.oktawia.gtopt.mixin.ae2;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;

import net.minecraft.core.BlockPos;
import net.oktawia.gtopt.ae2.buffer.ProxyBufferRecipeHandlerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = MEPatternBufferProxyPartMachine.class, remap = false)
public abstract class MEPatternBufferProxyPartMachineMixin {

    @Unique
    private ProxyBufferRecipeHandlerList gtopt$proxyHandler;

    @Unique
    private List<RecipeHandlerList> gtopt$proxyHandlers;

    @Unique
    private void gtopt$syncBuffer() {
        MEPatternBufferPartMachine buffer = ((MEPatternBufferProxyPartMachine) (Object) this).getBuffer();
        if (buffer != null) {
            gtopt$proxyHandler.setBuffer(buffer);
        } else {
            gtopt$proxyHandler.clearBuffer();
        }
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"))
    private void gtopt$initProxyHandler(IMachineBlockEntity holder, CallbackInfo ci) {
        gtopt$proxyHandler = new ProxyBufferRecipeHandlerList((MetaMachine) (Object) this);
        gtopt$proxyHandlers = List.of(gtopt$proxyHandler);
    }

    @Inject(
            method = "getRecipeHandlers",
            at = @At("HEAD"),
            cancellable = true)
    private void gtopt$useProxyHandler(CallbackInfoReturnable<List<RecipeHandlerList>> cir) {
        gtopt$syncBuffer();
        cir.setReturnValue(gtopt$proxyHandlers);
    }

    @Inject(
            method = "setBuffer",
            at = @At("TAIL"))
    private void gtopt$onBufferSet(BlockPos pos, CallbackInfo ci) {
        gtopt$syncBuffer();
    }

    @Inject(
            method = "onMachineRemoved",
            at = @At("TAIL"))
    private void gtopt$onMachineRemoved(CallbackInfo ci) {
        gtopt$proxyHandler.clearBuffer();
    }
}
