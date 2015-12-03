package com.homes.popmovies.utilities;

import rx.functions.Func1;

public class Optional<A> {

    // Singleton empty monad

    public static final Optional sEmpty = new Optional<>(null);

    public static <A> Optional<A> empty() {
        return new Optional<>(null);
    }

    // Member properties

    private final A option;

    // Monad functions

    public static <A> Optional<A> bind(final A option) {
        return option != null ? new Optional<>(option) : sEmpty;
    }

    public <B> Optional<B> flatMap(final Func1<A, Optional<B>> fun) {
        return option != null ? fun.call(option) : sEmpty;
    }

    private Optional(final A myOption) {
        option = myOption;
    }

    // Convenience functions

    public <B> Optional<B> map(final Func1<A, B> fun) {
        return flatMap(a -> bind(fun.call(a)));
    }
}
