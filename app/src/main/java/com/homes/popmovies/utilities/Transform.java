package com.homes.popmovies.utilities;

import org.pcollections.TreePVector;

import rx.functions.Func1;
import rx.functions.Func2;

public class Transform<A> {

    static public <A, B> TreePVector<B> map(
        final TreePVector<A> vector,
        final Func1<A, B> fun) {

        TreePVector<B> retVal = TreePVector.empty();

        for (final A a : vector) {
            retVal = retVal.plus(fun.call(a));
        }

        return retVal;
    }

    static public <A> TreePVector<A> filter(
        final TreePVector<A> vector,
        final Func1<A, Boolean> fun) {

        TreePVector<A> retVal = TreePVector.empty();

        for (final A a : vector) {

            if (fun.call(a)) {
                retVal = retVal.plus(a);
            }
        }

        return retVal;
    }

    static public <A, B> B reduce(
        final TreePVector<A> vector,
        final Func2<B, A, B> fun,
        B memo) {

        for (final A a : vector) {
            memo = fun.call(memo, a);
        }

        return memo;
    }

    static public <A> Transform<A> from(final TreePVector<A> vector) {
        return new Transform<>(vector);
    }

    private final TreePVector<A> mVector;

    private Transform(final TreePVector<A> vector) {
        mVector = vector;
    }

    public <B> Transform<B> map(final Func1<A, B> fun) {
        return new Transform<>(map(mVector, fun));
    }

    public Transform<A> filter(final Func1<A, Boolean> fun) {
        return new Transform<>(filter(mVector, fun));
    }

    public <B> B reduce(final Func2<B, A, B> fun, B memo) {
        return reduce(mVector, fun, memo);
    }

    public TreePVector<A> result() {
        return mVector;
    }
}
