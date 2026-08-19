package rc55.mc.rfapi.mixin;

import com.google.common.base.Predicates;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.FrogBrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(FrogBrain.class)
public abstract class FrogBrainMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/TaskTriggerer;predicate(Ljava/util/function/Predicate;)Lnet/minecraft/entity/ai/brain/task/SingleTickTask;"
            ),
            method = "addSwimActivities"
    )
    private static <E extends LivingEntity> SingleTickTask<E> rfapi$fixFrogSwimmingCheck(Predicate<E> predicate) {
        return TaskTriggerer.predicate(Predicates.or(
                e -> Objects.requireNonNull(e).isTouchingFluid(fluid -> fluid.getSettings().canSwim()),
                predicate::test
        ));
    }
}
