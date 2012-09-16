package com.comphenix.protocol.injector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;
import com.google.common.collect.Sets;

import net.minecraft.server.Packet;

/**
 * Injection hook that overrides the packet queue lists in NetworkHandler.
 * 
 * @author Kristian
 */
class NetworkFieldInjector extends PlayerInjector {

	/**
	 * Marker interface that indicates a packet is fake and should not be processed.
	 * @author Kristian
	 */
	public interface FakePacket {
		// Nothing
	}
	
	// Packets to ignore
	private Set<Packet> ignoredPackets = Sets.newSetFromMap(new ConcurrentHashMap<Packet, Boolean>());
	
	// Overridden fields
	private List<VolatileField> overridenLists = new ArrayList<VolatileField>();
	
	// Sync field
	private static Field syncField;
	private Object syncObject;
	
	public NetworkFieldInjector(Player player, PacketFilterManager manager, Set<Integer> sendingFilters) throws IllegalAccessException {
		super(player, manager, sendingFilters);
	}
	
	@Override
	protected void initialize() throws IllegalAccessException {
		super.initialize();
	
		// Get the sync field as well
		if (hasInitialized) {
			if (syncField == null)
				syncField = FuzzyReflection.fromObject(networkManager, true).getFieldByType("java\\.lang\\.Object");
			syncObject = FieldUtils.readField(syncField, networkManager, true);
		}
	}

	@Override
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {
		
		if (networkManager != null) {
			try {
				if (!filtered) {
					ignoredPackets.add(packet);
				}
				
				// Note that invocation target exception is a wrapper for a checked exception
				queueMethod.invoke(networkManager, packet);
				
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access queue method.", e);
			}
		} else {
			throw new IllegalStateException("Unable to load network mananager. Cannot send packet.");
		}
	}
	
	@Override
	public void injectManager() {
		
		if (networkManager != null) {

			@SuppressWarnings("rawtypes")
			StructureModifier<List> list = networkModifier.withType(List.class);

			// Subclass both send queues
			for (Field field : list.getFields()) {
				VolatileField overwriter = new VolatileField(field, networkManager, true);
				
				@SuppressWarnings("unchecked")
				List<Packet> minecraftList = (List<Packet>) overwriter.getOldValue();
				
				synchronized(syncObject) {
					// The list we'll be inserting
					List<Packet> hackedList = new InjectedArrayList(manager.getClassLoader(), this, ignoredPackets);
					
					// Add every previously stored packet
					for (Packet packet : minecraftList) {
						hackedList.add(packet);
					}
					
					// Don' keep stale packets around
					minecraftList.clear();
					overwriter.setValue(Collections.synchronizedList(hackedList));
				}
				
				overridenLists.add(overwriter);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void cleanupAll() {
		// Clean up
		for (VolatileField overriden : overridenLists) {
			List<Packet> minecraftList = (List<Packet>) overriden.getOldValue();
			List<Packet> hacketList = (List<Packet>) overriden.getValue();
			
			if (minecraftList == hacketList) {
				return;
			}
	
			// Get a lock before we modify the list
			synchronized(syncObject) {
				try {
					// Copy over current packets
					for (Packet packet : (List<Packet>) overriden.getValue()) {
						minecraftList.add(packet);
					}
				} finally {
					overriden.revertValue();
				}
			}
		}
		overridenLists.clear();
	}
}
