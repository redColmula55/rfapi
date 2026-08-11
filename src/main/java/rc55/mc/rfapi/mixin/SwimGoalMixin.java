package rc55.mc.rfapi.mixin;

import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import rc55.mc.rfapi.fluid.FluidHelper;

@Mixin(SwimGoal.class)
public abstract class SwimGoalMixin {
    @Shadow
    private @Final MobEntity mob;

    /**
     * @author redColmula55
     * @reason Fix navigation in fluids with physics but has no valid tags
     */
    @Overwrite
    public boolean canStart() {
        return FluidHelper.checkCanMobSwim(this.mob);
    }
}
