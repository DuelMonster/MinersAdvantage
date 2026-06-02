package uk.co.duelmonster.minersadvantage.client;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * KeyBindings keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class KeyBindings {
    /**
     * ClientAction keeps this part of MinersAdvantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public enum ClientAction {
        EXCAVATION_MODE_TOGGLE,
        SHAFT_VENT_TOGGLE,
        EXCAVATION_SHAPE_NEXT,
        EXCAVATION_SHAPE_PREV,
        SHAFTANATION_SHAPE_NEXT,
        SHAFTANATION_SHAPE_PREV,
        ILLUMINATION_PLACE,
        ILLUMINATION_AREA,
        ABORT_WORKERS,

        CAPTIVATION_TOGGLE,
        CROPINATION_TOGGLE,
        EXCAVATION_TOGGLE,
        ILLUMINATION_TOGGLE,
        SUBSTITUTION_TOGGLE,
        LUMBINATION_TOGGLE,
        SHAFTANATION_TOGGLE,
        VEINATION_TOGGLE
    }

    /**
     * KeyBindingSpec keeps this part of MinersAdvantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record KeyBindingSpec(ClientAction action, String defaultKey, FeatureId feature, String translationKey) {}

    private static final List<KeyBindingSpec> ALL = List.of(
        new KeyBindingSpec(ClientAction.EXCAVATION_MODE_TOGGLE, "GRAVE", FeatureId.EXCAVATION, "minersadvantage.excavation.toggle"),
        new KeyBindingSpec(ClientAction.SHAFT_VENT_TOGGLE, "LEFT_ALT", FeatureId.SHAFTANATION, "minersadvantage.shaft.vent.toggle"),
        new KeyBindingSpec(ClientAction.ILLUMINATION_PLACE, "V", FeatureId.ILLUMINATION, "minersadvantage.illumination.place"),
        new KeyBindingSpec(ClientAction.ILLUMINATION_AREA, "F12", FeatureId.ILLUMINATION, "minersadvantage.illumination.area"),
        new KeyBindingSpec(ClientAction.ABORT_WORKERS, "DELETE", null, "minersadvantage.abort.agents"),
        // Feature toggles - no default keys, but still need translation keys and feature associations for the config screen.
        new KeyBindingSpec(ClientAction.CAPTIVATION_TOGGLE, null, FeatureId.CAPTIVATION, "minersadvantage.captivation.enabled.comment"),
        new KeyBindingSpec(ClientAction.EXCAVATION_TOGGLE, null, FeatureId.EXCAVATION, "minersadvantage.excavation.enabled.comment"),
        new KeyBindingSpec(ClientAction.ILLUMINATION_TOGGLE, null, FeatureId.ILLUMINATION, "minersadvantage.illumination.enabled.comment"),
        new KeyBindingSpec(ClientAction.LUMBINATION_TOGGLE, null, FeatureId.LUMBINATION, "minersadvantage.lumbination.enabled.comment"),
        new KeyBindingSpec(ClientAction.SHAFTANATION_TOGGLE, null, FeatureId.SHAFTANATION, "minersadvantage.shaftanation.enabled.comment"),
        new KeyBindingSpec(ClientAction.SUBSTITUTION_TOGGLE, null, FeatureId.SUBSTITUTION, "minersadvantage.substitution.enabled.comment"),
        new KeyBindingSpec(ClientAction.VEINATION_TOGGLE, null, FeatureId.VEINATION, "minersadvantage.veination.enabled.comment"),
        new KeyBindingSpec(ClientAction.CROPINATION_TOGGLE, null, FeatureId.CROPINATION, "minersadvantage.cropination.enabled.comment")
    );

    /**
     * KeyBindings exists so this path stays predictable and easier to debug when things get weird.
     */
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
