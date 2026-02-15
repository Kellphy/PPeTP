package nl.theepicblock.ppetp;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PetTeleporter {
    // Minimum distance (in blocks) before the mod stores the pet.
    private static final double MIN_DISTANCE_SQ = 48 * 48;

    /**
     * Callback which is called by {@link nl.theepicblock.ppetp.mixin.TameableEntityMixin} whenever
     * minecraft tries to teleport a pet. Only intervenes if 48+ blocks away.
     */
    public static void teleportPet(TameableEntity pet, LivingEntity owner) {
        if (owner instanceof ServerPlayerEntity player && shouldTeleportToInventory(pet, owner)) {
            // Check if this entity type is enabled
            if (!PPeTP.isTeleportEnabled(player.getEntityWorld().getServer(), pet.getType())) return;
            teleportToInventory(pet, player);
        }
    }

    /**
     * The pet is in a chunk that's about to be unloaded! Store it in the player's data
     * only if it's far enough away that it would genuinely be lost.
     */
    public static void petAlmostUnloaded(TameableEntity pet) {
        if (pet.cannotFollowOwner()) {
            // Nvm, the pet is not following us right now
            return;
        }

        // We can't use the normal getOwner method because the player might've died
        var owner = getOwner(pet);
        if (owner == null) return;

        // Check if this entity type is enabled
        if (!PPeTP.isTeleportEnabled(owner.getEntityWorld().getServer(), pet.getType())) return;

        // Different dimension (e.g. nether) — always store
        if (pet.getEntityWorld() != owner.getEntityWorld()) {
            teleportToInventory(pet, owner);
            return;
        }

        // Same dimension — only store if far enough that vanilla can't save it
        var distSq = pet.getEntityPos().subtract(owner.getEntityPos()).horizontalLengthSquared();
        if (distSq >= MIN_DISTANCE_SQ) {
            teleportToInventory(pet, owner);
        }
    }

    /**
     * Alternative implementation of {@link TameableEntity#getOwner()} that accounts for
     * the player being dead, or in a different dimension
     */
    public static @Nullable ServerPlayerEntity getOwner(TameableEntity pet) {
        var server = pet.getEntityWorld().getServer();
        if (server == null) return null;
        var ref = pet.getOwnerReference();
        if (ref == null) return null;
        var ownerUuid = ref.getUuid();
        if (ownerUuid == null) return null;
        return server.getPlayerManager().getPlayer(ownerUuid);
    }

    /**
     * Determines if a teleport to the inventory should occur.
     * Only triggers when the pet is 48+ blocks away horizontally, or in a different dimension.
     */
    public static boolean shouldTeleportToInventory(TameableEntity pet, LivingEntity owner) {
        if (pet.getEntityWorld() != owner.getEntityWorld()) {
            return true;
        }
        var dist = pet.getEntityPos().subtract(owner.getEntityPos()).horizontalLengthSquared();
        return dist >= MIN_DISTANCE_SQ;
    }

    /**
     * "Teleports" the pet into the players "inventory". Aka, it deletes the pet from the world and stores it
     * in the player's data instead
     */
    public static void teleportToInventory(TameableEntity pet, ServerPlayerEntity player) {
        var storage = ((PlayerDuck)player).PPeTP$getStorage();
        var success = storage.insert(pet);
        if (!success) {
            // Something went wrong whilst saving. Just abort
            return;
        }

        // Discard is in fact the right method to call here. It's what parrots do
        // when they sit on their owner's shoulders
        pet.discard();
    }
}
