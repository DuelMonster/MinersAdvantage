package net.minecraft.network.protocol.common.custom;

public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();

    final class Type<T extends CustomPacketPayload> {
        private final Object id;

        public Type(Object id) {
            this.id = id;
        }

        public Object id() {
            return id;
        }
    }
}
