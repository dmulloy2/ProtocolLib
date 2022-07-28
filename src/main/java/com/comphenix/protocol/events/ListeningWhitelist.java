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

package com.comphenix.protocol.events;

import java.util.*;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.GamePhase;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Determines which packets will be observed by a listener, and with what priority.
 *
 * @author Kristian
 */
public class ListeningWhitelist {

	/**
	 * A whitelist with no packets - indicates that the listener shouldn't observe any packets.
	 */
	public static final ListeningWhitelist EMPTY_WHITELIST = new ListeningWhitelist(ListenerPriority.LOW);

	private final ListenerPriority priority;
	private final GamePhase gamePhase;
	private final Set<ListenerOptions> options;
	private final Set<PacketType> types;

	private ListeningWhitelist(Builder builder) {
		this.priority = builder.priority;
		this.types = builder.types;
		this.gamePhase = builder.gamePhase;
		this.options = builder.options;
	}

	private ListeningWhitelist(ListenerPriority priority) {
		this.priority = priority;
		this.types = new HashSet<>();
		this.gamePhase = GamePhase.PLAYING;
		this.options = EnumSet.noneOf(ListenerOptions.class);
	}

	/**
	 * Determine if the given whitelist is empty or not.
	 *
	 * @param whitelist - the whitelist to test.
	 * @return TRUE if the whitelist is empty, FALSE otherwise.
	 */
	public static boolean isEmpty(ListeningWhitelist whitelist) {
		if (whitelist == EMPTY_WHITELIST) {
			return true;
		} else if (whitelist == null) {
			return true;
		} else {
			return whitelist.getTypes().isEmpty();
		}
	}

	/**
	 * Construct a new builder of whitelists.
	 *
	 * @return New whitelist builder.
	 */
	public static Builder newBuilder() {
		return new Builder(null);
	}

	/**
	 * Construct a new builder of whitelists initialized to the same values as the template.
	 *
	 * @param template - the template object.
	 * @return New whitelist builder.
	 */
	public static Builder newBuilder(ListeningWhitelist template) {
		return new Builder(template);
	}

	/**
	 * Construct a copy of a given enum.
	 *
	 * @param options - the options to copy, or NULL to indicate the empty set.
	 * @return A copy of the enum set.
	 */
	private static <T extends Enum<T>> EnumSet<T> safeEnumSet(Collection<T> options, Class<T> enumClass) {
		if (options != null && !options.isEmpty()) {
			return EnumSet.copyOf(options);
		} else {
			return EnumSet.noneOf(enumClass);
		}
	}

