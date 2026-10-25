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
import rc55.mc.rfapi.compat.rei.category.SourceConversionReactionCategory;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;
import rc55.mc.rfapi.fluid.reaction.SourceConversionReaction;

import java.util.Collections;
import java.util.List;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SourceConversionReactionDisplay extends BasicDisplay implements IFluidReactionDisplay {
    private final EntryIngredient source, ingredient, result;
    private final float chance;

    public SourceConversionReactionDisplay(EntryIngredient source, EntryIngredient ingredient, float chance, EntryIngredient result) {
        super(ImmutableList.of(source, ingredient), Collections.singletonList(result));
        this.source = source;
        this.ingredient = ingredient;
        this.chance = chance;
        this.result = result;
    }

    public static SourceConversionReactionDisplay of(List<EntryIngredient> input, List<EntryIngredient> output, NbtCompound nbt) {
        return new SourceConversionReactionDisplay(
                input.get(0),
                input.get(1),
                IFluidReactionDisplay.getChance(nbt),
                output.get(1)
        );
    }

    public static SourceConversionReactionDisplay fromReaction(SourceConversionReaction reaction) {
        return new SourceConversionReactionDisplay(
                ReiUtils.getEntryIngredient(reaction.getSource()),
                ReiUtils.getEntryIngredient(reaction.getIngredient()),
                reaction.getChance(),
                EntryIngredients.of(reaction.getResult().getBlock())
        );
    }

    public static boolean validate(SourceConversionReaction reaction) {
        // Validate reaction type to avoid adding subclasses
        return reaction.getType() == FluidReactionType.SOURCE_CONVERSION;
    }

    @Override
    public FluidReactionType<?> getType() {
        return FluidReactionType.SOURCE_CONVERSION;
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
        return SourceConversionReactionCategory.ID;
    }

    public EntryIngredient getIngredient() {
        return ingredient;
    }
}
