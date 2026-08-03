package rc55.mc.fluidlib.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Renderer for FluidLib fluids
 * Modified from Forge fluid renderer
 * @see FluidRenderRegistry
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Environment(EnvType.CLIENT)
public class ExtendedFluidRenderHandler implements FluidRenderHandler {
    protected final boolean flowsUp;

    protected final Identifier stillTexture;
    protected final Identifier flowingTexture;
    protected final Identifier overlayTexture;

    protected final Sprite[] sprites;

    /**
     * Creates a fluid renderer with an overlay texture
     * @param flowsUp If the fluid flows upwards
     * @param stillTexture Texture for still fluid
     * @param flowingTexture Texture for flowing/falling fluid
     * @param overlayTexture Texture behind {@linkplain FluidRenderHandlerRegistry#setBlockTransparency registered transparent blocks}(e.g. glass, leaves)
     */
    public ExtendedFluidRenderHandler(boolean flowsUp, Identifier stillTexture, Identifier flowingTexture, @Nullable Identifier overlayTexture) {
        this.stillTexture = Objects.requireNonNull(stillTexture, "Still texture may not be null");
        this.flowingTexture = Objects.requireNonNull(flowingTexture, "Flowing texture may not be null");
        this.overlayTexture = overlayTexture;
        this.sprites = new Sprite[overlayTexture == null ? 2 : 3];
        this.flowsUp = flowsUp;
    }

    /**
     * Creates a fluid renderer without overlay textures
     * @param flowsUp If the fluid flows upwards
     * @param stillTexture Texture for still fluid
     * @param flowingTexture Texture for flowing/falling fluid
     */
    public ExtendedFluidRenderHandler(boolean flowsUp, Identifier stillTexture, Identifier flowingTexture) {
        this(flowsUp, stillTexture, flowingTexture, null);
    }

    /**
     * Creates a fluid renderer with water texture
     * @param flowsUp If the fluid flows upwards
     */
    public static ExtendedFluidRenderHandler coloredWater(boolean flowsUp) {
        return new ExtendedFluidRenderHandler(flowsUp, SimpleFluidRenderHandler.WATER_STILL, SimpleFluidRenderHandler.WATER_FLOWING, SimpleFluidRenderHandler.WATER_OVERLAY);
    }

