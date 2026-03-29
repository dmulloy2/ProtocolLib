# Packet Wrappers

Type-safe wrappers around ProtocolLib `PacketContainer` objects.
Each wrapper lives under `net.dmulloy2.protocol.wrappers`, organized by protocol phase then direction.

---

## Package layout

```
net.dmulloy2.protocol/
  AbstractPacket.java                          # base class for all wrappers

  wrappers/
    game/
      clientbound/
        WrappedClientbound<PacketName>Packet.java
      serverbound/
        WrappedServerbound<PacketName>Packet.java
    login/
      clientbound/ …
      serverbound/ …
    status/
      clientbound/ …
      serverbound/ …
```

New NMS types that are reusable across multiple packets belong in the core ProtocolLib packages, not here:

| What | Where                                                                                 |
|---|---------------------------------------------------------------------------------------|
| New enum wrapper | inner enum + registration in `com.comphenix.protocol.wrappers.EnumWrappers            |
| New structure wrapper | `com.comphenix.protocol.wrappers.Wrapped<Name>.java` + getter in `AbstractStructure`  |
| Packet wrapper | `net.dmulloy2.protocol.wrappers.<phase>.<direction>.Wrapped<NMSPacketName>`           |

---

## Class Structure

* Every wrapper extends `AbstractPacket`
* There should be 3 constructors: no-args (create new packet with default values), all-args (create new packet with specified values), and from-container (wrap existing packet)
* Then create getters and setters for each field in the packet, using the appropriate `StructureModifier` methods from `AbstractStructure`.

```java
package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import net.dmulloy2.protocol.AbstractPacket;

public class WrappedServerboundAcceptTeleportationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TELEPORT_ACCEPT;

    public WrappedServerboundAcceptTeleportationPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundAcceptTeleportationPacket(int id) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { int.class }).createPacket(id));
    }

    public WrappedServerboundAcceptTeleportationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // getters and setters for packet fields
}
```

`AbstractPacket` validates the type and exposes `getHandle()`, `sendPacket(Player)`, and `receivePacket(Player)`.

---

## Reading and writing packet fields

Always go through `AbstractStructure`'s typed getters. Never touch `getModifier()` directly or read raw field indices without a type-checked accessor.

### Primitives

```java
handle.getIntegers().read(0)          // int
handle.getIntegers().write(0, value)

handle.getBooleans().read(0)          // boolean
handle.getBooleans().write(0, value)

handle.getFloat().read(0)             // float  (note: singular, not getFloats)
handle.getFloat().write(0, value)

handle.getLongs() / getDoubles() / getBytes() / getShorts() / getStrings() …
```

Field indices within a type are zero-based and ordered by their declaration order in the NMS class.

### Bukkit / ProtocolLib wrapper types

```java
handle.getVectors().read(0)                     // org.bukkit.util.Vector  ↔  Vec3
handle.getPositionMoveRotations().read(0)       // WrappedPositionMoveRotation  ↔  PositionMoveRotation
handle.getEntityModifier(world).read(0)         // org.bukkit.entity.Entity
handle.getItemModifier().read(0)                // org.bukkit.inventory.ItemStack
handle.getBlockPositionModifier().read(0)       // BlockPosition
handle.getNbtModifier().read(0)                 // NbtBase<?>
handle.getDataWatcherModifier().read(0)         // WrappedDataWatcher
// …see AbstractStructure for the full list
```

### Enums

Use `EnumWrappers.get<Name>Converter()` with the appropriate accessor:

```java
// Single enum field
handle.getEnumModifier(EnumWrappers.Hand.class, EnumWrappers.getHandClass()).read(0)

// Set of enum values (e.g. RelativeArgument)
handle.getSets(EnumWrappers.getRelativeArgumentConverter()).read(0)
handle.getSets(EnumWrappers.getRelativeArgumentConverter()).write(0, set)
```

### Nested structures (InternalStructure)

Only use `getStructures()` when you genuinely need to inspect a sub-structure that has no dedicated typed wrapper yet. Once you have written a `Wrapped<Name>` class, add a getter to `AbstractStructure` and use that instead.

---

## Adding a new NMS enum wrapper

1. **Declare** the enum as an inner class of `EnumWrappers`, matching NMS constant names exactly:

```java
// EnumWrappers.java
public enum RelativeArgument {
    X, Y, Z, Y_ROT, X_ROT, DELTA_X, DELTA_Y, DELTA_Z, ROTATE_DELTA
}
```

2. **Register** the NMS class, class getter, converter, and `associate()` call — all following the same pattern as `Hand`, `Direction`, etc.:

```java
// static field
private static Class<?> RELATIVE_CLASS = null;

