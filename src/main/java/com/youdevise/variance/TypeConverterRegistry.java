package com.youdevise.variance;

import java.util.function.Function;

public interface TypeConverterRegistry {
    boolean hasConverter(Class<?> sourceType, Class<?> targetType);
    <S, T> Function<? super S, ? extends T> getConverter(Class<S> sourceType, Class<T> targetType);
}
