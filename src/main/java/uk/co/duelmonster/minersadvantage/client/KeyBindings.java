package uk.co.duelmonster.minersadvantage.client;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * KeyBindings keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class KeyBindings {
    /**
     * ClientAction keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public enum ClientAction {
        CAPTIVATION_TOGGLE,
        EXCAVATION_TOGGLE,
        EXCAVATION_MODE_TOGGLE,
        EXCAVATION_SINGLE_LAYER_TOGGLE,
        ILLUMINATION_TOGGLE,
        ILLUMINATION_PLACE,
        ILLUMINATION_AREA,
        LUMBINATION_TOGGLE,
        SHAFTANATION_TOGGLE,
        SHAFT_VENT_TOGGLE,
        SUBSTITUTION_TOGGLE,
        VEINATION_TOGGLE,
        CROPINATION_TOGGLE,
        ABORT_WORKERS
    }

    /**
     * KeyBindingSpec keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record KeyBindingSpec(ClientAction action, String defaultKey, FeatureId feature, String translationKey) {}

    private static final List<KeyBindingSpec> ALL = List.of(
        new KeyBindingSpec(ClientAction.CAPTIVATION_TOGGLE, "KP_1", FeatureId.CAPTIVATION, "minersadvantage.captivation.enabled.comment"),
        new KeyBindingSpec(ClientAction.EXCAVATION_TOGGLE, "KP_2", FeatureId.EXCAVATION, "minersadvantage.excavation.enabled.comment"),
        new KeyBindingSpec(ClientAction.EXCAVATION_MODE_TOGGLE, "GRAVE", FeatureId.EXCAVATION, "minersadvantage.excavation.toggle"),
        new KeyBindingSpec(ClientAction.EXCAVATION_SINGLE_LAYER_TOGGLE, "TAB", FeatureId.EXCAVATION, "minersadvantage.excavation.toggle.sl"),
        new KeyBindingSpec(ClientAction.ILLUMINATION_TOGGLE, "KP_3", FeatureId.ILLUMINATION, "minersadvantage.illumination.enabled.comment"),
        new KeyBindingSpec(ClientAction.ILLUMINATION_PLACE, "V", FeatureId.ILLUMINATION, "minersadvantage.illumination.place"),
        new KeyBindingSpec(ClientAction.ILLUMINATION_AREA, "F12", FeatureId.ILLUMINATION, "minersadvantage.illumination.area"),
        new KeyBindingSpec(ClientAction.LUMBINATION_TOGGLE, "KP_4", FeatureId.LUMBINATION, "minersadvantage.lumbination.enabled.comment"),
        new KeyBindingSpec(ClientAction.SHAFTANATION_TOGGLE, "KP_5", FeatureId.SHAFTANATION, "minersadvantage.shaftanation.enabled.comment"),
        new KeyBindingSpec(ClientAction.SHAFT_VENT_TOGGLE, "LEFT_ALT", FeatureId.SHAFTANATION, "minersadvantage.shaft.vent.toggle"),
        new KeyBindingSpec(ClientAction.SUBSTITUTION_TOGGLE, "KP_6", FeatureId.SUBSTITUTION, "minersadvantage.substitution.enabled.comment"),
        new KeyBindingSpec(ClientAction.VEINATION_TOGGLE, "KP_7", FeatureId.VEINATION, "minersadvantage.veination.enabled.comment"),
        new KeyBindingSpec(ClientAction.CROPINATION_TOGGLE, "KP_8", FeatureId.CROPINATION, "minersadvantage.cropination.enabled.comment"),
        new KeyBindingSpec(ClientAction.ABORT_WORKERS, "DELETE", null, "minersadvantage.abort.agents")
    );

    private KeyBindings() {
    }

    /**
     * all exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static List<KeyBindingSpec> all() {
        return ALL;
    }
}
