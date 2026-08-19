package rc55.mc.rfapi.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
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
import rc55.mc.rfapi.client.FluidFogEvents;
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
        if (camera.getSubmersionType() != CameraSubmersionType.NONE && !RFApiConfigs.getInstance().replaceVanillaFog) return;
        FluidState state = world.getFluidState(camera.getBlockPos());
        if (!state.isEmpty()) {
            FluidSettings.ColorSettings settings = getCustomColor(world, camera.getBlockPos());
            // Vanilla water fog color depends on biome, so here we exclude it so vanilla logic can perform
            if (!state.isOf(Fluids.WATER) && settings != null && settings.shouldRenderFog()) {
                final int color = settings.fogColor();
                red = ((color >> 16) & 0xFF) / 255f;
                green = ((color >> 8) & 0xFF) / 255f;
                blue = (color & 0xFF) / 255f;
            }
            final int color = FluidFogEvents.COLOR.invoker().getFogColor(
                    camera, tickDelta, world, camera.getBlockPos(), state, viewDistance, skyDarkness
            );
            if (color != -1) {
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
        return getFogRenderType(MinecraftClient.getInstance().world, instance);
    }

    @Inject(at = @At("TAIL"), method = "applyFog")
    private static void rfapi$clearFogOnConfig(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        CameraSubmersionType type = getFogRenderType(MinecraftClient.getInstance().world, camera);
        if (type == CameraSubmersionType.LAVA && RFApiConfigs.getInstance().clearLavaFog) {
            RenderSystem.setShaderFogStart(-8f);
            RenderSystem.setShaderFogEnd(viewDistance * 0.1f);
        } else if (type == CameraSubmersionType.POWDER_SNOW && RFApiConfigs.getInstance().clearSnowFog) {
            RenderSystem.setShaderFogStart(-8f);
            RenderSystem.setShaderFogEnd(viewDistance * 0.1f);
        }
        FluidFogEvents.DISTANCE.invoker().modifyFogDistance(
                camera, fogType, viewDistance, thickFog, tickDelta,
                RenderSystem::setShaderFogStart, RenderSystem::setShaderFogEnd, RenderSystem::setShaderFogShape
        );
    }

    @Unique
    private static @Nullable FluidSettings.ColorSettings getCustomColor(BlockView world, BlockPos pos) {
        return world.getFluidState(pos).isEmpty() ? null : world.getFluidState(pos).getFluid().getSettings().getColor();
    }

    @Unique
    private static CameraSubmersionType getFogRenderType(@Nullable ClientWorld world, Camera camera) {
        if (world != null) {
            FluidSettings.ColorSettings settings = getCustomColor(world, camera.getBlockPos());
            if (settings != null && settings.shouldRenderFog()) {
                return switch (settings.fogType()) {
                    case WATER -> CameraSubmersionType.WATER;
                    case LAVA -> CameraSubmersionType.LAVA;
                    case SNOW -> CameraSubmersionType.POWDER_SNOW;
                    default -> camera.getSubmersionType();
                };
            }
        }
        return camera.getSubmersionType();
    }
}
