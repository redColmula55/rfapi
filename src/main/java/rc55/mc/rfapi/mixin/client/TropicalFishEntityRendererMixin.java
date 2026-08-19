package rc55.mc.rfapi.mixin.client;

import net.minecraft.client.render.entity.TropicalFishEntityRenderer;
import net.minecraft.entity.passive.TropicalFishEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TropicalFishEntityRenderer.class)
public abstract class TropicalFishEntityRendererMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/TropicalFishEntity;isTouchingWater()Z"
            ),
            method = "setupTransforms(Lnet/minecraft/entity/passive/TropicalFishEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V"
    )
    public boolean rfapi$fixFishRenderDirection(TropicalFishEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
