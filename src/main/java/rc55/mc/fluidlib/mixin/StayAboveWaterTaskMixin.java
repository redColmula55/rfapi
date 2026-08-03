package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import rc55.mc.fluidlib.fluid.FluidHelper;

@Mixin(StayAboveWaterTask.class)
public class StayAboveWaterTaskMixin {
    /**
     * @author redColmula55
     * @reason Fix entity swimming in fluid without valid tags
     */
    @Overwrite
    public boolean shouldRun(ServerWorld world, MobEntity mob) {
        return FluidHelper.checkCanMobSwim(mob);
    }
}
