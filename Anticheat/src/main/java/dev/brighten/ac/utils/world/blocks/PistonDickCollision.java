package dev.brighten.ac.utils.world.blocks;

import dev.brighten.ac.data.APlayer;
import dev.brighten.ac.handler.block.WrappedBlock;
import dev.brighten.ac.packet.ProtocolVersion;
import dev.brighten.ac.utils.world.CollisionBox;
import dev.brighten.ac.utils.world.types.CollisionFactory;
import dev.brighten.ac.utils.world.types.ComplexCollisionBox;
import dev.brighten.ac.utils.world.types.SimpleCollisionBox;

public class PistonDickCollision implements CollisionFactory {
    public static final int[] offsetsXForSide = new int[]{0, 0, 0, 0, -1, 1};

    @Override
    public CollisionBox fetch(ProtocolVersion version, APlayer player, WrappedBlock block) {
        byte data = block.getData();

        switch (clamp_int(data & 7, 0, offsetsXForSide.length - 1)) {
            case 0:
                return new ComplexCollisionBox(new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F),
                        new SimpleCollisionBox(0.375F, 0.25F, 0.375F, 0.625F, 1.0F, 0.625F));
            case 1:
                return new ComplexCollisionBox(new SimpleCollisionBox(0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F),
                        new SimpleCollisionBox(0.375F, 0.0F, 0.375F, 0.625F, 0.75F, 0.625F));
            case 2:
                return new ComplexCollisionBox(new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.25F),
                        new SimpleCollisionBox(0.25F, 0.375F, 0.25F, 0.75F, 0.625F, 1.0F));
            case 3:
                return new ComplexCollisionBox(new SimpleCollisionBox(0.0F, 0.0F, 0.75F, 1.0F, 1.0F, 1.0F),
                        new SimpleCollisionBox(0.25F, 0.375F, 0.0F, 0.75F, 0.625F, 0.75F));
            case 4:
                return new ComplexCollisionBox(new SimpleCollisionBox(0.0F, 0.0F, 0.0F, 0.25F, 1.0F, 1.0F),
                        new SimpleCollisionBox(0.375F, 0.25F, 0.25F, 0.625F, 0.75F, 1.0F));
            case 5:
                return new ComplexCollisionBox(new SimpleCollisionBox(0.75F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                        new SimpleCollisionBox(0.0F, 0.375F, 0.25F, 0.75F, 0.625F, 0.75F));
        }
        return null;
    }

    public static int clamp_int(int p_76125_0_, int p_76125_1_, int p_76125_2_) {
        return p_76125_0_ < p_76125_1_ ? p_76125_1_ : (p_76125_0_ > p_76125_2_ ? p_76125_2_ : p_76125_0_);
    }
}
