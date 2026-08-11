package rc55.mc.rfapi.mixin;

import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rc55.mc.rfapi.entity.IFluidCollidable;
import rc55.mc.rfapi.fluid.FluidHelper;
import rc55.mc.rfapi.fluid.FluidTags;
import rc55.mc.rfapi.fluid.FluidSettings;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin implements IFluidCollidable {
    @Shadow
    private @Final Set<TagKey<Fluid>> submergedFluidTag;

    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    // Adds dummy tags in submergedFluidTag, in order to modify physics related submersion check
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;streamTags()Ljava/util/stream/Stream;"),
            method = "updateSubmergedInWaterState"
    )
    public Stream<TagKey<Fluid>> rfapi$appendDummyFluidTags(FluidState instance) {
        final FluidSettings.EntityMovementType type = instance.getFluid().getSettings().getMovementType();
        if (type == FluidSettings.EntityMovementType.WATER) {
            this.submergedFluidTag.add(FluidTags.DUMMY_WATER_PHYSICS_TAG);
        } else if (type == FluidSettings.EntityMovementType.LAVA) {
            this.submergedFluidTag.add(FluidTags.DUMMY_LAVA_PHYSICS_TAG);
        }
        if (!instance.isEmpty() && !instance.getFluid().getSettings().canEntityBreath()) {
            this.submergedFluidTag.add(FluidTags.DUMMY_UNBREATHABLE_TAG);
        }
        return instance.streamTags();
    }

    @Inject(at = @At("HEAD"), method = "updateWaterState")
    public void rfapi$initCustomFluidHeightMap(CallbackInfoReturnable<Boolean> cir) {
        this.rfapi$fluidHeightMap.clear();
    }

    @ModifyReturnValue(method = "updateWaterState", at = @At("RETURN"))
    public boolean rfapi$hookTouchingFluidCheck(boolean original) {
        this.updateMovementInFluid(FluidSettings.EntityMovementType.HORIZONTAL, 0.014f);
        return original || this.isTouchingFluid(Predicates.alwaysTrue());
    }

    @Redirect(method = {"move", "isCrawling"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isTouchingWater()Z"))
    public boolean rfapi$fixPlayerSwimmingSpeed(Entity instance) {
        return this.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }

    @Inject(at = @At("TAIL"), method = "baseTick")
    public void rfapi$applyLavaLikeFluidEffects(CallbackInfo ci) {
        final Entity self = (Entity)(Object) this;
        if (this.isTouchingFluid(fluid -> fluid.getSettings().getMovementType() == FluidSettings.EntityMovementType.LAVA)) {
            if (self.fallDistance > 0f) {
                self.fallDistance *= 0.5f;
            }
        }
        if (this.isTouchingFluid(fluid -> fluid.getSettings().canSetFire())) {
            self.setOnFireFromLava();
        }
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "shouldSpawnSprintingParticles")
    public boolean rfapi$fixSprintingParticle(boolean original) {
        final Entity self = (Entity)(Object) this;
        return original && !self.isInSwimmingPose() && !this.isTouchingFluid(Predicates.alwaysTrue());
    }

    /**
     * @author redColmula55
     * @reason Changes fluid check for entities
     */
    @Overwrite
    public double getFluidHeight(TagKey<Fluid> tag) {
        if (tag == FluidTags.WATER) {
            return this.fluidHeight.getDouble(FluidTags.DUMMY_WATER_PHYSICS_TAG);
        } else if (tag == FluidTags.LAVA) {
            return this.fluidHeight.getDouble(FluidTags.DUMMY_LAVA_PHYSICS_TAG);
        }
        return this.fluidHeight.getDouble(tag);
    }

    /**
     * @author redColmula55
     * @reason Make watery fluid(Movement type set to {@link rc55.mc.rfapi.fluid.FluidSettings.EntityMovementType#WATER}) swimmable
     */
    @Overwrite
    public void updateSwimming() {
        final Entity self = (Entity)(Object) this;

        if (self.isSwimming()) {
            self.setSwimming(self.isSprinting()
                    && this.isTouchingFluid(fluid -> fluid.getSettings().canSwim())
                    && !self.hasVehicle()
            );
        } else {
            self.setSwimming(self.isSprinting()
                    && this.submergedFluidTag.contains(FluidTags.DUMMY_WATER_PHYSICS_TAG)
                    && !self.hasVehicle()
                    && FluidSettings.get(self.getWorld().getFluidState(self.getBlockPos())).canSwim()
            );
        }
    }

    /**
     * @author redColmula55
     * @reason Make fluids able to push entities
     */
    @Overwrite
    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        if (tag == FluidTags.WATER) {
            this.updateMovementInFluid(FluidSettings.EntityMovementType.WATER, speed);
        } else if (tag == FluidTags.LAVA) {
            this.updateMovementInFluid(FluidSettings.EntityMovementType.LAVA, speed);
        } else {
            this.updateMovementInFluid(FluidSettings.EntityMovementType.HORIZONTAL, speed);
            return false;
        }
        return this.fluidHeight.containsKey(tag);
    }

    @Unique
    private final Object2DoubleMap<Fluid> rfapi$fluidHeightMap = new Object2DoubleArrayMap<>();

    @Unique
    public void updateMovementInFluid(FluidSettings.EntityMovementType type, double speed) {
        final Entity self = (Entity)(Object) this;
        if (self.isRegionUnloaded()) {
            return;// false;
        }
        Box box = self.getBoundingBox().contract(0.001);
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);
        double touchedFluidHeight = 0.0;
        boolean canPush = self.isPushedByFluids();
        boolean bl2 = false;
        Vec3d motionVec = Vec3d.ZERO;
        int count = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        final List<Fluid> list = new ArrayList<>(2);
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutable.set(x, y, z);
                    FluidState fluidState = self.getWorld().getFluidState(mutable);
                    FluidSettings settings = FluidSettings.get(fluidState);
                    if (!fluidState.isEmpty() && settings.getMovementType() == type) {//fluidState.isIn(tag)
                        double fluidHeight = y + fluidState.getHeight(self.getWorld(), mutable);
                        if (fluidHeight >= box.minY) {
                            bl2 = true;
                            touchedFluidHeight = Math.max(fluidHeight - box.minY, touchedFluidHeight);
                            if (canPush) {
                                Vec3d vec3d2 = fluidState.getVelocity(self.getWorld(), mutable);
                                if (touchedFluidHeight < 0.4) {
                                    vec3d2 = vec3d2.multiply(touchedFluidHeight);
                                }

                                motionVec = motionVec.add(vec3d2);
                                count++;
                            }
                        }

                        list.add(FluidHelper.trim(fluidState.getFluid()));
                    }
                }
            }
        }

        if (motionVec.length() > 0.0) {
            if (count > 0) {
                motionVec = motionVec.multiply(1.0 / count);
            }

            if (!(self instanceof PlayerEntity)) {
                motionVec = motionVec.normalize();
            }

            Vec3d vec3d3 = self.getVelocity();
            motionVec = motionVec.multiply(speed * 1.0);
            double f = 0.003;
            if (Math.abs(vec3d3.x) < 0.003 && Math.abs(vec3d3.z) < 0.003 && motionVec.length() < 0.0045000000000000005) {
                motionVec = motionVec.normalize().multiply(0.0045000000000000005);
            }

            self.setVelocity(self.getVelocity().add(motionVec));
        }

