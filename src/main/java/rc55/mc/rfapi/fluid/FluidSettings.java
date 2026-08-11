package rc55.mc.rfapi.fluid;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.RFApiMain;

import java.util.Optional;
import java.util.function.*;

/**
 * Determines properties for a certain fluid
 * Note that the flowing and the still factor shares the same FluidSettings
 * Use {@link Builder} to create an instance
 */
@SuppressWarnings({"UnstableApiUsage", "OptionalUsedAsFieldOrParameterType"})
public class FluidSettings implements FluidVariantAttributeHandler {
    private final Supplier<Block> fluidBlockSupplier;
    private final Supplier<@Nullable Item> bucketItemSupplier;
    private final ToIntFunction<WorldView> flowSpeedGetter, tickRateGetter, levelDecrPerBlockGetter;
    private final Optional<SoundEvent> bucketFillSound, bucketEmptySound;
    private final @Nullable ParticleEffect drippingParticle;
    private final boolean setsFire, randomTick, flowsUp, retainsAir;
    private final ToIntFunction<BlockState> luminanceGetter;
    private final ColorSettings color;
    private final int temperature;
    private final Predicate<World> infinite;
    private final EntityMovementType movementType;

    private FluidSettings(
            Supplier<Block> fluidBlockSupplier,
            Supplier<@Nullable Item> bucketItemSupplier,
            ToIntFunction<WorldView> flowSpeedGetter,
            ToIntFunction<WorldView> tickRateGetter,
            ToIntFunction<WorldView> levelDecrPerBlockGetter,
            Optional<SoundEvent> bucketEmptySound,
            Optional<SoundEvent> bucketFillSound,
            @Nullable ParticleEffect drippingParticle,
            boolean setsFire,
            boolean randomTick,
            boolean flowsUp,
            ToIntFunction<BlockState> luminanceGetter,
            ColorSettings color,
            Predicate<World> infinite,
            int temperature,
            boolean retainsAir,
            EntityMovementType movementType
    ) {
        this.fluidBlockSupplier = fluidBlockSupplier;
        this.bucketItemSupplier = bucketItemSupplier;
        this.flowSpeedGetter = flowSpeedGetter;
        this.tickRateGetter = tickRateGetter;
        this.levelDecrPerBlockGetter = levelDecrPerBlockGetter;
        this.bucketEmptySound = bucketEmptySound;
        this.bucketFillSound = bucketFillSound;
        this.drippingParticle = drippingParticle;
        this.setsFire = setsFire;
        this.randomTick = randomTick;
        this.flowsUp = flowsUp;
        this.luminanceGetter = luminanceGetter;
        this.color = color;
        this.infinite = infinite;
        this.temperature = temperature;
        this.retainsAir = retainsAir;
        this.movementType = movementType;
    }

    private static FluidSettings fromFabricVariant(FluidVariant fluidVariant) {
        final Fluid fluid = fluidVariant.getFluid();
        final FluidVariantAttributeHandler fabricAttribute = FluidVariantAttributes.getHandler(fluid);
        final Builder builder;
        if (fluid.isIn(FluidTags.WATER)) {
            builder = waterLike();
        } else if (fluid.isIn(FluidTags.LAVA)) {
            builder = lavaLike();
        } else {
            builder = builder();
        }
        if (fabricAttribute == null) {
            RFApiMain.LOGGER.error("Fluid {} has neither fabric/fluidlib settings(aka property) defined!", FluidRegistry.getId(fluid));
            return builder.bucket(fluid::getBucketItem)
                    .block(() -> fluid.getDefaultState().getBlockState().getBlock())
                    .build();
        }
        if (fabricAttribute.isLighterThanAir(fluidVariant)) {
            builder.flowsUp();
        }
        return builder.pourSound(fabricAttribute.getEmptySound(fluidVariant))
                .pickUpSound(fabricAttribute.getEmptySound(fluidVariant))
                .luminance(fabricAttribute.getLuminance(fluidVariant))
                .temperature(fabricAttribute.getTemperature(fluidVariant))
                .flowSpeed(fabricAttribute.getViscosity(fluidVariant, null) / FluidConstants.VISCOSITY_RATIO)
                .bucket(fluid::getBucketItem)
                .block(() -> fluid.getDefaultState().getBlockState().getBlock())
                .build();
    }

