package dev.protocollib.api.reflect;

public interface Converter<T> {

    Object getGeneric(T specific);

    Class<?> getGenericType();

    T getSpecific(Object generic);

    Class<T> getSpecificType();
}
