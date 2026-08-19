package rc55.mc.rfapi.mixin;

import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import rc55.mc.rfapi.fluid.FluidSettings;

@Mixin(NavigationConditions.class)
public abstract class NavigationConditionsMixin {
    /**
     * @author redColmula55
     * @reason Fix entity navigation under water-like fluids
     */
    @Overwrite
    public static boolean isWaterAt(PathAwareEntity entity, BlockPos pos) {
        return FluidSettings.get(entity.getWorld().getFluidState(pos)).canSwim();
    }
}
