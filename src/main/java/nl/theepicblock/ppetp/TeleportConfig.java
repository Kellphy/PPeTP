package nl.theepicblock.ppetp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persistent per-world configuration that stores which entity types
 * are enabled/disabled for PPeTP teleportation.
 *
 * By default, wolf, cat, parrot, and allay are enabled.
 * Use /ppetp enable/disable <entity_type> to change.
 */
public class TeleportConfig extends PersistentState {
    private static final Set<Identifier> DEFAULT_ENABLED = Set.of();

    public static final Codec<TeleportConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Identifier.CODEC)
                            .fieldOf("enabled_types")
                            .forGetter(config -> List.copyOf(config.enabledTypes))
            ).apply(instance, TeleportConfig::new)
    );

    public static final PersistentStateType<TeleportConfig> TYPE = new PersistentStateType<>(
            "ppetp_config",
            TeleportConfig::new,
            CODEC,
            null
    );

    private final Set<Identifier> enabledTypes;

    public TeleportConfig() {
        this.enabledTypes = new HashSet<>(DEFAULT_ENABLED);
    }

    private TeleportConfig(List<Identifier> enabledTypes) {
        this.enabledTypes = new HashSet<>(enabledTypes);
    }

    public boolean isEnabled(EntityType<?> type) {
        var id = Registries.ENTITY_TYPE.getId(type);
        return enabledTypes.contains(id);
    }

    public boolean enable(Identifier typeId) {
        var changed = enabledTypes.add(typeId);
        if (changed) markDirty();
        return changed;
    }

    public boolean disable(Identifier typeId) {
        var changed = enabledTypes.remove(typeId);
        if (changed) markDirty();
        return changed;
    }

    public Set<Identifier> getEnabledTypes() {
        return Set.copyOf(enabledTypes);
    }

    public static TeleportConfig get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }
}
