package rc55.mc.rfapi.mixin.client;

import net.minecraft.client.render.entity.*;
import net.minecraft.entity.passive.CodEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CodEntityRenderer.class)
public abstract class CodEntityRendererMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/CodEntity;isTouchingWater()Z"
            ),
            method = "setupTransforms(Lnet/minecraft/entity/passive/CodEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V"
    )
    public boolean rfapi$fixFishRenderDirection(CodEntity entity) {
        return entity.isTouchingFluid(fluid -> fluid.getSettings().canSwim());
    }
}
