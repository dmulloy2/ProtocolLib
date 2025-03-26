package dev.protocollib.api.reflect;

import java.util.function.Consumer;

public interface MutableGenericAccessor extends GenericAccessor {

    void update(Consumer<GenericMutator> consumer);

}
