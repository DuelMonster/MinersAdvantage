package uk.co.duelmonster.minersadvantage.client;

//? if fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenuEntrypoint keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ModMenuEntrypoint implements ModMenuApi {
    @Override
    /**
     * getModConfigScreenFactory exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MinersAdvantageConfigScreen::create;
    }
}
//?} else {
/*
// This class is Fabric-only.
*/ //?}
