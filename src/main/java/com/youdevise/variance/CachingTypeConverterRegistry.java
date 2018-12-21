package com.youdevise.variance;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CachingTypeConverterRegistry implements TypeConverterRegistry {
    
    private final Map<ConversionKey, Function<?, ?>> cache = new HashMap<>();
    
    private static final class ConversionKey {
        private final Class<?> sourceClass;
        private final Class<?> targetClass;
        
        public ConversionKey(Class<?> sourceClass, Class<?> targetClass) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
        }
        
        @Override public boolean equals(Object other) {
            if (other instanceof ConversionKey) {
                return ((ConversionKey) other).sourceClass.equals(sourceClass)
                        && ((ConversionKey) other).targetClass.equals(targetClass);
            }
            return false;
        }
        
        @Override public int hashCode() {
        	return Objects.hash(sourceClass, targetClass);
        }
    }
    
    private final TypeConverterRegistry innerRegistry;
    
    public CachingTypeConverterRegistry(TypeConverterRegistry innerRegistry) {
        this.innerRegistry = innerRegistry;
    }
    
    @Override
    public boolean hasConverter(Class<?> sourceClass, Class<?> targetClass) {
        return findConverter(sourceClass, targetClass) != null;
    }

    @Override
    public <S, T> Function<? super S, ? extends T> getConverter(Class<S> sourceClass, Class<T> targetClass) {
        Function<? super S, ? extends T> converter = findConverter(sourceClass, targetClass);
        Objects.requireNonNull(converter, String.format("No converter found between [%s] and [%s]", sourceClass, targetClass));
        return converter;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <S, T> Function<? super S, ? extends T> findConverter(Class<S> sourceClass, Class<T> targetClass) {
        ConversionKey key = new ConversionKey(sourceClass, targetClass);
    	return (Function) cache.computeIfAbsent(key, new Function() {
			@Override
			public Object apply(Object t) {
				if (innerRegistry.hasConverter(key.sourceClass, key.targetClass)) {
					return innerRegistry.getConverter(key.sourceClass, key.targetClass);
				}
				return null;
			}
    	});
    }
}
