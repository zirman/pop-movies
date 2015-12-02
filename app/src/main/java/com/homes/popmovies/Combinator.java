package com.homes.popmovies;

import rx.functions.Func0;
import rx.functions.Func1;
//import rx.functions.Func2;

public class Combinator {

    // Higher-order function returning a function
    // F: F -> (-> R)
    private interface FuncToFunc0<R> {
        Func0<R> call(FuncToFunc0<R> x);
    }

    // The Y combinator
    // λr.(λf.(f f)) λf.(r λx.((f f) x))
    public static <R> Func0<R> y0(final Func1<Func0<R>, Func0<R>> r) {
        return ((FuncToFunc0<R>) f -> f.call(f)).call(f -> r.call(() -> f.call(f).call()));
    }

    // Higher-order function returning a function
    // F: F -> (T -> R)
//    private interface FuncToFunc1<T, R> {
//        Func1<T, R> call(FuncToFunc1<T, R> x);
//    }

    // The Y combinator
    // λr.(λf.(f f)) λf.(r λx.((f f) x))
//    public static <T, R> Func1<T, R> y1(final Func1<Func1<T, R>, Func1<T, R>> r) {
//        return ((FuncToFunc1<T, R>) f -> f.call(f)).call(f -> r.call(x -> f.call(f).call(x)));
//    }

    // Higher-order function returning a function
    // F: F -> (T1, T2 -> R)
//    private interface FuncToFunc2<T1, T2, R> {
//        Func2<T1, T2, R> call(FuncToFunc2<T1, T2, R> x);
//    }

    // The Y combinator
    // λr.(λf.(f f)) λf.(r λx.((f f) x))
//    public static <T1, T2, R> Func2<T1, T2, R> y2(
//        final Func1<Func2<T1, T2, R>, Func2<T1, T2, R>> r) {
//        return ((FuncToFunc2<T1, T2, R>) f ->
//            f.call(f)).call(f -> r.call((x1, x2) -> f.call(f).call(x1, x2)));
//    }
}
