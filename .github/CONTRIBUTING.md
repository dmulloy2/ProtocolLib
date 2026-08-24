# Contributing to ProtocolLib

ProtocolLib supports a wide range of Minecraft versions through reflection and compatibility wrappers. Changes to packet
registration, reflection, or public wrappers can affect plugins on versions other than the one currently used for
development, so keep changes focused and preserve existing behavior unless a break is unavoidable.

## Building ProtocolLib

Use the Gradle wrapper from the repository root. The root project builds the platform-neutral library published to
Maven. The `paper` module compiles and tests it against Paper's development bundle and produces the primary plugin,
`build/libs/ProtocolLib.jar`. The `spigot` compatibility module produces `build/libs/ProtocolLib-Spigot.jar`.

```shell
./gradlew test
./gradlew build shadowJar
```

`shadowJar` builds both plugin distributions with the required Byte Buddy classes included. Neither platform provider
nor plugin descriptor is included in the Maven core artifact.

ProtocolLib uses the Java 25 toolchain to compile against the current server, but production classes target Java 17 for
compatibility with older supported servers.

## Updating ProtocolLib for a Minecraft Release

Start by reviewing the [Java Edition protocol](https://minecraft.wiki/w/Java_Edition_protocol) and the corresponding
server classes. The server packet classes and codecs are authoritative when documentation and implementation differ.

### 1. Update version metadata

- Update `mcVersion` in `gradle.properties`.
- Add the release to `MinecraftVersion` and update `MinecraftVersion.LATEST`.
- Update `ProtocolLibrary.MAXIMUM_MINECRAFT_VERSION` and `ProtocolLibrary.MINECRAFT_LAST_RELEASE_DATE`.
- Update `MinecraftProtocolVersion` if the release changed the protocol number.

### 2. Reconcile packet types

`PacketType` must contain a matching definition for every packet in the current server protocol registries. Compare the
serverbound and clientbound packet classes and codecs against every protocol phase, including configuration.

- Add new, renamed, or split packets and update their current IDs and aliases.
- Do not remove an existing public `PacketType` constant. Move removed types to the deprecated section and document their
  replacement when one exists.
- Represent split packets as separate packet types instead of redirecting incompatible packet shapes.
- Run the packet registry tests after changing packet definitions. A missing definition can prevent later packets in the
  same protocol registry from being registered.

### 3. Update reflection and wrappers

- Inspect changed fields, constructors, records, nested classes, and stream codecs instead of relying only on class names.
- Prefer structural or type-based reflection where names are unstable.
- Treat the current server API shape as primary and use older supported shapes only as narrow compatibility fallbacks.
- Put version checks at the narrowest compatibility boundary and leave the older path unchanged.
- If an NMS concept was removed, report it as unsupported or absent rather than treating a different class as equivalent.
- Preserve packet fields that are unrelated to the value being changed.
- Keep converters symmetric and preserve wrapper cloning and equality semantics.
- A custom `StructureModifier` must maintain the complete modifier contract, including safe reads and writes, field count,
  metadata, introspection, defaults, and `withTarget()`.

### 4. Add regression coverage

- Initialize the test environment with `BukkitInitialization` when tests access CraftBukkit or NMS classes.
- Exercise the actual packet structure supplied by the current Paper dependency.
- Cover both sides of a version guard when feasible.
- Re-enable previously disabled tests when the underlying incompatibility is fixed.
- Run targeted tests while developing, followed by `./gradlew test`.
- Before declaring a Minecraft release supported, build the shaded plugin and test it on the corresponding server.

## Pull Request Expectations

- Keep the change limited to the reported problem or release update.
- Include regression tests for packet, reflection, converter, or wrapper behavior changes.
- Preserve public API signatures and binary compatibility where possible.
- Clearly document any unavoidable compatibility change.
- Do not hide reflection failures with broad exception handling or silent fallbacks.
