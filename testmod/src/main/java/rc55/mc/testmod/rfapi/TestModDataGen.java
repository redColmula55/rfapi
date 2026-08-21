package rc55.mc.testmod.rfapi;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import rc55.mc.testmod.rfapi.datagen.TestModEnLangDataGen;
import rc55.mc.testmod.rfapi.datagen.TestModModelDataGen;
import rc55.mc.testmod.rfapi.datagen.tag.TestModBlockTagDataGen;
import rc55.mc.testmod.rfapi.datagen.tag.TestModFluidTagDataGen;
import rc55.mc.testmod.rfapi.datagen.tag.TestModItemTagDataGen;

public class TestModDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack generator = fabricDataGenerator.createPack();
        generator.addProvider(TestModModelDataGen::new);
        generator.addProvider(TestModEnLangDataGen::new);
        // Tags
        generator.addProvider(TestModBlockTagDataGen::new);
        generator.addProvider(TestModItemTagDataGen::new);
        generator.addProvider(TestModFluidTagDataGen::new);
    }
}
