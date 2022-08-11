package dev.brighten.ac.listener;

import dev.brighten.ac.Anticheat;
import dev.brighten.ac.data.APlayer;
import dev.brighten.ac.handler.thread.ThreadHandler;
import dev.brighten.ac.packet.handler.HandlerAbstract;
import dev.brighten.ac.packet.wrapper.PacketType;
import dev.brighten.ac.utils.Init;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

@Init
public class JoinListener implements Listener {

    public JoinListener() {
        Anticheat.INSTANCE.getPacketProcessor().processAsync(Anticheat.INSTANCE, EventPriority.NORMAL, event -> {
            if(event.isCancelled()) return;
            Optional<APlayer> aplayer = Anticheat.INSTANCE.getPlayerRegistry()
                    .getPlayer(event.getPlayer().getUniqueId());

            aplayer.ifPresent(player -> ThreadHandler.INSTANCE.getThread(player)
                    .runTask(() -> Anticheat.INSTANCE.getPacketHandler()
                            .process(player, event.getType(), event.getPacket())));
        });

        Anticheat.INSTANCE.getPacketProcessor().process(Anticheat.INSTANCE, EventPriority.HIGHEST, event -> {
            Optional<APlayer> op = Anticheat.INSTANCE.getPlayerRegistry().getPlayer(event.getPlayer().getUniqueId());

            if(!op.isPresent()) {
                return;
            }

            APlayer player = op.get();

            if(player.isSendingPackets()) return;

            if(event.getType().equals(PacketType.CLIENT_TRANSACTION)) {
                player.setSendingPackets(true);
                Object packetToSend = null;

                synchronized (player.getPacketQueue()) {
                    while((packetToSend = player.getPacketQueue().pollFirst()) != null) {
                        HandlerAbstract.getHandler().sendPacket(player, packetToSend);
                    }
                }
                player.setSendingPackets(false);
            } else {
                switch (event.getType()) {
                    case ENTITY:
                    case ENTITY_DESTROY:
                    case ENTITY_HEAD_ROTATION:
                    case ENTITY_MOVE:
                    case ENTITY_MOVELOOK:
                    case ENTITY_LOOK:
                    case BLOCK_CHANGE:
                    case MULTI_BLOCK_CHANGE:
                    case MAP_CHUNK: {
                        synchronized (player.getPacketQueue()) {
                            player.getPacketQueue().add(event.getPacket());
                        }
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        System.out.println("Generating for " + event.getPlayer().getName());
        APlayer player = Anticheat.INSTANCE.getPlayerRegistry().generate(event.getPlayer());

        HandlerAbstract.getHandler().add(event.getPlayer());

        player.callEvent(event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Anticheat.INSTANCE.getPlayerRegistry().unregister(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerKickEvent event) {
        Anticheat.INSTANCE.getPlayerRegistry().unregister(event.getPlayer().getUniqueId());
    }
}