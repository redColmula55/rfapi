package rc55.mc.rfapi.compat.rei.display;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.annotations.FieldsAreNonnullByDefault;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.nbt.NbtCompound;
import rc55.mc.rfapi.compat.rei.ReiUtils;
import rc55.mc.rfapi.compat.rei.category.SimpleFlowableReactionCategory;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;
import rc55.mc.rfapi.fluid.reaction.SimpleFlowableFluidReaction;

import java.util.List;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFlowableReactionDisplay extends BasicDisplay implements IFluidReactionDisplay {

    private final EntryIngredient source, surroundingIngredient, verticalIngredient, stillResult, flowingResult;
    private final float chance;

    public SimpleFlowableReactionDisplay(EntryIngredient source, EntryIngredient surroundingIngredient, EntryIngredient verticalIngredient, EntryIngredient stillResult, EntryIngredient flowingResult, float chance) {
        super(ImmutableList.of(source, surroundingIngredient, verticalIngredient), ImmutableList.of(stillResult, flowingResult));
        this.source = source;
        this.surroundingIngredient = surroundingIngredient;
        this.verticalIngredient = verticalIngredient;
        this.stillResult = stillResult;
        this.flowingResult  = flowingResult;
        this.chance = chance;
    }

    public static SimpleFlowableReactionDisplay of(List<EntryIngredient> input, List<EntryIngredient> output, NbtCompound nbt) {
        return new SimpleFlowableReactionDisplay(
                input.get(0),
                input.get(1),
                input.get(2),
                output.get(0),
                output.get(1),
                IFluidReactionDisplay.getChance(nbt)
        );
    }

    public static SimpleFlowableReactionDisplay fromReaction(SimpleFlowableFluidReaction reaction) {
        return new SimpleFlowableReactionDisplay(
                ReiUtils.getEntryIngredientInput(reaction.getSource()),
                ReiUtils.getEntryIngredient(reaction.getSurroundingIngredient()),
                ReiUtils.getEntryIngredient(reaction.getVerticalIngredient()),
                EntryIngredient.of(ReiUtils.entryStack(reaction.getStillResult(), ReiUtils.STILL_RESULT_TEXT)),
                EntryIngredient.of(ReiUtils.entryStack(reaction.getFlowingResult(), ReiUtils.FLOWING_RESULT_TEXT)),
                reaction.getChance()
        );
    }

    public static boolean validate(SimpleFlowableFluidReaction reaction) {
        // Validate reaction type to avoid adding subclasses
        return reaction.getType() == FluidReactionType.FLOWABLE;
    }

    @Override
    public FluidReactionType<?> getType() {
        return FluidReactionType.FLOWABLE;
    }

    @Override
    public EntryIngredient getSource() {
        return source;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Deprecated
    @Override
    public EntryIngredient getResult() {
        return stillResult;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SimpleFlowableReactionCategory.ID;
    }

    public EntryIngredient getSurroundingIngredient() {
        return surroundingIngredient;
    }
    public EntryIngredient getVerticalIngredient() {
        return verticalIngredient;
    }

    public EntryIngredient getStillResult() {
        return stillResult;
    }
    public EntryIngredient getFlowingResult() {
        return flowingResult;
    }
}
