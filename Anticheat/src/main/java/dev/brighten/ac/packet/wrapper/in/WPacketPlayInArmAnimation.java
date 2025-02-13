package dev.brighten.ac.packet.wrapper.in;

import dev.brighten.ac.packet.wrapper.PacketType;
import dev.brighten.ac.packet.wrapper.WPacket;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WPacketPlayInArmAnimation extends WPacket {

    private long timestamp;

    @Override
    public PacketType getPacketType() {
        return PacketType.ARM_ANIMATION;
    }

    @Override
    public Object getPacket() {
        return null;
    }

    @Override
    public String toString() {
        return "WPacketPlayInArmAnimation{" +
                "timestamp=" + timestamp +
                '}';
    }
}