package rc55.mc.fluidlib.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rc55.mc.fluidlib.fluid.FluidLibFluidTags;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedInWater()Z"),
            method = "tickMovement"
    )
    public boolean fluidlib$hookCanSwimCheck(ClientPlayerEntity instance) {
        return instance.isSubmergedIn(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG);
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"),
            method = "tickMovement"
    )
    public boolean fluidlib$hookCanSwimCheck2(ClientPlayerEntity instance) {
        return instance.getFluidHeight(FluidLibFluidTags.DUMMY_WATER_PHYSICS_TAG) > 0.;
    }
}
