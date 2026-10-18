package rc55.mc.rfapi.compat.rei.display;

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
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

public class SimpleFluidReactionDisplay extends BasicDisplay implements Display {

    public final EntryIngredient source, surroundingIngredient, verticalIngredient, result;
    public final float chance;

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
                nbt.getFloat("Chance")
        );
    }

    public static SimpleFluidReactionDisplay fromReaction(SimpleFluidReaction reaction) {
        return new SimpleFluidReactionDisplay(
                ReiUtils.getEntryIngredient(reaction.getSource()),
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
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SimpleFluidReactionCategory.ID;
    }
}
