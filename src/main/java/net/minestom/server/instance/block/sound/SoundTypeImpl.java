package net.minestom.server.instance.block.sound;

import net.minestom.server.registry.Registry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public record SoundTypeImpl(NamespaceID namespace, int id, float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound, SoundEvent hitSound, SoundEvent fallSound) implements SoundType {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    private static final Registry.Container<SoundType> CONTAINER = Registry.createContainer(Registry.Resource.SOUND_TYPES, (namespace, properties) -> {
        final double volume = properties.getDouble("volume");
        final double pitch = properties.getDouble("pitch");
        final SoundEvent breakSound = SoundEvent.fromNamespaceId(properties.getString("breakSound"));
        final SoundEvent stepSound = SoundEvent.fromNamespaceId(properties.getString("stepSound"));
        final SoundEvent placeSound = SoundEvent.fromNamespaceId(properties.getString("placeSound"));
        final SoundEvent hitSound = SoundEvent.fromNamespaceId(properties.getString("hitSound"));
        final SoundEvent fallSound = SoundEvent.fromNamespaceId(properties.getString("fallSound"));
        return new SoundTypeImpl(NamespaceID.from("minecraft:" + namespace.toLowerCase()), ID_COUNTER.getAndIncrement(), (float) volume, (float) pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
    });

    public static SoundType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static SoundType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static SoundType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<SoundType> values() {
        return CONTAINER.values();
    }

}
