package com.youdevise.variance;

import java.util.function.Function;

public final class Variants {
    
    public static final Function<Object, Variant> toVariant = new Function<Object, Variant>() {
        @Override public Variant apply(Object object) { return Variant.of(object); }
    };

    private Variants() { }

    public static final Function<Object, Variant> toVariant(TypeConversionContext context)  {
    	return toVariant.andThen(Variants.inContext(context));
    }

    public static <T> Function<Variant, T> variantTo(final Class<T> targetClass) {
        return new Function<Variant, T>() {
            @Override public T apply(Variant variant) { return variant.as(targetClass); }
        };
    }

    public static <T> Function<Variant, T> variantTo(Class<T> targetClass, TypeConversionContext context) {
    	return Variants.inContext(context).andThen(variantTo(targetClass));
    }

    public static Function<Variant, Variant> inContext(final TypeConversionContext context) {
        return new Function<Variant, Variant>() {
            @Override public Variant apply(Variant variant) {
                return variant.in(context);
            }
        };
    }
    
    
}