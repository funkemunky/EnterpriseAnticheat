package dev.brighten.ac.utils.math;

import dev.brighten.ac.utils.MathHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

@AllArgsConstructor
@NoArgsConstructor
public class FloatVector implements Cloneable {
    @Getter
    @Setter
    private float x, y, z;

    public FloatVector(Location location) {
        this.x = MathHelper.floor_double(location.getX());
        this.y = MathHelper.floor_double(location.getY());
        this.z = MathHelper.floor_double(location.getZ());
    }

    public FloatVector clone() {
        try {
            return (FloatVector) super.clone();
        } catch(CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public Vector toBukkitVector() {
        return new Vector(x, y, z);
    }

    @Override
    public String toString() {
        return "FloatVector[" + x + ", " +  y + ", " + z + "]";
    }

    public FloatVector add(float x, float y, float z) {
        this.x+= x;
        this.y+= y;
        this.z+= z;
        return this;
    }

    public FloatVector add(FloatVector vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof FloatVector)) return false;

        FloatVector FloatVector = (FloatVector) o;
        return x == FloatVector.x && y == FloatVector.y && z == FloatVector.z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (Float.floatToIntBits(this.x) ^ Float.floatToIntBits(this.x) >>> 16);
        hash = 79 * hash + (Float.floatToIntBits(this.y) ^ Float.floatToIntBits(this.y) >>> 16);
        hash = 79 * hash + (Float.floatToIntBits(this.z) ^ Float.floatToIntBits(this.z) >>> 16);
        return hash;
    }
}