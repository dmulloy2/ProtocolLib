/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol.injector.temporary;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.ByteBuddyFactory;
import com.comphenix.protocol.utility.ChatExtensions;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 * Create fake player instances that represents pre-authenticated clients.
 */
public class TemporaryPlayerFactory {

	private static final Constructor<? extends Player> PLAYER_CONSTRUCTOR = setupProxyPlayerConstructor();

	/**
	 * Retrieve the injector from a given player if it contains one.
	 *
	 * @param player - the player that may contain a reference to a player injector.
	 * @return The referenced player injector, or NULL if none can be found.
	 */
	public static MinimalInjector getInjectorFromPlayer(Player player) {
		if (player instanceof TemporaryPlayer) {
			return ((TemporaryPlayer) player).getInjector();
		}
		return null;
	}

	/**
	 * Set the player injector, if possible.
	 *
	 * @param player   - the player to update.
	 * @param injector - the injector to store.
	 */
	public static void setInjectorInPlayer(Player player, MinimalInjector injector) {
		((TemporaryPlayer) player).setInjector(injector);
	}

	@SuppressWarnings("unchecked")
	private static Constructor<? extends Player> setupProxyPlayerConstructor() {
		final MethodDelegation implementation = MethodDelegation.to(new Object() {
			@RuntimeType
			public Object delegate(
					@This Object obj,
					@Origin Method method,
					@FieldValue("server") Server server,
					@AllArguments Object... args
			) throws Throwable {
				String methodName = method.getName();
				MinimalInjector injector = ((TemporaryPlayer) obj).getInjector();

				if (injector == null) {
					throw new IllegalStateException("Unable to find injector.");
				}

				// Use the socket to get the address
				else if (methodName.equals("getPlayer")) {
					return injector.getPlayer();
				} else if (methodName.equals("getAddress")) {
					return injector.getAddress();
				} else if (methodName.equals("getServer")) {
					return server;
				}

				// Handle send message methods
				if (methodName.equals("chat") || methodName.equals("sendMessage")) {
					try {
						Object argument = args[0];

						// Dynamic overloading
						if (argument instanceof String) {
							return sendMessage(injector, (String) argument);
						} else if (argument instanceof String[]) {
							for (String message : (String[]) argument) {
								sendMessage(injector, message);
							}
							return null;
						}
					} catch (Exception exception) {
						throw exception.getCause();
					}
				}

				// Also, handle kicking
				if (methodName.equals("kickPlayer")) {
					injector.disconnect((String) args[0]);
					return null;
				}

				// The fallback instance
				Player updated = injector.getPlayer();
				if (updated != obj && updated != null) {
					return method.invoke(updated, args);
				}

				// Methods that are supported in the fallback instance
				if (methodName.equals("isOnline")) {
					return injector.isConnected();
				} else if (methodName.equals("getName")) {
					return "UNKNOWN[" + injector.getAddress() + "]";
				}

				// Ignore all other methods
				throw new UnsupportedOperationException(
						"The method " + method.getName() + " is not supported for temporary players.");
			}
		});

		final ElementMatcher.Junction<ByteCodeElement> callbackFilter = ElementMatchers.not(
				ElementMatchers.isDeclaredBy(Object.class).or(ElementMatchers.isDeclaredBy(TemporaryPlayer.class)));

		try {
			final Constructor<?> constructor = ByteBuddyFactory.getInstance()
					.createSubclass(TemporaryPlayer.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
					.name(TemporaryPlayerFactory.class.getPackage().getName() + ".TemporaryPlayerInvocationHandler")
					.implement(Player.class)

					.defineField("server", Server.class, Visibility.PRIVATE)
					.defineConstructor(Visibility.PUBLIC)
					.withParameters(Server.class)
					.intercept(MethodCall.invoke(TemporaryPlayer.class.getDeclaredConstructor())
							.andThen(FieldAccessor.ofField("server").setsArgumentAt(0)))
					.method(callbackFilter)
					.intercept(implementation)
					.make()
					.load(ByteBuddyFactory.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
					.getLoaded()
					.getDeclaredConstructor(Server.class);
			return (Constructor<? extends Player>) constructor;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Failed to find Temporary Player constructor!", e);
		}
	}

	/**
	 * Send a message to the given client.
	 *
	 * @param injector - the injector representing the client.
	 * @param message  - a message.
	 * @return Always NULL.
	 */
	private static Object sendMessage(MinimalInjector injector, String message) {
		for (PacketContainer packet : ChatExtensions.createChatPackets(message)) {
			injector.sendServerPacket(packet.getHandle(), null, false);
		}

		return null;
	}

	/**
	 * Construct a temporary player that supports a subset of every player command.
	 * <p>
	 * Supported methods include:
	 * <ul>
	 *   <li>getPlayer()</li>
	 *   <li>getAddress()</li>
	 *   <li>getServer()</li>
	 *   <li>chat(String)</li>
	 *   <li>sendMessage(String)</li>
	 *   <li>sendMessage(String[])</li>
	 *   <li>kickPlayer(String)</li>
	 * </ul>
	 * <p>
	 * Note that a temporary player has not yet been assigned a name, and thus cannot be
	 * uniquely identified. Use the address instead.
	 *
	 * @param server - the current server.
	 * @return A temporary player instance.
	 */
	public Player createTemporaryPlayer(final Server server) {
		try {
			return PLAYER_CONSTRUCTOR.newInstance(server);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to create temporary player", exception);
		}
	}

	/**
	 * Construct a temporary player with the given associated socket injector.
	 *
	 * @param server   - the parent server.
	 * @param injector - the referenced socket injector.
	 * @return The temporary player.
	 */
	public Player createTemporaryPlayer(Server server, MinimalInjector injector) {
		Player temporary = this.createTemporaryPlayer(server);
		((TemporaryPlayer) temporary).setInjector(injector);
		return temporary;
	}
}
