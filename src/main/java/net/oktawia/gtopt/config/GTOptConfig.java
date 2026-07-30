package net.oktawia.gtopt.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class GTOptConfig {

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue DISABLE_POWERFAILING_BY_DEFAULT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("powerfailing");
        DISABLE_POWERFAILING_BY_DEFAULT = builder
                .comment(
                        "If true, all GT multiblocks have prevent power failing on by default, as if every one of them",
                        "had a Machine Controller Cover with prevent power fail enabled, without having to place",
                        "the cover. It can still be turned off per machine with a Machine Controller Cover that",
                        "has prevent power fail disabled.",
                        "Default false, which keeps vanilla GregTech behaviour.")
                .define("disablePowerFailingByDefault", false);
        builder.pop();
        SPEC = builder.build();
    }

    public static boolean disablePowerFailingByDefault() {
        return SPEC.isLoaded() && DISABLE_POWERFAILING_BY_DEFAULT.get();
    }

    private GTOptConfig() {}
}
