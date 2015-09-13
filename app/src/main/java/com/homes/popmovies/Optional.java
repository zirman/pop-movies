package com.homes.popmovies;

import rx.functions.Func1;
import rx.functions.Function;

public class Optional<A> {

    // Singleton empty monad

    public static final Optional empty = new Optional(null);

    // Member properties

    private final A option;

    // Monad functions

    public static <A> Optional<A> unit(final A option) {
        return option != null ? new Optional(option) : empty;
    }

    public <B> Optional<B> flatMap(final Func1<A, Optional<B>> fun) {
        return option != null ? fun.call(option) : empty;
    }

    // Constructor

    private Optional(final A myOption) {
        option = myOption;
    }

    // Convenience functions

    public boolean isPresent() {
        return option != null;
    }

    public A get() {
        return option;
    }

    public <B> Optional<B> map(final Func1<A, B> fun) {
        return flatMap(foo -> unit(fun.call(foo)));
    }

    public Optional<A> filter(final Func1<A, Boolean> fun) {
        return flatMap(foo -> fun.call(foo) ? unit(foo) : empty);
    }

    public void ifPresent(final Func1_<A> fun) {
        flatMap(foo -> {
            fun.call(foo);
            return empty;
        });
    }

    public interface Func1_<B> extends Function {
        void call(B b);
    }
}
