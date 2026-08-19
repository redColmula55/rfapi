package rc55.mc.rfapi.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.AxolotlBrain;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.rfapi.fluid.FluidSettings;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(AxolotlBrain.class)
public abstract class AxolotlBrainMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/TaskTriggerer;predicate(Ljava/util/function/Predicate;)Lnet/minecraft/entity/ai/brain/task/SingleTickTask;"
            ),
            method = "addIdleActivities"
    )
    private static <E extends LivingEntity> SingleTickTask<E> rfapi$fixAxolotlSwimmingCheck(Predicate<E> predicate) {
        return TaskTriggerer.predicate(AxolotlBrainMixin::isInsideWaterOrBubbleColumn);
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isInsideWaterOrBubbleColumn()Z"
            ),
            method = {"getTargetApproachingSpeed", "getTargetApproachingSpeed", "getAdultFollowingSpeed"}
    )
    private static boolean rfapi$fixAxolotlSwimmingSpeed(LivingEntity instance) {
        return isInsideWaterOrBubbleColumn(instance);
    }

    /**
     * @author redColmula55
     * @reason Fix axolotl navigation
     */
    @Overwrite
    private static boolean canGoToLookTarget(LivingEntity entity) {
        World world = entity.getWorld();
        Optional<LookTarget> optional = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LOOK_TARGET);
        if (optional.isPresent()) {
            BlockPos blockPos = optional.get().getBlockPos();
            return FluidSettings.get(world.getFluidState(blockPos)).canSwim() == isInsideWaterOrBubbleColumn(entity);
        } else {
            return false;
        }
    }

    @Unique
    private static boolean isInsideWaterOrBubbleColumn(Entity entity) {
        return entity.isInsideWaterOrBubbleColumn() || entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
