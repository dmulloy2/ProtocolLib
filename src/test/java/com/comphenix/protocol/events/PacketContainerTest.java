/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2012 Kristian S.
 * Stangeland
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.events;

import static com.comphenix.protocol.utility.TestUtils.assertItemCollectionsEqual;
import static com.comphenix.protocol.utility.TestUtils.assertItemsEqual;
import static com.comphenix.protocol.utility.TestUtils.equivalentItem;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.cloning.SerializableCloner;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ComponentConverter;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.SoundCategory;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import com.comphenix.protocol.wrappers.WrappedRemoteChatSessionData;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket.AttributeSnapshot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.frog.FrogVariants;

public class PacketContainerTest {

    private static BaseComponent[] TEST_COMPONENT;
    // Helper converters
    private final EquivalentConverter<WrappedDataWatcher> watchConvert = BukkitConverters.getDataWatcherConverter();
    private final EquivalentConverter<ItemStack> itemConvert = BukkitConverters.getItemStackConverter();

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();

        TEST_COMPONENT = new ComponentBuilder("Hit or miss?")
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://reddit.com"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("The \"front page\" of the internet")))
                .append("I guess they never miss, huh?")
                .create();
    }

    private <T> void testPrimitive(StructureModifier<T> modifier, int index, T initialValue, T testValue) {
        // Check initial value
        assertEquals(initialValue, modifier.read(index));

        // Test assignment
        modifier.write(index, testValue);
        assertEquals(testValue, modifier.read(0));
    }

    private <T> void testObjectArray(StructureModifier<T[]> modifier, int index, T[] initialValue, T[] testValue) {
        // Check initial value
        assertEquals(modifier.read(index).length, initialValue.length);
        modifier.writeDefaults();

        // Test initial
        assertArrayEquals(initialValue, modifier.read(index));

        // Test assignment
        modifier.write(index, testValue);
        assertArrayEquals(testValue, modifier.read(0));
    }

    @Test
    public void testGetByteArrays() {
        // Contains a byte array we will test
        PacketContainer customPayload = new PacketContainer(PacketType.Login.Client.ENCRYPTION_BEGIN);
        StructureModifier<byte[]> bytes = customPayload.getByteArrays();
        byte[] testArray = new byte[]{1, 2, 3};

        // It's NULL at first
        // assertEquals(null, bytes.read(0));
        customPayload.getModifier().writeDefaults();

        // Then it should create an empty array
        assertArrayEquals(new byte[0], bytes.read(0));

        // Check and see if we can write to it
        bytes.write(0, testArray);
        assertArrayEquals(testArray, bytes.read(0));
    }

    @Test
    public void testGetShorts() {
        PacketContainer itemData = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
        this.testPrimitive(itemData.getShorts(), 0, (short) 0, (short) 1);
    }

    @Test
    public void testGetIntegers() {
        PacketContainer updateSign = new PacketContainer(PacketType.Play.Client.CLOSE_WINDOW);
        this.testPrimitive(updateSign.getIntegers(), 0, 0, 1);
    }

    @Test
    public void testGetLongs() {
        PacketContainer updateTime = new PacketContainer(PacketType.Play.Server.UPDATE_TIME);
        this.testPrimitive(updateTime.getLongs(), 0, (long) 0, (long) 1);
    }

    // @Test // TODO: Explosion no longer contains floats
    public void testGetFloat() {
        PacketContainer explosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
        this.testPrimitive(explosion.getFloat(), 0, (float) 0, (float) 0.8);
    }

    // @Test // TODO: Explosion no longer contains doubles
    public void testGetDoubles() {
        PacketContainer explosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
        this.testPrimitive(explosion.getDoubles(), 0, (double) 0, 0.8);
    }

    @Test
    public void testGetStrings() {
        PacketContainer explosion = new PacketContainer(PacketType.Play.Client.CHAT);
        this.testPrimitive(explosion.getStrings(), 0, "", "hello");
    }

    @Test
    @Disabled // TODO
    public void testGetStringArrays() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Client.UPDATE_SIGN);
        this.testObjectArray(packet.getStringArrays(), 0,
                new String[]{"", "", "", ""},
                new String[]{"hello", "world"}
        );
    }

    @Test
    public void testGetIntegerArrays() {
        // Contains a byte array we will test
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        StructureModifier<int[]> integers = packet.getIntegerArrays();
        int[] testArray = new int[]{1, 2, 3};

        assertArrayEquals(new int[0], integers.read(0));

        integers.write(0, testArray);
        assertArrayEquals(testArray, integers.read(0));
    }

    @Test
    public void testGetItemModifier() {
        PacketContainer windowClick = new PacketContainer(PacketType.Play.Server.SET_CURSOR_ITEM);

        ItemStack item = this.itemWithData();

        StructureModifier<ItemStack> items = windowClick.getItemModifier();
        // assertNull(items.read(0));

        // Insert the item and check if it's there
        items.write(0, item);
        assertTrue(equivalentItem(item, items.read(0)), "Item " + item + " != " + items.read(0));
    }

    private ItemStack itemWithData() {
        ItemStack item = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Green Wool");
        meta.setLore(Lists.newArrayList(ChatColor.WHITE + "This is lore."));
        item.setItemMeta(meta);
        return item;
    }

    @Test
    public void testGetItemListModifier() {
        PacketContainer windowItems = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
        StructureModifier<List<ItemStack>> itemAccess = windowItems.getItemListModifier();

        List<ItemStack> items = new ArrayList<>();
        items.add(this.itemWithData());
        items.add(new ItemStack(Material.DIAMOND_AXE));

        assertEquals(itemAccess.read(0).size(), 0);

        // Insert and check that it was succesful
        itemAccess.write(0, items);

        // Read back array
        List<ItemStack> comparison = itemAccess.read(0);
        assertItemCollectionsEqual(items, comparison);
    }

    @Test
    public void testGetNbtModifier() {
        PacketContainer updateTileEntity = new PacketContainer(PacketType.Play.Server.TILE_ENTITY_DATA);

        NbtCompound compound = NbtFactory.ofCompound("test");
        compound.put("test", "name");
        compound.put(NbtFactory.ofList("ages", 1, 2, 3));

        updateTileEntity.getNbtModifier().write(0, compound);

        NbtCompound result = (NbtCompound) updateTileEntity.getNbtModifier().read(0);

        assertEquals(compound.getString("test"), result.getString("test"));
        assertEquals(compound.getList("ages"), result.getList("ages"));
    }

    // TODO They removed DataWatchers from packets, it's all entity metadata packets now
    /* @Test
    public void testGetDataWatcherModifier() {
        PacketContainer mobSpawnPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        StructureModifier<WrappedDataWatcher> watcherAccessor = mobSpawnPacket.getDataWatcherModifier();

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setObject(new WrappedDataWatcherObject(1, Registry.get(Byte.class)), (byte) 1);
        dataWatcher.setObject(new WrappedDataWatcherObject(2, Registry.get(String.class)), "Lorem");
        dataWatcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), true);
        dataWatcher.setObject(new WrappedDataWatcherObject(4, Registry.getUUIDSerializer(true)), Optional.of(UUID.randomUUID()));

        assertNull(watcherAccessor.read(0));

        // Insert and read back
        watcherAccessor.write(0, dataWatcher);
        assertEquals(dataWatcher, watcherAccessor.read(0));
    } */

    // Unfortunately, it might be too difficult to mock this one
    //
    //  @Test
    //  public void testGetEntityModifier() { }

    // No packet expose this type directly.
    //
    //  @Test
    //  public void testGetPositionModifier() { }

    @Test
    public void testEntityTypeModifier() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getEntityTypeModifier().write(0, EntityType.ARROW);
        assertEquals(packet.getEntityTypeModifier().read(0), EntityType.ARROW);
    }

    // @Test // TODO: explosion no longer contains block position list
    public void testGetPositionCollectionModifier() {
        PacketContainer explosionPacket = new PacketContainer(PacketType.Play.Server.EXPLOSION);
        StructureModifier<List<BlockPosition>> positionAccessor = explosionPacket.getBlockPositionCollectionModifier();

        assertEquals(positionAccessor.read(0).size(), 0);

        List<BlockPosition> positions = new ArrayList<>();
        positions.add(new BlockPosition(1, 2, 3));
        positions.add(new BlockPosition(3, 4, 5));

        // Insert and read back
        positionAccessor.write(0, positions);
        List<BlockPosition> cloned = positionAccessor.read(0);

        assertEquals(positions, cloned);
    }

    @Test
    // @Disabled // TODO -- handle type is null
    public void testGetDataValueCollectionModifier() {
        PacketContainer entityMetadata = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        StructureModifier<List<WrappedDataValue>> watchableAccessor = entityMetadata.getDataValueCollectionModifier();

        assertEquals(0, watchableAccessor.read(0).size());

        List<WrappedDataValue> values = Lists.newArrayList(
                new WrappedDataValue(0, Registry.get(Byte.class), (byte) 21),
                new WrappedDataValue(1, Registry.get(String.class), "World"));

        // Insert and read back
        watchableAccessor.write(0, values);
        assertEquals(values, watchableAccessor.read(0));
    }

    @Test
    public void testGameProfiles() {
        PacketContainer spawnEntity = new PacketContainer(PacketType.Login.Server.SUCCESS);
        WrappedGameProfile profile = new WrappedGameProfile(UUID.fromString("d7047a08-3150-4aa8-a2f2-7c1e2b17e298"),
                "name");
        spawnEntity.getGameProfiles().write(0, profile);

        assertEquals(profile, spawnEntity.getGameProfiles().read(0));
    }

    @Test
    public void testChatComponents() {
        PacketContainer chatPacket = new PacketContainer(PacketType.Login.Server.DISCONNECT);
        chatPacket.getChatComponents().write(0,
                WrappedChatComponent.fromChatMessage("You shall not " + ChatColor.ITALIC + "pass!")[0]);

        assertEquals("{\"text\":\"\",\"extra\":[\"You shall not \",{\"text\":\"pass!\",\"italic\":true}]}",
                chatPacket.getChatComponents().read(0).getJson());
    }

    @Test
    @Disabled // TODO
    public void testSerialization() {
        PacketContainer useItem = new PacketContainer(PacketType.Play.Client.USE_ITEM);
        useItem.getMovingBlockPositions().write(0, new MovingObjectPositionBlock(
                new BlockPosition(0, 1, 0),
                new Vector(0, 1, 0),
                Direction.DOWN,
                false));
        useItem.getHands().write(0, Hand.MAIN_HAND);
        useItem.getIntegers().write(0, 5);
        useItem.getLongs().write(0, System.currentTimeMillis());

        PacketContainer copy = (PacketContainer) SerializationUtils.clone(useItem);

        assertEquals(PacketType.Play.Client.USE_ITEM, copy.getType());
        assertEquals(Hand.MAIN_HAND, copy.getHands().read(0));
        assertEquals(5, copy.getIntegers().read(0));

        MovingObjectPositionBlock pos = copy.getMovingBlockPositions().read(0);
        assertEquals(1, pos.getBlockPosition().getY());
        assertEquals(Direction.DOWN, pos.getDirection());
        assertFalse(pos.isInsideBlock());
    }

    @Test
    @Disabled // TODO -- can't find get payload id
    public void testBigPacketSerialization() {
        PacketContainer payload = new PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD);

        byte[] randomData = new byte[8192];
        ThreadLocalRandom.current().nextBytes(randomData);
        CustomPacketPayloadWrapper payloadWrapper = new CustomPacketPayloadWrapper(randomData, new com.comphenix.protocol.wrappers.MinecraftKey("test"));
        payload.getCustomPacketPayloads().write(0, payloadWrapper);

        PacketContainer cloned = SerializableCloner.clone(payload);
        Assertions.assertNotSame(payload, cloned);
    }

	/*
    @Test
    public void testUnknownPayloadDeserialize() {
        MinecraftKey id = new MinecraftKey("test");
        byte[] payloadData = new byte[]{0x00, 0x01, 0x05, 0x07};
        ByteBuf buffer = Unpooled.wrappedBuffer(payloadData);
        ServerboundCustomPayloadPacket.UnknownPayload payload = new ServerboundCustomPayloadPacket.UnknownPayload(id, buffer);
        ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket(payload);

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Client.CUSTOM_PAYLOAD, packet);
        CustomPacketPayloadWrapper payloadWrapper = packetContainer.getCustomPacketPayloads().read(0);

        com.comphenix.protocol.wrappers.MinecraftKey key = payloadWrapper.getId();
        Assertions.assertEquals("minecraft", key.getPrefix());
        Assertions.assertEquals("test", key.getKey());
        Assertions.assertArrayEquals(payloadData, payloadWrapper.getPayload());
    }
    */

	/*
    @Test
    public void testCustomPayloadPacket() {
        byte[] customPayload = "Hello World, This is A Super-Cool-Test!!!!!".getBytes(StandardCharsets.UTF_8);
        com.comphenix.protocol.wrappers.MinecraftKey key = new com.comphenix.protocol.wrappers.MinecraftKey("protocollib", "test");
        CustomPacketPayloadWrapper payloadWrapper = new CustomPacketPayloadWrapper(customPayload, key);

        PacketContainer container = new PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD);
        container.getCustomPacketPayloads().write(0, payloadWrapper);

        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        ClientboundCustomPayloadPacket constructedHandle = (ClientboundCustomPayloadPacket) container.getHandle();
        constructedHandle.a(serializer);

        ServerboundCustomPayloadPacket deserializedHandle = new ServerboundCustomPayloadPacket(serializer);
        PacketContainer serverContainer = new PacketContainer(PacketType.Play.Client.CUSTOM_PAYLOAD, deserializedHandle);

        CustomPacketPayloadWrapper deserializedPayloadWrapper = serverContainer.getCustomPacketPayloads().read(0);
        Assertions.assertEquals(key, deserializedPayloadWrapper.getId());
        Assertions.assertArrayEquals(customPayload, deserializedPayloadWrapper.getPayload());
    }
    */

	/*
    @Test
    public void testSomeCustomPayloadRead() {
        BrandPayload payload = new BrandPayload("Hello World!");
        ClientboundCustomPayloadPacket handle = new ClientboundCustomPayloadPacket(payload);

        PacketContainer container = new PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD, handle);
        CustomPacketPayloadWrapper payloadWrapper = container.getCustomPacketPayloads().read(0);

        com.comphenix.protocol.wrappers.MinecraftKey payloadId = payloadWrapper.getId();
        Assertions.assertEquals(BrandPayload.a.toString(), payloadId.getFullKey());

        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.wrappedBuffer(payloadWrapper.getPayload()));
        BrandPayload deserializedPayload = new BrandPayload(serializer);
        Assertions.assertEquals(payload.b(), deserializedPayload.b());
    }
    */

	/*
    @Test
    public void testUnknownPayloadNotReleasedOnRead() {
        MinecraftKey id = new MinecraftKey("plib", "main");
        ByteBuf data = Unpooled.wrappedBuffer("This is a Test!!".getBytes(StandardCharsets.UTF_8));
        ServerboundCustomPayloadPacket.UnknownPayload payload = new ServerboundCustomPayloadPacket.UnknownPayload(id, data);
        ServerboundCustomPayloadPacket handle = new ServerboundCustomPayloadPacket(payload);

        PacketContainer container = new PacketContainer(PacketType.Play.Client.CUSTOM_PAYLOAD, handle);
        CustomPacketPayloadWrapper payloadWrapper = container.getCustomPacketPayloads().read(0);

        Assertions.assertEquals(id.toString(), payloadWrapper.getId().getFullKey());
        Assertions.assertEquals("This is a Test!!", new String(payloadWrapper.getPayload()));
        Assertions.assertEquals(1, payload.data().refCnt());
        Assertions.assertEquals(0, payload.data().readerIndex());
    }
	 */

    @Test
    public void testIntList() {
        PacketContainer destroy = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroy.getIntLists().write(0, new ArrayList<Integer>() {{
            this.add(420);
            this.add(69);
        }});
        List<Integer> back = destroy.getIntLists().read(0);
        assertEquals(back.size(), 2);
        assertEquals((int) back.get(0), 420);
        assertEquals((int) back.get(1), 69);
    }

    @Test
    @Disabled // TODO -- cloning fails
    public void testAttributeList() {
        PacketContainer attribute = new PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES);
        attribute.getIntegers().write(0, 123); // Entity ID

        // Initialize some test data
        List<AttributeModifier> modifiers = Lists.newArrayList(
                new AttributeModifier(Identifier.parse("protocollib:test"),10, Operation.ADD_VALUE));

        // Obtain an AttributeSnapshot instance. This is complicated by the fact that AttributeSnapshots
        // are inner classes (which is ultimately pointless because AttributeSnapshots don't access any
        // members of the packet itself)
        ClientboundUpdateAttributesPacket packet = (ClientboundUpdateAttributesPacket) attribute.getHandle();
		net.minecraft.core.Registry<Attribute> registry = BuiltInRegistries.ATTRIBUTE;
        Attribute base = registry.getValue(Identifier.parse("max_health"));
        AttributeSnapshot snapshot = new AttributeSnapshot(registry.wrapAsHolder(base), 20.0D, modifiers);
        attribute.getSpecificModifier(List.class).write(0, Lists.newArrayList(snapshot));

        PacketContainer cloned = attribute.deepClone();
        AttributeSnapshot
                clonedSnapshot = (AttributeSnapshot) cloned.getSpecificModifier(List.class).read(0).get(0);

        // Compare the fields, because apparently the packet is a field in AttributeSnapshot
        for (Field field : AttributeSnapshot.class.getDeclaredFields()) {
            try {
                // Skip the packet
                if (field.getType().equals(packet.getClass())) {
                    continue;
                }

                field.setAccessible(true);
                this.testEquality(field.get(snapshot), field.get(clonedSnapshot));
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void testBlocks() {
        PacketContainer blockAction = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
        blockAction.getBlocks().write(0, Material.STONE);

        assertEquals(Material.STONE, blockAction.getBlocks().read(0));
    }

    @Test
    public void testBlockData() {
        PacketContainer blockChange = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);

        Material material = Material.GLOWSTONE;
        WrappedBlockData data = WrappedBlockData.createData(material);
        blockChange.getBlockData().write(0, data);

        WrappedBlockData read = blockChange.getBlockData().read(0);
        assertEquals(material, read.getType());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testPotionEffect() {
        PotionEffect effect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60, 1);
        MobEffectInstance mobEffect = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, effect.getDuration(),
                effect.getAmplifier(), effect.isAmbient(),
                effect.hasParticles());
        int entityId = 42;

        // The constructor we want to call
        PacketConstructor creator = PacketConstructor.DEFAULT.withPacket(
                PacketType.Play.Server.ENTITY_EFFECT, new Class<?>[] { int.class, MobEffectInstance.class, boolean.class });
        PacketContainer packet = creator.createPacket(entityId, mobEffect, true);

        assertEquals(entityId, packet.getIntegers().read(0));
        assertEquals(effect.getAmplifier(), packet.getIntegers().read(1));
        assertEquals(effect.getDuration(), packet.getIntegers().read(2));

        WrappedRegistry registry = WrappedRegistry.getRegistry(MinecraftReflection.getMobEffectListClass());

        Object effectList = assertInstanceOf(
            MobEffect.class,
            packet.getHolders(MobEffect.class, InternalStructure.CONVERTER).read(0).getHandle()
        );

        assertEquals(effect.getType().getId(), registry.getId(effectList) + 1); // +1 is correct, see CraftPotionEffectType

        byte b = 0;
        if (effect.isAmbient()) {
            b |= 1;
        }
        if (effect.hasParticles()) {
            b |= 2;
        }
        if (effect.hasIcon()) {
            b |= 4;
        }

        b |= 8;

        assertEquals(b, (byte) packet.getBytes().read(0));
    }

    @Test
    public void testPlayerAction() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.ENTITY_ACTION);

        // no change across nms versions
        container.getPlayerActions().write(0, EnumWrappers.PlayerAction.OPEN_INVENTORY);
        assertEquals(container.getPlayerActions().read(0), EnumWrappers.PlayerAction.OPEN_INVENTORY);
    }

    @Test
    public void testMobEffectList() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
        container.getEffectTypes().write(0, PotionEffectType.GLOWING);

        assertEquals(container.getEffectTypes().read(0), PotionEffectType.GLOWING);
    }

    @Test
    public void testSoundCategory() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
        container.getSoundCategories().write(0, SoundCategory.PLAYERS);

        assertEquals(SoundCategory.PLAYERS, container.getSoundCategories().read(0));
    }

    @Test
    public void testSoundEffects() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
        container.getSoundEffects().optionRead(0);

        container.getSoundEffects().write(0, Sound.ENTITY_CAT_HISS);

        assertEquals(container.getSoundEffects().read(0), Sound.ENTITY_CAT_HISS);
    }

    // @Test
    public void testGenericEnums() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.BOSS);
        container.getEnumModifier(Action.class, 1).write(0, Action.UPDATE_PCT);

        // assertEquals(container.getEnumModifier(Action.class, PacketPlayOutBoss.d.class).read(0), Action.UPDATE_PCT);
    }

    @Test
    @Disabled // TODO -- need a way to create a structure
    public void testInternalStructures() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        Optional<InternalStructure> optStruct = container.getOptionalStructures().read(0);
        assertTrue(optStruct.isPresent());
        InternalStructure struct = optStruct.get();
        struct.getChatComponents().write(0, WrappedChatComponent.fromText("hi there"));
        container.getOptionalStructures().write(0, Optional.of(struct));

        optStruct = container.getOptionalStructures().read(0);
        assertTrue(optStruct.isPresent());
        struct = optStruct.get();
        this.testEquality(
                struct.getChatComponents().read(0),
                WrappedChatComponent.fromText("hi there")
        );
    }

    // @Test
    public void testDimensions() {
        // TODO this won't work in testing, but hopefully will in live
        PacketContainer container = new PacketContainer(PacketType.Play.Server.RESPAWN);
        container.getDimensions().write(0, 1);
        assertEquals((Object) 1, container.getDimensions().read(0));
    }

    @Test
    public void testEntityEquipment() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> data = new ArrayList<>();
        data.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, new ItemStack(Material.NETHERITE_CHESTPLATE)));
        data.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, new ItemStack(Material.GOLDEN_LEGGINGS)));

        container.getSlotStackPairLists().write(0, data);

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> written = container.getSlotStackPairLists().read(0);
        assertEquals(data, written);
    }

    @Test
    public void testMovingBlockPos() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.USE_ITEM_ON);

        Vector vector = new Vector(0, 1, 2);
        BlockPosition position = new BlockPosition(3, 4, 5);
        EnumWrappers.Direction direction = EnumWrappers.Direction.DOWN;

        MovingObjectPositionBlock movingPos = new MovingObjectPositionBlock(position, vector, direction, true);
        container.getMovingBlockPositions().write(0, movingPos);

        MovingObjectPositionBlock back = container.getMovingBlockPositions().read(0);

        assertEquals(back.getPosVector(), vector);
        assertEquals(back.getBlockPosition(), position);
        assertEquals(back.getDirection(), direction);
        assertTrue(back.isInsideBlock());
    }

    @Test
    public void testMultiBlockChange() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        packet.getShortArrays().writeSafely(0, new short[]{420, 69});
        assertArrayEquals(new short[]{420, 69}, packet.getShortArrays().readSafely(0));

        packet.getBlockDataArrays().writeSafely(0, new WrappedBlockData[]{
                WrappedBlockData.createData(Material.IRON_BARS),
                WrappedBlockData.createData(Material.IRON_BLOCK)
        });
        assertArrayEquals(new WrappedBlockData[]{
                WrappedBlockData.createData(Material.IRON_BARS),
                WrappedBlockData.createData(Material.IRON_BLOCK)
        }, packet.getBlockDataArrays().readSafely(0));

        packet.getSectionPositions().writeSafely(0, new BlockPosition(42, 43, 44));
        assertEquals(new BlockPosition(42, 43, 44), packet.getSectionPositions().readSafely(0));

        PacketContainer clone = packet.deepClone();
        assertNotSame(clone, packet);
    }

    @Test
    public void testGameStateChange() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        assertEquals(0, (int) packet.getGameStateIDs().read(0));

        packet.getGameStateIDs().write(0, 2);
        assertEquals(2, (int) packet.getGameStateIDs().read(0));
    }

    @Test
    public void testUseEntity() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Client.USE_ENTITY);

        WrappedEnumEntityUseAction action;
        WrappedEnumEntityUseAction clone;
        // test attack
        packet.getEnumEntityUseActions().write(0, WrappedEnumEntityUseAction.attack());
        action = packet.getEnumEntityUseActions().read(0);
        // attack's handle should always be the same
        assertEquals(WrappedEnumEntityUseAction.attack(), action);
        assertEquals(EntityUseAction.ATTACK, action.getAction());
        // hand & position should not be available
        assertThrows(IllegalArgumentException.class, action::getHand);
        assertThrows(IllegalArgumentException.class, action::getPosition);
        // test cloning
        clone = action.deepClone();
        assertSame(WrappedEnumEntityUseAction.attack(), clone);

        // test interact
        packet.getEnumEntityUseActions().write(0, WrappedEnumEntityUseAction.interact(Hand.OFF_HAND));
        action = packet.getEnumEntityUseActions().read(0);
        assertEquals(EntityUseAction.INTERACT, action.getAction());
        assertEquals(Hand.OFF_HAND, action.getHand());
        // position should not be available
        assertThrows(IllegalArgumentException.class, action::getPosition);
        // test cloning
        clone = action.deepClone();
        assertEquals(EntityUseAction.INTERACT, clone.getAction());
        assertEquals(Hand.OFF_HAND, clone.getHand());

        // test interact_at
        Vector position = new Vector(1, 199, 4);
        packet.getEnumEntityUseActions().write(0, WrappedEnumEntityUseAction.interactAt(Hand.MAIN_HAND, position));
        action = packet.getEnumEntityUseActions().read(0);
        assertEquals(EntityUseAction.INTERACT_AT, action.getAction());
        assertEquals(Hand.MAIN_HAND, action.getHand());
        assertEquals(position, action.getPosition());
        // test cloning
        clone = action.deepClone();
        assertEquals(EntityUseAction.INTERACT_AT, clone.getAction());
        assertEquals(Hand.MAIN_HAND, clone.getHand());
        assertEquals(position, clone.getPosition());
    }

    @Test
    public void testSetSimulationDistance() {
        // first packet which is a record - set will fail if we missed something during patching
        PacketContainer container = new PacketContainer(PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE);
        container.getIntegers().write(0, 1234);
        assertEquals(1234, (int) container.getIntegers().read(0));
    }

    @Test
    @Disabled // TODO -- can't create MAP_CHUNK packet
    public void testMapChunk() {
        // this is a special case as we are generating a data serializer class (we only need to construct the packet)
        PacketContainer container = new PacketContainer(PacketType.Play.Server.MAP_CHUNK);
        // check if we can read an nbt compound from the class
        assertTrue(container.getStructures().read(0).getNbtModifier().optionRead(0).isPresent());
    }

    @Test
    public void testComponentArrays() {
        PacketContainer signChange = new PacketContainer(PacketType.Play.Server.TILE_ENTITY_DATA);
        WrappedChatComponent[] components = new WrappedChatComponent[]{
                WrappedChatComponent.fromText("hello world"), WrappedChatComponent.fromText(""),
                WrappedChatComponent.fromText(""), WrappedChatComponent.fromText("")
        };
        signChange.getChatComponentArrays().write(0, components);

        WrappedChatComponent[] back = signChange.getChatComponentArrays().read(0);
        assertArrayEquals(components, back);
    }

    @Test
    public void testPlayerInfoActions() {
        PacketContainer updatePlayerInfoActions = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

        EnumSet<EnumWrappers.PlayerInfoAction> actions = EnumSet.of(
                EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
                EnumWrappers.PlayerInfoAction.UPDATE_LISTED);
        updatePlayerInfoActions.getPlayerInfoActions().write(0, actions);

        UUID id = UUID.randomUUID();
        PlayerInfoData data = new PlayerInfoData(
                id,
                20,
                false,
                NativeGameMode.CREATIVE,
                new WrappedGameProfile(new UUID(0, 0), "system"),
                null,
                (WrappedRemoteChatSessionData) null);
        updatePlayerInfoActions.getPlayerInfoDataLists().write(1, Collections.singletonList(data));

        Set<EnumWrappers.PlayerInfoAction> readActions = updatePlayerInfoActions.getPlayerInfoActions().read(0);
        Assertions.assertTrue(readActions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
        Assertions.assertTrue(readActions.contains(EnumWrappers.PlayerInfoAction.UPDATE_LATENCY));
        Assertions.assertTrue(readActions.contains(EnumWrappers.PlayerInfoAction.UPDATE_LISTED));

        Collection<PlayerInfoData> readData = updatePlayerInfoActions.getPlayerInfoDataLists().read(1);
        Assertions.assertEquals(1, readData.size());

        PlayerInfoData firstData = readData.iterator().next();
        Assertions.assertFalse(data.isListed());
        Assertions.assertEquals(id, data.getProfileId());
        Assertions.assertEquals("system", firstData.getProfile().getName());
        Assertions.assertEquals(20, firstData.getLatency());
        Assertions.assertEquals(NativeGameMode.CREATIVE, firstData.getGameMode());
    }

    @Test
    public void testSignedChatMessage() {
        PacketContainer chatPacket = new PacketContainer(PacketType.Play.Client.CHAT);

        byte[] signature = new byte[256];
        WrappedMessageSignature wrappedSignature = new WrappedMessageSignature(signature);
        chatPacket.getMessageSignatures().write(0, wrappedSignature);

        WrappedMessageSignature read = chatPacket.getMessageSignatures().read(0);
        assertArrayEquals(signature, read.getBytes());
    }

    private void assertPacketsEqualAndSerializable(PacketContainer constructed, PacketContainer cloned) {
        StructureModifier<Object> firstMod = constructed.getModifier(), secondMod = cloned.getModifier();
        assertEquals(firstMod.size(), secondMod.size());

        if (PacketType.Status.Server.SERVER_INFO.equals(constructed.getType())) {
            assertArrayEquals(SerializationUtils.serialize(constructed), SerializationUtils.serialize(cloned));
        } else {
            // Make sure all the fields are equivalent
            for (int i = 0; i < firstMod.size(); i++) {
                if (firstMod.getField(i).getType().isArray()) {
                    assertArrayEquals(this.getArray(firstMod.read(i)), this.getArray(secondMod.read(i)));
                } else {
                    this.testEquality(firstMod.read(i), secondMod.read(i));
                }
            }
        }

        Object buffer = MinecraftReflection.createPacketDataSerializer(0);
        MinecraftMethods.getPacketWriteByteBufMethod().invoke(cloned.getHandle(), buffer);
    }

    @Test
    @Disabled // TODO -- cloning is borked
    public void testCloning() {
        // Try constructing all the packets
        for (PacketType type : PacketType.values()) {
            // TODO: try to support chat - for now chat contains to many sub classes to properly clone it
            if (type.isDeprecated() || !type.isSupported() || type.name().contains("CUSTOM_PAYLOAD") || type.name().contains("CHAT")
                || type.name().contains("BUNDLE")) {
                continue;
            }

            try {
                PacketContainer constructed = new PacketContainer(type);

                // Initialize default values
                constructed.getModifier().writeDefaults();

                // Make sure watchable collections can be cloned
                if (type == PacketType.Play.Server.ENTITY_METADATA) {
                    constructed.getDataValueCollectionModifier().write(0, Lists.newArrayList(
                            new WrappedDataValue(0, Registry.get(Byte.class), (byte) 1),
                            new WrappedDataValue(0, Registry.get(Float.class), 5F),
                            new WrappedDataValue(0, Registry.get(String.class), "String"),
                            new WrappedDataValue(0, Registry.get(Boolean.class), true),
                            new WrappedDataValue(
                                    0, 
                                    Registry.getChatComponentSerializer(true), 
                                    Optional.of(ComponentConverter.fromBaseComponent(TEST_COMPONENT).getHandle())),
                            new WrappedDataValue(
                                    0,
                                    Registry.getItemStackSerializer(false),
                                    BukkitConverters.getItemStackConverter().getGeneric(new ItemStack(Material.WOODEN_AXE))),
                            new WrappedDataValue(0, Registry.get(CatVariant.class), CatVariants.RED),
                            new WrappedDataValue(0, Registry.get(FrogVariant.class), FrogVariants.COLD)
                    ));
                } else if (type == PacketType.Play.Server.CHAT || type == PacketType.Login.Server.DISCONNECT) {
                    constructed.getChatComponents().write(0, ComponentConverter.fromBaseComponent(TEST_COMPONENT));
                } else if (type == PacketType.Play.Server.REMOVE_ENTITY_EFFECT || type == PacketType.Play.Server.ENTITY_EFFECT) {
                    constructed.getEffectTypes().write(0, PotionEffectType.GLOWING);
                } else if (type == PacketType.Play.Server.GAME_STATE_CHANGE) {
                    constructed.getStructures().write(
                            0,
                            InternalStructure.getConverter().getSpecific(ClientboundGameEventPacket.WIN_GAME));
                } else if (type == PacketType.Play.Client.USE_ITEM || type == PacketType.Play.Client.BLOCK_PLACE) {
                    constructed.getLongs().write(0, 0L); // timestamp of the packet, not sent over the network
                }

                // gives some indication which cloning process fails as the checks itself are happening outside this method

                // Clone the packet all three ways
                PacketContainer shallowCloned = constructed.shallowClone();
                this.assertPacketsEqualAndSerializable(constructed, shallowCloned);

                PacketContainer deepCloned = constructed.deepClone();
                this.assertPacketsEqualAndSerializable(constructed, deepCloned);

                PacketContainer serializedCloned = SerializableCloner.clone(constructed);
                if (type == PacketType.Play.Client.USE_ITEM || type == PacketType.Play.Client.BLOCK_PLACE) {
                    // shit fix - but what are we supposed to do :/
                    serializedCloned.getLongs().write(0, 0L);
                }
                this.assertPacketsEqualAndSerializable(constructed, serializedCloned);
            } catch (Throwable t) {
                Assertions.fail("Unable to clone " + type, t);
            }
        }
    }

    // Convert to objects that support equals()
    private void testEquality(Object a, Object b) {
        if (a == null) {
            if (b == null) {
                return;
            } else {
                throw new AssertionError("a was null, but b was not");
            }
        } else if (b == null) {
            throw new AssertionError("a was not null, but b was null");
        }

        if (a instanceof Optional) {
            if (b instanceof Optional) {
                this.testEquality(((Optional<?>) a).orElse(null), ((Optional<?>) b).orElse(null));
                return;
            } else {
                throw new AssertionError("a was optional, but b was not");
            }
        }

        if (a.equals(b) || Objects.equals(a, b) || this.stringEquality(a, b)) {
            return;
        }

        if (MinecraftReflection.isDataWatcher(a)) {
            a = this.watchConvert.getSpecific(a);
            b = this.watchConvert.getSpecific(b);
        } else if (MinecraftReflection.isItemStack(a)) {
            a = this.itemConvert.getSpecific(a);
            b = this.itemConvert.getSpecific(b);
        }

        if (a instanceof ItemStack && b instanceof ItemStack) {
            assertItemsEqual((ItemStack) a, (ItemStack) b);
            return;
        }

        if (a instanceof List<?>) {
            if (b instanceof List<?>) {
                List<?> listA = (List<?>) a;
                List<?> listB = (List<?>) b;

                assertEquals(listA.size(), listB.size());
                for (int i = 0; i < listA.size(); i++) {
                    this.testEquality(listA.get(i), listB.get(i));
                }
                return;
            } else {
                throw new AssertionError("a was a list, but b was not");
            }
        }

        if (a.getClass().isArray()) {
            if (b.getClass().isArray()) {
                int arrayLengthA = Array.getLength(a);
                int arrayLengthB = Array.getLength(b);

                assertEquals(arrayLengthA, arrayLengthB);
                for (int i = 0; i < arrayLengthA; i++) {
                    Object elementA = Array.get(a, i);
                    Object elementB = Array.get(b, i);

                    testEquality(elementA, elementB);
                }
                return;
            } else {
                throw new AssertionError("a was an array, but b was not");
            }
        }

        if (!a.getClass().isAssignableFrom(b.getClass())) {
            assertEquals(a, b);
            return;
        }

        Set<Field> fields = FuzzyReflection.fromObject(a, true).getFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                FieldAccessor accessor = Accessors.getFieldAccessor(field);
                testEquality(accessor.get(a), accessor.get(b));
            }
        }
    }

    private boolean stringEquality(Object a, Object b) {
        try {
            return a.toString().equals(b.toString());
        } catch (Exception ex) {
            // internal null pointers, usually
            return false;
        }
    }

    /**
     * Get the underlying array as an object array.
     *
     * @param val - array wrapped as an Object.
     * @return An object array.
     */
    private Object[] getArray(Object val) {
        if (val instanceof Object[]) {
            return (Object[]) val;
        }
        if (val == null) {
            return null;
        }

        int arrlength = Array.getLength(val);
        Object[] outputArray = new Object[arrlength];

        for (int i = 0; i < arrlength; ++i) {
            outputArray[i] = Array.get(val, i);
        }
        return outputArray;
    }

    /**
     * Actions from the outbound Boss packet. Used for testing generic enums.
     *
     * @author dmulloy2
     */
    public enum Action {
        ADD,
        REMOVE,
        UPDATE_PCT,
        UPDATE_NAME,
        UPDATE_STYLE,
        UPDATE_PROPERTIES
    }
}
