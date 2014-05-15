ProtocolLib 
===========

Certain tasks are impossible to perform with the standard Bukkit API, and may require 
working with and even modify Minecraft directly. A common technique is to modify incoming 
and outgoing [packets](http://www.wiki.vg/Protocol), or inject custom packets into the
stream. This is quite cumbersome to do, however, and most implementations will break 
as soon as a new version of Minecraft has been released, mostly due to obfuscation.

Critically, different plugins that use this approach may _hook_ into the same classes, 
with unpredictable outcomes. More than often this causes plugins to crash, but it may also 
lead to more subtle bugs.


### Resources

* [JavaDoc](http://aadnk.github.com/ProtocolLib/Javadoc/) 


Building
--------
You can compile this project yourself by using the latest version of Maven.


A new API
---------

__ProtocolLib__ attempts to solve this problem by providing a event API, much like Bukkit, 
that allow plugins to monitor, modify or cancel packets sent and received. But more importantly, 
the API also hides all the gritty, obfuscated classes with a simple index based read/write system. 
You no longer have to reference CraftBukkit!


### Using ProtocolLib

To use the library, first add ProtocolLib.jar to your Java build path. Then, add ProtocolLib
as a dependency (or soft-dependency, if you can live without it) to your plugin.yml file:

````yml
depends: [ProtocolLib]
````

Future versions will be available in a public Maven repository, possibly on Maven central. But it
will always be possible to reference ProtocolLib manually.

Then get a reference to ProtocolManager in onLoad() and you're good to go.

````java
private ProtocolManager protocolManager;

public void onLoad() {
    protocolManager = ProtocolLibrary.getProtocolManager();
}
````

To listen for packets sent by the server to a client, add a server-side listener:

````java
// Disable all sound effects
protocolManager.addPacketListener(
  new PacketAdapter(this, ListenerPriority.NORMAL, 
          PacketType.Play.Server.NAMED_SOUND_EFFECT) {
    @Override
    public void onPacketSending(PacketEvent event) {
        // Item packets (id: 0x29)
        if (event.getPacketType() == 
                PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            event.setCancelled(true);
        }
    }
});
````

It's also possible to read and modify the content of these packets. For instance, you can create a global
censor by listening for Packet3Chat events:

````java
// Censor
protocolManager.addPacketListener(new PacketAdapter(this,
        ListenerPriority.NORMAL, 
        PacketType.Play.Client.CHAT) {
    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CHAT) {
            PacketContainer packet = event.getPacket();
            String message = packet.getStrings().read(0);

            if (message.contains("shit")
                    || message.contains("damn")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Bad manners!");
            }
        }
    }
});
````

### Sending packets

Normally, you might have to do something ugly like the following:

````java
Packet60Explosion fakeExplosion = new Packet60Explosion();
	
fakeExplosion.a = player.getLocation().getX();
fakeExplosion.b = player.getLocation().getY();
fakeExplosion.c = player.getLocation().getZ();
fakeExplosion.d = 3.0F;
fakeExplosion.e = new ArrayList<Object>();

((CraftPlayer) player).getHandle().netServerHandler.sendPacket(fakeExplosion);
````

But with ProtocolLib, you can turn that into something more manageable. Notice that 
you don't have to create an ArrayList with this version:

````java

fakeExplosion.getDoubles().
    write(0, player.getLocation().getX()).
    write(1, player.getLocation().getY()).
    write(2, player.getLocation().getZ());
fakeExplosion.getFloat().write(0, 3.0F);

try {
    protocolManager.sendServerPacket(player, fakeExplosion);
} catch (InvocationTargetException e) {
    throw new RuntimeException(
        "Cannot send packet " + fakeExplosion, e);
}
````

Compatiblity
------------

One of the main goals of this project was to achieve maximum compatibility with CraftBukkit. And the end
result is quite flexible - in tests I successfully ran an unmodified ProtocolLib on CraftBukkit 1.8.0, and
it should be resiliant against future changes. It's likely that I won't have to update ProtocolLib for
anything but bug and performance fixes. 

How is this possible? It all comes down to reflection in the end. Essentially, no name is hard coded - 
every field, method and class is deduced by looking at field types, package names or parameter
types. It's remarkably consistent across different versions.


### Incompatiblity

The following plugins (to be expanded) are not compatible with ProtocolLib:
