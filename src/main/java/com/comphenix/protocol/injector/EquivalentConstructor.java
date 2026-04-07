package com.comphenix.protocol.injector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.Tuple;

import java.util.ArrayList;
import java.util.List;

public class EquivalentConstructor {
    private final PacketType packetType;
    private final List<Tuple<Class<?>, Object>> converters = new ArrayList<>();

    private ConstructorAccessor constructorAccessor;

    public EquivalentConstructor(PacketType packetType) {
        this.packetType = packetType;
    }

    public EquivalentConstructor withParam(Class<?> param, EquivalentConverter<?> converter) {
        converters.add(new Tuple<>(param, converter));
        return this;
    }

    public EquivalentConstructor withParam(Class<?> param, PacketConstructor.Unwrapper unwrapper) {
        converters.add(new Tuple<>(param, unwrapper));
        return this;
    }

    public EquivalentConstructor withParam(Class<?> param) {
        converters.add(new Tuple<>(param, null));
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object create(Object... args) {
        if (constructorAccessor == null) {
            Class<?>[] params = new Class<?>[converters.size()];
            for (int i = 0; i < converters.size(); i++) {
                params[i] = converters.get(i).first();
            }
            constructorAccessor = Accessors.getConstructorAccessor(packetType.getPacketClass(), params);
        }

        Object[] convertedArgs = new Object[args.length];

        int i = 0;
        for (Tuple<Class<?>, Object> entry : converters) {
            Object rawConverter = entry.second();
            switch (rawConverter) {
                case EquivalentConverter converter -> convertedArgs[i] = converter.getGeneric(args[i]);
                case PacketConstructor.Unwrapper unwrapper -> convertedArgs[i] = unwrapper.unwrapItem(args[i]);
                case null -> convertedArgs[i] = args[i];
                default -> throw new IllegalStateException("Invalid converter type: " + rawConverter.getClass());
            }

            i++;
        }

        return constructorAccessor.invoke(convertedArgs);
    }
}
