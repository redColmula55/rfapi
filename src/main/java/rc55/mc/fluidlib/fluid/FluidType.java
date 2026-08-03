package rc55.mc.fluidlib.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.AbstractBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a certain amount of fluid. This works similar to vanilla {@link ItemStack}
 * Use {@link #equals(Object)} to compare them, and use {@link #isOf(FluidType)} to only compare type of the fluid
 */
public class FluidType implements Comparable<FluidType> {
    private final Fluid fluid;
    private int amount;

    public FluidType(@NotNull Fluid fluid, int amount) {
        this.fluid = FluidHelper.trim(fluid);
        this.amount = amount;
    }
    public FluidType(FluidReference<?> fluid, int amount) {
        this(fluid.getStill(), amount);
    }
    public FluidType(@NotNull String fluid, int amount) {
        this(FluidRegistry.get(fluid), amount);
    }
    public FluidType(@NotNull Identifier fluid, int amount) {
        this(FluidRegistry.get(fluid), amount);
    }
    public FluidType(int fluid, int amount) {
        this(FluidRegistry.get(fluid), amount);
    }

    /**
     * Create from NBT
     */
    public static FluidType fromNbt(NbtCompound nbt) {
        String id = nbt.getString("id");
        int amount = nbt.getInt("amount");
        return nbt.isEmpty() || id.isEmpty() || amount <= 0 ? EMPTY : new FluidType(id, amount);
    }

    /// Empty
    public static final FluidType EMPTY = new FluidType(Fluids.EMPTY, 0);

    public static final Codec<FluidType> CODEC = RecordCodecBuilder.create(i -> i.group(
            Registries.FLUID.getCodec().fieldOf("id").forGetter(t -> t.fluid),
            Codec.INT.fieldOf("amount").forGetter(t -> t.amount)
    ).apply(i, FluidType::new));

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof FluidType type) {
            if (type.fluid == this.fluid && type.amount == this.amount) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fluid, this.amount);
    }

    @Override
    public String toString() {
        return this.isEmpty() ? "FluidType[EMPTY]" : String.format("FluidType[Fluid=%s,Amount=%d]", this.getFluidId(), this.amount);
    }

    @Override
    public int compareTo(@NotNull FluidType fluidType) {
        return this.getAmount() - fluidType.getAmount();
    }

    /// If it is empty
    public boolean isEmpty() {
        return this.fluid == null || this.fluid == Fluids.EMPTY || this.amount <= 0;
    }
    /// Stored fluid
    public Fluid getFluid() {
        return this.isEmpty() ? Fluids.EMPTY : this.fluid;
    }
    /// Stored fluid id
    public String getFluidId() {
        return this.isEmpty() ? "" : FluidRegistry.getId(this.getFluid()).toString();
    }
    public int getFluidRawId() {
        return this.isEmpty() ? -1 : FluidRegistry.getRawId(this.getFluid());
    }
    /// Stored fluid amount (mB)
    public int getAmount() {
        return this.isEmpty() ? 0 : this.amount;
    }

    public long getFabricAmount() {
        return (long) this.getAmount() * (FluidConstants.BUCKET / 1000L);
    }

    public boolean isOf(Fluid fluid) {
        return this.getFluid().matchesType(fluid);
    }
    public boolean isOf(FluidType type) {
        return type != null && this.isOf(type.getFluid());
    }
    public boolean isOf(FluidReference<?> fluid) {
        return fluid != null && fluid.isOf(this.fluid);
    }
    public boolean isIn(TagKey<Fluid> tag) {
        return this.getFluid().isIn(tag);
    }

    public boolean matches(Predicate<Fluid> predicate) {
        return predicate.test(this.fluid);
    }

    /**
     * Serialize this to a compound tag
     */
    public NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();
        if (!this.isEmpty()) {
            nbt.putString("id", this.getFluidId());
            nbt.putInt("amount", this.amount);
        }
        return nbt;
    }

    /**
     * @see ItemStack#copy()
     */
    public FluidType copy() {
        if (this.isEmpty()) return EMPTY;
        return new FluidType(this.fluid, this.amount);
    }

    /**
     * @see ItemStack#copyWithCount(int)
     */
    public FluidType copyWithCount(int i) {
        if (this.isEmpty()) return EMPTY;
        return this.copy().setAmount(i);
    }

    /**
     * @see ItemStack#copyAndEmpty()
     */
    public FluidType copyAndEmpty() {
        if (this.isEmpty()) return EMPTY;
        FluidType newType = this.copy();
        this.setAmount(0);
        return newType;
    }

    /**
     * @see ItemStack#split(int)
     */
    public FluidType split(int i) {
        int amount = Math.min(this.getAmount(), i);
        FluidType remain = this.copyWithCount(amount);
        this.decrease(amount);
        return remain;
    }

    /**
     * @see ItemStack#setCount(int)
     */
    public FluidType setAmount(int amount) {
        this.amount = Math.max(0, amount);
        return this;
    }

    /**
     * @see ItemStack#increment(int)
     */
    public FluidType increase(int amount) {
        return this.setAmount(this.getAmount() + amount);
    }

    /**
     * @see ItemStack#decrement(int)
     */
    public FluidType decrease(int amount) {
        return this.increase(-amount);
    }
}
