package dev.brighten.ac.handler;

import dev.brighten.ac.data.APlayer;
import dev.brighten.ac.packet.wrapper.in.WPacketPlayInFlying;
import dev.brighten.ac.packet.wrapper.out.WPacketPlayOutEntityVelocity;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class VelocityHandler {

    private final APlayer PLAYER;

    private final Map<Vector, Boolean> VELOCITY_MAP = new HashMap<>();
    private final Set<Consumer<Vector>> VELOCITY_TASKS = new HashSet<>();

    

    /*
     * I want to be able to verify velocity when the pre packet comes back an the post packet comes back
     * So essentially I want to only take out the velocity from possibilities after the post flying comes back.
     */

    public void onPre(WPacketPlayOutEntityVelocity packet) {
        if(packet.getEntityId() != PLAYER.getBukkitPlayer().getEntityId()) return;

        VELOCITY_MAP.put(new Vector(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ()), false);
    }

    public void onPost(WPacketPlayOutEntityVelocity packet) {
        if(packet.getEntityId() != PLAYER.getBukkitPlayer().getEntityId()) return;

        VELOCITY_MAP.computeIfPresent(new Vector(packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ()),
                (velocity, queuedToRemove) -> true);
    }

    public Set<Vector> getPossibleVectors() {
        return VELOCITY_MAP.keySet();
    }

    public void onAccurateVelocity(Consumer<Vector> task) {
        VELOCITY_TASKS.add(task);
    }

    public void onFlyingPost(WPacketPlayInFlying packet) {
        val iterator = VELOCITY_MAP.entrySet().iterator();
        while(iterator.hasNext()) {
            val value = iterator.next();

            // Velocity definitely occurred, run task.
            if(Math.abs(value.getKey().getY() - packet.getY()) < 1E-6) {
                VELOCITY_TASKS.forEach(vel -> vel.accept(value.getKey()));
            }

            if(value.getValue())
                iterator.remove();
        }
    }


}
