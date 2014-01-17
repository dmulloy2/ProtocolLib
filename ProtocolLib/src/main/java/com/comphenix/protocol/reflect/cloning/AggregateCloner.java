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

package com.comphenix.protocol.reflect.cloning;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.ExistingGenerator;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Implements a cloning procedure by trying multiple methods in turn until one is successful.
 * 
 * @author Kristian
 */
public class AggregateCloner implements Cloner {
	/**
	 * Supplies the cloner factories with necessary parameters.
	 * 
	 * @author Kristian
	 */
	public static class BuilderParameters {
		// Can only be modified by the builder
		private InstanceProvider instanceProvider;
		private Cloner aggregateCloner;

		// Used to construct the different types
		private InstanceProvider typeConstructor;
		
		private BuilderParameters() {
			// Only allow inner classes to construct it.
		}
		
		/**
		 * Retrieve the instance provider last set in the builder.
		 * @return Current instance provider.
		 */
		public InstanceProvider getInstanceProvider() {
			return instanceProvider;
		}

		/**
		 * Retrieve the aggregate cloner that is being built.
		 * @return The parent cloner.
		 */
		public Cloner getAggregateCloner() {
			return aggregateCloner;
		}
	}
	
	/**
	 * Represents a builder for aggregate (combined) cloners.
	 * 
	 * @author Kristian
	 */
	public static class Builder {
		private List<Function<BuilderParameters, Cloner>> factories = Lists.newArrayList();
		private BuilderParameters parameters;
		
		/**
		 * Create a new aggregate builder.
		 */
		public Builder() {
			this.parameters = new BuilderParameters();
		}
		
		/**
		 * Set the instance provider supplied to all cloners in this builder.
		 * @param provider - new instance provider.
		 * @return The current builder.
		 */
		public Builder instanceProvider(InstanceProvider provider) {
			this.parameters.instanceProvider = provider;
			return this;
		}

		/**
		 * Add the next cloner that will be considered in turn.
		 * @param type - the type of the next cloner.
		 * @return This builder.
		 */
		public Builder andThen(final Class<? extends Cloner> type) {
			// Use reflection to generate a factory on the fly
			return andThen(new Function<BuilderParameters, Cloner>() {
				@Override
				public Cloner apply(@Nullable BuilderParameters param) {
					Object result = param.typeConstructor.create(type);
					
					if (result == null) {
						throw new IllegalStateException("Constructed NULL instead of " + type);
					}
						
					if (type.isAssignableFrom(result.getClass())) 
						return (Cloner) result;
					else 
						throw new IllegalStateException("Constructed " + result.getClass() + " instead of " + type);
				}
			});
		}
		
		/**
		 * Add the next cloner that will be considered in turn.
		 * @param factory - factory constructing the next cloner.
		 * @return This builder.
		 */
		public Builder andThen(Function<BuilderParameters, Cloner> factory) {
			factories.add(factory);
			return this;
		}
		
		/**
		 * Build a new aggregate cloner using the supplied values.
		 * @return A new aggregate cloner.
		 */
		public AggregateCloner build() {
			AggregateCloner newCloner = new AggregateCloner();
			
			// The parameters we will pass to our cloners
			Cloner paramCloner = new NullableCloner(newCloner);
			InstanceProvider paramProvider = parameters.instanceProvider;
			
			// Initialize parameters
			parameters.aggregateCloner = paramCloner;
			parameters.typeConstructor = DefaultInstances.fromArray(
					ExistingGenerator.fromObjectArray(new Object[] { paramCloner, paramProvider })
			);
			
			// Build every cloner in the correct order
			List<Cloner> cloners = Lists.newArrayList();
			
			for (int i = 0; i < factories.size(); i++) {
				Cloner cloner = factories.get(i).apply(parameters);
				
				// See if we were successful
				if (cloner != null)
					cloners.add(cloner);
				else
					throw new IllegalArgumentException(
							String.format("Cannot create cloner from %s (%s)", factories.get(i), i)
					);
			}
			
			// We're done
			newCloner.setCloners(cloners);
			return newCloner;
		}
	}
	
	/**
	 * Represents a default aggregate cloner.
	 */
	public static final AggregateCloner DEFAULT = newBuilder().
			instanceProvider(DefaultInstances.DEFAULT).
			andThen(BukkitCloner.class).
			andThen(ImmutableDetector.class).
			andThen(CollectionCloner.class).
			andThen(FieldCloner.class).
			build();
	
	// List of clone methods
	private List<Cloner> cloners;

	private WeakReference<Object> lastObject;
	private int lastResult;
	
	/**
	 * Begins constructing a new aggregate cloner.
	 * @return A builder for a new aggregate cloner.
	 */
	public static Builder newBuilder() {
		return new Builder();
	}
	
	/**
	 * Construct a new, empty aggregate cloner.
	 */
	private AggregateCloner() {
		// Only used by our builder above.
	}
	
	/**
	 * Retrieves a view of the current list of cloners.
	 * @return Current cloners.
	 */
	public List<Cloner> getCloners() {
		return Collections.unmodifiableList(cloners);
	}
	
	/**
	 * Set the cloners that will be used.
	 * @param cloners - the cloners that will be used.
	 */
	private void setCloners(Iterable<? extends Cloner> cloners) {
		this.cloners = Lists.newArrayList(cloners);
	}

	@Override
	public boolean canClone(Object source) {
		// Optimize a bit
		lastResult = getFirstCloner(source);
		lastObject = new WeakReference<Object>(source);
		return lastResult >= 0 && lastResult < cloners.size();
	}
	
	/**
	 * Retrieve the index of the first cloner capable of cloning the given object.
	 * <p>
	 * Returns an invalid index if no cloner is able to clone the object.
	 * @param source - the object to clone.
	 * @return The index of the cloner object.
	 */
	private int getFirstCloner(Object source) {
		for (int i = 0; i < cloners.size(); i++) {
			if (cloners.get(i).canClone(source))
				return i;
		}
		
		return cloners.size();
	}

	@Override
	public Object clone(Object source) {
		if (source == null)
			throw new IllegalAccessError("source cannot be NULL.");
		int index = 0;
		
		// Are we dealing with the same object?
		if (lastObject != null && lastObject.get() == source) {
			index = lastResult;
		} else {
			index = getFirstCloner(source);
		}

		// Make sure the object is valid
		if (index < cloners.size()) {
			return cloners.get(index).clone(source);
		}

		// Damn - failure
		throw new IllegalArgumentException("Cannot clone " + source + ": No cloner is suitable.");
	}
}
