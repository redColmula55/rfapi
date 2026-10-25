package rc55.mc.rfapi.compat.rei.display;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.annotations.FieldsAreNonnullByDefault;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.NbtCompound;
import rc55.mc.rfapi.compat.rei.ReiUtils;
import rc55.mc.rfapi.compat.rei.category.SimpleFluidReactionCategory;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;
import rc55.mc.rfapi.fluid.reaction.SimpleFluidReaction;

import java.util.Collections;
import java.util.List;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluidReactionDisplay extends BasicDisplay implements IFluidReactionDisplay {

    private final EntryIngredient source, surroundingIngredient, verticalIngredient, result;
    private final float chance;

    public SimpleFluidReactionDisplay(EntryIngredient source, EntryIngredient surroundingIngredient, EntryIngredient verticalIngredient, EntryIngredient result, float chance) {
        super(ImmutableList.of(source, surroundingIngredient, verticalIngredient), Collections.singletonList(result));
        this.source = source;
        this.surroundingIngredient = surroundingIngredient;
        this.verticalIngredient = verticalIngredient;
        this.result = result;
        this.chance = chance;
    }

    public static SimpleFluidReactionDisplay of(List<EntryIngredient> input, List<EntryIngredient> output, NbtCompound nbt) {
        return new SimpleFluidReactionDisplay(
                input.get(0),
                input.get(1),
                input.get(2),
                output.get(0),
                IFluidReactionDisplay.getChance(nbt)
        );
    }

    public static SimpleFluidReactionDisplay fromReaction(SimpleFluidReaction reaction) {
        return new SimpleFluidReactionDisplay(
                ReiUtils.getEntryIngredientInput(reaction.getSource()),
                ReiUtils.getEntryIngredient(reaction.getSurroundingIngredient()),
                ReiUtils.getEntryIngredient(reaction.getVerticalIngredient()),
                EntryIngredients.of(reaction.getResult().getBlock()),
                reaction.getChance()
        );
    }

    public static boolean validate(SimpleFluidReaction reaction) {
        // Validate reaction type to avoid adding subclasses
        return reaction.getType() == FluidReactionType.SIMPLE;
    }

    @Override
    public FluidReactionType<?> getType() {
        return FluidReactionType.SIMPLE;
    }

    @Override
    public EntryIngredient getSource() {
        return source;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public EntryIngredient getResult() {
        return result;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SimpleFluidReactionCategory.ID;
    }

    public EntryIngredient getSurroundingIngredient() {
        return surroundingIngredient;
    }

    public EntryIngredient getVerticalIngredient() {
        return verticalIngredient;
    }
}
