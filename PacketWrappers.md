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

## AbstractPacket

Every wrapper extends `AbstractPacket`:

```java
public class WrappedClientboundTeleportEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_TELEPORT;

    /** Create a fresh outgoing packet. */
    public WrappedClientboundTeleportEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    /** Wrap a packet received from ProtocolLib. */
    public WrappedClientboundTeleportEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
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

Cover three scenarios for each wrapper:

| Scenario | What to assert                                                                                                                                 |
|---|------------------------------------------------------------------------------------------------------------------------------------------------|
| **Creation** | Construct a fresh wrapper, set **every** field to a known value, read back and verify against known value                                      |
| **Reading** | Construct the raw NMS packet with known values, construct a wrapper from it, and verify that the reads from the wrapper equal the known values |
| **Modification** | Wrap an existing NMS packet, change fields through a wrapper sharing the same handle, verify the actual packet sees the change                 |
| **Different Packet** | Assert that passing a packet with the wrong `PacketType` to the wrapping constructor throws `IllegalArgumentException`. |

Here's a good example of a complete test class for `WrappedClientboundSetExperiencePacket`:

```java
class WrappedClientboundSetExperiencePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetExperiencePacket w = new WrappedClientboundSetExperiencePacket();
        w.setExperienceProgress(0.75f);
        w.setTotalExperience(350);
        w.setExperienceLevel(12);

        assertEquals(PacketType.Play.Server.EXPERIENCE, w.getHandle().getType());

        ClientboundSetExperiencePacket p = (ClientboundSetExperiencePacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.getExperienceProgress(), 1e-4f);
        assertEquals(350,   p.getTotalExperience());
        assertEquals(12,    p.getExperienceLevel());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetExperiencePacket nmsPacket = new ClientboundSetExperiencePacket(
                0.5f, 100, 5
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetExperiencePacket wrapper = new WrappedClientboundSetExperiencePacket(container);

        assertEquals(0.5f, wrapper.getExperienceProgress(), 1e-4f);
        assertEquals(100, wrapper.getTotalExperience());
        assertEquals(5,   wrapper.getExperienceLevel());
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
