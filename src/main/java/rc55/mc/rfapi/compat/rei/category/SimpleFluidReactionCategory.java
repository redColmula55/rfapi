package rc55.mc.rfapi.compat.rei.category;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import rc55.mc.rfapi.compat.rei.ReiUtils;
import rc55.mc.rfapi.compat.rei.display.SimpleFluidReactionDisplay;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;

import java.util.ArrayList;
import java.util.List;

public class SimpleFluidReactionCategory implements DisplayCategory<SimpleFluidReactionDisplay> {
    public static final CategoryIdentifier<SimpleFluidReactionDisplay> ID = ReiUtils.categoryId(FluidReactionType.SIMPLE);

    @Override
    public CategoryIdentifier<? extends SimpleFluidReactionDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Text getTitle() {
        return FluidReactionType.SIMPLE.getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.COBBLESTONE);
    }

    @Override
    public List<Widget> setupDisplay(SimpleFluidReactionDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>(11);
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(bounds.getCenterX(), bounds.getCenterY() - 8)));

        Point inputPoint = new Point(bounds.getCenterX() - 30, bounds.getCenterY() - 21);
        widgets.add(Widgets.createSlot(inputPoint).entries(display.getSource()).markInput());
        widgets.add(Widgets.createSlot(inputPoint));

        Point viPoint = new Point(bounds.getCenterX() - 30, bounds.getCenterY());
        if (!display.getVerticalIngredient().isEmpty()) {
            widgets.add(Widgets.createSlot(viPoint).entries(display.getVerticalIngredient()).markInput());
        }
        widgets.add(Widgets.createSlot(viPoint));

        Point siPoint = new Point(bounds.getCenterX() - 52, bounds.getCenterY() - 21);
        if (!display.getSurroundingIngredient().isEmpty()) {
            widgets.add(Widgets.createSlot(siPoint).entries(display.getSurroundingIngredient()).markInput());
        }
        widgets.add(Widgets.createSlot(siPoint));

        Point resultPoint = new Point(bounds.getCenterX() + 41, bounds.getCenterY() - 8);
        widgets.add(Widgets.createSlot(resultPoint).entries(display.getResult()).markOutput());
        widgets.add(Widgets.createSlot(resultPoint));

        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() + 18), ReiUtils.getChanceDisplayText(display.getChance())));
        return widgets;
    }
}
