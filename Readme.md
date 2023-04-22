# ProtocolLib

Certain tasks are impossible to perform with the standard Bukkit API, and may require
working with and even modifying Minecraft directly. A common technique is to modify incoming
and outgoing [packets](https://www.wiki.vg/Protocol), or inject custom packets into the
stream. This is quite cumbersome to do, however, and most implementations will break
as soon as a new version of Minecraft has been released, mostly due to obfuscation.

Critically, different plugins that use this approach may _hook_ into the same classes,
with unpredictable outcomes. More than often this causes plugins to crash, but it may also
lead to more subtle bugs.

Currently maintained by dmulloy2 on behalf of [Spigot](https://www.spigotmc.org/).

### Resources

* [Resource Page](https://www.spigotmc.org/resources/protocollib.1997/)
* [Dev Builds](https://ci.dmulloy2.net/job/ProtocolLib)
* [JavaDoc](https://ci.dmulloy2.net/job/ProtocolLib/javadoc/index.html)

### Compilation

ProtocolLib is built with [Gradle](https://gradle.org/). If you have it installed, just run
`./gradlew build` in the root project folder. Other gradle targets you may be interested in 
include `clean`, `test`, and `shadowJar`. `shadowJar` will create a jar with all dependencies
(ByteBuddy) included.

### A new API

__ProtocolLib__ attempts to solve this problem by providing an event API, much like Bukkit,
that allows plugins to monitor, modify, or cancel packets sent and received. But, more importantly,
the API also hides all the gritty, obfuscated classes with a simple index based read/write system.
You no longer have to reference CraftBukkit!

### Using ProtocolLib

To use this library, first add ProtocolLib.jar to your Java build path. Then, add ProtocolLib
as a dependency or soft dependency to your plugin.yml file like any other plugin:

````yml
depend: [ ProtocolLib ]
````

You can also add ProtocolLib as a Maven dependency:

````xml
<repositories>
  <repository>
    <id>dmulloy2-repo</id>
    <url>https://repo.dmulloy2.net/repository/public/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.comphenix.protocol</groupId>
    <artifactId>ProtocolLib</artifactId>
    <version>5.0.0-SNAPSHOT</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
````

Or use the maven dependency with gradle:

```gradle
repositories {
    maven { url "https://repo.dmulloy2.net/repository/public/" }
}

dependencies {
    compileOnly 'com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT'
}
```

Then get a reference to ProtocolManager in onLoad() or onEnable() and you're good to go.

````java
private ProtocolManager protocolManager;

public void onLoad() {
    protocolManager = ProtocolLibrary.getProtocolManager();
}
````

To listen for packets sent by the server to a client, add a server-side listener:

````java
// Disable all sound effects
protocolManager.addPacketListener(new PacketAdapter(
    this,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.NAMED_SOUND_EFFECT
) {
    @Override
    public void onPacketSending(PacketEvent event) {
        event.setCancelled(true);
    }
});
````

It's also possible to read and modify the content of these packets. For instance, you can create a global
censor by listening for Packet3Chat events:

````java
// Censor
protocolManager.addPacketListener(new PacketAdapter(
    this,
    ListenerPriority.NORMAL,
    PacketType.Play.Client.CHAT
) {
    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        String message = packet.getStrings().read(0);

        if (message.contains("shit") || message.contains("damn")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Bad manners!");
        }
    }
});
````

### Sending packets

Normally, you might have to do something ugly like the following:

````java
PacketPlayOutExplosion fakeExplosion = new PacketPlayOutExplosion(
    player.getLocation().getX(),
    player.getLocation().getY(),
    player.getLocation().getZ(),
    3.0F,
    new ArrayList<>(),
    new Vec3D(
        player.getVelocity().getX() + 1,
        player.getVelocity().getY() + 1,
        player.getVelocity().getZ() + 1
    )
);

((CraftPlayer) player).getHandle().b.a(fakeExplosion);
````

But with ProtocolLib, you can turn that into something more manageable:

````java
PacketContainer fakeExplosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
fakeExplosion.getDoubles()
    .write(0, player.getLocation().getX())
    .write(1, player.getLocation().getY())
    .write(2, player.getLocation().getZ());
fakeExplosion.getFloat().write(0, 3.0F);
fakeExplosion.getBlockPositionCollectionModifier().write(0, new ArrayList<>());
fakeExplosion.getVectors().write(0, player.getVelocity().add(new Vector(1, 1, 1)));

protocolManager.sendServerPacket(player, fakeExplosion);
````

### Compatibility

One of the main goals of this project was to achieve maximum compatibility with CraftBukkit. And the end
result is quite flexible. It's likely that I won't have to update ProtocolLib for anything but bug fixes
and new features.

How is this possible? It all comes down to reflection in the end. Essentially, no name is hard coded -
every field, method and class is deduced by looking at field types, package names or parameter
types. It's remarkably consistent across different versions.
