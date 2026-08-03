package rc55.mc.fluidlib.mixin;

import net.minecraft.block.ConduitBlock;
import net.minecraft.block.CoralParentBlock;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({SeagrassBlock.class, KelpBlock.class, CoralParentBlock.class, ConduitBlock.class})
public class WaterloggablePlantBlocksMixin {
    /* Vanilla underwater plants etc. can be placed in any fluid with #minecraft:water tag
     * But Fabric mods will usually add fluids in #minecraft:water in order to make them has physics
     * Also when in vanilla or in most mods, blocks can only be waterlogged with actual vanilla water
     * Thus making mod fluids turns into water while Minecraft trying to waterlog these blocks
     * Therefor we need to Mixin them to make them only waterlog in vanilla water to prevent this
     */
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            method = "getPlacementState"
    )
    public boolean fluidlib_checkWaterloggingPlacement(FluidState instance, TagKey<Fluid> tag) {
        return instance.isOf(Fluids.WATER);
    }
}
