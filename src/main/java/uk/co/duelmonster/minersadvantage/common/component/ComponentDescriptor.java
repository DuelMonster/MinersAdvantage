package uk.co.duelmonster.minersadvantage.common.component;

/**
 * ComponentDescriptor keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public record ComponentDescriptor(String id, String displayName, ComponentLifecycle component) {
}



