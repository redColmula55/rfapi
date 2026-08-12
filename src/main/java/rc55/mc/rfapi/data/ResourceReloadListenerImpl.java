package rc55.mc.rfapi.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rc55.mc.rfapi.RFApiMain;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ResourceReloadListenerImpl<T> implements SimpleSynchronousResourceReloadListener {
    private final ResourceType type;
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
        this.type = type;
    }

    public ResourceReloadListenerImpl(ResourceType type, Identifier id, String path, ResourceMap<?, T> resourceMap, @NotNull Function<JsonElement, T> resourceParser) {
        this.id = id;
        this.path = path;
        this.codec = null;
        this.parser = resourceParser;
        this.resourceMap = resourceMap;
        this.type = type;
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
        return new ResourceReloadListenerImpl<>(ResourceType.SERVER_DATA, id, id.getPath(), codec, cache).register();
    }

    public ResourceReloadListenerImpl<T> register() {
        ResourceManagerHelper.get(this.type).registerReloadListener(this);
        return this;
    }

    public ResourceType getType() {
        return type;
    }

    public String getPath() {
        return path;
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
                // Remove prefix & extension for the resource id
                resources.put(id.withPath(s -> s.matches("\\w+/[\\w/.]+") ? s.substring(s.indexOf('/') + 1, s.length() - 5) : s), result);
            } catch (Throwable e) {
                RFApiMain.LOGGER.error("Failed to load resource {}: {}", entry.getKey(), e);
            }
        }
        this.resourceMap.reload(resources);
        RFApiMain.LOGGER.info("Loaded {}x {} from enabled data packs.", resources.size(), this.getFabricId());
    }
}
