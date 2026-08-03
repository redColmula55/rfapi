package rc55.mc.fluidlib.fluid;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.fluid.*;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Reference of a specific type of fluid
 * This will hold it's still, flowing, block factor
 * @param <T> Class type of the fluid
 */
public class FluidReference<T extends FlowableFluid> {

    private final Identifier stillId, flowId, blockId;
    private T still, flowing;
    private Block block;
    private final FluidSettings settings;

    public FluidReference(Identifier stillId, Identifier flowId, Identifier blockId, Function<FluidReference<T>, FluidSettings.Builder> settingsProvider) {
        this.stillId = stillId;
        this.flowId = flowId;
        this.blockId = blockId;
        this.settings = settingsProvider.apply(this).block(this::getBlock).build();
    }

    /**
     * Create a reference for an existing fluid
     * @param fluid Fluid instance, can be either still or flow
     */
    @SuppressWarnings("unchecked")
    public FluidReference(@NotNull T fluid) {
        this.still = (T) fluid.getStill();
        this.stillId = FluidRegistry.getId(this.still);
        this.flowing = (T) fluid.getFlowing();
        this.flowId = FluidRegistry.getId(this.flowing);
        this.block = fluid.getDefaultState().getBlockState().getBlock();
        this.blockId = Registries.BLOCK.getId(this.block);
        this.settings = FluidSettings.get(fluid);
    }

    /**
     * Reference for {@linkplain Fluids#WATER vanilla water}
     */
    public static final FluidReference<WaterFluid> VANILLA_WATER = new FluidReference<>((WaterFluid) Fluids.WATER);

    /**
     * Reference for {@linkplain Fluids#LAVA vanilla lava}
     */
    public static final FluidReference<LavaFluid> VANILLA_LAVA = new FluidReference<>((LavaFluid) Fluids.LAVA);

    void onRegister(T still, T flowing, Block block) {
        this.still = still;
        this.flowing = flowing;
        this.block = block;
    }

    @SuppressWarnings("unchecked")
    public T getStill() {
        return this.still == null ? (this.still = (T) FluidRegistry.get(this.stillId)) : this.still;
    }

    public Identifier getStillId() {
        return this.stillId;
    }

    @SuppressWarnings("unchecked")
    public T getFlowing() {
        return this.flowing == null ? (this.flowing = (T) FluidRegistry.get(this.flowId)) : this.flowing;
    }

    public Identifier getFlowingId() {
        return this.flowId;
    }

    public Block getBlock() {
        return this.block == null ? (this.block = Registries.BLOCK.get(this.blockId)) : this.block;
    }

    public Identifier getBlockId() {
        return blockId;
    }

    public FluidSettings getSettings() {
        return settings;
    }

    @ApiStatus.Internal
    AbstractBlock.Settings createBlockSettings() {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .replaceable()
                .noCollision()
                .strength(100.0F)
                .pistonBehavior(PistonBehavior.DESTROY)
                .dropsNothing()
                .liquid()
                .sounds(BlockSoundGroup.INTENTIONALLY_EMPTY)
                .luminance(this.settings::getLight)
                .mapColor(this.settings.getMapColor());
        if (this.settings.hasRandomTick()) {
            settings.ticksRandomly();
        }
        return settings;
    }

    public boolean isOf(Fluid fluid) {
        return fluid != null && (fluid == this.getStill() || fluid == this.getFlowing());
    }

    public boolean isOf(FluidReference<?> fluid) {
        return fluid.getStillId().equals(this.getStillId()) && fluid.getFlowingId().equals(this.getFlowingId());
    }

    @Override
    public int hashCode() {
        return FluidRegistry.getRawId(this.getStill());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof FluidReference<?>) {
            return this.isOf((FluidReference<?>) obj);
        } else {
            return false;
        }
    }
}
