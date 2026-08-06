package rc55.mc.fluidlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(SquidEntity.class)
public abstract class SquidEntityMixin extends WaterCreatureEntity {
    protected SquidEntityMixin(EntityType<? extends WaterCreatureEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/SquidEntity;isInsideWaterOrBubbleColumn()Z"),
            method = "tickMovement"
    )
    public boolean fluidlib$fixMovementUnderwater(boolean original) {
        return original || this.getFluidHeight(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.;
    }
}
