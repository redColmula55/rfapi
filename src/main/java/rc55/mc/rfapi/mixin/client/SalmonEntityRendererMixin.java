package rc55.mc.rfapi.mixin.client;

import net.minecraft.client.render.entity.SalmonEntityRenderer;
import net.minecraft.entity.passive.SalmonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SalmonEntityRenderer.class)
public abstract class SalmonEntityRendererMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/SalmonEntity;isTouchingWater()Z"
            ),
            method = "setupTransforms(Lnet/minecraft/entity/passive/SalmonEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V"
    )
    public boolean rfapi$fixFishRenderDirection(SalmonEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
