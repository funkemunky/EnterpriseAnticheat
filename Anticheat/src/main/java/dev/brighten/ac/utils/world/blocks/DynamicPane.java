package dev.brighten.ac.utils.world.blocks;

import dev.brighten.ac.data.APlayer;
import dev.brighten.ac.handler.block.WrappedBlock;
import dev.brighten.ac.packet.ProtocolVersion;
import dev.brighten.ac.utils.BlockUtils;
import dev.brighten.ac.utils.Materials;
import dev.brighten.ac.utils.XMaterial;
import dev.brighten.ac.utils.world.CollisionBox;
import dev.brighten.ac.utils.world.types.CollisionFactory;
import dev.brighten.ac.utils.world.types.ComplexCollisionBox;
import dev.brighten.ac.utils.world.types.SimpleCollisionBox;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.Optional;

@SuppressWarnings("Duplicates")
public class DynamicPane implements CollisionFactory {

    private static final double width = 0.0625;
    private static final double min = .5 - width;
    private static final double max = .5 + width;

    @Override
    public CollisionBox fetch(ProtocolVersion version, APlayer player, WrappedBlock b) {
        ComplexCollisionBox box = new ComplexCollisionBox(new SimpleCollisionBox(min, 0, min, max, 1, max));
        boolean east =  fenceConnects(version, player, b, BlockFace.EAST);
        boolean north = fenceConnects(version, player, b, BlockFace.NORTH);
        boolean south = fenceConnects(version, player, b, BlockFace.SOUTH);
        boolean west =  fenceConnects(version, player, b, BlockFace.WEST);

        if (version.isBelow(ProtocolVersion.V1_9) && !(east||north||south||west)) {
            east = true;
            west = true;
            north = true;
            south = true;
        }

        if (east) box.add(new SimpleCollisionBox(max, 0, min, 1, 1, max));
        if (west) box.add(new SimpleCollisionBox(0, 0, min, max, 1, max));
        if (north) box.add(new SimpleCollisionBox(min, 0, 0, max, 1, min));
        if (south) box.add(new SimpleCollisionBox(min, 0, max, max, 1, 1));
        return box;
    }


    private static boolean fenceConnects(ProtocolVersion v, APlayer player, WrappedBlock fenceBlock, BlockFace direction) {
        Optional<WrappedBlock> targetBlock = BlockUtils.getRelative(player, fenceBlock.getLocation(), direction, 1);

        if(!targetBlock.isPresent()) return false;
        Material target = targetBlock.get().getType();

        if (!isPane(target)&&DynamicFence.isBlacklisted(target))
            return false;

        if(Materials.checkFlag(target, Materials.STAIRS)) {
            if (v.isBelow(ProtocolVersion.V1_12)) return false;

            return dir(fenceBlock.getData()).getOppositeFace() == direction;
        }  else return isPane(target) || (target.isSolid() && !target.isTransparent());
    }

    private static boolean isPane(Material m) {
        XMaterial mat = BlockUtils.getXMaterial(m);

        return mat == XMaterial.IRON_BARS || mat.name().contains("PANE")
                || mat.name().contains("THIN");
    }

    private static BlockFace dir(byte data) {
        switch(data & 3) {
            case 0:
            default:
                return BlockFace.EAST;
            case 1:
                return BlockFace.WEST;
            case 2:
                return BlockFace.SOUTH;
            case 3:
                return BlockFace.NORTH;
        }
    }

}
