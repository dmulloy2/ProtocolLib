package com.comphenix.protocol.events;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.Converters;

public class InternalStructure extends AbstractStructure {

    public InternalStructure(Object handle, StructureModifier<Object> structureModifier) {
        super(handle, structureModifier);
    }

    // one modifier per structure class; building it scans the class and discovers default values
    private static final Map<Class<?>, StructureModifier<Object>> MODIFIERS = new ConcurrentHashMap<>();

    private static StructureModifier<Object> modifierFor(Class<?> type) {
        StructureModifier<Object> modifier = MODIFIERS.get(type);
        if (modifier == null) {
            modifier = new StructureModifier<>(type);
            StructureModifier<Object> existing = MODIFIERS.putIfAbsent(type, modifier);
            if (existing != null) {
                modifier = existing;
            }
        }
        return modifier;
    }

    protected static final EquivalentConverter<InternalStructure> CONVERTER = new EquivalentConverter<InternalStructure>() {
        @Override
        public Object getGeneric(InternalStructure specific) {
            return specific.handle;
        }

        @Override
        public InternalStructure getSpecific(Object generic) {
            return new InternalStructure(generic, modifierFor(generic.getClass()).withTarget(generic));
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
