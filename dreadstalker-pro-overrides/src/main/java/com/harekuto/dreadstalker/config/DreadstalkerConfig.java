package com.harekuto.dreadstalker.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class DreadstalkerConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue naturalSpawning;
        public final ForgeConfigSpec.IntValue naturalSpawnChance;
        public final ForgeConfigSpec.IntValue maximumSpawnLight;
        public final ForgeConfigSpec.BooleanValue freezeWhenObserved;
        public final ForgeConfigSpec.DoubleValue freezeDistance;
        public final ForgeConfigSpec.BooleanValue teleporting;
        public final ForgeConfigSpec.BooleanValue darknessEffects;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.push("spawning");
            naturalSpawning = builder.comment("Allow Dreadstalkers to spawn naturally.")
                    .define("naturalSpawning", true);
            naturalSpawnChance = builder.comment("Additional 1-in-N gate after vanilla/Forge spawn checks. Higher values make natural encounters rarer.")
                    .defineInRange("naturalSpawnChance", 5, 1, 100);
            maximumSpawnLight = builder.comment("Maximum local raw light level for natural spawning.")
                    .defineInRange("maximumSpawnLight", 4, 0, 15);
            builder.pop();

            builder.push("behaviour");
            freezeWhenObserved = builder.comment("Freeze the Dreadstalker while its target clearly watches it.")
                    .define("freezeWhenObserved", true);
            freezeDistance = builder.comment("Maximum distance at which direct observation can freeze the Dreadstalker.")
                    .defineInRange("freezeDistance", 26.0D, 6.0D, 64.0D);
            teleporting = builder.comment("Allow rare line-of-sight based repositioning and behind-the-player teleports.")
                    .define("teleporting", true);
            darknessEffects = builder.comment("Allow proximity/attack Darkness effects used by the horror behaviour.")
                    .define("darknessEffects", true);
            builder.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue cameraEffects;
        public final ForgeConfigSpec.DoubleValue cameraEffectStrength;
        public final ForgeConfigSpec.BooleanValue terrorVignette;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.push("horrorEffects");
            cameraEffects = builder.comment("Enable subtle camera instability when a nearby Dreadstalker is actively hunting you.")
                    .define("cameraEffects", true);
            cameraEffectStrength = builder.comment("Strength multiplier for Dreadstalker camera effects.")
                    .defineInRange("cameraEffectStrength", 1.0D, 0.0D, 2.0D);
            terrorVignette = builder.comment("Enable the low-cost edge vignette while a Dreadstalker is close.")
                    .define("terrorVignette", true);
            builder.pop();
        }
    }

    private DreadstalkerConfig() {}
}
