package rc55.mc.rfapi.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rc55.mc.rfapi.RFApiConfigs;
import rc55.mc.rfapi.fluid.FluidSettings;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Shadow
    private static float red, green, blue;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"
            ),
            method = "render"
    )
    private static void rfapi$modifyFluidFogColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        FluidState state = world.getFluidState(camera.getBlockPos());
        if (camera.getSubmersionType() == CameraSubmersionType.NONE && !state.isEmpty()) {
            FluidSettings.ColorSettings settings = getCustomColor(world, camera.getBlockPos());
            if (settings != null && settings.shouldRenderFog()) {
                final int color = settings.fogColor();
                red = ((color >> 16) & 0xFF) / 255f;
                green = ((color >> 8) & 0xFF) / 255f;
                blue = (color & 0xFF) / 255f;
            }
        }
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;"
            ),
            method = "applyFog"
    )
    private static CameraSubmersionType rfapi$modifyFluidFogRenderDistance(Camera instance) {
        if (MinecraftClient.getInstance().world != null) {
            FluidSettings.ColorSettings settings = getCustomColor(MinecraftClient.getInstance().world, instance.getBlockPos());
            if (settings != null && settings.shouldRenderFog()) {
                return switch (settings.fogType()) {
                    case WATER -> CameraSubmersionType.WATER;
                    case LAVA -> CameraSubmersionType.LAVA;
                    case SNOW -> CameraSubmersionType.POWDER_SNOW;
                    default -> instance.getSubmersionType();
                };
            }
        }
        return instance.getSubmersionType();
    }

    @Inject(at = @At("TAIL"), method = "applyFog")
    private static void rfapi$clearFogOnConfig(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (camera.getSubmersionType() == CameraSubmersionType.LAVA && RFApiConfigs.getInstance().clearLavaFog) {
            RenderSystem.setShaderFogStart(-8f);
            RenderSystem.setShaderFogEnd(viewDistance * 0.1f);
        } else if (camera.getSubmersionType() == CameraSubmersionType.POWDER_SNOW && RFApiConfigs.getInstance().clearSnowFog) {
            RenderSystem.setShaderFogStart(-8f);
            RenderSystem.setShaderFogEnd(viewDistance * 0.1f);
        }
    }

    @Unique
    private static @Nullable FluidSettings.ColorSettings getCustomColor(BlockView world, BlockPos pos) {
        return world.getFluidState(pos).isEmpty() ? null : world.getFluidState(pos).getFluid().getSettings().getColor();
    }
}
