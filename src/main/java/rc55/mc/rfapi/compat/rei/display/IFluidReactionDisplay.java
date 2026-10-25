package rc55.mc.rfapi.compat.rei.display;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.nbt.NbtCompound;
import rc55.mc.rfapi.fluid.reaction.FluidReactionType;

@MethodsReturnNonnullByDefault
public interface IFluidReactionDisplay extends Display {
    FluidReactionType<?> getType();
    EntryIngredient getSource();
    float getChance();
    EntryIngredient getResult();

    static float getChance(NbtCompound nbt) {
        return nbt.getFloat("Chance");
    }
    static void writeChance(IFluidReactionDisplay display, NbtCompound nbt) {
        nbt.putFloat("Chance", display.getChance());
    }
}
