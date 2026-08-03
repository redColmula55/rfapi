package rc55.mc.fluidlib.mixin;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/Fluid;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = "placeFluid",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;ultrawarm()Z"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getX()I")
            )
    )
    public boolean fluidlib_hookUltrawarmPlaceableCheck(Fluid instance, TagKey<Fluid> tag) {
        return instance.isIn(tag == FluidTags.WATER ? FluidLibFluidTags.DISAPPEAR_IN_ULTRAWARM : tag);
    }
}