    public static final int WATER_TEMPERATURE = FluidConstants.WATER_TEMPERATURE, LAVA_TEMPERATURE = FluidConstants.LAVA_TEMPERATURE;

    public static final Direction[] FLOW_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN};
    public static final Direction[] AIR_FLOW_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};

    protected static final ApiProviderMap<Fluid, FluidSettings> REGISTRY = ApiProviderMap.create();

    /**
     * Creates a builder for FluidSettings
     * @return The {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a FluidSettings builder which by default acts like vanilla water
     */
    public static Builder waterLike() {
        return builder().temperature(WATER_TEMPERATURE)
                .particle(ParticleTypes.DRIPPING_WATER)
                .movementType(EntityMovementType.WATER)
                .color(ColorSettings.builder()
                        .defaultColor(0x3F76E4)
                        .renderColor((world, pos) -> world.getColor(pos, (biome, x, z) -> biome.getWaterColor()))
                        // Currently no biome based fog color for water, so we use default color here
                        .fog(ColorSettings.FogType.WATER, 0x050533)
                )
                .infiniteWhen(world -> world.getGameRules().getBoolean(GameRules.WATER_SOURCE_CONVERSION));
    }

    /**
     * Creates a FluidSettings builder which by default acts like vanilla lava
     */
    public static Builder lavaLike() {
        return builder().ticksRandomly()
                .setsFire()
                // I know its funny, but players can regain air in lava(yes its a vanilla feature)
                .canBreath()
                .temperature(LAVA_TEMPERATURE)
                .movementType(EntityMovementType.LAVA)
                .luminance(15)
                .color(ColorSettings.builder().fixedColor(0xFFFFFF).mapColor(MapColor.BRIGHT_RED).fog(ColorSettings.FogType.LAVA, 10033408))
                .pickUpSound(SoundEvents.ITEM_BUCKET_FILL_LAVA)
                .pourSound(SoundEvents.ITEM_BUCKET_EMPTY_LAVA)
                .particle(ParticleTypes.DRIPPING_LAVA)
                .flowSpeed(world -> world.getDimension().ultrawarm() ? 4 : 2)
                .levelDecreasePerBlock(world -> world.getDimension().ultrawarm() ? 1 : 2)
                .tickRate(world -> world.getDimension().ultrawarm() ? 10 : 30)
                .infiniteWhen(world -> world.getGameRules().getBoolean(GameRules.LAVA_SOURCE_CONVERSION));
    }

    public static void register(Fluid fluid, FluidSettings settings) {
        fluid = FluidHelper.trim(fluid);
        if (REGISTRY.putIfAbsent(fluid, settings) != null) {
            throw new IllegalStateException("Duplicate settings registration for fluid " + FluidRegistry.getId(fluid));
        }
        FluidVariantAttributes.register(fluid, settings);
    }

    public static void register(FluidReference<?> fluid) {
        register(fluid.getStill(), fluid.getSettings());
    }

    static {
        REGISTRY.putIfAbsent(Fluids.EMPTY, FluidSettings.builder().canBreath().color(ColorSettings.ofFixedColor(0xFFFFFF, MapColor.WHITE, ColorSettings.FogType.NONE)).build());
        REGISTRY.putIfAbsent(Fluids.WATER, FluidSettings.waterLike().bucket(() -> Items.WATER_BUCKET).block(() -> Blocks.WATER).build());
        REGISTRY.putIfAbsent(Fluids.LAVA, FluidSettings.lavaLike().bucket(() -> Items.LAVA_BUCKET).block(() -> Blocks.LAVA).build());
    }

    public static FluidSettings get(Fluid fluid) {
        fluid = FluidHelper.trim(fluid);
        FluidSettings settings = REGISTRY.get(fluid);
        if (settings == null) {
            // Try to create settings from FabricAPI if no settings is registered
            settings = fromFabricVariant(FluidVariant.of(fluid));
            //register(fluid, settings);
            REGISTRY.putIfAbsent(fluid, settings);
        }
        return settings;
    }

    public static FluidSettings get(FluidReference<?> fluid) {
        return REGISTRY.get(fluid.getStill());
    }

    public static FluidSettings get(FluidState state) {
        return get(state.getFluid());
    }

    /// @deprecated Use {@link FluidReference#getBlock()}
    @Deprecated
    public Block getFluidBlock() {
        return fluidBlockSupplier.get();
    }

    public Item getBucketItem() {
        return Optional.ofNullable(this.bucketItemSupplier).map(Supplier::get).orElse(Items.AIR);
    }
    public int getFlowSpeed(WorldView world) {
        return this.flowSpeedGetter.applyAsInt(world);
    }
    public int getTickRate(WorldView world) {
        return this.tickRateGetter.applyAsInt(world);
    }
    public int getLevelDecreasePerBlock(WorldView world) {
        return this.levelDecrPerBlockGetter.applyAsInt(world);
    }
    public Optional<SoundEvent> getBucketFillSound() {
        return bucketFillSound;
    }

    public Optional<SoundEvent> getBucketEmptySound() {
        return bucketEmptySound;
    }

    @Nullable
    public ParticleEffect getDrippingParticle() {
        return drippingParticle;
    }
    public boolean canSetFire() {
        return this.setsFire;
    }
    public boolean hasRandomTick() {
        return this.randomTick;
    }
    public int getLight(BlockState state) {
        return this.luminanceGetter == null ? 0 : this.luminanceGetter.applyAsInt(state);
    }
    public ColorSettings getColor() {
        return this.color;
    }
    public MapColor getMapColor() {
        return this.color.mapColor();
    }
    public int getColor(@Nullable BlockRenderView world, @Nullable BlockPos pos) {
        return this.color.getColorInWorld(world, pos);
    }
    public int getDefaultColor() {
        return this.color.defaultColor;
    }

    public boolean isInfinite(World world) {
        return this.infinite.test(world);
    }
    public int getTemperature() {
        return temperature;
    }

    public boolean flowsUp() {
        return flowsUp;
    }

    public boolean canEntityBreath() {
        return this.retainsAir;
    }

    public boolean canSwim() {
        return this.getMovementType() == EntityMovementType.WATER;
    }

    public EntityMovementType getMovementType() {
        return this.movementType;
    }

    public Direction[] getFlowDirections() {
        return this.flowsUp ? AIR_FLOW_DIRECTIONS : FLOW_DIRECTIONS;
    }

    @Override
    public Text getName(FluidVariant fluidVariant) {
        return FluidVariantAttributeHandler.super.getName(fluidVariant);
    }

    @Override
    public Optional<SoundEvent> getFillSound(FluidVariant variant) {
        return this.getBucketFillSound();
    }

    @Override
    public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
        return this.getBucketEmptySound();
    }

    @Override
    public int getLuminance(FluidVariant variant) {
        return this.getLight(variant.getFluid().getDefaultState().getBlockState());
    }

    @Override
    public int getTemperature(FluidVariant variant) {
        return this.getTemperature();
    }

    @Override
    public int getViscosity(FluidVariant variant, @Nullable World world) {
        if (world == null) {
            return FluidVariantAttributeHandler.super.getViscosity(variant, world);
        } else {
            return FluidConstants.VISCOSITY_RATIO * this.getFlowSpeed(world);
        }
    }

    @Override
    public boolean isLighterThanAir(FluidVariant variant) {
        return this.flowsUp();
    }

    /**
     * Builder for FluidSettings
     * @see FluidSettings#builder()
     */
    public static class Builder {
        private Supplier<Block> fluidBlockSupplier;
        private Supplier<@Nullable Item> bucketItemSupplier = () -> null;
        private ToIntFunction<WorldView> flowSpeedGetter, tickRateGetter, levelDecrPerBlockGetter;
        private Optional<SoundEvent> bucketFillSound, bucketEmptySound;
        private @Nullable ParticleEffect drippingParticle;
        private boolean setsFire, randomTick, flowsUp, entityBreath;
        private ToIntFunction<BlockState> luminanceGetter;
        private ColorSettings color = ColorSettings.ofFixedColor(0x3F76E4, MapColor.WATER_BLUE, ColorSettings.FogType.WATER);
        private Predicate<World> infinite = w -> false;
        private int temperature = 300;
        private EntityMovementType movementType = EntityMovementType.AIR;

        protected Builder() {
            this.flowSpeed(4).tickRate(5).levelDecreasePerBlock(1).luminance(0);
            this.pickUpSound(SoundEvents.ITEM_BUCKET_FILL).pourSound(SoundEvents.ITEM_BUCKET_EMPTY);
        }

        /**
         * Creates the FluidSettings
         */
        public FluidSettings build() {
            return new FluidSettings(
                    fluidBlockSupplier,
                    bucketItemSupplier,
                    flowSpeedGetter,
                    tickRateGetter,
                    levelDecrPerBlockGetter,
                    bucketEmptySound,
                    bucketFillSound,
                    drippingParticle,
                    setsFire,
                    randomTick,
                    flowsUp,
                    luminanceGetter,
                    color,
                    infinite,
                    temperature,
                    entityBreath,
                    movementType
            );
        }

        /**
         * Sets the block factor for this fluid
         * Do nat call this directly, as this is automatically applied
         * for fluids
         * @deprecated {@link FluidReference#getBlock()}
         */
        @ApiStatus.Internal
        @Deprecated
        public Builder block(Supplier<@NotNull Block> fluidBlockSupplier) {
            this.fluidBlockSupplier = Suppliers.memoize(fluidBlockSupplier::get);
            return this;
        }

        /**
         * Sets the default bucket item for this fluid
         * null if there is no bucket item
         */
        public Builder bucket(@Nullable Supplier<@NotNull Item> itemSupplier) {
            if (itemSupplier != null) {
                this.bucketItemSupplier = Suppliers.memoize(itemSupplier::get);
            } else {
                this.bucketItemSupplier = () -> Items.AIR;
            }
            return this;
        }

        public Builder flowSpeed(ToIntFunction<WorldView> flowSpeedGetter) {
            this.flowSpeedGetter = flowSpeedGetter;
            return this;
        }

        public Builder flowSpeed(int i) {
            return this.flowSpeed(w -> i);
        }

        public Builder tickRate(ToIntFunction<WorldView> tickRateGetter) {
            this.tickRateGetter = tickRateGetter;
            return this;
        }

        public Builder tickRate(int i) {
            return this.tickRate(w -> i);
        }

        public Builder levelDecreasePerBlock(ToIntFunction<WorldView> levelDecrPerBlockGetter) {
            this.levelDecrPerBlockGetter = levelDecrPerBlockGetter;
            return this;
        }

        public Builder levelDecreasePerBlock(int i) {
            return this.levelDecreasePerBlock(w -> i);
        }

        /**
         * Sets the sound when the fluid was picked up by players through buckets
         */
        public Builder pickUpSound(Optional<@Nullable SoundEvent> bucketFillSound) {
            this.bucketFillSound = bucketFillSound;
            return this;
        }

        /// @see #pickUpSound(Optional)
        public Builder pickUpSound(SoundEvent fillSound) {
            return this.pickUpSound(Optional.of(fillSound));
        }

        /**
         * Sets the sound when the fluid was placed through buckets
         */
        public Builder pourSound(Optional<@Nullable SoundEvent> bucketEmptySound) {
            this.bucketEmptySound = bucketEmptySound;
            return this;
        }

        /// @see #pourSound(Optional)
        public Builder pourSound(SoundEvent bucketEmptySound) {
            return this.pourSound(Optional.of(bucketEmptySound));
        }

        /**
         * Sets the dripping particle effect when there is <=1 block below the fluid
         */
        public Builder particle(@Nullable ParticleEffect particle) {
            this.drippingParticle = particle;
            return this;
        }

        /**
         * Determines if the fluid can set blocks on fire like vanilla lava
         * Note that this will also make the fluid to have random ticks
         */
        public Builder setsFire(boolean bl) {
            this.setsFire = bl;
            if (bl) {
                return this.ticksRandomly(bl);
            }
            return this;
        }

        public Builder setsFire() {
            return this.setsFire(true);
        }

        public Builder ticksRandomly(boolean bl) {
            this.randomTick = bl;
            return this;
        }

        public Builder ticksRandomly() {
            return this.ticksRandomly(true);
        }

        public Builder luminance(ToIntFunction<BlockState> luminance) {
            this.luminanceGetter = luminance;
            return this;
        }

        public Builder luminance(int i) {
            return this.luminance(s -> i);
        }

        public Builder color(ColorSettings color) {
            this.color = color;
            return this;
        }

        public Builder color(ColorSettings.Builder color) {
            return this.color(color.build());
        }

        /**
         * Sets the color of the fluid
         * @param defaultColor The default color value
         * @param color Color in different place in world(like vanilla water)
         */
        public Builder color(int defaultColor, ToIntBiFunction<BlockRenderView, BlockPos> color, MapColor mapColor) {
            return this.color(ColorSettings.builder().defaultColor(defaultColor).renderColor(color).mapColor(mapColor));
        }

        /**
         * Sets the fluid always has the same color
         * @see ColorSettings#ofFixedColor(int, MapColor, ColorSettings.FogType)
         */
        public Builder color(int color, MapColor mapColor) {
            return this.color(ColorSettings.ofFixedColor(color, mapColor, ColorSettings.FogType.NONE));
        }

        /**
         * Marks when the fluid should be infinite(creates new source while 2 sources matches)
         */
        public Builder infiniteWhen(Predicate<World> infinite) {
            this.infinite = infinite;
            return this;
        }

        /// @see #infiniteWhen(Predicate)
        public Builder isInfinite(boolean bl) {
            this.infinite = w -> bl;
            return this;
        }

        /**
         * Sets temperature for this fluid
         * @param i Temperature, in Kelvin
         * @throws IllegalArgumentException When temperature is negative
         */
        public Builder temperature(int i) {
            if (i < 0) {
                throw new IllegalArgumentException("This should be in Kelvin, which shall never be negative!");
            }
            this.temperature = i;
            return this;
        }

        /**
         * Mark this fluid lighter than air(which typically flows upward, otherwise flows downward)
         */
        public Builder flowsUp() {
            this.flowsUp = true;
            return this;
        }

        /**
         * Determines if entities can regenerate air in this fluid
         * Note that aquatic mobs will invert this value(drown in "breathable" fluids)
         * @param bl Whether entities can breathe in the fluid
         */
        public Builder canBreath(boolean bl) {
            this.entityBreath = bl;
            return this;
        }

        /**
         * Make normal entities breathable in the fluid
         * @see #canBreath(boolean)
         */
        public Builder canBreath() {
            return this.canBreath(true);
        }

        /**
         * Sets how the fluid affects entity moves
         */
        public Builder movementType(EntityMovementType type) {
            this.movementType = type;
            return this;
        }
    }

    /**
     * How entities move when in this fluid
     * @see rc55.mc.rfapi.mixin.EntityMixin
     */
    public enum EntityMovementType {
        /**
         * Same action as in the air(default action while in neither tags)
         */
        AIR,
        /**
         * Same action as in water(action while in tag #minecraft:water)
         */
        WATER,
        /**
         * Same actions as in lava(action while in tag #minecraft:lava)
         */
        LAVA,
        /**
         * Will push entities horizontally, but has no buoyancy(which means entities will take fall damage in it)
         */
        HORIZONTAL,
    }

    /**
     * Color settings for the fluid
     * All colors are in RGB format!
     * @param defaultColor Default color when world or pos is null
     * @param renderColor Fluid color in different place in the world
     * @param itemColor Color for item layer 1, set to -1 to disable
     * @param mapColor Color for fluid blocks on map
     * @param fogType How the fog will be rendered when the camera is
     *                submerged in the fluid, see {@link FogType}
     * @param fogColor Color for the fog
     */
    public record ColorSettings(
            int defaultColor,
            ToIntBiFunction<BlockRenderView, BlockPos> renderColor,
            int itemColor,
            MapColor mapColor,
            FogType fogType,
            int fogColor
    ) implements ToIntBiFunction<@Nullable BlockRenderView, @Nullable BlockPos> {
        /**
         * @apiNote Do <strong>**NOT**</strong> call this, call the {@linkplain #getColorInWorld(BlockRenderView, BlockPos) null safe version} instead!
         */
        @Deprecated
        public ToIntBiFunction<BlockRenderView, BlockPos> renderColor() {
            throw new AssertionError("You should call #getColorInWorld(BlockRenderView, BlockPos) instead of this!");
        }

        @Override
        public int applyAsInt(@Nullable BlockRenderView world, @Nullable BlockPos pos) {
            if (world == null || pos == null) {
                return this.defaultColor();
            } else {
                return this.renderColor.applyAsInt(world, pos);
            }
        }

        public int getColorInWorld(@Nullable BlockRenderView world, @Nullable BlockPos pos) {
            return this.applyAsInt(world, pos);
        }

        /**
         * If the custom fog render should be used
         * Applied via {@linkplain rc55.mc.rfapi.mixin.client.BackgroundRendererMixin mixins}
         */
        public boolean shouldRenderFog() {
            return this.fogType != FogType.NONE && this.fogColor != -1;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static ColorSettings ofFixedColor(int color, MapColor mapColor, FogType fogType) {
            int fixed = color & 0xFFFFFF;
            return new ColorSettings(
                    fixed,
                    (w, p) -> fixed,
                    fixed,
                    mapColor,
                    fogType,
                    fogType == FogType.NONE ? -1 : fixed
            );
        }
        
        public static int toRgb(int r, int g, int b) {
            return (((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF)) & 0xFFFFFF;
        }
        
        public static int toRgb(float r, float g, float b) {
            return toRgb((int) (r * 255f), (int) (g * 255f), (int) (b * 255f));
        }

        public static class Builder {
            private int defaultColor = 0xFFFFFF;
            private int itemColor = -1;
            private FogType fogType = FogType.NONE;
            private int fogColor = -1;
            private MapColor mapColor = MapColor.WATER_BLUE;
            private ToIntBiFunction<BlockRenderView, BlockPos> renderColor = (world, pos) -> 0xFFFFFF;

            /**
             * Set default, render, fog color for the fluid
             */
            public Builder fixedColor(int color) {
                this.fogColor = color & 0xFFFFFF;
                return this.defaultColor(fogColor).renderColor((w, p) -> fogColor).itemColor(fogColor);
            }

            /**
             * Sets the default color for the fluid
             * Will fallback if the color getter called with null values
             */
            public Builder defaultColor(int color) {
                this.defaultColor = color & 0xFFFFFF;
                return this;
            }

            /**
             * Sets the color of the fluid
             * @param color Color in different place in world(like vanilla water)
             */
            public Builder renderColor(ToIntBiFunction<@NotNull BlockRenderView, @NotNull BlockPos> color) {
                this.renderColor = color;
                return this;
            }

            /**
             * Sets color for the bucket items, will be applied to layer with tintIndex=1
             * @param color Item render color, set to -1 to disable item color
             */
            public Builder itemColor(int color) {
                this.itemColor = color == -1 ? -1 : (color & 0xFFFFFF);
                return this;
            }

            public Builder mapColor(MapColor color) {
                this.mapColor = color;
                return this;
            }

            /**
             * Sets the underwater fog
             * Note that currently only fixed color are supported
             * If you want your fluid to act like vanilla water, add it to {@code #minecraft:water} tag instead
             * @param type How the fog appears
             * @param color The fog color
             */
            public Builder fog(FogType type, int color) {
                this.fogType = type;
                this.fogColor = color & 0xFFFFFF;
                return this;
            }

            public ColorSettings build() {
                return new ColorSettings(
                        defaultColor,
                        renderColor,
                        itemColor,
                        mapColor,
                        fogType,
                        fogColor
                );
            }
        }

        /**
         * How the fog will be rendered when the camera is submerged in the fluid
         */
        public enum FogType {
            /**
             * No fog, set to this if you want to disable underwater fog
             */
            NONE,
            /**
             * Fog visibility like vanilla water
             */
            WATER,
            /**
             * Fog visibility like vanilla lava
             */
            LAVA,
            /**
             * Fog visibility like submerging in vanilla powder snow
             */
            SNOW
        }
    }
}
