package com.homes.popmovies;

import rx.functions.Func1;
import rx.functions.Function;

public class Optional<A> {

    public interface Func1_<B> extends Function {
        void call(B b);
    }

    // Singleton empty monad

    public static final Optional sEmpty = new Optional<>(null);

    public static <A> Optional<A> empty() {
        return new Optional<>(null);
    }

    // Member properties

    private final A option;

    // Monad functions

    public static <A> Optional<A> unit(final A option) {
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
        return flatMap(a -> unit(fun.call(a)));
    }

    // Type system gets confused when Func1<A, B> -> Optional<B> and Func1_<A> -> have the same
    // name.

    public void map_(final Func1_<A> fun) {
        flatMap(a -> {
            fun.call(a);
            return sEmpty;
        });
    }

//    public Optional<A> filter(final Func1<A, Boolean> fun) {
//        return flatMap(foo -> fun.call(foo) ? unit(foo) : _empty);
//    }
}
