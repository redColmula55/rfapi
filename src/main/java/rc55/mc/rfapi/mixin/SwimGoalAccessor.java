package rc55.mc.rfapi.mixin;

import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@ApiStatus.Internal
@Mixin(SwimGoal.class)
public interface SwimGoalAccessor {
    @Accessor("mob")
    MobEntity rfapi$getGoalMob();
}
