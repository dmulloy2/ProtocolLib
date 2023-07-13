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
package com.comphenix.protocol.reflect;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Provides {@link Spliterator} access to a {@link StructureModifier}
 *
 * @param <T> Type of the fields in the {@link StructureModifier}
 * @author BradBot_1
 */
class StructureModifierSpliterator<T> implements Spliterator<StructureModifierIntermediate<T>> {
    
    private final StructureModifier<T> structure;
    private int index;
    private int bounds;

    /**
     * @param structure - {@link StructureModifier} to operate on.
     */
    StructureModifierSpliterator(final StructureModifier<T> structure) {
        this(structure, 0, structure.size());
    }

    /**
     * @param structure - {@link StructureModifier} to operate on.
     * @param offset - The offset to start at
     * @param bounds - The max bound for reading.
     */
    StructureModifierSpliterator(final StructureModifier<T> structure, final int offset, final int bounds) {
        this.structure = structure;
        this.index = offset;
        this.bounds = bounds;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public StructureModifierSpliterator<T> trySplit() {
        final int remainingElements = this.bounds - this.index;
        if (remainingElements <= 1) return null; // as spec
        final int newSplitBounds = bounds;
        this.bounds -= (remainingElements - (remainingElements % 2)) / 2;
        return new StructureModifierSpliterator<T>(this.structure, this.bounds, newSplitBounds);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tryAdvance(final Consumer<? super StructureModifierIntermediate<T>> action) throws NullPointerException {
        // as demanded by the spec
        if (action == null) throw new NullPointerException("The provided consumer is null!");
        if (this.index >= this.bounds) return false; // hit end of this spliterator
        final StructureModifierIntermediate<T> intermediate = new StructureModifierIntermediate<T>(this.structure, this.index++);
        // since it's defined as not null we have to perform this check
        // yes this makes {@link #estimateSize} inaccurate in some situations, but that's better than a null
        if (intermediate.get() == null) return tryAdvance(action);
        action.accept(intermediate);
        return true;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void forEachRemaining(Consumer<? super StructureModifierIntermediate<T>> action) {
        // as demanded by the spec
        if (action == null) throw new NullPointerException("The provided consumer is null!");
        while (this.index < this.bounds) {
            final StructureModifierIntermediate<T> intermediate = new StructureModifierIntermediate<T>(this.structure, this.index++);
            // since it's defined as not null we have to perform this check
            if (intermediate.get() == null) continue;
            action.accept(intermediate);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateSize() {
        return this.bounds - this.index;
    }

    
    /**
     * {@inheritDoc}
     * 
     * @apiNote Since this implementation is ordered we do not need to perform the check, instead we can just call the method
     */
    @Override
    public long getExactSizeIfKnown() {
        return this.estimateSize();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public int characteristics() {
        return Spliterator.DISTINCT/* | Spliterator.IMMUTABLE*/ | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

}