// inside initialize() — class loading
RELATIVE_CLASS = MinecraftReflection.getMinecraftClass("world.entity.Relative");

// inside initialize() — association
associate(RELATIVE_CLASS, RelativeArgument.class, getRelativeArgumentConverter());

// public API
public static Class<?> getRelativeArgumentClass() {
    initialize();
    return RELATIVE_CLASS;
}

public static EquivalentConverter<RelativeArgument> getRelativeArgumentConverter() {
    return new EnumConverter<>(getRelativeArgumentClass(), RelativeArgument.class);
}
```

`EnumConverter` maps by constant name (`Enum.valueOf`). Use it whenever the NMS class **is** a real Java enum and the constant names match. Use `FauxEnumConverter` only for NMS classes that are enum-like but not `java.lang.Enum`.

---

## Adding a new NMS structure wrapper

`PositionMoveRotation` is the reference example.

### 1 — Create `com.comphenix.protocol.wrappers.Wrapped<Name>.java`

```java
public class WrappedPositionMoveRotation {

    // mutable fields matching the NMS record

    /** Read from a live InternalStructure using AbstractStructure typed getters. */
    public static WrappedPositionMoveRotation fromHandle(InternalStructure handle) {
        WrappedPositionMoveRotation w = new WrappedPositionMoveRotation();
        Vector pos   = handle.getVectors().read(0);
        Vector delta = handle.getVectors().read(1);
        w.x = pos.getX();  w.y = pos.getY();  w.z = pos.getZ();
        w.deltaX = delta.getX(); …
        w.yaw   = handle.getFloat().read(0);
        w.pitch = handle.getFloat().read(1);
        return w;
    }

