package dev.protocollib.api.reflect;

import java.util.function.Consumer;

public interface MutableGenericAccessor extends GenericAccessor {

    MutableGenericAccessor getAccessor(Class<?> type, int ordinal);
    MutableGenericAccessor getAccessorOrThrow(Class<?> type, int ordinal);

    void update(Consumer<GenericMutator> consumer);

}
