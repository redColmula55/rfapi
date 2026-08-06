package rc55.mc.fluidlib.mixin;

import net.minecraft.entity.passive.FishEntity;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(targets = "net.minecraft.entity.passive.FishEntity$FishMoveControl")
public abstract class FishMoveControlMixin {
    @SuppressWarnings("rawtypes")
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FishEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = "tick"
    )
    public boolean fluidlib$fixFishMoveUnderwater(FishEntity instance, TagKey tagKey) {
        return instance.isSubmergedIn(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG);
    }
}
