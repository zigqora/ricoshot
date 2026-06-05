# Ricoshot 1.0.0 (Minecraft 1.21.2 - 1.21.3)

Changelog for the Ricoshot mod port to Minecraft versions 1.21.2 and 1.21.3.

## Compatibility

- Minecraft: 1.21.2, 1.21.3
- Mod Loader: Fabric Loader >=0.19.2
- Dependency: Fabric API (tested on 0.106.1+1.21.2)
- Java: 21

## Core Features

- **Coin Toss:** Place gold nuggets in the off-hand and a bow in the main hand. Right-click to throw a coin.
- **Ricoshot:** Shoot the flying gold nugget with a bow. The arrow redirects to the nearest mob, dealing critical damage.
- **Perfect Split:** Shoot the coin at the peak of its throw (indicated by green particles and a chime) to deal fatal damage to targets within 40 blocks and in line of sight.
- **Shield Parrying:** Block incoming ricoshot damage with a shield. This blocks the damage but uses 10 shield durability.
- **Config & Toggle:** Toggle action bar messages via the coin button in the pause menu, or customize messages in `config/ricoshot.json`.

## Technical Changes

Adapted the codebase to API changes introduced in Minecraft 1.21.2:
- Updated shield durability damage logic to pass `ServerWorld` and `ServerPlayerEntity` to `ItemStack.damage`.
- Updated entity damage calls to pass `ServerWorld`.
- Updated `FlyingNuggetEntity` initialization to match the new `ThrownItemEntity` constructor signature.
- Switched from `FabricEntityTypeBuilder` to the vanilla `EntityType.Builder` for entity registration.
- Updated `CooldownManager` checks to pass `ItemStack` instead of `Item`.
- Updated interaction callback to return `ActionResult` instead of `TypedActionResult`.
