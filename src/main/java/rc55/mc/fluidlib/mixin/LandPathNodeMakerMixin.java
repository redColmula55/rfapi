package rc55.mc.fluidlib.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidHelper;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;
import rc55.mc.fluidlib.fluid.FluidSettings;

@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin {
    // Changes how entity moves inside fluid
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = {"getCommonNodeType", "getNodeTypeFromNeighbors", "getFeetY(Lnet/minecraft/util/math/BlockPos;)D"}
    )
    private static boolean fluidlib$fixNavigationInFluid(FluidState state, TagKey<Fluid> tag) {
        return FluidHelper.checkEntityMoveAction(state.getFluid(), tag);
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;isTouchingWater()Z"),
            method = "getStart()Lnet/minecraft/entity/ai/pathing/PathNode;"
    )
    public boolean fluidlib$fixNavigationInFluid(MobEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"),
            method = "getStart()Lnet/minecraft/entity/ai/pathing/PathNode;"
    )
    public boolean fluidlib$fixNavigationInFluid(BlockState instance, Block block) {
        return FluidSettings.get(instance.getFluidState()).canSwim();
    }
}
