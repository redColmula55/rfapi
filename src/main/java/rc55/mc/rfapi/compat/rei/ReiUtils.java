package rc55.mc.rfapi.compat.rei;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rc55.mc.rfapi.RFApiMain;
import rc55.mc.rfapi.compat.rei.display.IFluidReactionDisplay;
import rc55.mc.rfapi.data.BlockIngredient;
import rc55.mc.rfapi.data.FluidIngredient;
import rc55.mc.rfapi.data.StateIngredient;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
public class ReiUtils {
    public static final Text STILL_RESULT_TEXT = Text.translatable("text.reservoir-api.rei.slot.still_result");
    public static final Text FLOWING_RESULT_TEXT = Text.translatable("text.reservoir-api.rei.slot.flowing_result");

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

    @SuppressWarnings("unchecked")
    public static EntryIngredient getEntryIngredientInput(FluidIngredient ingredient) {
        if (ingredient == FluidIngredient.EMPTY) return EntryIngredient.empty();
        return EntryIngredient.of(
                ingredient.stream().flatMap(FluidIngredient.Entry::stream).map(ReiUtils::createInputStack).toArray(EntryStack[]::new)
        );
    }

    public static Text getChanceDisplayText(float chance) {
        if (chance <= 0f) {
            return Text.translatable("text.reservoir-api.rei.display.chance.never");
        } else if (chance >= 1f) {
            return Text.translatable("text.reservoir-api.rei.display.chance.always");
        } else {
            return Text.translatable("text.reservoir-api.rei.display.chance", "%.1f".formatted(chance * 100f));
        }
    }

    public static EntryStack<?> entryStack(ItemConvertible item, Text... tooltip) {
        if (FabricLoaderImpl.INSTANCE.getEnvironmentType() == EnvType.CLIENT) {
            return EntryStacks.of(item).tooltip(tooltip);
        }
        return EntryStacks.of(item);
    }

    public static EntryStack<?> entryStack(Fluid fluid, Text... tooltip) {
        if (FabricLoaderImpl.INSTANCE.getEnvironmentType() == EnvType.CLIENT) {
            return EntryStacks.of(fluid).tooltip(tooltip);
        }
        return EntryStacks.of(fluid);
    }

    public static EntryStack<?> entryStack(BlockState state, Text... tooltip) {
        return entryStack(state.getBlock(), tooltip);
    }

    private static EntryStack<?> createInputStack(Fluid fluid) {
        return entryStack(fluid, Text.translatable("text.reservoir-api.rei.slot.input"));
    }

    public static <D extends IFluidReactionDisplay> CategoryIdentifier<D> categoryId(FluidReactionType<?> type) {
        Identifier id = Objects.requireNonNull(FluidReactionType.REGISTRY.getId(type));
        return CategoryIdentifier.of(RFApiMain.MODID, String.format("reaction/%s/%s", id.getNamespace(), id.getPath()));
    }

    public static List<Widget> build2To1Display(Rectangle bounds, IFluidReactionDisplay display, EntryIngredient ingredient) {
        List<Widget> widgets = new ArrayList<>(9);
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(bounds.getCenterX(), bounds.getCenterY() - 8)));

        Point inputPoint = new Point(bounds.getCenterX() - 30, bounds.getCenterY() - 8);
        widgets.add(Widgets.createSlot(inputPoint).entries(display.getSource()).markInput());
        widgets.add(Widgets.createSlot(inputPoint));

        Point siPoint = new Point(bounds.getCenterX() - 52, bounds.getCenterY() - 8);
        widgets.add(Widgets.createSlot(siPoint).entries(ingredient).markInput());
        widgets.add(Widgets.createSlot(siPoint));

        Point resultPoint = new Point(bounds.getCenterX() + 41, bounds.getCenterY() - 8);
        widgets.add(Widgets.createSlot(resultPoint).entries(display.getResult()).markOutput());
        widgets.add(Widgets.createSlot(resultPoint));

        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() + 18), ReiUtils.getChanceDisplayText(display.getChance())));
        return widgets;
    }
}
