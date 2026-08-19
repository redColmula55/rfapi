package rc55.mc.rfapi.mixin;

import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import rc55.mc.rfapi.fluid.FluidSettings;
import rc55.mc.rfapi.fluid.FluidTags;

@Mixin(targets = "net.minecraft.entity.passive.FoxEntity$FoxSwimGoal")
public abstract class FoxSwimGoalMixin extends SwimGoal {
    private FoxSwimGoalMixin(MobEntity mob) {
        super(mob);
    }

    @Override
    public boolean canStart() {
        return ((SwimGoalAccessor)this).rfapi$getGoalMob().getFluidHeight(FluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.25
                || ((SwimGoalAccessor)this).rfapi$getGoalMob().isTouchingFluid(fluid -> fluid.getSettings().getMovementType() == FluidSettings.EntityMovementType.LAVA);
    }
}
