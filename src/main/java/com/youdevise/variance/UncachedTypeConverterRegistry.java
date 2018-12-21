package com.youdevise.variance;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class UncachedTypeConverterRegistry implements TypeConverterRegistry {

    private final TypeConverterDictionary dictionary;
    
    public UncachedTypeConverterRegistry(TypeConverterDictionary dictionary) {
        this.dictionary = dictionary;
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
    
    @SuppressWarnings({ "unchecked" })
    private <S, T> Function<? super S, ? extends T> findConverter(Class<S> sourceClass, Class<T> targetClass) {
        if (targetClass.isAssignableFrom(sourceClass)) {
            return (Function<? super S, ? extends T>) Function.identity();
        }
        
        ClassHierarchyInspector inspector = new ClassHierarchyInspector(dictionary.sourceClasses());
        
        Class<? super S> nearestSuperclass = inspector.nearestClassAssignableFrom(sourceClass);
        while (nearestSuperclass != null) {
            Class<? extends T> targetSubclass = findTargetSubclass(targetClass, nearestSuperclass);
            if (targetSubclass != null) {
                return dictionary.converterFor(nearestSuperclass, targetSubclass);
            }
            nearestSuperclass = inspector.nearestSuperclassOf(nearestSuperclass);
        }
        return null;
    }

    private <S, T> Class<? extends T> findTargetSubclass(Class<T> targetClass, Class<? super S> nearestSuperclass) {
        Set<Class<?>> targetClasses = dictionary.targetClassesFor(nearestSuperclass);
        ClassHierarchyInspector inspector = new ClassHierarchyInspector(targetClasses);
        return inspector.nearestClassAssignableTo(targetClass);
    }

}
