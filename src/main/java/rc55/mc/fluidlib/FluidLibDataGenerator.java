package rc55.mc.fluidlib;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import rc55.mc.fluidlib.data.gen.DefaultedFluidReactionDataProvider;

public class FluidLibDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack generator = fabricDataGenerator.createPack();
        generator.addProvider(DefaultedFluidReactionDataProvider::new);
	}
}