	/**
	 * Construct a copy of a given set.
	 *
	 * @param set - the set to copy.
	 * @return The copied set.
	 */
	private static <T> Set<T> safeSet(Collection<T> set) {
		if (set != null) {
			return new HashSet<>(set);
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Whether or not this whitelist has any enabled packets.
	 *
	 * @return TRUE if there are any packets, FALSE otherwise.
	 */
	public boolean isEnabled() {
		return types != null && types.size() > 0;
	}

	/**
	 * Retrieve the priority in the execution order of the packet listener. Highest priority will be executed last.
	 *
	 * @return Execution order in terms of priority.
	 */
	public ListenerPriority getPriority() {
		return priority;
	}

	/**
	 * Retrieves a set of the packets that will be observed by the listeners.
	 *
	 * @return Packet whitelist.
	 */
	public Set<PacketType> getTypes() {
		return types;
	}

	/**
	 * Retrieve which game phase this listener is active under.
	 *
	 * @return The active game phase.
	 */
	public GamePhase getGamePhase() {
		return gamePhase;
	}

	/**
	 * Retrieve every special option associated with this whitelist.
	 *
	 * @return Every special option.
	 */
	public Set<ListenerOptions> getOptions() {
		return Collections.unmodifiableSet(options);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(priority, types, gamePhase, options);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ListeningWhitelist) {
			final ListeningWhitelist other = (ListeningWhitelist) obj;
			return Objects.equal(priority, other.priority)
					&& Objects.equal(types, other.types)
					&& Objects.equal(gamePhase, other.gamePhase)
					&& Objects.equal(options, other.options);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		if (this == EMPTY_WHITELIST) {
			return "EMPTY_WHITELIST";
		} else {
			return "ListeningWhitelist[priority=" + priority + ", packets=" + types + ", gamephase=" + gamePhase
					+ ", options=" + options + "]";
		}
	}

	/**
	 * Represents a builder of whitelists.
	 *
	 * @author Kristian
	 */
	public static class Builder {

		// Default values
		private ListenerPriority priority = ListenerPriority.NORMAL;
		private Set<PacketType> types = new HashSet<>();
		private GamePhase gamePhase = GamePhase.PLAYING;
		private Set<ListenerOptions> options = new HashSet<>();

		/**
		 * Construct a new listening whitelist template.
		 *
		 * @param template - the template.
		 */
		private Builder(ListeningWhitelist template) {
			if (template != null) {
				priority(template.getPriority());
				gamePhase(template.getGamePhase());
				types(template.getTypes());
				options(template.getOptions());
			}
		}

		/**
		 * Set the priority to use when constructing new whitelists.
		 *
		 * @param priority - the priority.
		 * @return This builder, for chaining.
		 */
		public Builder priority(ListenerPriority priority) {
			this.priority = priority;
			return this;
		}

		/**
		 * Set the priority of the whitelist to monitor.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder monitor() {
			return priority(ListenerPriority.MONITOR);
		}

		/**
		 * Set the priority of the whitelist to normal.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder normal() {
			return priority(ListenerPriority.NORMAL);
		}

		/**
		 * Set the priority of the whitelist to lowest.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder lowest() {
			return priority(ListenerPriority.LOWEST);
		}

		/**
		 * Set the priority of the whitelist to low.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder low() {
			return priority(ListenerPriority.LOW);
		}

		/**
		 * Set the priority of the whitelist to highest.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder highest() {
			return priority(ListenerPriority.HIGHEST);
		}

		/**
		 * Set the priority of the whitelist to high.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder high() {
			return priority(ListenerPriority.HIGH);
		}

		/**
		 * Set the whitelist of packet types to copy when constructing new whitelists.
		 *
		 * @param types - the whitelist of packets.
		 * @return This builder, for chaining.
		 */
		public Builder types(PacketType... types) {
			this.types = safeSet(Sets.newHashSet(types));
			return this;
		}

		/**
		 * Set the whitelist of packet types to copy when constructing new whitelists.
		 *
		 * @param types - the whitelist of packets.
		 * @return This builder, for chaining.
		 */
		public Builder types(Collection<PacketType> types) {
			this.types = safeSet(types);
			return this;
		}

		/**
		 * Set the gamephase to use when constructing new whitelists.
		 *
		 * @param gamePhase - the gamephase.
		 * @return This builder, for chaining.
		 */
		public Builder gamePhase(GamePhase gamePhase) {
			this.gamePhase = gamePhase;
			return this;
		}

		/**
		 * Set the gamephase to {@link GamePhase#BOTH}.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder gamePhaseBoth() {
			return gamePhase(GamePhase.BOTH);
		}

		/**
		 * Set the options to copy when constructing new whitelists.
		 *
		 * @param options - the options.
		 * @return This builder, for chaining.
		 */
		public Builder options(Set<ListenerOptions> options) {
			this.options = safeSet(options);
			return this;
		}

		/**
		 * Set the options to copy when constructing new whitelists.
		 *
		 * @param options - the options.
		 * @return This builder, for chaining.
		 */
		public Builder options(Collection<ListenerOptions> options) {
			this.options = safeSet(options);
			return this;
		}

		/**
		 * Set the options to copy when constructing new whitelists.
		 *
		 * @param serverOptions - the options array.
		 * @return This builder, for chaining.
		 */
		public Builder options(ListenerOptions[] serverOptions) {
			this.options = safeSet(Sets.newHashSet(serverOptions));
			return this;
		}

		/**
		 * Options to merge into the current set of options.
		 *
		 * @param serverOptions - the options array.
		 * @return This builder, for chaining.
		 */
		public Builder mergeOptions(ListenerOptions... serverOptions) {
			return mergeOptions(Arrays.asList(serverOptions));
		}

		/**
		 * Options to merge into the current set of options.
		 *
		 * @param serverOptions - the options array.
		 * @return This builder, for chaining.
		 */
		public Builder mergeOptions(Collection<ListenerOptions> serverOptions) {
			if (options == null) {
				return options(serverOptions);
			}

			// Merge the options
			this.options.addAll(serverOptions);
			return this;
		}

		/**
		 * Construct a new whitelist from the values in this builder.
		 *
		 * @return The new whitelist.
		 */
		public ListeningWhitelist build() {
			return new ListeningWhitelist(this);
		}
	}
}
