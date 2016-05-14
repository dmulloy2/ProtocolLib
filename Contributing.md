Contributing to ProtocolLib
===========================
This page will detail specific things that must be done if you intend to contribute to this project.

## Updating ProtocolLib to a New Protocol Version
#### Before we get started:
1. **When do we need to update the protocol version?**
    _A Minecraft protocol version is a number for each specification of the Minecraft protocol.
    This means it is incremented when there is a change to the [Protocol Specification](http://wiki.vg/Protocol),
    but not necessarily for each Minecraft version update. If the latest Minecraft version contains a protocol change,
    that means ProtocolLib needs to be updated._
2. **When do we need to update other parts of ProtocolLib?**
    _When the package version guard changes. The package version guard is the version string inserted
    into the package identifier for CraftBukkit and net.minecraft.server (e.g. v1_9_R2). It is incremented
    by Spigot if there is a major breaking change in the internal server code. This is to encourage plugins
    that interact directly with CraftBukkit/nms code to update more specifically. However, since most of
    ProtocolLib uses reflection, and the structure of packet classes is generally static, one will have
    to go through the code and change imports from nms/CraftBukkit._

#### Ready? Let's get started!
1. Read the [Protocol Changes](http://wiki.vg/Protocol_History). Always make sure the list is both
complete and correct. If you're unsure, don't hesitate to ask in #mcdevs (the people who maintain wiki.vg) on [freenode.net](http://freenode.net)
or #spigot on [irc.spi.gt](http://irc.spi.gt) ([webchat](https://irc.spi.gt/iris/?channels=spigot)).
2. Search for usages of the now-defunct NMS package guard and change them.
3. The class `com.comphenix.protocol.PacketType` contains a list of all the packets. If any packets were added or removed
(or had their ID changed), make sure to update this list. If a packet was removed
in favor of usage of another packet, instead of removing it, move it to the bottom of the list
in its section, add a deprecation warning to it, and redirect it to the packet that replaced it.
4. `mvn` in the root directory to build the project.
5. If tests fail in the maven build, go through the tests to make sure you removed references to any removed packets
and changed the NMS version guards.
6. Increment the package version in `com.comphenix.protocol.utility.Constants`.
7. `com.comphenix.protocol.ProtocolLibrary` contains several constants that must be updated, including the Minecraft version
and the release date.
8. `com.comphenix.protocol.utility.MinecraftProtocolVersion` contains a map of all the protocol version integers.
If the protocol version has been incremented, add a new line to the map.
9. `mvn` in root directory again. If it builds successfully, test on the appropriate version of a Spigot server. If
the build fails, debug!