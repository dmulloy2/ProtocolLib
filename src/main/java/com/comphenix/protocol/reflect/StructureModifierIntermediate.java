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

/**
 * Created by {@link StructureModifierIterator} iterator access to a {@link StructureModifier}
 *
 * @param <T> Type of the fields in the {@link StructureModifier}
 * @author BradBot_1
 */
public class StructureModifierIntermediate<T> {
    
    protected final StructureModifier<T> structure;
    protected final int index;

    /**
     * @param structure - {@link StructureModifier} to operate on.
     * @param index - index to access.
     * 
     * @apiNote Must be public, else extending it would not work properly.
     */
    public StructureModifierIntermediate(final StructureModifier<T> structure, final int index) {
        this.structure = structure;
        this.index = index;
    }

    /**
     * @return Object at index.
     */
    public T get() {
        return this.structure.read(this.index);
    }

    /**
     * Overwrites the existing value at the index in the structure.
     * 
     * @param toWrite - Object to overwrite the existing one.
     */
    public void set(final T toWrite) {
        this.structure.write(index, toWrite);
    }

    /**
     * @return The internal structure modifier
     */
    public StructureModifier<T> getInternalStructureModifier() {
        return this.structure;
    }

}
