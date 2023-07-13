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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides iterator access to a {@link StructureModifier}
 *
 * @param <T> Type of the fields in the {@link StructureModifier}
 * @author BradBot_1
 */
class StructureModifierIterator<T> implements Iterator<StructureModifierIntermediate<T>> {
    
    private final StructureModifier<T> structure;
    private int index = 0;

    /**
     * @param structure - {@link StructureModifier} to operate on.
     */
    StructureModifierIterator(final StructureModifier<T> structure) {
        this.structure = structure;
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public boolean hasNext() {
        return this.index < this.structure.size();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public StructureModifierIntermediate<T> next() {
        if (!this.hasNext()) throw new NoSuchElementException();
        return new StructureModifierIntermediate<T>(this.structure, this.index++);
    }

}
