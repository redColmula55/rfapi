package rc55.mc.rfapi.compat.rei;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;
import rc55.mc.rfapi.data.BlockIngredient;
import rc55.mc.rfapi.data.FluidIngredient;
import rc55.mc.rfapi.data.StateIngredient;

public class ReiUtils {
    public static EntryIngredient getEntryIngredient(StateIngredient ingredient) {
        if (ingredient == StateIngredient.EMPTY) return EntryIngredient.empty();
        final EntryIngredient.Builder builder = EntryIngredient.builder();
        ingredient.getValidBlocks().stream().flatMap(BlockIngredient.Entry::stream).map(EntryStacks::of).forEach(builder::add);
        ingredient.getValidFluids().stream().flatMap(FluidIngredient.Entry::stream).map(EntryStacks::of).forEach(builder::add);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public static EntryIngredient getEntryIngredient(FluidIngredient ingredient) {
        if (ingredient == FluidIngredient.EMPTY) return EntryIngredient.empty();
        return EntryIngredient.of(
                ingredient.stream().flatMap(FluidIngredient.Entry::stream).map(EntryStacks::of).toArray(EntryStack[]::new)
        );
    }

    public static Text getChanceDisplayText(float chance) {
        if (chance <= 0f) {
            return Text.translatable("text.chance.never");
        } else if (chance >= 1f) {
            return Text.translatable("text.chance.always");
        } else {
            return Text.translatable("text.chance", "%.1f".formatted(chance * 100f));
        }
    }
}
