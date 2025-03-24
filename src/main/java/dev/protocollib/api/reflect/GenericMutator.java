package dev.protocollib.api.reflect;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface GenericMutator extends GenericAccessor {

    MutableGenericAccessor getAccessor(Class<?> type, int ordinal);
    MutableGenericAccessor getAccessorOrThrow(Class<?> type, int ordinal);

    // ====================================================
    //  Value Modification
    // ====================================================

    Object updateObject(Class<?> type, int ordinal, UnaryOperator<Object> operator);
    Object updateObjectOrThrow(Class<?> type, int ordinal, UnaryOperator<Object> operator);

    <T> T update(Class<T> type, int ordinal, UnaryOperator<T> operator);
    <T> T updateOrThrow(Class<T> type, int ordinal, UnaryOperator<T> operator);

    <T> T update(Converter<T> converter, int ordinal, UnaryOperator<T> operator);
    <T> T updateOrThrow(Converter<T> converter, int ordinal, UnaryOperator<T> operator);

    Object update(Class<?> type, int ordinal, Consumer<GenericMutator> operator);
    Object updateOrThrow(Class<?> type, int ordinal, Consumer<GenericMutator> operator);

    // ====================================================
    //  Value Assignment
    // ====================================================

    void setObject(Class<?> type, int ordinal, Object value);
    void setObjectOrThrow(Class<?> type, int ordinal, Object value);

    void setDefault(Class<?> type, int ordinal);
    void setDefaultOrThrow(Class<?> type, int ordinal);

    void setDefault(Converter<?> type, int ordinal);
    void setDefaultOrThrow(Converter<?> type, int ordinal);

    <T> void set(Class<T> type, int ordinal, T value);
    <T> void setOrThrow(Class<T> type, int ordinal, T value);

    <T> void set(Converter<T> converter, int ordinal, T value);
    <T> void setOrThrow(Converter<T> converter, int ordinal, T value);

}
