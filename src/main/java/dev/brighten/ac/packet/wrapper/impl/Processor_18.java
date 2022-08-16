package dev.brighten.ac.packet.wrapper.impl;

import dev.brighten.ac.packet.wrapper.PacketConverter;
import dev.brighten.ac.packet.wrapper.in.*;
import dev.brighten.ac.packet.wrapper.objects.EnumParticle;
import dev.brighten.ac.packet.wrapper.objects.WrappedEnumDirection;
import dev.brighten.ac.packet.wrapper.out.*;
import dev.brighten.ac.utils.math.IntVector;
import dev.brighten.ac.utils.reflections.types.WrappedClass;
import dev.brighten.ac.utils.reflections.types.WrappedField;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Processor_18 implements PacketConverter {
    @Override
    public WPacketPlayInFlying processFlying(Object object) {
        PacketPlayInFlying flying = (PacketPlayInFlying) object;
        return WPacketPlayInFlying.builder().x(flying.a()).y(flying.b()).z(flying.c()).yaw(flying.d())
                .pitch(flying.e()).onGround(flying.f()).moved(flying.g()).looked(flying.h()).build();
    }

    private static WrappedClass classUseEntity = new WrappedClass(PacketPlayInUseEntity.class);
    private static WrappedField fieldEntityId = classUseEntity.getFieldByName("a");
    @Override
    public WPacketPlayInUseEntity processUseEntity(Object object) {
        PacketPlayInUseEntity useEntity = (PacketPlayInUseEntity) object;

        return WPacketPlayInUseEntity.builder().entityId(fieldEntityId.get(useEntity))
                .action(WPacketPlayInUseEntity.EnumEntityUseAction.valueOf(useEntity.a().name()))
                .vector(useEntity.b() != null ? new Vector(useEntity.b().a, useEntity.b().b, useEntity.b().c) : null)
                .build();
    }

    private static final WrappedClass classAbilities = new WrappedClass(PacketPlayInAbilities.class);
    private static final WrappedField fieldFlySpeed = classAbilities.getFieldByType(float.class, 0),
            fieldWalkSpeed = classAbilities.getFieldByType(float.class, 1);
    @Override
    public WPacketPlayInAbilities processAbilities(Object object) {
        PacketPlayInAbilities abilities = (PacketPlayInAbilities) object;

        return WPacketPlayInAbilities.builder().allowedFlight(abilities.a()).flying(abilities.isFlying())
                .allowedFlight(abilities.c()).creativeMode(abilities.d()).flySpeed(fieldFlySpeed.get(abilities))
                .walkSpeed(fieldWalkSpeed.get(abilities)).build();
    }

    @Override
    public WPacketPlayInArmAnimation processAnimation(Object object) {
        PacketPlayInArmAnimation packet = (PacketPlayInArmAnimation) object;
        return WPacketPlayInArmAnimation.builder().timestamp(packet.timestamp).build();
    }

    @Override
    public WPacketPlayInBlockDig processBlockDig(Object object) {
        PacketPlayInBlockDig packet = (PacketPlayInBlockDig) object;

        BlockPosition pos = packet.a();

        return WPacketPlayInBlockDig.builder().blockPos(new IntVector(pos.getX(), pos.getY(), pos.getZ()))
                .direction(WrappedEnumDirection.valueOf(packet.b().name()))
                .digType(WPacketPlayInBlockDig.EnumPlayerDigType.valueOf(packet.c().name()))
                .build();
    }

    @Override
    public WPacketPlayInBlockPlace processBlockPlace(Object object) {
        PacketPlayInBlockPlace packet = (PacketPlayInBlockPlace) object;

        BlockPosition pos = packet.a();

        return WPacketPlayInBlockPlace.builder().blockPos(new IntVector(pos.getX(), pos.getY(), pos.getZ()))
                .direction(WrappedEnumDirection.values()[Math.min(packet.getFace(), 5)])
                .itemStack(CraftItemStack.asCraftMirror(packet.getItemStack()))
                .vecX(packet.d()).vecY(packet.e()).vecZ(packet.f())
                .build();
    }


    private static final WrappedClass classCloseWindow = new WrappedClass(PacketPlayInCloseWindow.class);
    private static final WrappedField fieldWindowId = classCloseWindow.getFieldByType(int.class, 0);
    @Override
    public WPacketPlayInCloseWindow processCloseWindow(Object object) {
        PacketPlayInCloseWindow packet = (PacketPlayInCloseWindow) object;

        return WPacketPlayInCloseWindow.builder().id(fieldWindowId.get(packet)).build();
    }

    @Override
    public WPacketPlayInEntityAction processEntityAction(Object object) {
        PacketPlayInEntityAction packet = (PacketPlayInEntityAction) object;

        return WPacketPlayInEntityAction.builder().action(WPacketPlayInEntityAction.EnumPlayerAction
                .valueOf(packet.b().name())).build();
    }

    @Override
    public WPacketPlayOutEntityEffect processEntityEffect(Object object) {
        PacketPlayOutEntityEffect packet = (PacketPlayOutEntityEffect) object;
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        try {
            packet.b(serializer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return WPacketPlayOutEntityEffect.builder().entityId(serializer.e())
                .effectId(serializer.readByte())
                .amplifier(serializer.readByte())
                .duration(serializer.e())
                .flags(serializer.readByte()).build();
    }

    @Override
    public WPacketPlayOutPosition processServerPosition(Object object) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        PacketPlayOutPosition position = (PacketPlayOutPosition) object;

        try {
            position.b(serializer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return WPacketPlayOutPosition.builder()
                .x(serializer.readDouble())
                .y(serializer.readDouble())
                .z(serializer.readDouble())
                .yaw(serializer.readFloat())
                .pitch(serializer.readFloat())
                .flags(PacketPlayOutPosition.EnumPlayerTeleportFlags.a(serializer.readUnsignedByte()).stream()
                        .map(f -> WPacketPlayOutPosition.EnumPlayerTeleportFlags.valueOf(f.name()))
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public WPacketPlayOutAttachEntity processAttach(Object object) {
        PacketDataSerializer serial = new PacketDataSerializer(Unpooled.buffer());
        PacketPlayOutAttachEntity packet = (PacketPlayOutAttachEntity) object;

        try {
            packet.b(serial);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return WPacketPlayOutAttachEntity.builder()
                .attachedEntityId(serial.readInt())
                .holdingEntityId(serial.readInt())
                .isLeashModifer((int)(serial.readUnsignedByte()) == 1)
                .build();
    }

    @Override
    public WPacketPlayOutEntity processOutEntity(Object object) {
        PacketDataSerializer serial = new PacketDataSerializer(Unpooled.buffer());
        PacketPlayOutEntity packet = (PacketPlayOutEntity) object;

        try {
            packet.b(serial);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int id = serial.e();
        byte x = 0, y = 0, z = 0, yaw = 0, pitch = 0;
        boolean ground = false, looked = false, moved = false;

        if(packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook) {
            x = serial.readByte();
            y = serial.readByte();
            z = serial.readByte();
            yaw = serial.readByte();
            pitch = serial.readByte();
            ground = serial.readBoolean();
            looked = moved = true;
        } else if(packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMove) {
            x = serial.readByte();
            y = serial.readByte();
            z = serial.readByte();
            ground = serial.readBoolean();

            moved = true;
        } else if(packet instanceof PacketPlayOutEntity.PacketPlayOutEntityLook) {
            yaw = serial.readByte();
            pitch = serial.readByte();
            ground = serial.readBoolean();

            looked = true;
        }

        return WPacketPlayOutEntity.builder()
                .id(id)
                .x(x / 32D).y(y / 32D).z(z / 32D).yaw(yaw / 256.0F * 360.0F).pitch(pitch / 256.0F * 360.0F)
                .onGround(ground).moved(moved).looked(looked)
                .build();
    }

    @Override
    public WPacketPlayOutEntityTeleport processEntityTeleport(Object object) {
        PacketDataSerializer serial = new PacketDataSerializer(Unpooled.buffer());
        PacketPlayOutEntityTeleport packet = (PacketPlayOutEntityTeleport) object;

        try {
            packet.b(serial);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return WPacketPlayOutEntityTeleport.builder()
                .entityId(serial.e())
                .x(serial.readInt() / 32D)
                .y(serial.readInt() / 32D)
                .z(serial.readInt() / 32D)
                .yaw(serial.readByte() / 256.0F * 360.0F)
                .pitch(serial.readByte() / 256.0F * 360.0F)
                .onGround(serial.readBoolean())
                .build();
    }

    @Override
    public WPacketHandshakingInSetProtocol processHandshakingProtocol(Object object) {
        PacketDataSerializer serial = new PacketDataSerializer(Unpooled.buffer());
        PacketHandshakingInSetProtocol packet = (PacketHandshakingInSetProtocol) object;

        try {
            packet.b(serial);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return WPacketHandshakingInSetProtocol.builder()
                .versionNumber(serial.e())
                .hostname(serial.c(32767))
                .port(serial.readUnsignedShort())
                .protocol(WPacketHandshakingInSetProtocol.EnumProtocol.valueOf(EnumProtocol.a(serial.e()).name()))
                .build();
    }

    @Override
    public WPacketPlayOutBlockChange processBlockChange(Object object) {
        PacketPlayOutBlockChange packet = (PacketPlayOutBlockChange) object;
        PacketDataSerializer serial = serialize(packet);

        BlockPosition blockPos = serial.c();
        IntVector vecPos = new IntVector(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Material material = CraftMagicNumbers.getMaterial(packet.block.getBlock());

        return WPacketPlayOutBlockChange.builder()
                .blockLocation(vecPos)
                .material(material)
                .build();
    }

    @Override
    public WPacketPlayOutMultiBlockChange processMultiBlockChange(Object object) {
        PacketPlayOutMultiBlockChange packet = (PacketPlayOutMultiBlockChange) object;
        PacketDataSerializer serial = serialize(packet);

        final int[] chunkLoc = new int[] {serial.readInt(), serial.readInt()};
        final WPacketPlayOutMultiBlockChange.BlockChange[]
                blockChanges = new WPacketPlayOutMultiBlockChange.BlockChange[serial.e()];

        for (int i = 0; i < blockChanges.length; i++) {
            short encodedloc = serial.readShort();

            IntVector loc = new IntVector(encodedloc >> 12 & 15, encodedloc & 255, encodedloc >> 8 & 15);
            Material blockType = CraftMagicNumbers.getMaterial(Block.d.a(serial.e()).getBlock());

            blockChanges[i] = new WPacketPlayOutMultiBlockChange.BlockChange(loc, blockType);
        }

        return WPacketPlayOutMultiBlockChange.builder()
                .chunk(chunkLoc)
                .changes(blockChanges)
                .build();
    }

    @Override
    public WPacketPlayOutEntityVelocity processVelocity(Object object) {
        PacketPlayOutEntityVelocity packet = (PacketPlayOutEntityVelocity) object;
        PacketDataSerializer serial = serialize(packet);

        return WPacketPlayOutEntityVelocity.builder()
                .entityId(serial.e())
                .deltaX(serial.readShort() / 8000D)
                .deltaY(serial.readShort() / 8000D)
                .deltaZ(serial.readShort() / 8000D)
                .build();
    }

    @Override
    public Object processVelocity(WPacketPlayOutEntityVelocity packet) {
        return new PacketPlayOutEntityVelocity(packet.getEntityId(), packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
    }

    @Override
    public WPacketPlayOutWorldParticles processParticles(Object object) {
        PacketPlayOutWorldParticles packet = (PacketPlayOutWorldParticles) object;
        PacketDataSerializer serial = serialize(packet);

        EnumParticle particle = EnumParticle.a(serial.readInt());

        if(particle == null) particle = EnumParticle.BARRIER;

        int[] data = new int[particle.d()];

        return WPacketPlayOutWorldParticles.builder()
                .particle(particle)
                .longD(serial.readBoolean())
                .x(serial.readFloat())
                .y(serial.readFloat())
                .z(serial.readFloat())
                .offsetX(serial.readFloat())
                .offsetY(serial.readFloat())
                .offsetZ(serial.readFloat())
                .speed(serial.readFloat())
                .amount(serial.readInt())
                .data(Arrays.stream(data).map(i -> serial.e()).toArray())
                .build();
    }

    @Override
    public Object processParticles(WPacketPlayOutWorldParticles packet) {
        return new PacketPlayOutWorldParticles(net.minecraft.server.v1_8_R3.EnumParticle.valueOf(packet.getParticle().name()),
                packet.isLongD(), packet.getX(), packet.getY(), packet.getZ(), packet.getOffsetX(), packet.getOffsetY(), packet.getOffsetZ(),
                packet.getSpeed(), packet.getAmount(), packet.getData());
    }

    private PacketDataSerializer serialize(Packet packet) {
        PacketDataSerializer serial = new PacketDataSerializer(Unpooled.buffer());
        try {
            packet.b(serial);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return serial;
    }
}
