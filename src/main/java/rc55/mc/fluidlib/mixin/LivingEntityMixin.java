package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    protected LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInLava()Z"),
            slice = @Slice(
                    from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=jump"),
                    to = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=travel")
            ),
            method = "tickMovement"
    )
    public boolean fluidlib$modifyLavaPhysicsFluidCheck(LivingEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_LAVA_PHYSICS_TAG) > 0.;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isTouchingWater()Z"),
            slice = @Slice(
                    from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=jump"),
                    to = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=travel")
            ),
            method = "tickMovement"
    )
    public boolean fluidlib$modifyWaterPhysicsFluidCheck(LivingEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInLava()Z"),
            method = "travel"
    )
    public boolean fluidlib$hookPhysicsInLavaLikeFluid(LivingEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_LAVA_PHYSICS_TAG) > 0.;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isTouchingWater()Z"),
            method = {"travel", "fall"}
    )
    public boolean fluidlib$hookPhysicsInWaterLikeFluid(LivingEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.;
    }

    @SuppressWarnings("rawtypes")
    @ModifyArg(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = "baseTick"
    )
    public TagKey fluidlib$modifyEntityBreathing(TagKey par1) {
        return FluidLibFluidTags.DUMMY_UNBREATHABLE_TAG;
    }
}
