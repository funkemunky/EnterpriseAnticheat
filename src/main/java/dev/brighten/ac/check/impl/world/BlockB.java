package dev.brighten.ac.check.impl.world;

import dev.brighten.ac.check.Action;
import dev.brighten.ac.check.Check;
import dev.brighten.ac.check.CheckData;
import dev.brighten.ac.check.CheckType;
import dev.brighten.ac.data.APlayer;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;

@CheckData(name = "Block (B)", type = CheckType.INTERACT)
public class BlockB extends Check {
    public BlockB(APlayer player) {
        super(player);
    }

    @Action
    public void onBlock(BlockPlaceEvent event) {
        Block ba = event.getBlockAgainst();

        if (!event.getBlockPlaced().getType().isBlock()) return;
        Block b = event.getBlock();
        double ypos = b.getLocation().getY() - player.getBukkitPlayer().getLocation().getY();
        double distance = player.getBukkitPlayer().getLocation().distance(b.getLocation());
        double ab_distance = player.getBukkitPlayer().getLocation().distance(ba.getLocation()) + 0.3;

        if (distance >= 1.4 && distance > ab_distance && ypos <= 0.5) {
            flag("d:%.4f, ad:%.4f y=%.1f", distance, ab_distance, ypos);
        }
    }
}
