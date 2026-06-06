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
        new KeyBindingSpec(ClientAction.CAPTIVATION_TOGGLE, "KP_1", FeatureId.CAPTIVATION, "key.minersadvantage.captivation_toggle"),
        new KeyBindingSpec(ClientAction.EXCAVATION_TOGGLE, "KP_2", FeatureId.EXCAVATION, "key.minersadvantage.excavation_toggle"),
        new KeyBindingSpec(ClientAction.ILLUMINATION_TOGGLE, "KP_3", FeatureId.ILLUMINATION, "key.minersadvantage.illumination_toggle"),
        new KeyBindingSpec(ClientAction.LUMBINATION_TOGGLE, "KP_4", FeatureId.LUMBINATION, "key.minersadvantage.lumbination_toggle"),
        new KeyBindingSpec(ClientAction.SHAFTANATION_TOGGLE, "KP_5", FeatureId.SHAFTANATION, "key.minersadvantage.shaftanation_toggle"),
        new KeyBindingSpec(ClientAction.SUBSTITUTION_TOGGLE, "KP_6", FeatureId.SUBSTITUTION, "key.minersadvantage.substitution_toggle"),
        new KeyBindingSpec(ClientAction.VEINATION_TOGGLE, "KP_7", FeatureId.VEINATION, "key.minersadvantage.veination_toggle"),
        new KeyBindingSpec(ClientAction.CROPINATION_TOGGLE, "KP_8", FeatureId.CROPINATION, "key.minersadvantage.cropination_toggle")
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
