package rc55.mc.rfapi.mixin;

import com.google.common.base.Predicates;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(StrollTask.class)
public abstract class StrollTaskMixin {
    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/StrollTask;create(FLjava/util/function/Function;Ljava/util/function/Predicate;)Lnet/minecraft/entity/ai/brain/task/SingleTickTask;"
            ),
            method = "createDynamicRadius",
            index = 2
    )
    private static Predicate<PathAwareEntity> rfapi$fixEntityUnderwaterNavigation(Predicate<PathAwareEntity> shouldRun) {
        return Predicates.or(e -> Objects.requireNonNull(e).isTouchingFluid(fluid -> fluid.getSettings().canSwim()), shouldRun::test);
    }
}
