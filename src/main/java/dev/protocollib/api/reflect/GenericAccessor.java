package dev.protocollib.api.reflect;

public interface GenericAccessor {

    // ====================================================
    //  Value Retrieval Optional
    // ====================================================

    GenericAccessor getAccessor(Class<?> type, int ordinal);
    GenericAccessor getAccessorOrThrow(Class<?> type, int ordinal);

    Object getObject(Class<?> type, int ordinal);
    Object getObjectOrThrow(Class<?> type, int ordinal);

    <T> T get(Class<T> type, int ordinal);
    <T> T getOrThrow(Class<T> type, int ordinal);

    <T> T get(Converter<T> converter, int ordinal);
    <T> T getOrThrow(Converter<T> converter, int ordinal);

    // ====================================================
    //  Metadata
    // ====================================================

    boolean isSupported(Class<?> type, int ordinal);
    boolean isSupported(Converter<?> type, int ordinal);

    int count(Class<?> type);
    int count(Converter<?> type);

}