//            this.fluidHeight.put(tag, touchedFluidHeight);
//            return bl2;
        double finalTouchedFluidHeight = touchedFluidHeight;
        list.forEach(fluid -> {
            this.rfapi$fluidHeightMap.put(fluid, finalTouchedFluidHeight);

            final FluidSettings settings = fluid.getSettings();
            if (settings.getMovementType() == FluidSettings.EntityMovementType.WATER) {
                this.fluidHeight.put(FluidTags.DUMMY_WATER_PHYSICS_TAG, finalTouchedFluidHeight);
                // Reduce fall damage in watery fluid
                self.onLanding();
            } else if (settings.getMovementType() == FluidSettings.EntityMovementType.LAVA) {
                this.fluidHeight.put(FluidTags.DUMMY_LAVA_PHYSICS_TAG, finalTouchedFluidHeight);
            }

            if (fluid.isIn(net.minecraft.registry.tag.FluidTags.WATER)) {
                this.fluidHeight.put(net.minecraft.registry.tag.FluidTags.WATER, finalTouchedFluidHeight);
            }
            if (fluid.isIn(net.minecraft.registry.tag.FluidTags.LAVA)) {
                this.fluidHeight.put(net.minecraft.registry.tag.FluidTags.LAVA, finalTouchedFluidHeight);
            }
        });
    }

    @Override
    public boolean isTouchingFluid(Predicate<Fluid> predicate) {
        return this.rfapi$fluidHeightMap.object2DoubleEntrySet().stream().anyMatch(entry -> {
            final Fluid fluid = entry.getKey();
            if (predicate.test(fluid)) {
                return entry.getDoubleValue() > 0.;
            }
            return false;
        });
    }
}
