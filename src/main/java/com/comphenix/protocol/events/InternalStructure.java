package com.comphenix.protocol.events;

import java.util.Optional;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.Converters;

public class InternalStructure extends AbstractStructure {

    public InternalStructure(Object handle, StructureModifier<Object> structureModifier) {
        super(handle, structureModifier);
    }

    protected static final EquivalentConverter<InternalStructure> CONVERTER = new EquivalentConverter<InternalStructure>() {
        @Override
        public Object getGeneric(InternalStructure specific) {
            return specific.handle;
        }

        @Override
        public InternalStructure getSpecific(Object generic) {
            return new InternalStructure(generic, new StructureModifier<>(generic.getClass()).withTarget(generic));
        }

        @Override
        public Class<InternalStructure> getSpecificType() {
            return InternalStructure.class;
        }
    };

    public static EquivalentConverter<InternalStructure> getConverter() {
        return CONVERTER;
    }

    public StructureModifier<InternalStructure> getStructures() {
        return structureModifier.withType(Object.class, CONVERTER);
    }

    public StructureModifier<Optional<InternalStructure>> getOptionalStructures() {
        return structureModifier.withType(Optional.class, Converters.optional(CONVERTER));
    }

    @Override
    public String toString() {
        return "InternalStructure[handle=" + handle + " (" + handle.getClass().getSimpleName() + ")]";
    }
}
