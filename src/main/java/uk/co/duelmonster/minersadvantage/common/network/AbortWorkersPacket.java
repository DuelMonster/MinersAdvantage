package uk.co.duelmonster.minersadvantage.common.network;

public record AbortWorkersPacket(
    long playerId,
    String source
) {}