    /**
     * Returns an EquivalentConverter for use in StructureModifier.withType().
     *
     * getSpecific — delegates to fromHandle(InternalStructure)
     * getGeneric  — constructs a new NMS record via its public constructor
     */
    public static EquivalentConverter<WrappedPositionMoveRotation> getConverter() {
        return new EquivalentConverter<>() {
            @Override
            public WrappedPositionMoveRotation getSpecific(Object generic) {
                return fromHandle(InternalStructure.getConverter().getSpecific(generic));
            }
            @Override
            public Object getGeneric(WrappedPositionMoveRotation s) {
                try {
                    Class<?> vec3 = MinecraftReflection.getVec3DClass();
                    Class<?> pmr  = MinecraftReflection.getMinecraftClass("world.entity.PositionMoveRotation");
                    Constructor<?> v = vec3.getDeclaredConstructor(double.class, double.class, double.class);
                    Constructor<?> p = pmr.getDeclaredConstructor(vec3, vec3, float.class, float.class);
                    return p.newInstance(v.newInstance(s.x, s.y, s.z),
                                        v.newInstance(s.deltaX, s.deltaY, s.deltaZ),
                                        s.yaw, s.pitch);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override public Class<WrappedPositionMoveRotation> getSpecificType() {
                return WrappedPositionMoveRotation.class;
            }
        };
    }
}
```

**Reading** always goes through `AbstractStructure` getters (`getVectors()`, `getFloat()`, …) — never raw field accessors.
**Writing** must construct a fresh NMS instance when the NMS type is an immutable record (final fields). Mutable NMS classes can be modified via their own `InternalStructure` instead.

### 2 — Add a getter to `AbstractStructure`

```java
// AbstractStructure.java — alongside getVectors(), getIntegers(), etc.
public StructureModifier<WrappedPositionMoveRotation> getPositionMoveRotations() {
    return structureModifier.withType(
            MinecraftReflection.getMinecraftClass("world.entity.PositionMoveRotation"),
            WrappedPositionMoveRotation.getConverter());
}
```

### 3 — Use it in the packet wrapper

```java
public WrappedPositionMoveRotation getChange() {
    return handle.getPositionMoveRotations().read(0);
}

public void setChange(WrappedPositionMoveRotation change) {
    handle.getPositionMoveRotations().write(0, change);
}
```

---

## Unit tests

Each wrapper class gets its own test file named `<WrapperClassName>Test.java`, co-located with the wrapper under `src/test/java/net/dmulloy2/protocol/wrappers/<phase>/<direction>/`. Do not combine tests for multiple wrappers in a single file.

Every test class must call `BukkitInitialization.initializeAll()` in a `@BeforeAll` method — this bootstraps the Minecraft registry and mocked Bukkit server required for `PacketContainer` and NMS reflection to work.

Cover these scenarios for each wrapper:

* testAllArgsCreate
  * Construct a fresh wrapper using the all-args constructor with known test values
  * Get the NMS handle packet via `getHandle()`
  * Assert that all fields in the NMS handle match the known test values
* testNoArgsCreate
    * Construct a fresh wrapper using the no-args constructor
    * Get the NMS handle packet via `getHandle()`
    * Assert that all fields in the NMS handle are not null
* testModifyExistingPacket
  * Create the NMS handle packet directly from its constructor, passing known test values
  * Create a packet container from the NMS handle via `PacketContainer.fromPacket()`
  * Construct the wrapper using the packet container
  * Assert that all fields in the wrapper match the known test values
  * Change all fields in the wrapper to new test values
  * Verify that the NMS handle packet reflects the new test values
* testWrongPacketTypeThrows
  * Create a packet container for a different packet type
  * Attempt to construct the wrapper using this incorrect packet container
  * Assert that an appropriate exception is thrown (e.g. IllegalArgumentException)

### Appropriate test values

* For numeric fields, use a mix of positive, negative, and zero values (e.g. -5, 0, 3)
* For enum fields, prefer the 2nd or 3rd constant rather than the first, to ensure the wrapper correctly handles non-default enum values
  * Additionally, if the enum constant is @Deprecated, do not use it

Here's a good example of a complete test class for `WrappedClientboundSetExperiencePacket`:

```java
class WrappedClientboundSetExperiencePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetExperiencePacket w = new WrappedClientboundSetExperiencePacket(0.75f, 350, 12);

        assertEquals(PacketType.Play.Server.EXPERIENCE, w.getHandle().getType());

        ClientboundSetExperiencePacket p = (ClientboundSetExperiencePacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.getExperienceProgress(), 1e-4f);
        assertEquals(350,   p.getTotalExperience());
        assertEquals(12,    p.getExperienceLevel());
    }
    
    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetExperiencePacket w = new WrappedClientboundSetExperiencePacket();

        assertEquals(PacketType.Play.Server.EXPERIENCE, w.getHandle().getType());

        ClientboundSetExperiencePacket p = (ClientboundSetExperiencePacket) w.getHandle().getHandle();

        assertEquals(0.0f, p.getExperienceProgress(), 1e-4f);
        assertEquals(0,    p.getTotalExperience());
        assertEquals(0,    p.getExperienceLevel());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetExperiencePacket nmsPacket = new ClientboundSetExperiencePacket(
                0.5f, 100, 5
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetExperiencePacket wrapper = new WrappedClientboundSetExperiencePacket(container);

        assertEquals(0.5f, wrapper.getExperienceProgress(), 1e-4f);
        assertEquals(100, wrapper.getTotalExperience());
        assertEquals(5,   wrapper.getExperienceLevel());
        
        wrapper.setExperienceProgress(0.25f);
        wrapper.setTotalExperience(200);
        wrapper.setExperienceLevel(10);
        
        assertEquals(0.25f, nmsPacket.getExperienceProgress(), 1e-4f);
        assertEquals(200, nmsPacket.getTotalExperience());
        assertEquals(10, nmsPacket.getExperienceLevel());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetExperiencePacket nmsPacket = new ClientboundSetExperiencePacket(
                0.5f, 100, 5
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetExperiencePacket wrapper = new WrappedClientboundSetExperiencePacket(container);

        wrapper.setTotalExperience(200);

        assertEquals(0.5f, wrapper.getExperienceProgress(), 1e-4f);
        assertEquals(200, wrapper.getTotalExperience());
        assertEquals(5,   wrapper.getExperienceLevel());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetExperiencePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
```
