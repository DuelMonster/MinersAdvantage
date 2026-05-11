package uk.co.duelmonster.minersadvantage.common.config;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * SyncedClientConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record SyncedClientConfig(
    ClientConfig client,
    CommonConfig common,
    CaptivationConfig captivation,
    CropinationConfig cropination,
    CultivationConfig cultivation,
    ExcavationConfig excavation,
    PathanationConfig pathanation,
    IlluminationConfig illumination,
    LumbinationConfig lumbination,
    ShaftanationConfig shaftanation,
    SubstitutionConfig substitution,
    VeinationConfig veination,
    VentilationConfig ventilation
) {
    /**
     * defaults exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static SyncedClientConfig defaults() {
        return new SyncedClientConfig(
            new ClientConfig(false),
            new CommonConfig(true, false, true, true, 1, true, 5, 3, 64),
            new CaptivationConfig(true, false, 16, 16, false, false, List.of("minecraft:rotten_flesh", "minecraft:egg")),
            new CropinationConfig(true, true),
            new CultivationConfig(true, 4),
            new ExcavationConfig(true, 3, 2, 10, false, false, false, List.of()),
            new PathanationConfig(true, 6, 3),
            new IlluminationConfig(true, 2, 1, 7, true),
            new LumbinationConfig(true, 32, 6, 8, true, true, false, true, true, List.of(), List.of(), List.of()),
            new ShaftanationConfig(true, 16, 10, 1, 2, TorchPlacement.FLOOR),
            new SubstitutionConfig(true, false, false, true, true, true, true, List.of()),
            new VeinationConfig(true, 4, List.of()),
            new VentilationConfig(true, 1, 16, 8, true)
        );
    }
}

