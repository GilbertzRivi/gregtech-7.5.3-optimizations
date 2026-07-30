package net.oktawia.gtopt;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.oktawia.gtopt.ae2.gridservice.StockingGridService;
import net.oktawia.gtopt.config.GTOptConfig;

@Mod(GTOpt.MODID)
public class GTOpt {

    public static final String MODID = "gtopt";

    public GTOpt() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GTOptConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("ae2")) {
            event.enqueueWork(StockingGridService::register);
        }
    }
}
