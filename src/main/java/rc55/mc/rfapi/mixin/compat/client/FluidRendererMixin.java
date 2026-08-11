package rc55.mc.rfapi.mixin.compat.client;

import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadViewMutable;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rc55.mc.rfapi.client.FluidRenderRegistry;
import rc55.mc.rfapi.fluid.FluidSettings;

/**
 * Fluid renderer for FluidLib fluids
 * Sodium compatible
 * @author redColmula55
 */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Shadow(remap = false)
    private @Final ModelQuadViewMutable quad;
    
    @Shadow(remap = false)
    private @Final LightPipelineProvider lighters;

    @Shadow(remap = false)
    private static boolean isAlignedEquals(float a, float b) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    protected abstract void updateQuad(ModelQuadView quad, WorldSlice world, BlockPos pos, LightPipeline lighter, Direction dir, float brightness, ColorProvider<FluidState> colorProvider, FluidState fluidState);

    @Shadow
    protected abstract void writeQuad(ChunkModelBuilder builder, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip);

    @Shadow
    private static FluidRenderHandler getFluidRenderHandler(FluidState fluidState) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    protected abstract ColorProvider<FluidState> getColorProvider(Fluid fluid, FluidRenderHandler handler);

    @Shadow(remap = false)
    private static void setVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {
        throw new UnsupportedOperationException();
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void rfapi$hookCustomFluidRender(WorldSlice world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkBuildBuffers buffers, CallbackInfo ci) {
        final BlockState blockState = world.getBlockState(pos);
        final Fluid fluid = fluidState.getFluid();

        if (!FluidRenderRegistry.hasCustomRenderer(fluid)) {
            return;
        }

        Material material = DefaultMaterials.forFluidState(fluidState);
        ChunkModelBuilder meshBuilder = buffers.get(material);
        
        final FluidSettings settings = fluid.getSettings();
        final boolean flowsUp = settings.flowsUp();

        //计算要渲染的面
        BlockState blockStateDown = world.getBlockState(pos.offset(Direction.DOWN));
        FluidState fluidStateDown = blockStateDown.getFluidState();
        BlockState blockStateUp = world.getBlockState(pos.offset(Direction.UP));
        FluidState fluidStateUp = blockStateUp.getFluidState();
        BlockState blockStateNorth = world.getBlockState(pos.offset(Direction.NORTH));
        FluidState fluidStateNorth = blockStateNorth.getFluidState();
        BlockState blockStateSouth = world.getBlockState(pos.offset(Direction.SOUTH));
        FluidState fluidStateSouth = blockStateSouth.getFluidState();
        BlockState blockStateWest = world.getBlockState(pos.offset(Direction.WEST));
        FluidState fluidStateWest = blockStateWest.getFluidState();
        BlockState blockStateEast = world.getBlockState(pos.offset(Direction.EAST));
        FluidState fluidStateEast = blockStateEast.getFluidState();
        boolean hasSideUp, hasSideDown;
        if (flowsUp) {
            hasSideDown = !isSameFluid(fluidState, fluidStateDown);
            hasSideUp = shouldRenderSide(world, pos, fluidState, blockState, Direction.UP, fluidStateUp, flowsUp)
                    && !isSideCovered(world, pos, Direction.UP, 0.8888889f, blockStateUp, flowsUp);
        } else {
            hasSideDown = shouldRenderSide(world, pos, fluidState, blockState, Direction.DOWN, fluidStateDown, flowsUp)
                    && !isSideCovered(world, pos, Direction.DOWN, 0.8888889f, blockStateDown, flowsUp);
            hasSideUp = !isSameFluid(fluidState, fluidStateUp);
        }
        boolean hasSideN = shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, fluidStateNorth, flowsUp);
        boolean hasSideS = shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, fluidStateSouth, flowsUp);
        boolean hasSideW = shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, fluidStateWest, flowsUp);
        boolean hasSideE = shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, fluidStateEast, flowsUp);
        //渲染
        if (hasSideDown || hasSideUp || hasSideE || hasSideW || hasSideN || hasSideS) {
            FluidRenderHandler handler = getFluidRenderHandler(fluidState);
            ColorProvider<FluidState> colorProvider = this.getColorProvider(fluid, handler);
            Sprite[] sprites = handler.getFluidSprites(world, pos, fluidState);

            //渲染高度
            final float thisHeight = getFluidHeight(world, fluid, pos, blockState, fluidState, flowsUp);
            //计算方块顶点位置
            float cornerHeightNE, cornerHeightNW, cornerHeightSE, cornerHeightSW;
            if (thisHeight >= 1.0F) {
                //完整方块
                cornerHeightNE = 1.0F;
                cornerHeightNW = 1.0F;
                cornerHeightSE = 1.0F;
                cornerHeightSW = 1.0F;
            } else {
                //非完整方块
                float heightN = getFluidHeight(world, fluid, pos.north(), blockStateNorth, fluidStateNorth, flowsUp);
                float heightS = getFluidHeight(world, fluid, pos.south(), blockStateSouth, fluidStateSouth, flowsUp);
                float heightE = getFluidHeight(world, fluid, pos.east(), blockStateEast, fluidStateEast, flowsUp);
                float heightW = getFluidHeight(world, fluid, pos.west(), blockStateWest, fluidStateWest, flowsUp);
                cornerHeightNE = calculateFluidHeight(world, fluid, thisHeight, heightN, heightE, pos.offset(Direction.NORTH).offset(Direction.EAST), flowsUp);
                cornerHeightNW = calculateFluidHeight(world, fluid, thisHeight, heightN, heightW, pos.offset(Direction.NORTH).offset(Direction.WEST), flowsUp);
                cornerHeightSE = calculateFluidHeight(world, fluid, thisHeight, heightS, heightE, pos.offset(Direction.SOUTH).offset(Direction.EAST), flowsUp);
                cornerHeightSW = calculateFluidHeight(world, fluid, thisHeight, heightS, heightW, pos.offset(Direction.SOUTH).offset(Direction.WEST), flowsUp);
            }
            //坐标值
            float yOffset = (flowsUp ? hasSideUp : hasSideDown) ? 0.001F : 0.0F;

            ModelQuadViewMutable quad = this.quad;
            LightMode lightMode = MinecraftClient.isAmbientOcclusionEnabled() ? LightMode.SMOOTH : LightMode.FLAT;//isWater && 
            LightPipeline lighter = this.lighters.getLighter(lightMode);
            quad.setFlags(0);
            
            boolean shouldRenderTop;
            if (flowsUp) {
                shouldRenderTop = hasSideDown && !isSideCovered(world, pos, Direction.DOWN, Math.min(Math.min(cornerHeightNW, cornerHeightSW), Math.min(cornerHeightSE, cornerHeightNE)), blockStateDown, flowsUp);
            } else {
                shouldRenderTop = hasSideUp && !isSideCovered(world, pos, Direction.UP, Math.min(Math.min(cornerHeightNW, cornerHeightSW), Math.min(cornerHeightSE, cornerHeightNE)), blockStateUp, flowsUp);
            }
            //渲染顶面（与流动方向相反的面）
            if (shouldRenderTop) {
                cornerHeightNW -= 0.001F;
                cornerHeightSW -= 0.001F;
                cornerHeightSE -= 0.001F;
                cornerHeightNE -= 0.001F;
                //流动性
                Vec3d flowVec = fluidState.getVelocity(world, pos);
                //获取贴图帧位置
                float u1, u2, u3, u4, v1, v2, v3, v4;
                Sprite sprite;
                ModelQuadFacing facing;
                if (flowVec.x == 0.0 && flowVec.z == 0.0) {
                    //静止
                    sprite = sprites[0];
                    facing = ModelQuadFacing.POS_Y;
                    u1 = sprite.getFrameU(0.0);
                    v1 = sprite.getFrameV(0.0);
                    u2 = u1;
                    v2 = sprite.getFrameV(16.0);
                    u3 = sprite.getFrameU(16.0);
                    v3 = v2;
                    u4 = u3;
                    v4 = v1;
                } else {
                    //流动
                    sprite = sprites[1];
                    facing = ModelQuadFacing.UNASSIGNED;
                    float ah = (float) MathHelper.atan2(flowVec.z, flowVec.x) - (float) (Math.PI / 2);
                    float ai = MathHelper.sin(ah) * 0.25F;
                    float aj = MathHelper.cos(ah) * 0.25F;
                    u1 = sprite.getFrameU(8.0F + (-aj - ai) * 16.0F);
                    v1 = sprite.getFrameV(8.0F + (-aj + ai) * 16.0F);
                    u2 = sprite.getFrameU(8.0F + (-aj + ai) * 16.0F);
                    v2 = sprite.getFrameV(8.0F + (aj + ai) * 16.0F);
                    u3 = sprite.getFrameU(8.0F + (aj + ai) * 16.0F);
                    v3 = sprite.getFrameV(8.0F + (aj - ai) * 16.0F);
                    u4 = sprite.getFrameU(8.0F + (aj - ai) * 16.0F);
                    v4 = sprite.getFrameV(8.0F + (-aj - ai) * 16.0F);
                }
                //计算材质顶点位置
                final float averageU = (u1 + u2 + u3 + u4) / 4.0F;
                final float averageV = (v1 + v2 + v3 + v4) / 4.0F;
                final float frameDelta = sprites[0].getAnimationFrameDelta();
                u1 = MathHelper.lerp(frameDelta, u1, averageU);
                u2 = MathHelper.lerp(frameDelta, u2, averageU);
                u3 = MathHelper.lerp(frameDelta, u3, averageU);
                u4 = MathHelper.lerp(frameDelta, u4, averageU);
                v1 = MathHelper.lerp(frameDelta, v1, averageV);
                v2 = MathHelper.lerp(frameDelta, v2, averageV);
                v3 = MathHelper.lerp(frameDelta, v3, averageV);
                v4 = MathHelper.lerp(frameDelta, v4, averageV);
                //渲染
                quad.setSprite(sprite);

                boolean aligned = isAlignedEquals(cornerHeightNE, cornerHeightNW) 
                        && isAlignedEquals(cornerHeightNW, cornerHeightSE) 
                        && isAlignedEquals(cornerHeightSE, cornerHeightSW) 
                        && isAlignedEquals(cornerHeightSW, cornerHeightNE);
                boolean creaseNorthEastSouthWest = aligned 
                        || cornerHeightNE > cornerHeightNW && cornerHeightNE > cornerHeightSE 
                        || cornerHeightNE < cornerHeightNW && cornerHeightNE < cornerHeightSE 
                        || cornerHeightSW > cornerHeightNW && cornerHeightSW > cornerHeightSE 
                        || cornerHeightSW < cornerHeightNW && cornerHeightSW < cornerHeightSE;
                if (flowsUp) {
                    if (creaseNorthEastSouthWest) {
                        setVertex(quad, 1, 1.0F, 1 - cornerHeightNE, 0.0F, u4, v4);
                        setVertex(quad, 2, 1.0F, 1 - cornerHeightSE, 1.0F, u3, v3);
                        setVertex(quad, 3, 0.0F, 1 - cornerHeightSW, 1.0F, u2, v2);
                        setVertex(quad, 0, 0.0F, 1 - cornerHeightNW, 0.0F, u1, v1);
                    } else {
                        setVertex(quad, 0, 1.0F, 1 - cornerHeightNE, 0.0F, u4, v4);
                        setVertex(quad, 1, 1.0F, 1 - cornerHeightSE, 1.0F, u3, v3);
                        setVertex(quad, 2, 0.0F, 1 - cornerHeightSW, 1.0F, u2, v2);
                        setVertex(quad, 3, 0.0F, 1 - cornerHeightNW, 0.0F, u1, v1);
                    }
                } else {
                    if (creaseNorthEastSouthWest) {
                        setVertex(quad, 1, 0.0F, cornerHeightNW, 0.0F, u1, v1);
                        setVertex(quad, 2, 0.0F, cornerHeightSW, 1.0F, u2, v2);
                        setVertex(quad, 3, 1.0F, cornerHeightSE, 1.0F, u3, v3);
                        setVertex(quad, 0, 1.0F, cornerHeightNE, 0.0F, u4, v4);
                    } else {
                        setVertex(quad, 0, 0.0F, cornerHeightNW, 0.0F, u1, v1);
                        setVertex(quad, 1, 0.0F, cornerHeightSW, 1.0F, u2, v2);
                        setVertex(quad, 2, 1.0F, cornerHeightSE, 1.0F, u3, v3);
                        setVertex(quad, 3, 1.0F, cornerHeightNE, 0.0F, u4, v4);
                    }
                }

                this.updateQuad(quad, world, pos, lighter, flowsUp ? Direction.DOWN : Direction.UP, 1.0F, colorProvider, fluidState);
                this.writeQuad(meshBuilder, material, offset, quad, facing, false);
                if (fluidState.canFlowTo(world, pos.offset(flowsUp ? Direction.DOWN : Direction.UP))) {
                    this.writeQuad(meshBuilder, material, offset, quad, ModelQuadFacing.NEG_Y, true);
                }
            }
            //渲染底面（与流动方向相同的面）
            if (flowsUp ? hasSideUp : hasSideDown) {
                float minU = sprites[0].getMinU();
                float maxU = sprites[0].getMaxU();
                float minV = sprites[0].getMinV();
                float maxV = sprites[0].getMaxV();

                quad.setSprite(sprites[0]);
                if (flowsUp) {
                    setVertex(quad, 0, 1.0F, 1 - yOffset, 0.0F, minU, maxV);
                    setVertex(quad, 1, 1.0F, 1 - yOffset, 1.0F, minU, minV);
                    setVertex(quad, 2, 0.0F, 1 - yOffset, 1.0F, maxU, minV);
                    setVertex(quad, 3, 0.0F, 1 - yOffset, 0.0F, maxU, maxV);
                } else {
                    setVertex(quad, 0, 0.0F, yOffset, 1.0F, minU, maxV);
                    setVertex(quad, 1, 0.0F, yOffset, 0.0F, minU, minV);
                    setVertex(quad, 2, 1.0F, yOffset, 0.0F, maxU, minV);
                    setVertex(quad, 3, 1.0F, yOffset, 1.0F, maxU, maxV);
                }
                this.updateQuad(quad, world, pos, lighter, flowsUp ? Direction.UP : Direction.DOWN, 1.0F, colorProvider, fluidState);
                this.writeQuad(meshBuilder, material, offset, quad, ModelQuadFacing.NEG_Y, false);
            }

            quad.setFlags(6);

            //渲染侧面
            for (Direction direction : Direction.Type.HORIZONTAL) {
                float height1;
                float height2;
                float x1, x2, z1, z2;
                boolean shouldSideRender;
                switch (direction) {
                    case NORTH:
                        height1 = cornerHeightNW;
                        height2 = cornerHeightNE;
                        x1 = 0.0F;
                        x2 = 0.0F + 1.0F;
                        z1 = 0.0F + 0.001F;
                        z2 = 0.0F + 0.001F;
                        shouldSideRender = hasSideN;
                        break;
                    case SOUTH:
                        height1 = cornerHeightSE;
                        height2 = cornerHeightSW;
                        x1 = 0.0F + 1.0F;
                        x2 = 0.0F;
                        z1 = 0.0F + 1.0F - 0.001F;
                        z2 = 0.0F + 1.0F - 0.001F;
                        shouldSideRender = hasSideS;
                        break;
                    case WEST:
                        height1 = cornerHeightSW;
                        height2 = cornerHeightNW;
                        x1 = 0.0F + 0.001F;
                        x2 = 0.0F + 0.001F;
                        z1 = 0.0F + 1.0F;
                        z2 = 0.0F;
                        shouldSideRender = hasSideW;
                        break;
                    default:
                        height1 = cornerHeightNE;
                        height2 = cornerHeightSE;
                        x1 = 0.0F + 1.0F - 0.001F;
                        x2 = 0.0F + 1.0F - 0.001F;
                        z1 = 0.0F;
                        z2 = 0.0F + 1.0F;
                        shouldSideRender = hasSideE;
                }

                if (shouldSideRender && !isSideCovered(world, pos, direction, Math.max(height1, height2), world.getBlockState(pos.offset(direction)), flowsUp)) {
                    BlockPos blockPos = pos.offset(direction);
                    Sprite sprite = sprites[1];
                    boolean isOverlay = false;
                    if (sprites.length > 2) {
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(block)) {
                            sprite = sprites[2];
                            isOverlay = true;
                        }
                    }

                    float u1 = sprite.getFrameU(0.0);
                    float u2 = sprite.getFrameU(8.0);
                    float v1 = sprite.getFrameV((1.0F - height1) * 16.0F * 0.5F);
                    float v2 = sprite.getFrameV((1.0F - height2) * 16.0F * 0.5F);
                    float v3 = sprite.getFrameV(8.0);

                    quad.setSprite(sprite);

                    if (flowsUp) {
                        setVertex(quad, 0, x2, Math.abs(1 - height2), z2, u2, v2);
                        setVertex(quad, 1, x2, Math.abs(1 - yOffset), z2, u2, v3);
                        setVertex(quad, 2, x1, Math.abs(1 - yOffset), z1, u1, v3);
                        setVertex(quad, 3, x1, Math.abs(1 - height1), z1, u1, v1);
//                        setVertex(quad, 0, x2, 1 - yOffset, z2, u2, v3);
//                        setVertex(quad, 1, x1, 1 - yOffset, z1, u1, v3);
//                        setVertex(quad, 2, x1, 1 - height1, z1, u1, v1);
//                        setVertex(quad, 3, x2, 1 - height2, z2, u2, v2);
                    } else {
                        setVertex(quad, 0, x2, height2, z2, u2, v2);
                        setVertex(quad, 1, x2, yOffset, z2, u2, v3);
                        setVertex(quad, 2, x1, yOffset, z1, u1, v3);
                        setVertex(quad, 3, x1, height1, z1, u1, v1);
                    }

                    float br = direction.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;

                    final Direction cullingDir = flowsUp ? direction.getOpposite() : direction;
                    final ModelQuadFacing facing = ModelQuadFacing.fromDirection(cullingDir);

                    this.updateQuad(quad, world, pos, lighter, cullingDir, br, colorProvider, fluidState);
                    this.writeQuad(meshBuilder, material, offset, quad, facing, false);
                    if (!isOverlay) {
                        this.writeQuad(meshBuilder, material, offset, quad, facing.getOpposite(), true);
                    }
                }
            }
        }
        
        ci.cancel();
    }

    @Unique
    private static boolean isSameFluid(FluidState a, FluidState b) {
        return b.getFluid().matchesType(a.getFluid());
    }

    @Unique
    private static boolean isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state, boolean flowsUp) {
        if (state.isOpaque()) {
            VoxelShape voxelShape = VoxelShapes.cuboid(0.0, flowsUp ? (1.0 - height) : 0.0, 0.0, 1.0, flowsUp ? 1.0 : height, 1.0);
            VoxelShape voxelShape2 = state.getCullingShape(world, pos);
            return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
        } else {
            return false;
        }
    }

    @Unique
    private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state, boolean flowsUp) {
        return isSideCovered(world, direction, maxDeviation, pos.offset(direction), state, flowsUp);
    }

    @Unique
    private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction, boolean flowsUp) {
        return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state, flowsUp);
    }

    @Unique
    private static boolean shouldRenderSide(
            BlockRenderView world, BlockPos pos, FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState, boolean flowsUp
    ) {
        return !isOppositeSideCovered(world, pos, blockState, direction, flowsUp) && !isSameFluid(fluidState, neighborFluidState);
    }

    @Unique
    private static float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos sidePos, BlockState sideState, FluidState sideFluidState, boolean flowsUp) {
        if (fluid.matchesType(sideFluidState.getFluid())) {
            BlockState blockState2 = world.getBlockState(flowsUp ? sidePos.down() : sidePos.up());
            return fluid.matchesType(blockState2.getFluidState().getFluid()) ? 1.0F : sideFluidState.getHeight();
        } else {
            return !sideState.isSolid() ? 0.0F : -1.0F;
        }
    }

    @Unique
    private static float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos, boolean flowsUp) {
        BlockState blockState = world.getBlockState(pos);
        return getFluidHeight(world, fluid, pos, blockState, blockState.getFluidState(), flowsUp);
    }

    @Unique
    private static float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos, boolean flowsUp) {
        if (!(eastWestHeight >= 1.0F) && !(northSouthHeight >= 1.0F)) {
            float[] fs = new float[2];
            if (eastWestHeight > 0.0F || northSouthHeight > 0.0F) {
                float f = getFluidHeight(world, fluid, pos, flowsUp);
                if (f >= 1.0F) {
                    return 1.0F;
                }

                addHeight(fs, f);
            }

            addHeight(fs, originHeight);
            addHeight(fs, eastWestHeight);
            addHeight(fs, northSouthHeight);
            return fs[0] / fs[1];
        } else {
            return 1.0F;
        }
    }

    @Unique
    private static void addHeight(float[] weightedAverageHeight, float height) {
        if (height >= 0.8F) {
            weightedAverageHeight[0] += height * 10.0F;
            weightedAverageHeight[1] += 10.0F;
        } else if (height >= 0.0F) {
            weightedAverageHeight[0] += height;
            weightedAverageHeight[1]++;
        }
    }
}
