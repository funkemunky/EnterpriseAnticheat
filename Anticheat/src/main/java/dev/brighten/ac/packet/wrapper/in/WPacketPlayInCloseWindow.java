package dev.brighten.ac.packet.wrapper.in;

import dev.brighten.ac.packet.wrapper.PacketType;
import dev.brighten.ac.packet.wrapper.WPacket;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WPacketPlayInCloseWindow extends WPacket {

    private int id;

    @Override
    public PacketType getPacketType() {
        return PacketType.CLIENT_CLOSE_WINDOW;
    }

    @Override
    public Object getPacket() {
        return null;
    }

    @Override
    public String toString() {
        return "WPacketPlayInCloseWindow{" +
                "id=" + id +
                '}';
    }
}
