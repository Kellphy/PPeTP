![A big body of water with a dog on one side, and a Minecraft character on the other. An arrow indicates the dog can still follow the character](https://github.com/TheEpicBlock/PPeTP/raw/main/banner.png)
# Proper Pet TP

Have you ever been exploring, and found a cool dog, cat or parrot?
Have you ever continued exploring only to find that pet not following you anymore?
This mod fixes that! In vanilla, pets stop following you if they're outside of simulation distance.
This mod ensures they follow you around either way. You can't accidentally lose your pet anymore!

## What if my dog's sitting?
This mod won't change that. If your pet wouldn't normally teleport, due to being leashed or sitting,
it still won't!

## How?
Pets commonly get lost if they don't have any safe spot to teleport to.
This mod temporarily attaches the pet to your player data until you land in a safe spot.
This is a simple way to ensure that the pet can always keep following you. For the pet-owner,
it'll look as if the pet just smoothly teleported thousands of blocks.

In addition to minecraft's own checks for when a pet is too far away, this mod also ensures
that pets have a chance to teleport when chunks get suddenly unloaded. This handles cases
where the player suddenly teleports (through commands or enderpearls), or when the player dies.

## What about cross-dimensional teleports?
PPeTP retains the vanilla behaviour of not teleporting across dimensions. If you take a long nether trip, this
mod will still allow your pet to teleport to you, but only once you're back in the overworld.
You can change this behaviour using `/gamerule petTeleportCrossDimension`. When set to true, your pet will teleport to
you even if it's in another dimension. If it's false, pets will only switch dimensions if they themselves touch a portal
(like in vanilla). This behaviour may be confusing if a pet accidentally walks through a portal.

## Does this work with modded pets?
Yeah, if they reuse Minecraft's code for tameable entities, it should be fine!

---

## Fork Changes (Kellphy)

### New Features

#### Allay Teleportation Support
Allays that have a "liked player" (from giving them items) are now teleported the same way as tameable pets. If an allay is stationed at a noteblock, it won't be teleported.

#### Nether Close-Range Placement
When a pet is extracted after a nether portal teleport, it spawns within **1 block** of the player (instead of the normal 2–3 block range), so it doesn't end up on the wrong side of a portal platform.

#### Per-Entity-Type Toggle via `/ppetp` Command
A single command controls which entity types are managed by the mod:
- `/ppetp enable <entity_type>` — e.g. `/ppetp enable minecraft:wolf`
- `/ppetp disable <entity_type>` — e.g. `/ppetp disable minecraft:allay`
- `/ppetp list` — shows all currently enabled types

Settings are saved per-world and survive restarts. **No entity types are enabled by default** — you must explicitly enable the ones you want. Works with modded entity types too. Requires op level 2.

### How Teleportation Works

There are **two trigger paths**, both requiring the entity to be **48+ blocks away** (or in a different dimension):

1. **Distance-based (tick hook)**
   - **Tameable pets:** When vanilla's `tryTeleportToOwner` fires and the pet is 48+ blocks from the owner, the mod stores the pet in the player's NBT data instead of doing a vanilla teleport.
   - **Allays:** Every server tick, if an allay has a liked player and is 48+ blocks away (or in a different dimension), it gets stored.

2. **Chunk unload**
   - When a chunk is about to unload, any tameable pet or allay in that chunk is checked. If it's supposed to be following a player and is 48+ blocks away (or in a different dimension), it gets stored before the chunk unloads — preventing the "lost pet" problem.

**Extraction:** Each player tick, `PlayerPetStorage` checks if any stored pets can be placed back. It looks for a valid spot near the player (1 block for nether teleports, 2–3 blocks otherwise) and re-spawns the entity.

**Cross-dimensional teleport** (e.g. through nether portals) is still controlled by the `proper-pet-tp:pet_teleport_cross_dimension` gamerule (default: false).
