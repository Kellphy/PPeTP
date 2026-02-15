package nl.theepicblock.ppetp;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class AllayTeleporter {
    // Minimum distance (in blocks) before the mod stores the allay.
    private static final double MIN_DISTANCE_SQ = 48 * 48;

    /**
     * Called from {@link nl.theepicblock.ppetp.mixin.AllayEntityMixin} every tick (server-side)
     * to check if the allay needs to be teleported to its liked player.
     * Only triggers when 48+ blocks away or in a different dimension.
     */
    public static void checkAllay(AllayEntity allay) {
        if (!allay.isAlive()) return;

        var owner = getLikedPlayer(allay);
        if (owner == null) return;

        if (cannotFollowOwner(allay)) return;

        // Check if allay teleportation is enabled
        if (!PPeTP.isTeleportEnabled(owner.getEntityWorld().getServer(), EntityType.ALLAY)) return;

        if (shouldTeleportToInventory(allay, owner)) {
            teleportToInventory(allay, owner);
        }
    }

    /**
     * The allay is in a chunk that's about to be unloaded. Store it only if
     * it's far enough away that it would genuinely be lost.
     */
    public static void allayAlmostUnloaded(AllayEntity allay) {
        if (cannotFollowOwner(allay)) return;

        var owner = getLikedPlayer(allay);
        if (owner == null) return;

        // Check if allay teleportation is enabled
        if (!PPeTP.isTeleportEnabled(owner.getEntityWorld().getServer(), EntityType.ALLAY)) return;

        // Different dimension (e.g. nether) — always store
        if (allay.getEntityWorld() != owner.getEntityWorld()) {
            teleportToInventory(allay, owner);
            return;
        }

        // Same dimension — only store if far enough
        var distSq = allay.getEntityPos().subtract(owner.getEntityPos()).horizontalLengthSquared();
        if (distSq >= MIN_DISTANCE_SQ) {
            teleportToInventory(allay, owner);
        }
    }

    /**
     * An allay should not follow its owner if it's stationed at a noteblock.
     */
    public static boolean cannotFollowOwner(AllayEntity allay) {
        var brain = allay.getBrain();
        return brain.getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK).isPresent();
    }

    /**
     * Gets the liked player for the allay from its brain memory.
     */
    public static @Nullable ServerPlayerEntity getLikedPlayer(AllayEntity allay) {
        var server = allay.getEntityWorld().getServer();
        if (server == null) return null;
        var brain = allay.getBrain();
        var likedPlayerOpt = brain.getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
        if (likedPlayerOpt.isEmpty()) return null;
        return server.getPlayerManager().getPlayer(likedPlayerOpt.get());
    }

    /**
     * Only triggers when the allay is 48+ blocks away or in a different dimension.
     */
    public static boolean shouldTeleportToInventory(AllayEntity allay, ServerPlayerEntity owner) {
        if (allay.getEntityWorld() != owner.getEntityWorld()) {
            return true;
        }
        var dist = allay.getEntityPos().subtract(owner.getEntityPos()).horizontalLengthSquared();
        return dist >= MIN_DISTANCE_SQ;
    }

    /**
     * Stores the allay in the player's data and discards the entity from the world.
     */
    public static void teleportToInventory(AllayEntity allay, ServerPlayerEntity player) {
        var storage = ((PlayerDuck)player).PPeTP$getStorage();
        var success = storage.insert(allay);
        if (!success) {
            return;
        }
        allay.discard();
    }
}
