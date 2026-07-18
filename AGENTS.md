# ProtocolLib Agent Guide

This file applies to the entire repository. Read `.github/CONTRIBUTING.md` before changing Minecraft support, packet
definitions, reflection, or public wrappers.

## Repository Basics

- The main project uses the Gradle wrapper. Do not use `TinyProtocol/pom.xml` to build ProtocolLib.
- The Java toolchain and current Spigot dependency are defined in `build.gradle.kts`.
- Production code is under `src/main/java`; JUnit tests and NMS test initialization are under `src/test/java`.
- Keep changes focused. Do not combine protocol fixes with unrelated cleanup or formatting.
- Preserve public API and binary compatibility unless the task explicitly requires a breaking change.

## Build and Test Commands

```shell
./gradlew test
./gradlew build shadowJar
```

Run the smallest relevant test while developing:

```shell
./gradlew test --tests fully.qualified.TestClass
./gradlew test --tests fully.qualified.TestClass.testMethod
```

After changing production Java code, run the targeted tests and then the complete test suite. Use `build shadowJar` when
the task affects packaging, dependencies, release support, or the final plugin artifact.

Do not introduce another build system, test runner, formatter, or lint tool unless the repository adopts it separately.

## Important Code Areas

- `PacketType.java`: public packet constants, current IDs, aliases, and deprecated packet definitions.
- `injector/packet/internal/ProtocolRegistry_1_20_5.java`: current packet discovery and codec registration.
- `utility/MinecraftVersion.java`: named Minecraft versions and the latest supported release.
- `utility/MinecraftProtocolVersion.java`: Minecraft release to protocol-number mappings.
- `ProtocolLibrary.java`: supported version bounds and latest release date.
- `utility/MinecraftReflection.java`: centralized NMS and CraftBukkit class discovery.
- `reflect/StructureModifier.java`: indexed packet field access and conversion contract.
- `wrappers/`: stable public representations of unstable NMS structures.
- `events/PacketContainer.java` and `events/AbstractStructure.java`: packet construction and typed field access.
- `src/test/java/com/comphenix/protocol/BukkitInitialization.java`: test bootstrap for CraftBukkit and NMS access.

## Minecraft Release Updates

Follow the complete checklist in `.github/CONTRIBUTING.md`. In particular:

- Update all version metadata together; do not bump only the Gradle dependency.
- Reconcile every serverbound and clientbound packet in every protocol phase with `PacketType`.
- Add packet types for newly split packets instead of forcing incompatible shapes through an older packet constant.
- Preserve removed public `PacketType` constants in the deprecated section.
- Update `MinecraftProtocolVersion` only when the protocol number actually changed.

Every packet exposed by the current NMS protocol registries must have a matching `PacketType`. An undefined or incorrectly
renamed packet can abort registration of later packets in the same registry.

## Version Compatibility

- Put version checks at the narrowest affected boundary.
- Keep the pre-change code path intact whenever possible.
- Use named `MinecraftVersion` constants with `atOrAbove()` instead of scattered version strings.
- A public method should keep the same signature and broad contract across versions.
- If Minecraft removed a concept, return an unsupported or absent result consistent with existing APIs. Do not claim an
  unrelated packet or class is the removed type.
- When Minecraft splits one packet into several packets, expose and use the new packet types explicitly.
- Preserve unrelated packet fields when adapting or rewriting one logical value.

The current test dependency exercises the latest supported server. Review older guarded paths carefully because they may
not execute in the local test run.

## Packet and Reflection Rules

- Treat the current server packet classes, record components, constructors, and codecs as authoritative.
- Prefer existing `MinecraftReflection`, `FuzzyReflection`, and accessor helpers over new direct reflection.
- Prefer structural and type-based matching where obfuscated names are unstable.
- Search for an existing class resolver, converter, or accessor before adding another one.
- Cache stable reflective lookups following nearby patterns; do not repeatedly scan classes on packet hot paths.
- Do not hide reflection failures with broad catches, silent defaults, or unrelated fallback types.
- Account for static fields, inherited fields, records, nested implementations, and duplicate field types when writing
  fuzzy contracts.

## `StructureModifier` Contract

Typed accessors returned from packet APIs must behave like normal `StructureModifier` instances.

- `size()`, `getFields()`, `getField()`, target metadata, and field metadata must agree.
- `readSafely()` returns `null` for an absent index.
- `writeSafely()` ignores an absent index without throwing.
- `withTarget()` must preserve the modifier's behavior for the new target.
- `writeDefaults()` must remain safe.
- Converters must be applied symmetrically on reads and writes.

If a packet no longer contains a real backing field, prefer an empty modifier when compatible. If a virtual field is
required for API compatibility, initialize all modifier metadata and test its inherited methods.

## Wrapper and Converter Rules

- Wrapper factories, converters, getters, setters, and `deepClone()` must describe the same logical value.
- Preserve converter symmetry: converting specific to generic and back must not unexpectedly lose represented state.
- If a wrapper handle changes from a nested value to an entire packet, define logical `equals()` and `hashCode()` so
  unrelated packet metadata does not affect equality.
- Clones that represent the same logical value should compare equal.
- Do not fabricate legacy states that the modern wire format cannot distinguish; document or surface the limitation.
- Record or immutable packet updates must retain entity IDs, flags, and other components not owned by the wrapper.

## Tests

- Call `BukkitInitialization.initializeAll()` before tests that resolve CraftBukkit or NMS classes.
- Prefer regression tests using the actual Spigot packet classes supplied by the test dependency.
- Test reads, writes, safe access, cloning, equality, and preservation of unrelated fields when changing wrappers.
- Test modifier metadata and inherited methods when introducing a custom `StructureModifier`.
- Re-enable disabled tests when fixing the incompatibility that caused them to be disabled.
- Do not weaken assertions or encode known incorrect behavior merely to make a new Minecraft version pass.

## Documentation and Review

- Update Javadocs when version support, nullability, or unsupported behavior changes.
- Update `.github/CONTRIBUTING.md` when the repository-wide build or release workflow changes.
- Keep this file operational and durable; do not add task-specific notes, temporary workarounds, or current PR details.
- Before finishing, inspect the complete diff for accidental API changes and unrelated edits.
