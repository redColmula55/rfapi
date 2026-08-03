package rc55.mc.fluidlib.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rc55.mc.fluidlib.FluidLib;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ResourceReloadListenerImpl<T> implements SimpleSynchronousResourceReloadListener {

    private final Identifier id;
    private final String path;
    private final ResourceMap<?, T> resourceMap;

    private final @Nullable Codec<T> codec;
    private final @Nullable Function<JsonElement, T> parser;

    public ResourceReloadListenerImpl(ResourceType type, Identifier id, String path, @NotNull Codec<T> resourceCodec, ResourceMap<?, T> resourceMap) {
        this.id = id;
        this.path = path;
        this.codec = resourceCodec;
        this.parser = null;
        this.resourceMap = resourceMap;
        ResourceManagerHelper.get(type).registerReloadListener(this);
    }

    public ResourceReloadListenerImpl(ResourceType type, Identifier id, String path, ResourceMap<?, T> resourceMap, @NotNull Function<JsonElement, T> resourceParser) {
        this.id = id;
        this.path = path;
        this.codec = null;
        this.parser = resourceParser;
        this.resourceMap = resourceMap;
        ResourceManagerHelper.get(type).registerReloadListener(this);
    }

    /**
     * Register resource loaded on server
     * @param id Fabric ID || Path - {@code /data/NAMESPACE/{id.getPath()}/}
     * @param codec Resource Codec
     * @param cache Resource cache
     * @return Listener instance
     * @param <T> Resource type
     */
    public static <T> ResourceReloadListenerImpl<T> ofServer(Identifier id, Codec<T> codec, ResourceMap<?, T> cache) {
        return new ResourceReloadListenerImpl<>(ResourceType.SERVER_DATA, id, id.getPath(), codec, cache);
    }

    @Override
    public Identifier getFabricId() {
        return this.id;
    }

    @Override
    public void reload(ResourceManager manager) {
        final Map<Identifier, T> resources = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : manager.findResources(this.path, id -> id.getPath().endsWith("json")).entrySet()) {
            final Identifier id = entry.getKey();
            try (Reader reader = new InputStreamReader(entry.getValue().getInputStream())) {
                final JsonElement json = JsonParser.parseReader(reader);
                final T result;
                if (this.codec != null) {
                    result = Util.getResult(this.codec.parse(JsonOps.INSTANCE, json), DecoderException::new);
                } else if (this.parser != null) {
                    result = this.parser.apply(json);
                } else {
                    throw new IllegalStateException("How");
                }
                //去除资源id的前缀及扩展名
                resources.put(id.withPath(s -> s.matches("\\w+/[\\w/.]+") ? s.substring(s.indexOf('/') + 1, s.length() - 5) : s), result);
            } catch (Throwable e) {
                FluidLib.LOGGER.error("Failed to load resource {}: {}", entry.getKey(), e);
            }
        }
        this.resourceMap.reload(resources);
        FluidLib.LOGGER.info("Loaded {} fluid reactions from enabled data packs", resources.size());
    }
}