    @Override
    public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
        return this.sprites;
    }

    @Override
    public void reloadTextures(SpriteAtlasTexture textureAtlas) {
        this.sprites[0] = textureAtlas.getSprite(this.stillTexture);
        this.sprites[1] = textureAtlas.getSprite(this.flowingTexture);

        if (this.overlayTexture != null) {
            this.sprites[2] = textureAtlas.getSprite(this.overlayTexture);
        }
    }

    @Override
    public int getFluidColor(@Nullable BlockRenderView world, @Nullable BlockPos pos, FluidState state) {
        return state.getFluid().getSettings().getColor(world, pos);
    }

    @Override
    public void renderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        this.render(world, pos, vertexConsumer, blockState, fluidState);
    }

    public void render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        //颜色
        final int color = this.getFluidColor(world, pos, fluidState);
        final float colorR = (color >> 16 & 0xFF) / 255.0f;
        final float colorG = (color >> 8 & 0xFF) / 255.0f;
        final float colorB = (color & 0xFF) / 255.0f;
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
            hasSideUp = shouldRenderSide(world, pos, fluidState, blockState, Direction.UP, fluidStateUp)
                    && !isSideCovered(world, pos, Direction.UP, 0.8888889f, blockStateUp);
        } else {
            hasSideDown = shouldRenderSide(world, pos, fluidState, blockState, Direction.DOWN, fluidStateDown)
                    && !isSideCovered(world, pos, Direction.DOWN, 0.8888889f, blockStateDown);
            hasSideUp = !isSameFluid(fluidState, fluidStateUp);
        }
        boolean hasSideN = shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, fluidStateNorth);
        boolean hasSideS = shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, fluidStateSouth);
        boolean hasSideW = shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, fluidStateWest);
        boolean hasSideE = shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, fluidStateEast);
        //渲染
        if (hasSideDown || hasSideUp || hasSideE || hasSideW || hasSideN || hasSideS) {
            //阴影强度
            final float shaderDown = world.getBrightness(Direction.DOWN, true);
            final float shaderUp = world.getBrightness(Direction.UP, true);
            final float shaderN = world.getBrightness(Direction.NORTH, true);
            final float shaderW = world.getBrightness(Direction.WEST, true);
            //流体
            final Fluid fluid = fluidState.getFluid();
            //渲染高度
            final float thisHeight = this.getFluidHeight(world, fluid, pos, blockState, fluidState);
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
                float heightN = this.getFluidHeight(world, fluid, pos.north(), blockStateNorth, fluidStateNorth);
                float heightS = this.getFluidHeight(world, fluid, pos.south(), blockStateSouth, fluidStateSouth);
                float heightE = this.getFluidHeight(world, fluid, pos.east(), blockStateEast, fluidStateEast);
                float heightW = this.getFluidHeight(world, fluid, pos.west(), blockStateWest, fluidStateWest);
                cornerHeightNE = this.calculateFluidHeight(world, fluid, thisHeight, heightN, heightE, pos.offset(Direction.NORTH).offset(Direction.EAST));
                cornerHeightNW = this.calculateFluidHeight(world, fluid, thisHeight, heightN, heightW, pos.offset(Direction.NORTH).offset(Direction.WEST));
                cornerHeightSE = this.calculateFluidHeight(world, fluid, thisHeight, heightS, heightE, pos.offset(Direction.SOUTH).offset(Direction.EAST));
                cornerHeightSW = this.calculateFluidHeight(world, fluid, thisHeight, heightS, heightW, pos.offset(Direction.SOUTH).offset(Direction.WEST));
            }
            //坐标值
            final double chunkX = pos.getX() & 15;
            final double chunkY = pos.getY() & 15;
            final double chunkZ = pos.getZ() & 15;
            float yOffset = (flowsUp ? hasSideUp : hasSideDown) ? 0.001F : 0.0F;
            boolean shouldRenderTop;
            if (flowsUp) {
                shouldRenderTop = hasSideDown && !isSideCovered(world, pos, Direction.DOWN, Math.min(Math.min(cornerHeightNW, cornerHeightSW), Math.min(cornerHeightSE, cornerHeightNE)), blockStateDown);
            } else {
                shouldRenderTop = hasSideUp && !isSideCovered(world, pos, Direction.UP, Math.min(Math.min(cornerHeightNW, cornerHeightSW), Math.min(cornerHeightSE, cornerHeightNE)), blockStateUp);
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
                if (flowVec.x == 0.0 && flowVec.z == 0.0) {
                    //静止
                    Sprite sprite = this.sprites[0];
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
                    Sprite sprite = this.sprites[1];
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
                final float frameDelta = this.sprites[0].getAnimationFrameDelta();
                u1 = MathHelper.lerp(frameDelta, u1, averageU);
                u2 = MathHelper.lerp(frameDelta, u2, averageU);
                u3 = MathHelper.lerp(frameDelta, u3, averageU);
                u4 = MathHelper.lerp(frameDelta, u4, averageU);
                v1 = MathHelper.lerp(frameDelta, v1, averageV);
                v2 = MathHelper.lerp(frameDelta, v2, averageV);
                v3 = MathHelper.lerp(frameDelta, v3, averageV);
                v4 = MathHelper.lerp(frameDelta, v4, averageV);
                //颜色
                int light = this.getLight(world, pos);
                float r = shaderUp * colorR;
                float g = shaderUp * colorG;
                float b = shaderUp * colorB;
                //渲染
                if (flowsUp) {
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + 1 - cornerHeightNE, chunkZ + 0.0, r, g, b, u4, v4, light);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + 1 - cornerHeightSE, chunkZ + 1.0, r, g, b, u3, v3, light);
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + 1 - cornerHeightSW, chunkZ + 1.0, r, g, b, u2, v2, light);
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + 1 - cornerHeightNW, chunkZ + 0.0, r, g, b, u1, v1, light);
                    if (fluidState.canFlowTo(world, pos.down())) {
                        this.vertex(vertexConsumer, chunkX + 1.0, chunkY + 1 - cornerHeightNE, chunkZ + 0.0, r, g, b, u4, v4, light);
                        this.vertex(vertexConsumer, chunkX + 0.0, chunkY + 1 - cornerHeightNW, chunkZ + 0.0, r, g, b, u1, v1, light);
                        this.vertex(vertexConsumer, chunkX + 0.0, chunkY + 1 - cornerHeightSW, chunkZ + 1.0, r, g, b, u2, v2, light);
                        this.vertex(vertexConsumer, chunkX + 1.0, chunkY + 1 - cornerHeightSE, chunkZ + 1.0, r, g, b, u3, v3, light);
                    }
                } else {
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + cornerHeightNW, chunkZ + 0.0, r, g, b, u1, v1, light);
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + cornerHeightSW, chunkZ + 1.0, r, g, b, u2, v2, light);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + cornerHeightSE, chunkZ + 1.0, r, g, b, u3, v3, light);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + cornerHeightNE, chunkZ + 0.0, r, g, b, u4, v4, light);
                    if (fluidState.canFlowTo(world, pos.up())) {
                        this.vertex(vertexConsumer, chunkX + 0.0, chunkY + cornerHeightNW, chunkZ + 0.0, r, g, b, u1, v1, light);
                        this.vertex(vertexConsumer, chunkX + 1.0, chunkY + cornerHeightNE, chunkZ + 0.0, r, g, b, u4, v4, light);
                        this.vertex(vertexConsumer, chunkX + 1.0, chunkY + cornerHeightSE, chunkZ + 1.0, r, g, b, u3, v3, light);
                        this.vertex(vertexConsumer, chunkX + 0.0, chunkY + cornerHeightSW, chunkZ + 1.0, r, g, b, u2, v2, light);
                    }
                }
            }
            //渲染底面（与流动方向相同的面）
            if (this.flowsUp ? hasSideUp : hasSideDown) {
                float minU = this.sprites[0].getMinU();
                float maxU = this.sprites[0].getMaxU();
                float minV = this.sprites[0].getMinV();
                float maxV = this.sprites[0].getMaxV();
                int light = this.getLight(world, flowsUp ? pos.up() : pos.down());
                float r = shaderDown * colorR;
                float g = shaderDown * colorG;
                float b = shaderDown * colorB;
                if (flowsUp) {
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + 1 - yOffset, chunkZ + 0.0, r, g, b, minU, maxV, light);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + 1 - yOffset, chunkZ + 1.0, r, g, b, minU, minV, light);
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + 1 - yOffset, chunkZ + 1.0, r, g, b, maxU, minV, light);
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + 1 - yOffset, chunkZ + 0.0, r, g, b, maxU, maxV, light);
                } else {
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + yOffset, chunkZ + 1.0, r, g, b, minU, maxV, light);
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY + yOffset, chunkZ + 0.0, r, g, b, minU, minV, light);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + yOffset, chunkZ + 0.0, r, g, b, maxU, minV, light);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY + yOffset, chunkZ + 1.0, r, g, b, maxU, maxV, light);
                }
            }
            //渲染侧面
            final int light = this.getLight(world, pos);
            for (Direction direction : Direction.Type.HORIZONTAL) {
                float height1;
                float height2;
                double x1, x2, z1, z2;
                boolean shouldSideRender;
                switch (direction) {
                    case NORTH:
                        height1 = cornerHeightNW;
                        height2 = cornerHeightNE;
                        x1 = chunkX;
                        x2 = chunkX + 1.0;
                        z1 = chunkZ + 0.001F;
                        z2 = chunkZ + 0.001F;
                        shouldSideRender = hasSideN;
                        break;
                    case SOUTH:
                        height1 = cornerHeightSE;
                        height2 = cornerHeightSW;
                        x1 = chunkX + 1.0;
                        x2 = chunkX;
                        z1 = chunkZ + 1.0 - 0.001F;
                        z2 = chunkZ + 1.0 - 0.001F;
                        shouldSideRender = hasSideS;
                        break;
                    case WEST:
                        height1 = cornerHeightSW;
                        height2 = cornerHeightNW;
                        x1 = chunkX + 0.001F;
                        x2 = chunkX + 0.001F;
                        z1 = chunkZ + 1.0;
                        z2 = chunkZ;
                        shouldSideRender = hasSideW;
                        break;
                    default:
                        height1 = cornerHeightNE;
                        height2 = cornerHeightSE;
                        x1 = chunkX + 1.0 - 0.001F;
                        x2 = chunkX + 1.0 - 0.001F;
                        z1 = chunkZ;
                        z2 = chunkZ + 1.0;
                        shouldSideRender = hasSideE;
                }

                if (shouldSideRender && !isSideCovered(world, pos, direction, Math.max(height1, height2), world.getBlockState(pos.offset(direction)))) {
                    BlockPos blockPos = pos.offset(direction);
                    Sprite sprite = this.sprites[1];
                    if (this.overlayTexture != null) {
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(block)) {
                            sprite = this.sprites[2];
                        }
                    }

                    float u1 = sprite.getFrameU(0.0);
                    float u2 = sprite.getFrameU(8.0);
                    float v1 = sprite.getFrameV((1.0F - height1) * 16.0F * 0.5F);
                    float v2 = sprite.getFrameV((1.0F - height2) * 16.0F * 0.5F);
                    float v3 = sprite.getFrameV(8.0);
                    float shaderSide = direction.getAxis() == Direction.Axis.Z ? shaderN : shaderW;
                    float r = shaderUp * shaderSide * colorR;
                    float g = shaderUp * shaderSide * colorG;
                    float b = shaderUp * shaderSide * colorB;
                    if (flowsUp) {
                        this.vertex(vertexConsumer, x1, chunkY + 1 - yOffset, z1, r, g, b, u1, v3, light);
                        this.vertex(vertexConsumer, x2, chunkY + 1 - yOffset, z2, r, g, b, u2, v3, light);
                        this.vertex(vertexConsumer, x2, chunkY + 1 - height2, z2, r, g, b, u2, v2, light);
                        this.vertex(vertexConsumer, x1, chunkY + 1 - height1, z1, r, g, b, u1, v1, light);
                        if (sprite.getAtlasId() != this.overlayTexture) {
                            this.vertex(vertexConsumer, x1, chunkY + 1 - height1, z1, r, g, b, u1, v1, light);
                            this.vertex(vertexConsumer, x2, chunkY + 1 - height2, z2, r, g, b, u2, v2, light);
                            this.vertex(vertexConsumer, x2, chunkY + 1 - yOffset, z2, r, g, b, u2, v2, light);
                            this.vertex(vertexConsumer, x1, chunkY + 1 - yOffset, z1, r, g, b, u1, v3, light);
                        }
                    } else {
                        this.vertex(vertexConsumer, x1, chunkY + height1, z1, r, g, b, u1, v1, light);
                        this.vertex(vertexConsumer, x2, chunkY + height2, z2, r, g, b, u2, v2, light);
                        this.vertex(vertexConsumer, x2, chunkY + yOffset, z2, r, g, b, u2, v3, light);
                        this.vertex(vertexConsumer, x1, chunkY + yOffset, z1, r, g, b, u1, v3, light);
                        if (sprite.getAtlasId() != this.overlayTexture) {
                            this.vertex(vertexConsumer, x1, chunkY + yOffset, z1, r, g, b, u1, v3, light);
                            this.vertex(vertexConsumer, x2, chunkY + yOffset, z2, r, g, b, u2, v3, light);
                            this.vertex(vertexConsumer, x2, chunkY + height2, z2, r, g, b, u2, v2, light);
                            this.vertex(vertexConsumer, x1, chunkY + height1, z1, r, g, b, u1, v1, light);
                        }
                    }
                }
            }
        }
    }

    protected float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos) {
        if (!(eastWestHeight >= 1.0F) && !(northSouthHeight >= 1.0F)) {
            float[] fs = new float[2];
            if (eastWestHeight > 0.0F || northSouthHeight > 0.0F) {
                float f = this.getFluidHeight(world, fluid, pos);
                if (f >= 1.0F) {
                    return 1.0F;
                }

                this.addHeight(fs, f);
            }

            this.addHeight(fs, originHeight);
            this.addHeight(fs, eastWestHeight);
            this.addHeight(fs, northSouthHeight);
            return fs[0] / fs[1];
        } else {
            return 1.0F;
        }
    }

    protected void addHeight(float[] weightedAverageHeight, float height) {
        if (height >= 0.8F) {
            weightedAverageHeight[0] += height * 10.0F;
            weightedAverageHeight[1] += 10.0F;
        } else if (height >= 0.0F) {
            weightedAverageHeight[0] += height;
            weightedAverageHeight[1]++;
        }
    }

    protected float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return this.getFluidHeight(world, fluid, pos, blockState, blockState.getFluidState());
    }

    protected float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos sidePos, BlockState sideState, FluidState sideFluidState) {
        if (fluid.matchesType(sideFluidState.getFluid())) {
            BlockState blockState2 = world.getBlockState(this.flowsUp ? sidePos.down() : sidePos.up());
            return fluid.matchesType(blockState2.getFluidState().getFluid()) ? 1.0F : sideFluidState.getHeight();
        } else {
            return !sideState.isSolid() ? 0.0F : -1.0F;
        }
    }

    protected void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, 1.0F).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F).next();
    }

    protected int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, this.flowsUp ? pos.down() : pos.up());
        int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        return (Math.max(k, l) | Math.max(m, n)) << 16;
    }

    protected static boolean isSameFluid(FluidState a, FluidState b) {
        return b.getFluid().matchesType(a.getFluid());
    }

    protected boolean isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state) {
        if (state.isOpaque()) {
            VoxelShape voxelShape = VoxelShapes.cuboid(0.0, this.flowsUp ? (1.0 - height) : 0.0, 0.0, 1.0, this.flowsUp ? 1.0 : height, 1.0);
            VoxelShape voxelShape2 = state.getCullingShape(world, pos);
            return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
        } else {
            return false;
        }
    }

    protected boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state) {
        return isSideCovered(world, direction, maxDeviation, pos.offset(direction), state);
    }

    protected boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
    }

    protected boolean shouldRenderSide(
            BlockRenderView world, BlockPos pos, FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState
    ) {
        return !isOppositeSideCovered(world, pos, blockState, direction) && !isSameFluid(fluidState, neighborFluidState);
    }
}
