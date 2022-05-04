package net.minestom.server.instance.block.sound;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.sound.SoundEvent;

public interface SoundType extends ProtocolObject {

    static SoundType fromNamespaceId(String namespaceId) {
        return SoundTypeImpl.getSafe(namespaceId);
    }

    float volume();

    float pitch();

    SoundEvent breakSound();

    SoundEvent stepSound();

    SoundEvent placeSound();

    SoundEvent hitSound();

    SoundEvent fallSound();
}
