package rc55.mc.rfapi.data;

import net.minecraft.text.Text;

public interface ITranslatable {
    String getTranslationKey();

    default Text getName() {
        return Text.translatable(this.getTranslationKey());
    }
}
