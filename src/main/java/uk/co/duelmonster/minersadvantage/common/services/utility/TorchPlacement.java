package uk.co.duelmonster.minersadvantage.common.services.utility;

/**
 * TorchPlacement keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public enum TorchPlacement {
    FLOOR,
    LEFT_WALL,
    RIGHT_WALL,
    BOTH_WALLS
}

