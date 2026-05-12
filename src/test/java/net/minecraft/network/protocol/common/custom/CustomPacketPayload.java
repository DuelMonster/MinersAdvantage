package net.minecraft.network.protocol.common.custom;

/**
 * CustomPacketPayload keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();

    /**
     * Type keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    final class Type<T extends CustomPacketPayload> {
        private final Object id;

        /**
         * Type exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        public Type(Object id) {
            this.id = id;
        }

        /**
         * id exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        public Object id() {
            return id;
        }
    }
}
