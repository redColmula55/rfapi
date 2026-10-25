package rc55.mc.rfapi.compat.rei.display;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.annotations.FieldsAreNonnullByDefault;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.nbt.NbtCompound;
import rc55.mc.rfapi.compat.rei.ReiUtils;
import rc55.mc.rfapi.compat.rei.category.InfectionReactionCategory;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;
import rc55.mc.rfapi.fluid.reaction.InfectionReaction;

import java.util.Collections;
import java.util.List;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfectionReactionDisplay extends BasicDisplay implements IFluidReactionDisplay {
    private final EntryIngredient source, target;
    private final float chance;

    public InfectionReactionDisplay(EntryIngredient source, EntryIngredient target, float chance) {
        super(ImmutableList.of(source, target), Collections.singletonList(source));
        this.source = source;
        this.target = target;
        this.chance = chance;
    }

    public static InfectionReactionDisplay of(List<EntryIngredient> input, List<EntryIngredient> output, NbtCompound nbt) {
        return new InfectionReactionDisplay(
                output.get(0),
                input.get(0),
                IFluidReactionDisplay.getChance(nbt)
        );
    }

    public static InfectionReactionDisplay fromReaction(InfectionReaction reaction) {
        return new InfectionReactionDisplay(
                ReiUtils.getEntryIngredient(reaction.getSource()),
                ReiUtils.getEntryIngredientInput(reaction.getTarget()),
                reaction.getChance()
        );
    }

    public static boolean validate(InfectionReaction reaction) {
        // Validate reaction type to avoid adding subclasses
        return reaction.getType() == FluidReactionType.INFECTION;
    }

    @Override
    public FluidReactionType<?> getType() {
        return FluidReactionType.INFECTION;
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
        return source;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return InfectionReactionCategory.ID;
    }

    public EntryIngredient getTarget() {
        return target;
    }
}
