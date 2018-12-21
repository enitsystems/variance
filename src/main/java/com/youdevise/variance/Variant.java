package com.youdevise.variance;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Variant extends Number implements Supplier<Object> {

    private static final long serialVersionUID = 6200248721405100437L;
    
    @SuppressWarnings({ "rawtypes" })
    public static Variant of(Object value) {
    	Objects.requireNonNull(value, "A variant cannot have a null value.");
        
        if (value instanceof Variant) {
            return (Variant) value;
        }
        
        if (value.getClass().isArray()) {
            return ofVariants(arrayToVariants(value));
        }
        
        if (value instanceof Iterable) {
            return of((Iterable) value);
        }
        
        return new Variant(value);
    }
    
    public static Variant of(Object firstValue, Object...moreValues) {
    	return of(Stream.concat(Stream.of(firstValue), Stream.of(moreValues)).collect(Collectors.toList()));
    }
    
    public static Variant of(Iterable<?> values) {
    	return ofVariants(StreamSupport.stream(values.spliterator(), true).map(Variant::of).toArray(Variant[]::new));
    }

    public static Variant ofVariants(Variant...variants) {
        return ofVariants(Stream.of(variants).collect(Collectors.toList()));
    }
    
    public static Variant ofVariants(Iterable<Variant> variants) {
        return new Variant(variants);
    }
    
    private static Iterable<Variant> arrayToVariants(Object value) {
        Collection<Variant> results = new ArrayList<Variant>(Array.getLength(value));
        for (int i=0; i<Array.getLength(value); i++) {
            results.add(Variant.of(Array.get(value, i)));
        }
        return results;
    }
    
    private final Object value;
    private final TypeConversionContext typeConversionContext;
    
    private Variant(Object value) {
        this(value, new ChainedTypeConversionContext(ImplicitTypeConversions.implicitContext, TypeConversions.standardContext));
    }
    
    
    @SuppressWarnings("unchecked")
    private Variant(Object value, TypeConversionContext boundContext) {
        if (value instanceof Iterable) {
            this.value = bound((Iterable<Variant>) value, boundContext);
        } else {
            this.value = value;
        }
        this.typeConversionContext = boundContext;
    }

    private Iterable<Variant> bound(Iterable<Variant> values, final TypeConversionContext typeConversionContext) {
    	return StreamSupport.stream(values.spliterator(), false)
	    	.map(new Function<Variant, Variant>() {
	            @Override public Variant apply(Variant arg0) {
	                return new Variant(arg0.value, typeConversionContext);
	            }
	        })
	    	.collect(Collectors.toList());
    }
    
    public <C> C as(Class<C> targetClass) {
    	if (!context().canConvert(value,  targetClass) ) {
    		throw new IllegalArgumentException(
    				String.format("Unable to convert a value of type [%s] to [%s] in the current context",
    						value.getClass(),
                            targetClass));
    	}
        return context().convert(value, targetClass);
    }
    
    @SuppressWarnings({ "unchecked" })
    public <C> Iterable<C> asIterableOf(final Class<C> targetClass) {
    	return (Iterable<C>) StreamSupport.stream(as(Iterable.class).spliterator(), false)
	    	.map(new Function<Object, C>() {
	            @Override public C apply(Object o) {
	                return context().convert(o, targetClass);
	            }
	        })
	    	.collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
	public <C> C[] asArrayOf(final Class<C> targetClass) {
        Collection<C> collection = (Collection<C>)asIterableOf(targetClass);
        return collection.toArray((C[])Array.newInstance(targetClass, collection.size()));
    }
    
    public Variant in(TypeConversionContext ctx) {
        return new Variant(value, new ChainedTypeConversionContext(ctx, typeConversionContext));
    }

    public Class<?> valueClass() {
        return value.getClass();
    }

    public TypeConversionContext context() {
        return typeConversionContext;
    }
    
    public boolean isConvertibleTo(Class<?> targetClass) {
        return context().canConvert(value, targetClass);
    }

    @Override
    public int intValue() {
        return as(Integer.class);
    }

    @Override
    public long longValue() {
        return as(Long.class);
    }

    @Override
    public float floatValue() {
        return as(Float.class);
    }

    @Override
    public double doubleValue() {
        return as(Double.class);
    }
    
    @Override
    public String toString() {
        return as(String.class);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Variant) {
        	return Objects.equals(((Variant) o).value, value);
        }
        return false;
    }

    @Override
    public Object get() {
        return value;
    }
}
