package dev.brighten.ac.handler.block;

import dev.brighten.ac.data.APlayer;
import dev.brighten.ac.packet.wrapper.in.WPacketPlayInBlockDig;
import dev.brighten.ac.packet.wrapper.in.WPacketPlayInBlockPlace;
import dev.brighten.ac.packet.wrapper.out.WPacketPlayOutBlockChange;
import dev.brighten.ac.packet.wrapper.out.WPacketPlayOutMultiBlockChange;
import dev.brighten.ac.utils.BlockUtils;
import dev.brighten.ac.utils.Tuple;
import dev.brighten.ac.utils.math.IntVector;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class BlockUpdateHandler {
    private final Map<IntVector, Deque<Material>> blockInformation = new ConcurrentHashMap<>();

    private final APlayer player;

    public void onWorldChange() {
        blockInformation.clear();
    }

    /**
     * Keep track of block placements since the Bukkit API will be a bit behind
     * @param place
     */
    public void onPlace(WPacketPlayInBlockPlace place) {
        if(!place.getItemStack().getType().isBlock()) return;

        Deque<Material> possible = getPossibleMaterials(place.getBlockPos());
        possible.add(place.getItemStack().getType());
    }

    /**
     * Keep track of block breaking since the Bukkit API will be a bit behind.
     * @param dig
     */
    public void onDig(WPacketPlayInBlockDig dig) {
        Deque<Material> possible = getPossibleMaterials(dig.getBlockPos());
        possible.clear();
        possible.add(Material.AIR);
    }

    public void runUpdate(WPacketPlayOutBlockChange packet) {
        Deque<Material> blockInfo = blockInformation.compute(packet.getBlockLocation(), (blockLoc, blockI) -> {
            if(blockI == null) {
                blockI = new LinkedList<>();

                val optional = BlockUtils
                        .getBlockAsync(packet.getBlockLocation().toBukkitVector()
                                .toLocation(player.getBukkitPlayer().getWorld()));

                if(optional.isPresent()) {
                    Block block = optional.get();

                    blockI.add(block.getType());
                }
            }

            return blockI;
        });

        // Updating block information
        player.runInstantAction(k -> {
            if(!k.isEnd()) {
                blockInfo.add(packet.getMaterial());
            } else if(blockInfo.size() > 1) {
                blockInfo.removeFirst();
            }
        });
    }

    public void runUpdate(WPacketPlayOutMultiBlockChange packet) {
        List<Tuple<Deque<Material>, Material>> changes = new ArrayList<>();
        for (WPacketPlayOutMultiBlockChange.BlockChange change : packet.getChanges()) {
            Deque<Material> blockInfo = blockInformation.compute(change.getLocation(), (blockLoc, blockI) -> {
                if(blockI == null) {
                    blockI = new LinkedList<>();

                    val optional = BlockUtils
                            .getBlockAsync(change.getLocation().toBukkitVector()
                                    .toLocation(player.getBukkitPlayer().getWorld()));

                    if(optional.isPresent()) {
                        Block block = optional.get();

                        blockI.add(block.getType());
                    }
                }

                return blockI;
            });

            changes.add(new Tuple<>(blockInfo, change.getMaterial()));
        }

        player.runInstantAction(k -> {
            if(!k.isEnd()) {
                for (Tuple<Deque<Material>, Material> tuple : changes) {
                    tuple.one.add(tuple.two);
                }
            } else {
                for (Tuple<Deque<Material>, Material> tuple : changes) {
                    if(tuple.one.size() > 1) {
                        tuple.one.removeFirst();
                    }
                }
            }
        });
    }

    public Deque<Material> getPossibleMaterials(IntVector loc) {
        return blockInformation.compute(loc, (blockLoc, blockI) -> {
            if(blockI == null) {
                blockI = new LinkedList<>();

                val optional = BlockUtils
                        .getBlockAsync(loc.toBukkitVector()
                                .toLocation(player.getBukkitPlayer().getWorld()));

                if(optional.isPresent()) {
                    Block block = optional.get();

                    blockI.add(block.getType());
                }
            }

            return blockI;
        });
    }
}