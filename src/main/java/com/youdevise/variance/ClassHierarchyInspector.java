package com.youdevise.variance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.Iterables;

public class ClassHierarchyInspector {
    
    private final Iterable<Class<?>> classes;
    
    public ClassHierarchyInspector(Iterable<Class<?>> classes) {
        this.classes = classes;
    }
    
    public <S> Class<? super S> nearestClassAssignableFrom(Class<S> klass) {
        if (Iterables.contains(classes,  klass)) {
            return klass;
        }
        return nearestSuperclassOf(klass);
    }

    public <S> Class<? super S> nearestSuperclassOf(Class<S> klass) {
        Collection<Class<? super S>> minima = nearestSuperclassesOf(klass);
        
        if (minima.size() > 1) {
            throw new IllegalArgumentException(String.format("Cannot find unambiguous nearest superclass of [%s] from the set [%s]",
                                                             klass,
                                                             minima.stream().map(Object::toString).collect(Collectors.joining(", "))));
        }
        
        return Iterables.getFirst(minima, null);
    }
    
    @SuppressWarnings({ "unchecked" })
    public <S> Collection<Class<? super S>> nearestSuperclassesOf(Class<S> klass) {
        Iterable<Class<? super S>> superclasses = StreamSupport.stream(classes.spliterator(), false)
            	.filter(isSuperclassOf(klass))
            	.map(k -> (Class<? super S>)k)
            	.collect(Collectors.toList());
        
        Collection<Class<? super S>> minima = new LinkedList<>();
        for (Class<? super S> superclass : superclasses) {
        	minima.removeIf(isSuperclassOf(superclass));
            if (!minima.stream().anyMatch(isSubclassOf(superclass))) {
                minima.add(superclass);
            }
        }
        
        return minima;
    }
    
    public <T> Class<? extends T> nearestClassAssignableTo(Class<T> klass) {
        if (Iterables.contains(classes,  klass)) {
            return klass;
        }
        return nearestSubclassOf(klass);
    }
    
    public <T> Class<? extends T> nearestSubclassOf(Class<T> klass) {
        Collection<Class<? extends T>> maxima = nearestSubclassesOf(klass);
        
        if (maxima.size() > 1) {
            throw new IllegalArgumentException(String.format("Cannot find unambiguous nearest subclass of [%s] from the set [%s]",
                                                             klass,
                                                             maxima.stream().map(Object::toString).collect(Collectors.joining(", "))));
        }
        return Iterables.getFirst(maxima, null);
    }
    
    @SuppressWarnings({ "unchecked" })
    public <T> Collection<Class<? extends T>> nearestSubclassesOf(Class<T> klass) {
        Iterable<Class<? extends T>> subclasses = StreamSupport.stream(classes.spliterator(), false)
            	.filter(isSubclassOf(klass))
            	.map(k -> (Class<? extends T>)k)
            	.collect(Collectors.toList());
        
        Collection<Class<? extends T>> maxima = new LinkedList<>();
        for (Class<?> subclass : subclasses) {
        	maxima.removeIf(isSubclassOf(subclass));
        	if (!maxima.stream().anyMatch(isSuperclassOf(subclass))) {
                maxima.add((Class<? extends T>) subclass);
            }
        }
        
        return maxima;
    }
    
    private <S> Predicate<Class<?>> isSuperclassOf(final Class<?> klass) {
        return new Predicate<Class<?>>() {
            @Override public boolean test(Class<?> otherClass) {
                return !otherClass.equals(klass) && otherClass.isAssignableFrom(klass);
            }
        };
    }
    
    private <S> Predicate<Class<?>> isSubclassOf(final Class<?> klass) {
        return new Predicate<Class<?>>() {
            @Override public boolean test(Class<?> otherClass) {
                return !otherClass.equals(klass) && klass.isAssignableFrom(otherClass);
            }
        };
    }
}